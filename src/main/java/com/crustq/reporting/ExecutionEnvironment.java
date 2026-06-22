package com.crustq.reporting;

import com.crustq.config.ConfigReader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Execution environment metadata for HTML and PDF dashboards.
 */
public final class ExecutionEnvironment {

    private final String automationEngine;
    private final String targetDeviceName;
    private final String osVersion;
    private final String appVersion;
    private final String appPackage;
    private final String platformName;
    private final String hostOs;
    private final String javaVersion;

    private ExecutionEnvironment(
            String automationEngine,
            String targetDeviceName,
            String osVersion,
            String appVersion,
            String appPackage,
            String platformName,
            String hostOs,
            String javaVersion) {
        this.automationEngine = automationEngine;
        this.targetDeviceName = targetDeviceName;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.appPackage = appPackage;
        this.platformName = platformName;
        this.hostOs = hostOs;
        this.javaVersion = javaVersion;
    }

    public static ExecutionEnvironment fromConfig() {
        ConfigReader.init();
        return new ExecutionEnvironment(
                resolve("report.env.automation.engine", "Selenium WebDriver"),
                resolve("report.env.device.name", "Google Chrome"),
                resolve("report.env.os.version", "Desktop"),
                resolve("report.env.app.version", "Latest"),
                resolve("report.env.app.package", "Crust & Q PWA"),
                resolve("report.env.platform.name", "Web (PWA)"),
                System.getProperty("os.name", "Unknown"),
                System.getProperty("java.version", "Unknown")
        );
    }

    private static String resolve(String configKey, String defaultValue) {
        String systemOverride = System.getProperty(configKey);
        if (systemOverride != null && !systemOverride.isBlank()) {
            return systemOverride;
        }
        return ConfigReader.get(configKey, defaultValue);
    }

    public Map<String, String> asSystemInfoMap() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Automation Engine", automationEngine);
        info.put("Platform", platformName);
        info.put("Browser", targetDeviceName);
        info.put("Viewport", osVersion);
        info.put("PWA Version", appVersion);
        info.put("Application", appPackage);
        info.put("Host OS", hostOs);
        info.put("Java Version", javaVersion);
        info.put("User URL", ConfigReader.get("user.base.url", "N/A"));
        info.put("Admin URL", ConfigReader.get("admin.base.url", "N/A"));
        info.put("Driver URL", ConfigReader.get("driver.base.url", "N/A"));
        info.put("Execution Mode", "Parallel (methods + DataProvider)");
        return info;
    }

    public String getAutomationEngine() {
        return automationEngine;
    }

    public String getTargetDeviceName() {
        return targetDeviceName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPlatformName() {
        return platformName;
    }
}
