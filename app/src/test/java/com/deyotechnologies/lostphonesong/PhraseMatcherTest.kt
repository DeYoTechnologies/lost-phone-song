package com.deyotechnologies.lostphonesong

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhraseMatcherTest {

    @Test
    fun matchesUppercase() = assertTrue(PhraseMatcher.matches("LOST PHONE SONG"))

    @Test
    fun matchesInsideSentence() = assertTrue(PhraseMatcher.matches("hey lost phone song please"))

    @Test
    fun matchesExtraWhitespace() = assertTrue(PhraseMatcher.matches("lost   phone song"))

    @Test
    fun matchesMixedCaseWithPunctuation() = assertTrue(PhraseMatcher.matches("Lost Phone Song!"))

    @Test
    fun rejectsWordInBetween() = assertFalse(PhraseMatcher.matches("lost my phone song"))

    @Test
    fun rejectsWrongOrder() = assertFalse(PhraseMatcher.matches("phone lost song"))

    @Test
    fun rejectsJoinedWords() = assertFalse(PhraseMatcher.matches("lostphone song"))

    @Test
    fun rejectsEmptyAndNull() {
        assertFalse(PhraseMatcher.matches(""))
        assertFalse(PhraseMatcher.matches(null))
    }
}
