package com.pricehub.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OOP: Utility class with static helper methods for string normalization and data parsing.
 * Converts unstructured warranty descriptions (e.g. "1 Year", "6 Months", "No Warranty")
 * into a comparable integer representing duration in months.
 */
public final class WarrantyParser {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)\\s*(year|yr|month|mo|m|y)", Pattern.CASE_INSENSITIVE);

    // Private constructor prevents instantiation of utility class
    private WarrantyParser() {
    }

    /**
     * Parses a raw warranty string into equivalent months.
     * Examples:
     * - "2 Years" -> 24
     * - "1 Year" -> 12
     * - "6 Months" -> 6
     * - "No Warranty" -> 0
     * - null / empty -> 0
     *
     * @param warrantyText raw string from CSV or marketplace
     * @return warranty duration in months
     */
    public static int parseToMonths(String warrantyText) {
        if (warrantyText == null || warrantyText.isBlank()) {
            return 0;
        }

        String normalized = warrantyText.trim().toLowerCase();
        if (normalized.contains("no warranty") || normalized.contains("none") || normalized.contains("not listed")) {
            return 0;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            if (unit.startsWith("y")) {
                return value * 12; // 1 year = 12 months
            } else if (unit.startsWith("m")) {
                return value;
            }
        }

        // Check if just "1 year" or "1 month" without spaces
        if (normalized.contains("year")) {
            return 12;
        } else if (normalized.contains("month")) {
            return 1;
        }

        return 0;
    }
}