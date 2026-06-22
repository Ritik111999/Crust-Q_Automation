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
 * Cross-platform E2E base for flows that need PWA (Selenium) and Mobile (Appium) in the same {@code @Test}.
 * <p>
 * Example: Admin approves an order on PWA while a Driver accepts it on Android.
 */
public abstract class MultiDriverBaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUpMultiDriver() {
        ConfigReader.init();
        PlatformType mobilePlatform = resolveCrossFunctionalMobilePlatform();

        DriverContext.bindThreadLabel("CrossFunctional-Thread-" + Thread.currentThread().threadId());
        DriverContext.startCrossFunctionalSession(mobilePlatform);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownMultiDriver() {
        DriverContext.stopSession();
    }

    public static WebDriver getThreadLocalWebDriver() {
        return DriverContext.getWebDriverOrNull();
    }

    public static AppiumDriver getThreadLocalAppiumDriver() {
        return DriverContext.getAppiumDriverOrNull();
    }

    public static String getThreadLabel() {
        return DriverContext.getThreadLabel();
    }

    protected WebDriver getWebDriver() {
        return DriverContext.getWebDriver();
    }

    protected AppiumDriver getAppiumDriver() {
        return DriverContext.getAppiumDriver();
    }

    protected WebElementUtils getWebUtils() {
        return DriverContext.getWebUtils();
    }

    protected WebActionsUtil getWebActions() {
        return new WebActionsUtil(getWebDriver());
    }

    protected MobileGesturesUtil getMobileGestures() {
        return DriverContext.getMobileGestures();
    }

    protected PlatformType getMobilePlatform() {
        return resolveCrossFunctionalMobilePlatform();
    }

    protected void navigateWebToRole(ApplicationRole role) {
        getWebDriver().get(role.getBaseUrl());
    }

    protected void navigateWebToRolePath(ApplicationRole role, String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        getWebDriver().get(role.getBaseUrl() + normalizedPath);
    }

    private PlatformType resolveCrossFunctionalMobilePlatform() {
        String configured = ConfigReader.get("cross.functional.mobile.platform", PlatformType.ANDROID.getConfigKey());
        return PlatformType.fromString(configured);
    }
}
