package com.crustq.pages.ios.driver;

import com.crustq.config.ConfigReader;
import com.crustq.utils.MobileGesturesUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Driver login screen for the iOS app.
 * Locator defaults are config-driven so they can be updated without code changes.
 */
public class IosDriverLoginPage extends IosBasePage {

    @iOSXCUITFindBy(accessibility = "Enter email address")
    private WebElement emailField;

    @iOSXCUITFindBy(accessibility = "Enter password")
    private WebElement passwordField;

    @iOSXCUITFindBy(accessibility = "Sign In")
    private WebElement signInButton;

    public IosDriverLoginPage(AppiumDriver driver) {
        super(driver);
    }

    public IosDriverLoginPage(AppiumDriver driver, MobileGesturesUtil gestures) {
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
        return ConfigReader.get("mobile.ios.login.email.accessibility", "Enter email address");
    }

    private String passwordAccessibilityId() {
        return ConfigReader.get("mobile.ios.login.password.accessibility", "Enter password");
    }

    private String signInAccessibilityId() {
        return ConfigReader.get("mobile.ios.login.signin.accessibility", "Sign In");
    }
}
