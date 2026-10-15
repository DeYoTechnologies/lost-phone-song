package com.deyotechnologies.lostphonesong

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log

/** Foreground service that loops the song on the alarm stream at full volume. */
class AlarmService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { stopEverything() }

    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var previousAlarmVolume: Int? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopEverything()
            return START_NOT_STICKY
        }

        try {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not enter foreground", e)
            stopEverything()
            return START_NOT_STICKY
        }

        // A second trigger while already playing just extends the timeout.
        if (player == null) startPlayback()
        handler.removeCallbacks(autoStop)
        handler.postDelayed(autoStop, AUTO_STOP_MS)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseResources()
        super.onDestroy()
    }

    private fun startPlayback() {
        acquireWakeLock()
        maxAlarmVolume()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        for (source in songSources()) {
            val mp = MediaPlayer()
            try {
                mp.setAudioAttributes(attributes)
                source(mp)
                mp.isLooping = true
                mp.prepare()
                mp.start()
                player = mp
                return
            } catch (e: Exception) {
                Log.w(TAG, "Song source failed, trying next", e)
                mp.release()
            }
        }
        Log.w(TAG, "No playable song source")
    }

    /** In priority order: user override, built-in song, system alarm/notification/ringtone. */
    private fun songSources(): List<(MediaPlayer) -> Unit> {
        val sources = mutableListOf<(MediaPlayer) -> Unit>()

        Prefs.songUri(this)?.takeIf(::isOpenable)?.let { uri ->
            sources.add { mp -> mp.setDataSource(this, uri) }
        }

        sources.add { mp ->
            resources.openRawResourceFd(R.raw.lost_phone_song).use { mp.setDataSource(it) }
        }

        for (type in listOf(
            RingtoneManager.TYPE_ALARM,
            RingtoneManager.TYPE_NOTIFICATION,
            RingtoneManager.TYPE_RINGTONE,
        )) {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, type)
                ?: RingtoneManager.getDefaultUri(type)
                ?: continue
            sources.add { mp -> mp.setDataSource(this, uri) }
        }
        return sources
    }

    private fun isOpenable(uri: Uri): Boolean = try {
        contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
    } catch (e: Exception) {
        Log.w(TAG, "Override song not openable: $uri", e)
        false
    }

    private fun maxAlarmVolume() {
        try {
            val audio = getSystemService(AudioManager::class.java)
            if (previousAlarmVolume == null) {
                previousAlarmVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM)
            }
            audio.setStreamVolume(
                AudioManager.STREAM_ALARM,
                audio.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                0,
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not raise alarm volume", e)
        }
    }

    private fun restoreAlarmVolume() {
        val volume = previousAlarmVolume ?: return
        previousAlarmVolume = null
        try {
            getSystemService(AudioManager::class.java)
                .setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
        } catch (e: Exception) {
            Log.w(TAG, "Could not restore alarm volume", e)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LostPhoneSong:playback")
            .apply {
                setReferenceCounted(false)
                acquire(AUTO_STOP_MS + 60_000L)
            }
    }

    private fun stopEverything() {
        releaseResources()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseResources() {
        handler.removeCallbacks(autoStop)
        player?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping player", e)
            }
            it.release()
        }
        player = null
        restoreAlarmVolume()
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.channel_description)
            setSound(null, null) // the service plays its own audio
        }
        manager.createNotificationChannel(channel)

        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, AlarmService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setCategory(Notification.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_stop),
                    getString(R.string.stop),
                    stopIntent,
                ).build()
            )
            .build()
    }

    companion object {
        private const val TAG = "LostPhoneSong"
        private const val ACTION_START = "com.deyotechnologies.lostphonesong.START"
        private const val ACTION_STOP = "com.deyotechnologies.lostphonesong.STOP"
        private const val CHANNEL_ID = "lost_phone_alarm"
        private const val NOTIFICATION_ID = 1
        private const val AUTO_STOP_MS = 5 * 60 * 1000L

        /** Returns false if Android refused to start the service (e.g. background-start limits). */
        fun start(context: Context): Boolean = try {
            context.startForegroundService(
                Intent(context, AlarmService::class.java).setAction(ACTION_START)
            )
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not start alarm service", e)
            false
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }
    }
}
