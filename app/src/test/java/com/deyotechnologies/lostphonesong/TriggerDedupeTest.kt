package com.deyotechnologies.lostphonesong

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerDedupeTest {

    @Test
    fun ignoresSecondTriggerWithinWindowWhilePlaying() =
        assertTrue(TriggerDedupe.shouldIgnore(105_000L, 100_000L, playing = true))

    @Test
    fun allowsTriggerAfterWindow() =
        assertFalse(TriggerDedupe.shouldIgnore(110_000L, 100_000L, playing = true))

    @Test
    fun allowsTriggerWhenNotPlaying() =
        assertFalse(TriggerDedupe.shouldIgnore(101_000L, 100_000L, playing = false))

    @Test
    fun allowsFirstTrigger() =
        assertFalse(TriggerDedupe.shouldIgnore(100_000L, null, playing = true))
}
