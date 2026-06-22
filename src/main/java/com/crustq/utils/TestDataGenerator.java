package com.crustq.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared test data builders for cross-platform E2E flows.
 */
public final class TestDataGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final DateTimeFormatter ORDER_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US);

    private TestDataGenerator() {
    }

    public static String uniqueOrderId() {
        return "ORD-" + ORDER_TIMESTAMP.format(LocalDateTime.now()) + "-" + randomDigits(4);
    }

    public static String uniqueReference(String prefix) {
        String safePrefix = prefix == null || prefix.isBlank() ? "REF" : prefix.trim().toUpperCase(Locale.US);
        return safePrefix + "-" + SEQUENCE.incrementAndGet() + "-" + randomDigits(3);
    }

    public static String uniqueEmail(String localPart) {
        String safeLocal = localPart == null || localPart.isBlank() ? "qa.user" : localPart.trim();
        return safeLocal + "+" + SEQUENCE.incrementAndGet() + "-" + System.currentTimeMillis() + "@knoxweb.us";
    }

    public static String randomDigits(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    public static String randomAlphanumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive");
        }
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    public static String currentTimestampLabel() {
        return ORDER_TIMESTAMP.format(LocalDateTime.now());
    }
}
