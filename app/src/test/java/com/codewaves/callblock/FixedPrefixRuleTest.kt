package com.codewaves.callblock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedPrefixRuleTest {
    @Test fun blocksSupportedIndianFormats() {
        listOf("+918697123456", "8697123456", "918697123456", "08697123456",
            "00918697123456", "+91 (8697) 123-456").forEach {
            assertTrue(it, FixedPrefixRule.matches(it))
        }
    }
    @Test fun allowsOtherPrefixesCountriesAndMalformedNumbers() {
        listOf(null, "", "8697", "+918698123456", "+18697123456", "+448697123456",
            "448697123456", "+9186971234567", "+91869712AB56", "8697123456;123",
            "8697123456#", "abc8697123456").forEach {
            assertFalse(it, FixedPrefixRule.matches(it))
        }
    }
}
