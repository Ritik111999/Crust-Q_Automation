package com.crustq.base;

import com.crustq.config.ApplicationRole;
import com.crustq.config.ConfigReader;
import com.crustq.utils.BrowserFactory;
import com.crustq.utils.WebElementUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

/**
 * Thread-safe Chrome lifecycle for parallel TestNG execution.
 * Each @Test or DataProvider row gets its own Chrome instance via ThreadLocal.
 */
public abstract class BaseTest {

    protected static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    protected static final ThreadLocal<WebElementUtils> UTILS = new ThreadLocal<>();
    protected static final ThreadLocal<String> THREAD_LABEL = new ThreadLocal<>();

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        ConfigReader.init();

        String threadLabel = "Chrome-Thread-" + Thread.currentThread().threadId();
        THREAD_LABEL.set(threadLabel);

        WebDriver driver = BrowserFactory.createChromeDriver();
        configureDriver(driver);

        DRIVER.set(driver);
        UTILS.set(new WebElementUtils(driver));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
                // Session may already be closed after a failure
            }
        }
        DRIVER.remove();
        UTILS.remove();
        THREAD_LABEL.remove();
    }

    public static WebDriver getThreadLocalDriver() {
        return DRIVER.get();
    }

    public static String getThreadLabel() {
        return THREAD_LABEL.get();
    }

    protected WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not initialized for the current thread");
        }
        return driver;
    }

    protected WebElementUtils getUtils() {
        WebElementUtils utils = UTILS.get();
        if (utils == null) {
            throw new IllegalStateException("WebElementUtils is not initialized for the current thread");
        }
        return utils;
    }

    protected void navigateToRole(ApplicationRole role) {
        getDriver().get(role.getBaseUrl());
    }

    protected void navigateToRolePath(ApplicationRole role, String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        getDriver().get(role.getBaseUrl() + normalizedPath);
    }

    private void configureDriver(WebDriver driver) {
        int implicitWait = ConfigReader.getInt("implicit.wait.seconds", 0);
        int pageLoadTimeout = ConfigReader.getInt("page.load.timeout.seconds", 60);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        if (!ConfigReader.getBoolean("chrome.headless", false)) {
            driver.manage().window().maximize();
        }
    }
}
