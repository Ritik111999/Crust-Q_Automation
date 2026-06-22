package com.crustq.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized reader for config.properties.
 * Supports environment-variable placeholders (${VAR_NAME}) for sensitive values.
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private static Properties properties;
    private static boolean initialized;

    private ConfigReader() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        properties = new Properties();
        try (InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Unable to find " + CONFIG_FILE + " on the classpath");
            }
            properties.load(inputStream);
            resolvePlaceholders();
            initialized = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    public static String get(String key) {
        ensureInitialized();
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return resolveValue(systemValue);
        }
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Property key not found: " + key);
        }
        return resolveValue(value);
    }

    public static String get(String key, String defaultValue) {
        ensureInitialized();
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return resolveValue(systemValue);
        }
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return resolveValue(value);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Returns the active platform from {@code platform.active} (default: pwa).
     */
    public static PlatformType getActivePlatform() {
        return PlatformType.active();
    }

    /**
     * Classpath-relative capabilities JSON for the given mobile platform.
     */
    public static String getMobileCapabilitiesFile(PlatformType platform) {
        if (platform == PlatformType.ANDROID) {
            return get("mobile.android.capabilities.file", "capabilities/android-local.json");
        }
        if (platform == PlatformType.IOS) {
            return get("mobile.ios.capabilities.file", "capabilities/ios-local.json");
        }
        throw new IllegalArgumentException("Capabilities file is only defined for mobile platforms: " + platform);
    }

    public static String getAppiumServerUrl() {
        String host = get("appium.server.host", "127.0.0.1");
        int port = getInt("appium.server.port", 4723);
        String basePath = get("appium.server.base.path", "/");
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        if (basePath.length() > 1 && basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return "http://" + host + ":" + port + basePath;
    }

    /**
     * Builds a 2D Object array for TestNG @DataProvider from indexed property groups.
     * Example prefix "crustq.user.login.negative" reads keys like .username.1, .password.1, etc.
     */
    public static Object[][] getIndexedDataSet(String keyPrefix, String... fieldNames) {
        ensureInitialized();
        List<Integer> indices = discoverIndices(keyPrefix, fieldNames[0]);
        List<Object[]> rows = new ArrayList<>();

        for (Integer index : indices) {
            Object[] row = new Object[fieldNames.length];
            for (int i = 0; i < fieldNames.length; i++) {
                row[i] = get(keyPrefix + "." + fieldNames[i] + "." + index);
            }
            rows.add(row);
        }

        return rows.toArray(new Object[0][]);
    }

    private static List<Integer> discoverIndices(String keyPrefix, String firstFieldName) {
        List<Integer> indices = new ArrayList<>();
        String searchPrefix = keyPrefix + "." + firstFieldName + ".";

        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(searchPrefix)) {
                String indexPart = key.substring(searchPrefix.length());
                try {
                    indices.add(Integer.parseInt(indexPart));
                } catch (NumberFormatException ignored) {
                    // skip non-numeric suffixes
                }
            }
        }

        Collections.sort(indices);
        return indices;
    }

    private static void resolvePlaceholders() {
        for (String key : properties.stringPropertyNames()) {
            properties.setProperty(key, resolveValue(properties.getProperty(key)));
        }
    }

    private static String resolveValue(String value) {
        if (value == null) {
            return null;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        StringBuilder resolved = new StringBuilder();

        while (matcher.find()) {
            String envVar = matcher.group(1);
            String replacement = System.getenv(envVar);
            if (replacement == null) {
                replacement = System.getProperty(envVar);
            }
            if (replacement == null) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }

    private static void ensureInitialized() {
        if (!initialized) {
            init();
        }
    }
}
