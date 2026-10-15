package com.deyotechnologies.lostphonesong

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseMatcherTest {

    private val default = PhraseMatcher.DEFAULT_PHRASE

    // --- default phrase ---

    @Test
    fun defaultIsLostPhoneSong() = assertEquals("lost phone song", default)

    @Test
    fun matchesUppercase() = assertTrue(PhraseMatcher.matches("LOST PHONE SONG", default))

    @Test
    fun matchesInsideSentence() = assertTrue(PhraseMatcher.matches("hey lost phone song please", default))

    @Test
    fun matchesExtraWhitespace() = assertTrue(PhraseMatcher.matches("lost   phone song", default))

    @Test
    fun matchesMixedCaseWithPunctuation() = assertTrue(PhraseMatcher.matches("Lost Phone Song!", default))

    @Test
    fun rejectsWordInBetween() = assertFalse(PhraseMatcher.matches("lost my phone song", default))

    @Test
    fun rejectsWrongOrder() = assertFalse(PhraseMatcher.matches("phone lost song", default))

    @Test
    fun rejectsJoinedWords() = assertFalse(PhraseMatcher.matches("lostphone song", default))

    @Test
    fun rejectsEmptyAndNull() {
        assertFalse(PhraseMatcher.matches("", default))
        assertFalse(PhraseMatcher.matches(null, default))
    }

    // --- custom phrase ---

    @Test
    fun customPhraseMatches() {
        val phrase = "find my phone"
        assertTrue(PhraseMatcher.matches("FIND MY PHONE now", phrase))
        assertTrue(PhraseMatcher.matches("hey find   my phone", phrase))
    }

    @Test
    fun customPhraseRejectsNearMisses() {
        val phrase = "find my phone"
        assertFalse(PhraseMatcher.matches("find my old phone", phrase))
        assertFalse(PhraseMatcher.matches("findmy phone", phrase))
        assertFalse(PhraseMatcher.matches("lost phone song", phrase))
    }

    @Test
    fun customPhraseIgnoresSurroundingAndRepeatedSpaces() {
        assertTrue(PhraseMatcher.matches("find my phone", "  find   my phone  "))
    }

    @Test
    fun punctuationIsMatchedLiterally() {
        val phrase = "help!? now"
        assertTrue(PhraseMatcher.matches("please help!? now", phrase))
        assertFalse(PhraseMatcher.matches("helpx now", phrase))
        assertFalse(PhraseMatcher.matches("help now", phrase))
    }

    @Test
    fun regexIsCachedPerPhrase() {
        assertSame(PhraseMatcher.regexFor("find my phone"), PhraseMatcher.regexFor("find my phone"))
    }

    // --- validation ---

    @Test
    fun blankPhraseRejectedByValidator() {
        assertFalse(PhraseMatcher.isValidPhrase(""))
        assertFalse(PhraseMatcher.isValidPhrase("   \t "))
        assertFalse(PhraseMatcher.isValidPhrase(null))
        assertTrue(PhraseMatcher.isValidPhrase(" find "))
    }

    @Test
    fun blankPhraseNeverMatches() {
        assertFalse(PhraseMatcher.matches("anything at all", ""))
        assertFalse(PhraseMatcher.matches("anything at all", "   "))
    }
}
