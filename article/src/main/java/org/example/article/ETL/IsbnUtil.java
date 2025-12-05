package org.example.article.ETL;

public final class IsbnUtil {

    public static String normalizePublisherName(String name) {
        if (name == null) return null;
        return name
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")  // keep letters/digits, collapse others
                .trim()
                .replaceAll("\\s+", " ");       // normalize spaces
    }

    public static String normalizeIsbn13Strict(String rawIsbn) {
        if (rawIsbn == null) {
            return null;
        }

        String digits = rawIsbn.replaceAll("\\D", "");
        if (digits.length() != 13) {
            throw new IllegalArgumentException("Expected 13-digit ISBN, got: " + rawIsbn);
        }

        if (!hasValidIsbn13Checksum(digits)) {
            throw new IllegalArgumentException("Invalid ISBN-13 checksum: " + rawIsbn);
        }

        return digits;
    }

    public static boolean hasValidIsbn13Checksum(String rawIsbn) {
        if (rawIsbn == null) {
            return false;
        }

        String digits = rawIsbn.replaceAll("\\D", "");
        if (digits.length() != 13) {
            return false;
        }

        String first12 = digits.substring(0, 12);
        char lastChar = digits.charAt(12);
        if (!Character.isDigit(lastChar)) {
            return false;
        }
        int givenCheck = lastChar - '0';

        int expectedCheck = computeIsbn13CheckDigit(first12);

        return givenCheck == expectedCheck;
    }

    public static int computeIsbn13CheckDigit(String first12Digits) {
        if (first12Digits == null) {
            throw new IllegalArgumentException("ISBN must not be null");
        }

        String digits = first12Digits.replaceAll("\\D", "");
        if (digits.length() != 12) {
            throw new IllegalArgumentException("ISBN-13 check digit requires exactly 12 digits");
        }

        int sum = 0;

        for (int i = 0; i < 12; i++) {
            char ch = digits.charAt(i);
            if (!Character.isDigit(ch)) {
                throw new IllegalArgumentException("ISBN can only contain digits");
            }
            int d = ch - '0';
            sum += (i % 2 == 0) ? d : d * 3;
        }

        int mod = sum % 10;
        int check = (10 - mod) % 10;

        return check;
    }
}
