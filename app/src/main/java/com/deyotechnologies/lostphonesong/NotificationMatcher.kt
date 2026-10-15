package com.deyotechnologies.lostphonesong

/**
 * Pure-Kotlin matching for message notifications (RCS/SMS shown by a messaging app).
 * Kept free of Android types so it can be unit tested on the JVM.
 */
object NotificationMatcher {

    /**
     * One message in a notification. [sender] is null when the notification has no per-message
     * sender; [timestamp] (epoch ms, 0 if unknown) only helps the caller tell repeats apart.
     */
    data class Message(val sender: String?, val text: String?, val timestamp: Long = 0L)

    /** Any run of digits with common phone separators between them, e.g. "+1 (555) 123-4567". */
    private val NUMBER_RUN = Regex("""\+?\d[\d\s().\-]*\d""")

    /**
     * True when a message contains the trigger [phrase] AND its sender (or [title] when the
     * message has no sender) is the trusted number or the trusted contact name.
     * With neither a trusted number nor a name configured, nothing matches.
     */
    fun matches(
        title: String?,
        messages: List<Message>,
        trustedNumber: String?,
        trustedName: String?,
        phrase: String,
    ): Boolean = matchingMessages(title, messages, trustedNumber, trustedName, phrase).isNotEmpty()

    /** The messages that would trigger, in order. See [matches]. */
    fun matchingMessages(
        title: String?,
        messages: List<Message>,
        trustedNumber: String?,
        trustedName: String?,
        phrase: String,
    ): List<Message> {
        if (trustedNumber.isNullOrBlank() && trustedName.isNullOrBlank()) return emptyList()
        return messages.filter { message ->
            PhraseMatcher.matches(message.text, phrase) &&
                senderMatches(message.sender ?: title, trustedNumber, trustedName)
        }
    }

    fun senderMatches(sender: String?, trustedNumber: String?, trustedName: String?): Boolean {
        if (sender.isNullOrBlank()) return false
        return nameMatches(sender, trustedName) || containsNumber(sender, trustedNumber)
    }

    /** Trimmed, case-insensitive, whole-string equality. A blank name never matches. */
    fun nameMatches(sender: String?, trustedName: String?): Boolean {
        val name = trustedName?.trim().orEmpty()
        if (name.isEmpty() || sender == null) return false
        return sender.trim().equals(name, ignoreCase = true)
    }

    /** True when any digit run in [text] is the trusted number (last-10-digit match). */
    fun containsNumber(text: String?, trustedNumber: String?): Boolean {
        if (text == null || trustedNumber.isNullOrBlank()) return false
        return NUMBER_RUN.findAll(text).any { PhoneNumbers.matches(it.value, trustedNumber) }
    }
}
