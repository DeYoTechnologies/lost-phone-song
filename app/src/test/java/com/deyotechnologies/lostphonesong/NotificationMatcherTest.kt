package com.deyotechnologies.lostphonesong

import com.deyotechnologies.lostphonesong.NotificationMatcher.Message
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationMatcherTest {

    private val number = "5551234567"
    private val name = "Jamie Smith"
    private val phrase = PhraseMatcher.DEFAULT_PHRASE

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
        assertTrue(NotificationMatcher.matches("+1 (555) 123-4567", messages, number, null, phrase))
        assertTrue(NotificationMatcher.matches("Message from +1 (555) 123-4567", messages, number, null, phrase))
    }

    @Test
    fun titleNameMatches() {
        val messages = listOf(Message(null, "Lost Phone Song!"))
        assertTrue(NotificationMatcher.matches("jamie smith", messages, null, name, phrase))
    }

    @Test
    fun perMessageSenderNameMatches() {
        val messages = listOf(Message("Jamie Smith", "lost phone song"))
        assertTrue(NotificationMatcher.matches("Group chat", messages, null, name, phrase))
    }

    @Test
    fun wrongSenderDoesNotMatch() {
        val messages = listOf(Message(null, "lost phone song"))
        assertFalse(NotificationMatcher.matches("+1 (555) 765-4321", messages, number, name, phrase))
        assertFalse(NotificationMatcher.matches("Alex", messages, number, name, phrase))
        assertFalse(NotificationMatcher.matches("Jamie", messages, number, name, phrase))
    }

    @Test
    fun phraseAbsentDoesNotMatch() {
        val messages = listOf(Message(null, "where is my phone?"))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, number, name, phrase))
        assertFalse(NotificationMatcher.matches("+1 (555) 123-4567", messages, number, name, phrase))
    }

    @Test
    fun nothingConfiguredNeverMatches() {
        val messages = listOf(Message("Jamie Smith", "lost phone song"))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, null, null, phrase))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, "", "  ", phrase))
    }

    @Test
    fun messagingStyleOnlyTrustedSenderWithPhraseTriggers() {
        // Phrase from a stranger, trusted sender without phrase: no trigger.
        val noTrigger = listOf(
            Message("Alex", "lost phone song"),
            Message("Jamie Smith", "hello"),
        )
        assertFalse(NotificationMatcher.matches("Group chat", noTrigger, number, name, phrase))

        // Same pair plus the trusted sender saying the phrase: trigger.
        val trigger = noTrigger + Message("Jamie Smith", "LOST PHONE SONG")
        assertTrue(NotificationMatcher.matches("Group chat", trigger, number, name, phrase))
    }

    @Test
    fun messagingStyleSenderNumberMatches() {
        val messages = listOf(
            Message("+1 555-765-4321", "lost phone song"),
            Message("(555) 123-4567", "lost phone song"),
        )
        assertTrue(NotificationMatcher.matches(null, messages, number, null, phrase))
        assertFalse(NotificationMatcher.matches(null, messages.take(1), number, null, phrase))
    }

    @Test
    fun perMessageSenderOverridesTitle() {
        // Title is the trusted name, but the message itself came from someone else.
        val messages = listOf(Message("Alex", "lost phone song"))
        assertFalse(NotificationMatcher.matches("Jamie Smith", messages, null, name, phrase))
    }

    @Test
    fun customPhraseIsUsedInsteadOfDefault() {
        val custom = "find my phone"
        val customText = listOf(Message("Jamie Smith", "Find my phone please"))
        val defaultText = listOf(Message("Jamie Smith", "lost phone song"))
        assertTrue(NotificationMatcher.matches(null, customText, number, name, custom))
        assertFalse(NotificationMatcher.matches(null, defaultText, number, name, custom))
    }
}
