package com.codewaves.callblock

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class RuleEngineTest {
    private fun rule(type: MatchType, value: String, enabled: Boolean = true) =
        BlockingRule(name = "Test", type = type, value = value, enabled = enabled)

    @Test fun eachMatchType() {
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.PREFIX, "+918697")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.PREFIX, "8697")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.SUFFIX, "3456")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.EXACT, "8697123456")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.EXACT, "+918697123456")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.CONTAINS, "9712")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.WILDCARD, "8697??????")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.WILDCARD, "+9186*56")))
        assertTrue(RuleEngine.matches("+918697123456", rule(MatchType.WILDCARD, "*97*56")))
    }

    @Test fun switchesAndEmptyDefaults() {
        val rules = listOf(rule(MatchType.PREFIX, "8697"))
        assertFalse(RuleEngine.shouldBlock("+918697123456", false, rules))
        assertFalse(RuleEngine.shouldBlock("+918697123456", true, emptyList()))
        assertFalse(RuleEngine.shouldBlock("+918697123456", true, rules.map { it.copy(enabled = false) }))
        assertTrue(RuleEngine.shouldBlock("+918697123456", true, rules))
        assertFalse(RuleEngine.shouldBlock("+918698123456", true, rules))
    }

    @Test fun normalizationAndForeignNumbers() {
        assertTrue(RuleEngine.matches("+91 (8697) 123-456", rule(MatchType.PREFIX, "+91 8697")))
        assertFalse(RuleEngine.matches("+448697123456", rule(MatchType.PREFIX, "8697")))
        assertFalse(RuleEngine.matches("8697123456", rule(MatchType.EXACT, "+918697123456")))
        assertFalse(RuleEngine.matches(null, rule(MatchType.CONTAINS, "8697")))
        assertFalse(RuleEngine.matches("", rule(MatchType.PREFIX, "8697")))
        assertFalse(RuleEngine.matches("+918697123456;123", rule(MatchType.PREFIX, "8697")))
    }

    @Test fun wildcardIsWholeNumberAndCanMatchZeroDigits() {
        assertTrue(RuleEngine.matches("8697123456", rule(MatchType.WILDCARD, "8697*123456")))
        assertFalse(RuleEngine.matches("8697123456", rule(MatchType.WILDCARD, "8697?????")))
        assertFalse(RuleEngine.matches("8697123456", rule(MatchType.WILDCARD, "8697*99")))
    }

    @Test fun malformedRulesCannotBlock() {
        listOf("", "*", "????", "86+97", "abcd", "1".repeat(33)).forEach {
            assertNotNull(it, RuleEngine.error(MatchType.WILDCARD, it))
            assertFalse(RuleEngine.matches("+918697123456", rule(MatchType.WILDCARD, it)))
        }
        assertNotNull(RuleEngine.error(MatchType.EXACT, "8697"))
        assertNotNull(RuleEngine.error(MatchType.PREFIX, "8697*"))
    }
    @Test fun wildcardAgreesWithWholeNumberRegex() {
        val random = Random(42)
        repeat(3000) {
            val number = (1..random.nextInt(1, 16)).map { ('0'..'9').random(random) }.joinToString("")
            val pattern = (1..random.nextInt(1, 18)).map { "0123456789*?".random(random) }.joinToString("")
            if (pattern.any { it in '0'..'9' }) {
                val regex = pattern.map { when (it) {
                    '*' -> "[0-9]*"
                    '?' -> "[0-9]"
                    else -> it.toString()
                } }.joinToString("")
                assertEquals("$pattern / $number", Regex(regex).matches(number),
                    RuleEngine.matches(number, rule(MatchType.WILDCARD, pattern)))
            }
        }
    }

}
