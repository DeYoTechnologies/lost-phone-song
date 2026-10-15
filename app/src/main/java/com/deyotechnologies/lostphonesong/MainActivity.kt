package com.deyotechnologies.lostphonesong

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class MainActivity : Activity() {

    private lateinit var numberInput: EditText
    private lateinit var savedNumberText: TextView
    private lateinit var nameInput: EditText
    private lateinit var savedNameText: TextView
    private lateinit var phraseInput: EditText
    private lateinit var savedPhraseText: TextView
    private lateinit var notificationAccessStatus: TextView
    private lateinit var songText: TextView
    private lateinit var smsStatus: TextView
    private lateinit var notificationStatus: TextView
    private lateinit var batteryStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberInput = findViewById(R.id.number_input)
        savedNumberText = findViewById(R.id.saved_number)
        nameInput = findViewById(R.id.name_input)
        savedNameText = findViewById(R.id.saved_name)
        phraseInput = findViewById(R.id.phrase_input)
        savedPhraseText = findViewById(R.id.saved_phrase)
        notificationAccessStatus = findViewById(R.id.notification_access_status)
        songText = findViewById(R.id.song_name)
        smsStatus = findViewById(R.id.sms_status)
        notificationStatus = findViewById(R.id.notification_status)
        batteryStatus = findViewById(R.id.battery_status)

        Prefs.trustedNumber(this)?.let { numberInput.setText(it) }
        Prefs.trustedName(this)?.let { nameInput.setText(it) }
        phraseInput.setText(Prefs.triggerPhrase(this))

        findViewById<Button>(R.id.save_number).setOnClickListener { saveNumber() }
        findViewById<Button>(R.id.save_name).setOnClickListener { saveName() }
        findViewById<Button>(R.id.save_phrase).setOnClickListener { savePhrase() }
        findViewById<Button>(R.id.reset_phrase).setOnClickListener { resetPhrase() }
        findViewById<Button>(R.id.notification_access_settings).setOnClickListener {
            openNotificationAccessSettings()
        }
        findViewById<Button>(R.id.choose_song).setOnClickListener { chooseSong() }
        findViewById<Button>(R.id.reset_song).setOnClickListener { resetSong() }
        findViewById<Button>(R.id.request_permissions).setOnClickListener { requestMissingPermissions() }
        findViewById<Button>(R.id.open_app_settings).setOnClickListener { openAppDetails() }
        findViewById<Button>(R.id.battery_settings).setOnClickListener { openBatterySettings() }
        findViewById<Button>(R.id.test_button).setOnClickListener {
            if (AlarmService.start(this)) {
                startActivity(StopActivity.intent(this))
            } else {
                toast(getString(R.string.test_failed))
            }
        }
        findViewById<Button>(R.id.stop_button).setOnClickListener { AlarmService.stop(this) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val number = Prefs.trustedNumber(this)
        savedNumberText.text = if (number == null) {
            getString(R.string.no_number_saved)
        } else {
            getString(R.string.saved_number, number)
        }

        val name = Prefs.trustedName(this)
        savedNameText.text = if (name == null) {
            getString(R.string.no_name_saved)
        } else {
            getString(R.string.saved_name, name)
        }

        savedPhraseText.text = getString(R.string.current_phrase, Prefs.triggerPhrase(this))

        songText.text = getString(R.string.current_song, currentSongName())

        smsStatus.text = getString(
            R.string.sms_permission_status,
            statusLabel(isGranted(Manifest.permission.RECEIVE_SMS)),
        )
        notificationStatus.text = getString(
            R.string.notification_permission_status,
            statusLabel(isGranted(Manifest.permission.POST_NOTIFICATIONS)),
        )
        val listenerEnabled = NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(packageName)
        notificationAccessStatus.text = getString(
            R.string.notification_access_status,
            getString(if (listenerEnabled) R.string.enabled else R.string.not_enabled),
        )
        val unrestricted = getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(packageName)
        batteryStatus.text = getString(
            R.string.battery_status,
            getString(if (unrestricted) R.string.unrestricted else R.string.optimized),
        )
    }

    private fun saveNumber() {
        val raw = numberInput.text.toString().trim()
        if (PhoneNumbers.normalize(raw).length < 10) {
            numberInput.error = getString(R.string.number_invalid)
            return
        }
        Prefs.setTrustedNumber(this, raw)
        toast(getString(R.string.number_saved))
        refresh()
    }

    private fun saveName() {
        val name = nameInput.text.toString().trim()
        Prefs.setTrustedName(this, name)
        toast(getString(if (name.isEmpty()) R.string.name_cleared else R.string.name_saved))
        refresh()
    }

    private fun savePhrase() {
        val phrase = phraseInput.text.toString().trim()
        if (!PhraseMatcher.isValidPhrase(phrase)) {
            phraseInput.error = getString(R.string.phrase_invalid)
            return
        }
        Prefs.setTriggerPhrase(this, phrase)
        phraseInput.setText(phrase)
        toast(getString(R.string.phrase_saved))
        refresh()
    }

    private fun resetPhrase() {
        Prefs.setTriggerPhrase(this, null)
        phraseInput.setText(PhraseMatcher.DEFAULT_PHRASE)
        phraseInput.error = null
        toast(getString(R.string.phrase_reset, PhraseMatcher.DEFAULT_PHRASE))
        refresh()
    }

    private fun openNotificationAccessSettings() {
        for (intent in listOf(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
            appDetailsIntent(),
        )) {
            try {
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "No activity for ${intent.action}", e)
            }
        }
    }

    private fun currentSongName(): String {
        val uri = Prefs.songUri(this) ?: return getString(R.string.default_song)
        val name: String? = try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read song name", e)
            null
        }
        return name ?: uri.lastPathSegment ?: uri.toString()
    }

    private fun chooseSong() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("audio/*")
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        try {
            startActivityForResult(intent, REQUEST_PICK_SONG)
        } catch (e: ActivityNotFoundException) {
            toast(getString(R.string.no_picker))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_SONG || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            Log.w(TAG, "Could not persist song permission", e)
            toast(getString(R.string.song_permission_failed))
            return
        }
        Prefs.songUri(this)?.takeIf { it != uri }?.let(::releaseSongPermission)
        Prefs.setSongUri(this, uri)
        refresh()
    }

    private fun resetSong() {
        Prefs.songUri(this)?.let(::releaseSongPermission)
        Prefs.setSongUri(this, null)
        refresh()
    }

    private fun releaseSongPermission(uri: Uri) {
        try {
            contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            Log.w(TAG, "Song permission already gone", e)
        }
    }

    private fun requestMissingPermissions() {
        val missing = listOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.POST_NOTIFICATIONS)
            .filterNot(::isGranted)
        if (missing.isEmpty()) {
            toast(getString(R.string.all_granted))
        } else {
            requestPermissions(missing.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
        // A denied (or "restricted setting") permission has to be granted from App info.
        if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            toast(getString(R.string.grant_in_settings))
        }
    }

    private fun openBatterySettings() {
        val attempts = listOf(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$packageName"),
            ),
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            appDetailsIntent(),
        )
        for (intent in attempts) {
            try {
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "No activity for ${intent.action}", e)
            }
        }
    }

    private fun openAppDetails() {
        try {
            startActivity(appDetailsIntent())
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No app details screen", e)
        }
    }

    private fun appDetailsIntent() =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))

    private fun isGranted(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun statusLabel(granted: Boolean) =
        getString(if (granted) R.string.granted else R.string.not_granted)

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private companion object {
        const val TAG = "LostPhoneSong"
        const val REQUEST_PICK_SONG = 1
        const val REQUEST_PERMISSIONS = 2
    }
}
