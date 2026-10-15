package com.deyotechnologies.lostphonesong

import com.deyotechnologies.lostphonesong.NotificationMatcher.Message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationMatcherTest {

    private val number = "5551234567"
    private val name = "Jamie Smith"

    // --- name matching ---

    @Test
    fun nameMatchIgnoresCase() {
        assertTrue(NotificationMatcher.nameMatches("JAMIE SMITH", name))
        assertTrue(NotificationMatcher.nameMatches("jamie smith", name))
    }

    @Test
    fun nameMatchTrimsWhitespace() {
        assertTrue(NotificationMatcher.nameMatches("  Jamie Smith ", name))
        assertTrue(NotificationMatcher.nameMatches("Jamie Smith", "  Jamie Smith  "))
    }

    @Test
    fun partialNameDoesNotMatch() {
        assertFalse(NotificationMatcher.nameMatches("Jamie", name))
        assertFalse(NotificationMatcher.nameMatches("Jamie Smith Jr", name))
        assertFalse(NotificationMatcher.nameMatches("Jamie Smith", "Jamie"))
    }

    @Test
    fun blankNameNeverMatches() {
        assertFalse(NotificationMatcher.nameMatches("", ""))
        assertFalse(NotificationMatcher.nameMatches("   ", "   "))
        assertFalse(NotificationMatcher.nameMatches("Jamie Smith", null))
        assertFalse(NotificationMatcher.nameMatches(null, name))
    }

    // --- notification matching ---

    @Test
    fun numberEmbeddedInTitleMatches() {
        val messages = listOf(Message(null, "lost phone song"))
        assertTrue(NotificationMatcher.matches("+1 (555) 123-4567", messages, number, null))
        assertTrue(NotificationMatcher.matches("Message from +1 (555) 123-4567", messages, number, null))
    }

    @Test
    fun titleNameMatches() {
        val messages = listOf(Message(null, "Lost Phone Song!"))
        assertTrue(NotificationMatcher.matches("jamie smith", messages, null, name))
    }

    @Test
    fun perMessageSenderNameMatches() {
        val messages = listOf(Message("Jamie Smith", "lost phone song"))
        assertTrue(NotificationMatcher.matches("Group chat", messages, null, name))
    }

    @Test
    fun wrongSenderDoesNotMatch() {
        val messages = listOf(Message(null, "lost phone song"))
        assertFalse(NotificationMatcher.matches("+1 (555) 765-4321", messages, number, name))
        assertFalse(NotificationMatcher.matches("Alex", messages, number, name))
        assertFalse(NotificationMatcher.matches("Jamie", messages, number, name))
    }

    @Test
    fun phraseAbsentDoesNotMatch() {
        val messages = listOf(Message(null, "where is my phone?"))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, number, name))
        assertFalse(NotificationMatcher.matches("+1 (555) 123-4567", messages, number, name))
    }

    @Test
    fun nothingConfiguredNeverMatches() {
        val messages = listOf(Message("Jamie Smith", "lost phone song"))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, null, null))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, "", "  "))
    }

    @Test
    fun messagingStyleOnlyTrustedSenderWithPhraseTriggers() {
        // Phrase from a stranger, trusted sender without phrase: no trigger.
        val noTrigger = listOf(
            Message("Alex", "lost phone song"),
            Message("Jamie Smith", "hello"),
        )
        assertFalse(NotificationMatcher.matches("Group chat", noTrigger, number, name))

        // Same pair plus the trusted sender saying the phrase: trigger.
        val trigger = noTrigger + Message("Jamie Smith", "LOST PHONE SONG")
        assertTrue(NotificationMatcher.matches("Group chat", trigger, number, name))
    }

    @Test
    fun messagingStyleSenderNumberMatches() {
        val messages = listOf(
            Message("+1 555-765-4321", "lost phone song"),
            Message("(555) 123-4567", "lost phone song"),
        )
        assertTrue(NotificationMatcher.matches(null, messages, number, null))
        assertFalse(NotificationMatcher.matches(null, messages.take(1), number, null))
    }

    @Test
    fun perMessageSenderOverridesTitle() {
        // Title is the trusted name, but the message itself came from someone else.
        val messages = listOf(Message("Alex", "lost phone song"))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, null, name))
    }
}
