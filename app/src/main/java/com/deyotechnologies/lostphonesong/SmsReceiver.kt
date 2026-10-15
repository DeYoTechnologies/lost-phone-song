package com.deyotechnologies.lostphonesong

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/**
 * Manifest-registered, so it runs even when the app's UI has never been opened since boot.
 * Plays the song when the trusted number texts the saved trigger phrase.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val trusted = Prefs.trustedNumber(context) ?: return

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not parse SMS", e)
            null
        } ?: return

        // A long text arrives as several parts; join them per sender before matching.
        val bodies = LinkedHashMap<String, StringBuilder>()
        for (message in messages) {
            val sender = message?.originatingAddress ?: continue
            bodies.getOrPut(sender) { StringBuilder() }.append(message.messageBody.orEmpty())
        }

        val phrase = Prefs.triggerPhrase(context)
        val triggered = bodies.any { (sender, body) ->
            PhoneNumbers.matches(sender, trusted) && PhraseMatcher.matches(body.toString(), phrase)
        }
        if (triggered) AlarmService.trigger(context)
    }

    private companion object {
        const val TAG = "LostPhoneSong"
    }
}
