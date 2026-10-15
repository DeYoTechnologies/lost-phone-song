package com.deyotechnologies.lostphonesong

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun normalizeKeepsDigitsOnly() {
        assertEquals("15551234567", PhoneNumbers.normalize("+1 555-123-4567"))
        assertEquals("5551234567", PhoneNumbers.normalize("(555) 123-4567"))
        assertEquals("5551234567", PhoneNumbers.normalize("5551234567"))
    }

    @Test
    fun normalizeEmptyAndNull() {
        assertEquals("", PhoneNumbers.normalize(""))
        assertEquals("", PhoneNumbers.normalize(null))
        assertEquals("", PhoneNumbers.normalize("abc-+()"))
    }

    @Test
    fun equivalentFormatsMatch() {
        val formats = listOf("+1 555-123-4567", "5551234567", "(555) 123-4567")
        for (a in formats) for (b in formats) {
            assertTrue("$a vs $b", PhoneNumbers.matches(a, b))
        }
    }

    @Test
    fun differentNumbersDoNotMatch() {
        assertFalse(PhoneNumbers.matches("5551234567", "5551234568"))
        assertFalse(PhoneNumbers.matches("+1 555-123-4567", "+1 555-765-4321"))
    }

    @Test
    fun emptyNumbersDoNotMatch() {
        assertFalse(PhoneNumbers.matches("", ""))
        assertFalse(PhoneNumbers.matches(null, null))
        assertFalse(PhoneNumbers.matches("", "5551234567"))
        assertFalse(PhoneNumbers.matches("5551234567", null))
    }

    @Test
    fun shortNumbersDoNotMatch() {
        assertFalse(PhoneNumbers.matches("1234567", "1234567"))
        assertFalse(PhoneNumbers.matches("555123456", "555123456"))
        assertFalse(PhoneNumbers.matches("12345", "5551212345"))
        assertFalse(PhoneNumbers.matches("5551234567", "1234567"))
    }
}
