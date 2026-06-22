package com.crustq.base;

import com.crustq.config.ApplicationRole;
import com.crustq.config.ConfigReader;
import com.crustq.config.PlatformType;
import com.crustq.utils.MobileGesturesUtil;
import com.crustq.utils.WebActionsUtil;
import com.crustq.utils.WebElementUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Single-platform test base.
 * <ul>
 *   <li>{@code platform.active=pwa} → Selenium WebDriver (Chrome)</li>
 *   <li>{@code platform.active=android|ios} → AppiumDriver</li>
 * </ul>
 * Each {@code @Test} gets its own isolated driver via {@link DriverContext}.
 */
public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        ConfigReader.init();
        PlatformType platform = ConfigReader.getActivePlatform();

        DriverContext.bindThreadLabel(buildThreadLabel(platform));
        DriverContext.startSinglePlatformSession(platform);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverContext.stopSession();
    }

    public static WebDriver getThreadLocalDriver() {
        return DriverContext.getWebDriverOrNull();
    }

    public static String getThreadLabel() {
        return DriverContext.getThreadLabel();
    }

    /**
     * Returns WebDriver for PWA tests. Use {@link #getAppiumDriver()} when {@code platform.active} is mobile.
     */
    protected WebDriver getDriver() {
        return DriverContext.getWebDriver();
    }

    protected AppiumDriver getAppiumDriver() {
        return DriverContext.getAppiumDriver();
    }

    protected WebElementUtils getUtils() {
        return DriverContext.getWebUtils();
    }

    protected WebActionsUtil getWebActions() {
        return new WebActionsUtil(getDriver());
    }

    protected MobileGesturesUtil getMobileGestures() {
        return DriverContext.getMobileGestures();
    }

    protected PlatformType getActivePlatform() {
        return DriverContext.getActivePlatform();
    }

    protected void navigateToRole(ApplicationRole role) {
        getDriver().get(role.getBaseUrl());
    }

    protected void navigateToRolePath(ApplicationRole role, String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        getDriver().get(role.getBaseUrl() + normalizedPath);
    }

    private String buildThreadLabel(PlatformType platform) {
        return platform.name() + "-Thread-" + Thread.currentThread().threadId();
    }
}
