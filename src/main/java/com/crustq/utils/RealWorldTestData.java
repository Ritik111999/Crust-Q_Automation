package com.crustq.utils;

import com.crustq.config.ConfigReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central catalog of real-world USA test data for all automation inputs.
 * <p>
 * Data lives in {@code real-world-test-data.properties}. Use indexed datasets
 * instead of inventing fake street names, cards, or personas in tests.
 */
public final class RealWorldTestData {

    private static final String DATA_FILE = "real-world-test-data.properties";

    private static Properties data;
    private static boolean initialized;
    private static final AtomicInteger ADDRESS_ROTATION = new AtomicInteger(0);

    private RealWorldTestData() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        ConfigReader.init();

        data = new Properties();
        try (InputStream inputStream = RealWorldTestData.class.getClassLoader().getResourceAsStream(DATA_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Unable to find " + DATA_FILE + " on the classpath");
            }
            data.load(inputStream);
            initialized = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + DATA_FILE, e);
        }
    }

    public record ProfileUser(
            int id,
            String label,
            String firstName,
            String lastName,
            String email,
            String password,
            String phone,
            String sidebarTapText,
            String loyaltyTier
    ) {
    }

    public record UsAddress(
            int id,
            String label,
            String street,
            String suite,
            String city,
            String state,
            String stateCode,
            String zip,
            String deliveryInstructions
    ) {
        public String streetWithUniqueUnit() {
            return street + " Unit " + TestDataGenerator.randomDigits(2);
        }
    }

    public record PaymentCard(
            int id,
            String label,
            String number,
            String expiry,
            String cvv,
            String zip,
            String last4,
            String brand
    ) {
    }

    public record LoyaltyTier(int id, String name, int pointsPerDollar) {
    }

    public static ProfileUser defaultProfileUser() {
        return profileUser(defaultId("testdata.default.profile.user.id", 1));
    }

    public static ProfileUser profileUser(int id) {
        ensureInitialized();
        return new ProfileUser(
                id,
                required("testdata.profile.user.label", id),
                required("testdata.profile.user.first.name", id),
                required("testdata.profile.user.last.name", id),
                required("testdata.profile.user.email", id),
                required("testdata.profile.user.password", id),
                required("testdata.profile.user.phone", id),
                required("testdata.profile.user.sidebar.tap", id),
                required("testdata.profile.user.loyalty.tier", id)
        );
    }

    public static UsAddress defaultAddress() {
        return address(defaultId("testdata.default.address.id", 1));
    }

    public static UsAddress address(int id) {
        ensureInitialized();
        return new UsAddress(
                id,
                required("testdata.address.label", id),
                required("testdata.address.street", id),
                optional("testdata.address.suite", id, ""),
                required("testdata.address.city", id),
                required("testdata.address.state", id),
                optional("testdata.address.state.code", id, ""),
                required("testdata.address.zip", id),
                optional("testdata.address.instructions", id, "")
        );
    }

    /**
     * Rotates through all configured addresses to reduce duplicate-save collisions.
     */
    public static UsAddress nextAddress() {
        List<Integer> ids = addressIds();
        if (ids.isEmpty()) {
            return defaultAddress();
        }
        int index = Math.floorMod(ADDRESS_ROTATION.getAndIncrement(), ids.size());
        return address(ids.get(index));
    }

    public static List<Integer> addressIds() {
        return discoverIndices("testdata.address", "label");
    }

    public static PaymentCard defaultPaymentCard() {
        return paymentCard(defaultId("testdata.default.payment.card.id", 1));
    }

    public static PaymentCard paymentCard(int id) {
        ensureInitialized();
        return new PaymentCard(
                id,
                required("testdata.payment.card.label", id),
                required("testdata.payment.card.number", id),
                required("testdata.payment.card.expiry", id),
                required("testdata.payment.card.cvv", id),
                required("testdata.payment.card.zip", id),
                required("testdata.payment.card.last4", id),
                required("testdata.payment.card.brand", id)
        );
    }

    public static List<Integer> paymentCardIds() {
        return discoverIndices("testdata.payment.card", "label");
    }

    public static String invalidPaymentZip() {
        return get("testdata.payment.card.invalid.zip.1", "12");
    }

    public static String invalidPaymentNumber() {
        return get("testdata.payment.card.invalid.number.1", "4000000000000002");
    }

    public static String invalidPaymentExpiry() {
        return get("testdata.payment.card.invalid.expiry.1", "01/20");
    }

    public static String invalidPaymentCvv() {
        return get("testdata.payment.card.invalid.cvv.1", "99");
    }

    public static String invalidPhone() {
        return get("testdata.profile.user.invalid.phone.1", "123");
    }

    public static String phoneValidationHint() {
        return get("testdata.profile.user.phone.validation.hint.1", "phone");
    }

    public static String invalidAddressZip() {
        return get("testdata.address.invalid.zip.1", "ABCDE");
    }

    public static String addressValidationHint() {
        return get("testdata.address.validation.hint.1", "zip");
    }

    public static LoyaltyTier loyaltyTier(int id) {
        ensureInitialized();
        return new LoyaltyTier(
                id,
                required("testdata.loyalty.tier.name", id),
                Integer.parseInt(required("testdata.loyalty.tier.points.per.dollar", id))
        );
    }

    public static List<LoyaltyTier> allLoyaltyTiers() {
        List<LoyaltyTier> tiers = new ArrayList<>();
        for (Integer id : discoverIndices("testdata.loyalty.tier", "name")) {
            tiers.add(loyaltyTier(id));
        }
        return tiers;
    }

    private static int defaultId(String key, int fallback) {
        ensureInitialized();
        String configValue = ConfigReader.get(key, String.valueOf(fallback));
        return Integer.parseInt(configValue);
    }

    private static String required(String prefix, int id) {
        String key = prefix + "." + id;
        String value = data.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing real-world test data key: " + key);
        }
        return value.trim();
    }

    private static String optional(String prefix, int id, String defaultValue) {
        String key = prefix + "." + id;
        String value = data.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String get(String key, String defaultValue) {
        ensureInitialized();
        String value = data.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static List<Integer> discoverIndices(String keyPrefix, String fieldName) {
        List<Integer> indices = new ArrayList<>();
        String searchPrefix = keyPrefix + "." + fieldName + ".";

        for (String key : data.stringPropertyNames()) {
            if (key.startsWith(searchPrefix)) {
                String indexPart = key.substring(searchPrefix.length());
                try {
                    indices.add(Integer.parseInt(indexPart));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        Collections.sort(indices);
        return indices;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }
}
