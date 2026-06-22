package com.crustq.factory;

import com.crustq.config.ConfigReader;
import com.crustq.config.PlatformType;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.MutableCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Creates isolated AppiumDriver instances for Android and iOS execution.
 */
public final class AppiumDriverFactory {

    private AppiumDriverFactory() {
    }

    public static AppiumDriver createDriver() {
        return createDriver(ConfigReader.getActivePlatform());
    }

    public static AppiumDriver createDriver(PlatformType platform) {
        ConfigReader.init();
        if (!platform.isMobile()) {
            throw new IllegalArgumentException("AppiumDriverFactory supports mobile platforms only: " + platform);
        }

        AppiumServerManager.startIfRequired();
        MutableCapabilities capabilities = CapabilitiesLoader.loadMobileCapabilities(platform);
        URL serverUrl = resolveServerUrl();

        AppiumDriver driver = switch (platform) {
            case ANDROID -> new AndroidDriver(serverUrl, capabilities);
            case IOS -> new IOSDriver(serverUrl, capabilities);
            default -> throw new IllegalArgumentException("Unsupported mobile platform: " + platform);
        };

        applyMobileTimeouts(driver);
        return driver;
    }

    private static URL resolveServerUrl() {
        try {
            return new URL(AppiumServerManager.getServerUrl());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid Appium server URL: " + AppiumServerManager.getServerUrl(), e);
        }
    }

    private static void applyMobileTimeouts(AppiumDriver driver) {
        int implicitWait = ConfigReader.getInt("implicit.wait.seconds", 0);
        int commandTimeout = ConfigReader.getInt("mobile.command.timeout.seconds", 120);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(commandTimeout));
    }
}
