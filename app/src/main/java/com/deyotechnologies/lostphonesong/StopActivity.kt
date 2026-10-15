package com.deyotechnologies.lostphonesong

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.window.OnBackInvokedDispatcher

/**
 * Full-screen "you found it" screen with one big Stop button. Shown over the lock screen from the
 * playback notification. Lives in its own task so closing it never reveals the settings screen.
 */
class StopActivity : Activity() {

    private val stoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_stop)

        findViewById<Button>(R.id.big_stop_button).setOnClickListener {
            AlarmService.stop(this)
            close()
        }
        // Back just closes this screen (the song keeps playing until Stop or the timeout).
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) { close() }

        registerReceiver(
            stoppedReceiver,
            IntentFilter(AlarmService.ACTION_STOPPED),
            RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onResume() {
        super.onResume()
        // The song may have stopped while this screen was in the background.
        if (!AlarmService.running) close()
    }

    override fun onDestroy() {
        unregisterReceiver(stoppedReceiver)
        super.onDestroy()
    }

    private fun close() {
        if (!isFinishing) finishAndRemoveTask()
    }

    companion object {
        fun intent(context: Context): Intent =
            Intent(context, StopActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
