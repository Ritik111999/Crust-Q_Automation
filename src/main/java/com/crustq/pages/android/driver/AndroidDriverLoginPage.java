package com.crustq.pages.android.driver;

import com.crustq.config.ConfigReader;
import com.crustq.utils.MobileGesturesUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

/**
 * Driver login screen for the Android app.
 * Locator defaults are config-driven so they can be updated without code changes.
 */
public class AndroidDriverLoginPage extends AndroidBasePage {

    @AndroidFindBy(accessibility = "Enter email address")
    private WebElement emailField;

    @AndroidFindBy(accessibility = "Enter password")
    private WebElement passwordField;

    @AndroidFindBy(accessibility = "Sign In")
    private WebElement signInButton;

    public AndroidDriverLoginPage(AppiumDriver driver) {
        super(driver);
    }

    public AndroidDriverLoginPage(AppiumDriver driver, MobileGesturesUtil gestures) {
        super(driver, gestures);
    }

    public void waitForLoginScreen() {
        gestures.waitForVisible(byAccessibilityId(emailAccessibilityId()));
        gestures.waitForVisible(byAccessibilityId(passwordAccessibilityId()));
        gestures.waitForVisible(byAccessibilityId(signInAccessibilityId()));
    }

    public void enterEmail(String email) {
        gestures.type(byAccessibilityId(emailAccessibilityId()), email);
    }

    public void enterPassword(String password) {
        gestures.type(byAccessibilityId(passwordAccessibilityId()), password);
    }

    public void tapSignIn() {
        gestures.tap(byAccessibilityId(signInAccessibilityId()));
        gestures.hideKeyboard();
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        tapSignIn();
    }

    public void loginWithConfiguredCredentials() {
        login(
                ConfigReader.get("crustq.driver.login.valid.email"),
                ConfigReader.get("crustq.driver.login.valid.password")
        );
    }

    private String emailAccessibilityId() {
        return ConfigReader.get("mobile.android.login.email.accessibility", "Enter email address");
    }

    private String passwordAccessibilityId() {
        return ConfigReader.get("mobile.android.login.password.accessibility", "Enter password");
    }

    private String signInAccessibilityId() {
        return ConfigReader.get("mobile.android.login.signin.accessibility", "Sign In");
    }
}
