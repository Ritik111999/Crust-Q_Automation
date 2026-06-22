package com.crustq.base;

import com.crustq.config.ConfigReader;
import com.crustq.config.PlatformType;
import com.crustq.factory.AppiumDriverFactory;
import com.crustq.factory.AppiumServerManager;
import com.crustq.factory.WebDriverFactory;
import com.crustq.utils.MobileGesturesUtil;
import com.crustq.utils.WebElementUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

/**
 * Thread-local holder for WebDriver and AppiumDriver instances.
 * Shared by {@link BaseTest} and {@link MultiDriverBaseTest}.
 */
public final class DriverContext {

    private static final ThreadLocal<WebDriver> WEB_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<AppiumDriver> APPIUM_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebElementUtils> WEB_UTILS = new ThreadLocal<>();
    private static final ThreadLocal<MobileGesturesUtil> MOBILE_GESTURES = new ThreadLocal<>();
    private static final ThreadLocal<String> THREAD_LABEL = new ThreadLocal<>();
    private static final ThreadLocal<PlatformType> ACTIVE_PLATFORM = new ThreadLocal<>();

    private DriverContext() {
    }

    public static void bindThreadLabel(String label) {
        THREAD_LABEL.set(label);
    }

    public static String getThreadLabel() {
        return THREAD_LABEL.get();
    }

    public static PlatformType getActivePlatform() {
        PlatformType platform = ACTIVE_PLATFORM.get();
        return platform != null ? platform : ConfigReader.getActivePlatform();
    }

    public static void startSinglePlatformSession(PlatformType platform) {
        clearAll();
        ACTIVE_PLATFORM.set(platform);

        if (platform.isPwa()) {
            WebDriver webDriver = WebDriverFactory.createDriver();
            configureWebDriver(webDriver);
            WEB_DRIVER.set(webDriver);
            WEB_UTILS.set(new WebElementUtils(webDriver));
            return;
        }

        AppiumDriver mobileDriver = AppiumDriverFactory.createDriver(platform);
        APPIUM_DRIVER.set(mobileDriver);
        MOBILE_GESTURES.set(new MobileGesturesUtil(mobileDriver));
    }

    public static void startCrossFunctionalSession(PlatformType mobilePlatform) {
        clearAll();
        ACTIVE_PLATFORM.set(mobilePlatform);

        WebDriver webDriver = WebDriverFactory.createDriver();
        configureWebDriver(webDriver);
        WEB_DRIVER.set(webDriver);
        WEB_UTILS.set(new WebElementUtils(webDriver));

        AppiumDriver mobileDriver = AppiumDriverFactory.createDriver(mobilePlatform);
        APPIUM_DRIVER.set(mobileDriver);
        MOBILE_GESTURES.set(new MobileGesturesUtil(mobileDriver));
    }

    public static void stopSession() {
        quitWebDriver();
        quitAppiumDriver();
        AppiumServerManager.stopIfManaged();
        clearAll();
    }

    public static WebDriver getWebDriver() {
        WebDriver driver = WEB_DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not initialized for the current thread");
        }
        return driver;
    }

    public static WebDriver getWebDriverOrNull() {
        return WEB_DRIVER.get();
    }

    public static AppiumDriver getAppiumDriver() {
        AppiumDriver driver = APPIUM_DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("AppiumDriver is not initialized for the current thread");
        }
        return driver;
    }

    public static AppiumDriver getAppiumDriverOrNull() {
        return APPIUM_DRIVER.get();
    }

    public static WebElementUtils getWebUtils() {
        WebElementUtils utils = WEB_UTILS.get();
        if (utils == null) {
            throw new IllegalStateException("WebElementUtils is not initialized for the current thread");
        }
        return utils;
    }

    public static WebElementUtils getWebUtilsOrNull() {
        return WEB_UTILS.get();
    }

    public static MobileGesturesUtil getMobileGestures() {
        MobileGesturesUtil gestures = MOBILE_GESTURES.get();
        if (gestures == null) {
            throw new IllegalStateException("MobileGesturesUtil is not initialized for the current thread");
        }
        return gestures;
    }

    public static MobileGesturesUtil getMobileGesturesOrNull() {
        return MOBILE_GESTURES.get();
    }

    private static void configureWebDriver(WebDriver driver) {
        int implicitWait = ConfigReader.getInt("implicit.wait.seconds", 0);
        int pageLoadTimeout = ConfigReader.getInt("page.load.timeout.seconds", 60);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        if (!ConfigReader.getBoolean("chrome.headless", false)) {
            driver.manage().window().maximize();
        }
    }

    private static void quitWebDriver() {
        WebDriver driver = WEB_DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // Session may already be closed after a failure
            }
        }
    }

    private static void quitAppiumDriver() {
        AppiumDriver driver = APPIUM_DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // Session may already be closed after a failure
            }
        }
    }

    private static void clearAll() {
        WEB_DRIVER.remove();
        APPIUM_DRIVER.remove();
        WEB_UTILS.remove();
        MOBILE_GESTURES.remove();
        THREAD_LABEL.remove();
        ACTIVE_PLATFORM.remove();
    }
}
