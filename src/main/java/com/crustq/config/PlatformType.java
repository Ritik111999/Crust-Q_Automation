package com.crustq.config;

/**
 * Supported execution platforms for the unified Crust &amp; Q automation framework.
 */
public enum PlatformType {

    PWA("pwa", "Web (PWA)"),
    ANDROID("android", "Android"),
    IOS("ios", "iOS");

    private final String configKey;
    private final String displayName;

    PlatformType(String configKey, String displayName) {
        this.configKey = configKey;
        this.displayName = displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Reads {@code platform.active} from config.properties or system property override.
     */
    public static PlatformType active() {
        ConfigReader.init();
        return fromString(ConfigReader.get("platform.active", PWA.configKey));
    }

    public static PlatformType fromString(String value) {
        if (value == null || value.isBlank()) {
            return PWA;
        }
        String normalized = value.trim().toLowerCase();
        for (PlatformType platform : values()) {
            if (platform.configKey.equals(normalized)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unsupported platform.active value: " + value
                + ". Expected: pwa, android, or ios");
    }

    public boolean isMobile() {
        return this == ANDROID || this == IOS;
    }

    public boolean isPwa() {
        return this == PWA;
    }
}
