package com.pricehub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WarrantyParserTest {

    @Test
    @DisplayName("Should parse standard 1 Year and multi-year warranties to months")
    void testParseYearWarranties() {
        assertEquals(12, WarrantyParser.parseToMonths("1 Year"));
        assertEquals(12, WarrantyParser.parseToMonths("1 year"));
        assertEquals(24, WarrantyParser.parseToMonths("2 Years"));
        assertEquals(24, WarrantyParser.parseToMonths("2 year"));
        assertEquals(36, WarrantyParser.parseToMonths("3 yrs"));
    }

    @Test
    @DisplayName("Should parse month-based warranties to months")
    void testParseMonthWarranties() {
        assertEquals(1, WarrantyParser.parseToMonths("1 Month"));
        assertEquals(6, WarrantyParser.parseToMonths("6 Months"));
        assertEquals(3, WarrantyParser.parseToMonths("3 mo"));
    }

    @Test
    @DisplayName("Should return 0 for No Warranty, empty, or unlisted warranties")
    void testParseZeroOrMissingWarranties() {
        assertEquals(0, WarrantyParser.parseToMonths("No Warranty"));
        assertEquals(0, WarrantyParser.parseToMonths("Warranty not listed"));
        assertEquals(0, WarrantyParser.parseToMonths("None"));
        assertEquals(0, WarrantyParser.parseToMonths(""));
        assertEquals(0, WarrantyParser.parseToMonths("   "));
        assertEquals(0, WarrantyParser.parseToMonths(null));
    }
}