package com.deyotechnologies.lostphonesong

/**
 * One text can reach us twice (as an SMS broadcast and as a messaging-app notification).
 * While the song is already playing, a trigger within [WINDOW_MS] of the previous one is ignored
 * so it doesn't restart the 5-minute timer twice.
 */
object TriggerDedupe {
    const val WINDOW_MS = 10_000L

    fun shouldIgnore(nowMs: Long, previousTriggerMs: Long?, playing: Boolean): Boolean =
        playing && previousTriggerMs != null && nowMs - previousTriggerMs in 0 until WINDOW_MS
}
