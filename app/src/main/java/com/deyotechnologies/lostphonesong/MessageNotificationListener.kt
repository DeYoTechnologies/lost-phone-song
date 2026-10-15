package com.deyotechnologies.lostphonesong

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Catches RCS (and SMS) texts as they appear in the messaging app's notification, since RCS
 * messages never arrive as an SMS broadcast. Needs Notification access enabled in Settings.
 */
class MessageNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        try {
            handle(sbn)
        } catch (e: Exception) {
            Log.w(TAG, "Could not inspect notification", e)
        }
    }

    private fun handle(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val notification = sbn.notification ?: return
        if (notification.category != Notification.CATEGORY_MESSAGE &&
            sbn.packageName !in MESSAGING_PACKAGES
        ) return

        val trustedNumber = Prefs.trustedNumber(this)
        val trustedName = Prefs.trustedName(this)
        if (trustedNumber == null && trustedName == null) return

        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val messages = mutableListOf<NotificationMatcher.Message>()
        for (key in listOf(Notification.EXTRA_TEXT, Notification.EXTRA_BIG_TEXT)) {
            extras.getCharSequence(key)?.toString()?.let {
                messages.add(NotificationMatcher.Message(null, it, notification.`when`))
            }
        }
        // MessagingStyle keeps recent history, so skip old messages that are just being re-shown.
        val now = System.currentTimeMillis()
        NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
            ?.messages
            ?.filter { it.timestamp <= 0 || now - it.timestamp < MAX_MESSAGE_AGE_MS }
            ?.forEach {
                val sender = it.person?.name?.toString()
                messages.add(NotificationMatcher.Message(sender, it.text?.toString(), it.timestamp))
            }

        val phrase = Prefs.triggerPhrase(this)
        val matching = NotificationMatcher.matchingMessages(
            title, messages, trustedNumber, trustedName, phrase,
        )
        // A conversation's notification is re-posted for every new message, carrying the old
        // ones with it; only trigger for a message we haven't triggered on before.
        val fresh = matching.map { "${sbn.key}|${it.sender}|${it.text}|${it.timestamp}" }
            .filter { remember(it) }
        if (fresh.isNotEmpty()) {
            Log.i(TAG, "Trigger phrase in notification from ${sbn.packageName}")
            AlarmService.trigger(this)
        }
    }

    private companion object {
        const val TAG = "LostPhoneSong"
        const val MAX_MESSAGE_AGE_MS = 10 * 60 * 1000L
        const val MAX_REMEMBERED = 100

        val MESSAGING_PACKAGES = setOf(
            "com.samsung.android.messaging",
            "com.google.android.apps.messaging",
        )

        private val handled = LinkedHashSet<String>()

        /** Returns true the first time a fingerprint is seen. */
        @Synchronized
        fun remember(fingerprint: String): Boolean {
            if (!handled.add(fingerprint)) return false
            while (handled.size > MAX_REMEMBERED) handled.remove(handled.first())
            return true
        }
    }
}
