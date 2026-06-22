package com.crustq.tests.infrastructure;

import com.crustq.base.MultiDriverBaseTest;
import com.crustq.reporting.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Dual-driver lifecycle wiring test for the framework.
 * Enable once Appium server and mobile app identifiers are configured.
 */
public class MultiDriverBaseWiringTest extends MultiDriverBaseTest {

    @Test(enabled = false,
            description = "Infrastructure | Wiring | Web + Appium drivers start in the same test")
    public void verifyDualDriversStart() {
        ExtentTestManager.getTest().info("Mobile platform: " + getMobilePlatform());
        Assert.assertNotNull(getWebDriver(), "WebDriver should be initialized");
        Assert.assertNotNull(getAppiumDriver(), "AppiumDriver should be initialized");
    }
}
