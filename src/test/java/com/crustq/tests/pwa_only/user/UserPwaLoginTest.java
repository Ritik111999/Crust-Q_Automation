package com.crustq.tests.pwa_only.user;

import com.crustq.base.BaseTest;
import com.crustq.config.ConfigReader;
import com.crustq.pages.pwa.PwaLoginPage;
import com.crustq.reporting.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * User (Customer) PWA login — positive scenarios on /auth.
 */
public class UserPwaLoginTest extends BaseTest {

    @Test(groups = {"Positive"},
            description = "User | Positive | Valid credentials redirect to dashboard")
    public void testPositiveUserLogin_validCredentials_redirectsToDashboard() {
        PwaLoginPage loginPage = new PwaLoginPage(getDriver());

        ExtentTestManager.getTest().info("Opening PWA auth page for User role");
        loginPage.open();

        Assert.assertTrue(loginPage.isSignInHeadingDisplayed(),
                "Sign In heading should be visible before login");

        String email = ConfigReader.get("crustq.user.login.valid.email");
        String password = ConfigReader.get("crustq.user.login.valid.password");
        ExtentTestManager.getTest().info("Signing in with user: " + email);
        loginPage.login(email, password);

        String expectedPath = ConfigReader.get("crustq.user.login.expected.success.path");
        ExtentTestManager.getTest().info("Waiting for redirect to: " + expectedPath);
        loginPage.waitForSuccessfulLogin(expectedPath);

        String currentUrl = getDriver().getCurrentUrl();
        ExtentTestManager.getTest().info("Landed on URL: " + currentUrl);
        Assert.assertTrue(currentUrl.contains(expectedPath),
                "User should be redirected to dashboard after successful login");
    }

    @Test(groups = {"Positive"},
            description = "User | Positive | Remember Me checkbox can be selected")
    public void testPositiveUserLogin_rememberMeCanBeSelected() {
        PwaLoginPage loginPage = new PwaLoginPage(getDriver());

        ExtentTestManager.getTest().info("Opening PWA auth page");
        loginPage.open();

        Assert.assertFalse(loginPage.isRememberMeChecked(),
                "Remember Me should be unchecked by default");

        ExtentTestManager.getTest().info("Selecting Remember Me");
        loginPage.clickRememberMe();

        Assert.assertTrue(loginPage.isRememberMeChecked(),
                "Remember Me should be checked after selection");
        Assert.assertTrue(loginPage.isOnLoginPage(),
                "User should remain on auth page after toggling Remember Me");
    }
}
