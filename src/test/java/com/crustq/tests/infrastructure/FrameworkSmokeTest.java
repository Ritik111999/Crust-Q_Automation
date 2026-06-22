package com.crustq.tests.infrastructure;

import com.crustq.base.BaseTest;
import com.crustq.config.ConfigReader;
import com.crustq.reporting.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Framework smoke test — verifies driver lifecycle, config loading, and reporting wiring.
 */
public class FrameworkSmokeTest extends BaseTest {

    @Test(groups = {"Positive"},
            description = "Framework | Positive | WebDriver and reporting initialize per thread")
    public void verifyFrameworkWiring() {
        ExtentTestManager.getTest().info("Verifying thread-local WebDriver binding");
        Assert.assertNotNull(getDriver(), "WebDriver should be initialized for the current thread");
        Assert.assertNotNull(getUtils(), "WebElementUtils should be initialized for the current thread");

        String userBaseUrl = ConfigReader.get("user.base.url", "");
        if (!userBaseUrl.isBlank()) {
            ExtentTestManager.getTest().info("Navigating to user base URL: " + userBaseUrl);
            getDriver().get(userBaseUrl);
            Assert.assertNotNull(getDriver().getTitle(), "Page title should be available after navigation");
        } else {
            ExtentTestManager.getTest().info("user.base.url not configured — skipping navigation check");
        }
    }
}
