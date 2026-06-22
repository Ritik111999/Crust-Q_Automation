package com.crustq.tests.pwa_only.driver;

import com.crustq.base.BaseTest;
import com.crustq.config.ConfigReader;
import com.crustq.pages.pwa.PwaLoginPage;
import com.crustq.reporting.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Driver (Delivery Agent) PWA login — positive scenarios on /auth.
 */
public class DriverPwaLoginTest extends BaseTest {

    @Test(groups = {"Positive"},
            description = "Driver | Positive | Valid credentials redirect to driver home")
    public void testPositiveDriverLogin_validCredentials_redirectsToDriverHome() {
        PwaLoginPage loginPage = new PwaLoginPage(getDriver());

        ExtentTestManager.getTest().info("Opening PWA auth page for Driver role");
        loginPage.open();

        Assert.assertTrue(loginPage.isSignInHeadingDisplayed(),
                "Sign In heading should be visible before login");

        String email = ConfigReader.get("crustq.driver.login.valid.email");
        String password = ConfigReader.get("crustq.driver.login.valid.password");
        ExtentTestManager.getTest().info("Signing in with driver: " + email);
        loginPage.login(email, password);

        String expectedPath = ConfigReader.get("crustq.driver.login.expected.success.path");
        ExtentTestManager.getTest().info("Waiting for redirect to: " + expectedPath);
        loginPage.waitForSuccessfulLogin(expectedPath);

        String currentUrl = getDriver().getCurrentUrl();
        ExtentTestManager.getTest().info("Landed on URL: " + currentUrl);
        Assert.assertTrue(currentUrl.contains(expectedPath),
                "Driver should be redirected to driver home after successful login");
    }
}
