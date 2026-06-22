package com.crustq.tests.pwa_only.auth;

import com.crustq.base.BaseTest;
import com.crustq.config.ConfigReader;
import com.crustq.pages.pwa.PwaLoginPage;
import com.crustq.reporting.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Shared PWA auth negative scenarios (User and Driver use the same /auth page).
 */
public class PwaLoginNegativeTest extends BaseTest {

    private static final String ASSERT_STAY_ON_AUTH = "STAY_ON_AUTH";
    private static final String ASSERT_VALIDATION_HINT = "VALIDATION_HINT";

    @DataProvider(name = "pwaLoginNegativeData", parallel = false)
    public Object[][] pwaLoginNegativeData() {
        return ConfigReader.getIndexedDataSet(
                "crustq.pwa.login.negative",
                "scenario",
                "email",
                "password",
                "assertion.type",
                "expected"
        );
    }

    @Test(groups = {"Negative"},
            dataProvider = "pwaLoginNegativeData",
            description = "PWA Auth | Negative | Scenario-driven credential validation")
    public void testNegativePwaLogin_scenarios(String scenario, String email, String password,
                                               String assertionType, String expected) {
        PwaLoginPage loginPage = new PwaLoginPage(getDriver());

        ExtentTestManager.getTest().info("Scenario: " + scenario);
        loginPage.open();

        Assert.assertTrue(loginPage.isSignInHeadingDisplayed(),
                "Sign In heading should be visible on auth page");

        if (email != null && !email.isBlank()) {
            ExtentTestManager.getTest().info("Entering email: " + email);
            loginPage.enterEmail(email);
        } else {
            ExtentTestManager.getTest().info("Leaving email field blank");
        }

        if (password != null && !password.isBlank()) {
            ExtentTestManager.getTest().info("Entering password");
            loginPage.enterPassword(password);
        } else {
            ExtentTestManager.getTest().info("Leaving password field blank");
        }

        if (ASSERT_VALIDATION_HINT.equals(assertionType)) {
            ExtentTestManager.getTest().info("Submitting Sign In to surface validation hint");
            loginPage.clickSignIn();
            ExtentTestManager.getTest().info("Validating inline hint: " + expected);
            loginPage.waitForValidationHint(expected);
            Assert.assertTrue(loginPage.isValidationHintDisplayed(expected),
                    "Validation hint should be displayed for scenario: " + scenario);
            Assert.assertTrue(loginPage.isOnLoginPage(),
                    "User should remain on auth page for scenario: " + scenario);
            return;
        }

        if (ASSERT_STAY_ON_AUTH.equals(assertionType)) {
            ExtentTestManager.getTest().info("Submitting Sign In");
            loginPage.clickSignIn();

            Assert.assertTrue(loginPage.isOnLoginPage(),
                    "Auth page URL should remain for scenario: " + scenario);
            Assert.assertFalse(getDriver().getCurrentUrl().contains("/dashboard"),
                    "User should not reach dashboard for scenario: " + scenario);
            Assert.assertFalse(getDriver().getCurrentUrl().contains("/driver-home"),
                    "Driver home should not load for scenario: " + scenario);
            return;
        }

        Assert.fail("Unknown assertion type in config: " + assertionType);
    }
}
