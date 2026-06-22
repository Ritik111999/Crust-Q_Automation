package com.crustq.factory;

import com.crustq.config.ConfigReader;
import com.crustq.config.PlatformType;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.json.Json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads Appium capabilities JSON from the test classpath and resolves ${config.key} placeholders.
 */
public final class CapabilitiesLoader {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)}");
    private static final Json JSON = new Json();

    private CapabilitiesLoader() {
    }

    @SuppressWarnings("unchecked")
    public static MutableCapabilities loadMobileCapabilities(PlatformType platform) {
        if (!platform.isMobile()) {
            throw new IllegalArgumentException("Capabilities loading applies to mobile platforms only: " + platform);
        }

        ConfigReader.init();
        String resourcePath = ConfigReader.getMobileCapabilitiesFile(platform);
        String json = readClasspathResource(resourcePath);
        String resolvedJson = resolveConfigPlaceholders(json);

        Map<String, Object> raw = JSON.toType(resolvedJson, Map.class);
        MutableCapabilities capabilities = new MutableCapabilities();
        raw.forEach(capabilities::setCapability);
        return capabilities;
    }

    private static String readClasspathResource(String resourcePath) {
        try (InputStream input = CapabilitiesLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("Capabilities file not found on classpath: " + resourcePath);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read capabilities file: " + resourcePath, e);
        }
    }

    private static String resolveConfigPlaceholders(String json) {
        Matcher matcher = PLACEHOLDER.matcher(json);
        StringBuilder resolved = new StringBuilder();

        while (matcher.find()) {
            String configKey = matcher.group(1);
            String replacement = ConfigReader.get(configKey, "");
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }
}
