package com.deyotechnologies.lostphonesong

import java.util.concurrent.ConcurrentHashMap

/**
 * Matches the trigger phrase in a message body: its words, in order, separated by any whitespace,
 * case-insensitive, each word matched literally and not glued to other letters or digits.
 * Pure Kotlin so it can be unit tested on the JVM.
 */
object PhraseMatcher {
    const val DEFAULT_PHRASE = "lost phone song"

    private val cache = ConcurrentHashMap<String, Regex>()

    /** A phrase needs at least one non-whitespace word. */
    fun isValidPhrase(phrase: String?): Boolean = !phrase.isNullOrBlank()

    /** False for a null body or an invalid (blank) phrase. */
    fun matches(body: String?, phrase: String): Boolean {
        if (body == null || !isValidPhrase(phrase)) return false
        return regexFor(phrase).containsMatchIn(body)
    }

    /**
     * e.g. "find my phone" -> (?i)(?<!\w)find\s+my\s+phone(?!\w), with each word escaped.
     * Lookarounds instead of \b so words that start or end with punctuation ("help!?") still work.
     */
    fun regexFor(phrase: String): Regex = cache.getOrPut(phrase) {
        require(isValidPhrase(phrase)) { "Trigger phrase must contain a word" }
        val words = phrase.trim().split(Regex("""\s+""")).joinToString("""\s+""") { Regex.escape(it) }
        Regex("""(?i)(?<!\w)$words(?!\w)""")
    }
}
