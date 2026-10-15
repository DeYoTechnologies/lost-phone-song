package com.deyotechnologies.lostphonesong

object PhoneNumbers {
    private const val SIGNIFICANT_DIGITS = 10

    /** Strips everything except digits, e.g. "+1 (555) 123-4567" -> "15551234567". */
    fun normalize(s: String?): String = s?.filter { it in '0'..'9' } ?: ""

    /** True when both numbers have at least 10 digits and their last 10 digits are equal. */
    fun matches(a: String?, b: String?): Boolean {
        val da = normalize(a)
        val db = normalize(b)
        if (da.length < SIGNIFICANT_DIGITS || db.length < SIGNIFICANT_DIGITS) return false
        return da.takeLast(SIGNIFICANT_DIGITS) == db.takeLast(SIGNIFICANT_DIGITS)
    }
}
