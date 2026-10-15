package com.deyotechnologies.lostphonesong

object PhraseMatcher {
    private val PHRASE = Regex("""(?i)\blost\s+phone\s+song\b""")

    fun matches(body: String?): Boolean = body != null && PHRASE.containsMatchIn(body)
}
