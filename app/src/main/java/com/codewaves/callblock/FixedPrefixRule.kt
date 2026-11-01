package com.codewaves.callblock

/** Phase 1: match complete Indian mobile numbers, never infer a foreign country. */
object FixedPrefixRule {
    fun matches(number: String?): Boolean {
        if (number == null) return false
        val compact = number.filterNot { it.isWhitespace() || it == '-' || it == '(' || it == ')' }
        val national = when {
            compact.startsWith("+91") -> compact.drop(3)
            compact.startsWith("0091") -> compact.drop(4)
            compact.startsWith("+") -> return false
            compact.length == 12 && compact.startsWith("91") -> compact.drop(2)
            compact.length == 11 && compact.startsWith("0") -> compact.drop(1)
            else -> compact
        }
        return national.length == 10 && national.all { it in '0'..'9' } && national.startsWith("8697")
    }
}
