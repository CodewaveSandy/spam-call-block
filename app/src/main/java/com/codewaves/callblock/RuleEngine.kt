package com.codewaves.callblock

import java.util.UUID

enum class MatchType(val label: String, val description: String) {
    PREFIX("Starts with", "Block numbers beginning with these digits"),
    SUFFIX("Ends with", "Block numbers ending with these digits"),
    EXACT("Exact number", "Block one complete phone number"),
    CONTAINS("Contains", "Block numbers with these digits anywhere"),
    WILDCARD("Pattern", "Use ? for one digit and * for any digits")
}

data class BlockingRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: MatchType,
    val value: String,
    val enabled: Boolean = true
)

object RuleEngine {
    fun clean(value: String): String = value.filterNot {
        it.isWhitespace() || it == '-' || it == '(' || it == ')'
    }

    fun error(type: MatchType, value: String): String? {
        val clean = clean(value)
        val digits = clean.removePrefix("+")
        if (digits.isEmpty()) return "Enter a number or pattern."
        if (clean.length > 32) return "Keep your rule under 33 characters."
        if (digits.any { it !in '0'..'9' && !(type == MatchType.WILDCARD && it in "*?") })
            return "Use digits${if (type == MatchType.WILDCARD) ", ? and *" else ""}. A + is allowed only at the start."
        if (digits.none { it in '0'..'9' }) return "Include at least one digit to avoid blocking every call."
        if (type == MatchType.EXACT && digits.length !in 7..15) return "An exact number must contain 7–15 digits."
        return null
    }

    fun matches(number: String?, rule: BlockingRule): Boolean {
        if (!rule.enabled || number == null || error(rule.type, rule.value) != null) return false
        val incoming = clean(number)
        val digits = incoming.removePrefix("+")
        if (digits.isEmpty() || digits.any { it !in '0'..'9' } || digits.length > 15) return false
        val pattern = clean(rule.value)
        // + rules use country codes. Local rules strip India's +91 for ten-digit mobiles.
        val candidate = if (pattern.startsWith("+")) {
            if (!incoming.startsWith("+")) return false
            digits
        } else if (incoming.startsWith("+91") && digits.length == 12) digits.drop(2) else digits
        val value = pattern.removePrefix("+")
        return when (rule.type) {
            MatchType.PREFIX -> candidate.startsWith(value)
            MatchType.SUFFIX -> candidate.endsWith(value)
            MatchType.EXACT -> candidate == value
            MatchType.CONTAINS -> candidate.contains(value)
            MatchType.WILDCARD -> wildcard(candidate, value)
        }
    }

    // Greedy wildcard matching avoids user-provided regex and backtracking expressions.
    private fun wildcard(number: String, pattern: String): Boolean {
        var n = 0; var p = 0; var star = -1; var retry = 0
        while (n < number.length) {
            when {
                p < pattern.length && (pattern[p] == '?' || pattern[p] == number[n]) -> { n++; p++ }
                p < pattern.length && pattern[p] == '*' -> { star = p++; retry = n }
                star >= 0 -> { p = star + 1; n = ++retry }
                else -> return false
            }
        }
        while (p < pattern.length && pattern[p] == '*') p++
        return p == pattern.length
    }

    fun shouldBlock(number: String?, enabled: Boolean, rules: List<BlockingRule>): Boolean =
        enabled && rules.any { matches(number, it) }
}
