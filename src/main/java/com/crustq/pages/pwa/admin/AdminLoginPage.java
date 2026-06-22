package com.crustq.pages.pwa.admin;

import com.crustq.config.ApplicationRole;
import com.crustq.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Admin dispatcher login page (standard web UI at admin.base.url).
 */
public class AdminLoginPage extends AdminBasePage {

    private static final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[name='email'], #email");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[name='password'], #password");
    private static final By SIGN_IN_BUTTON = By.xpath(
            "//button[contains(normalize-space(), 'Sign In') or contains(normalize-space(), 'Login') or contains(normalize-space(), 'Log In')]");

    public AdminLoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        openAdminPath(ConfigReader.get("crustq.admin.login.path"));
        waitForLoginPageReady();
    }

    public void waitForLoginPageReady() {
        utils.waitForVisibility(EMAIL_INPUT);
        utils.waitForVisibility(PASSWORD_INPUT);
        utils.waitForVisibility(SIGN_IN_BUTTON);
    }

    public void enterEmail(String email) {
        utils.sendKeys(EMAIL_INPUT, email);
    }

    public void enterPassword(String password) {
        utils.sendKeys(PASSWORD_INPUT, password);
    }

    public void clickSignIn() {
        utils.click(SIGN_IN_BUTTON);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    public void loginWithConfiguredCredentials() {
        login(
                ConfigReader.get("crustq.admin.login.valid.email"),
                ConfigReader.get("crustq.admin.login.valid.password")
        );
    }

    public void waitForSuccessfulLogin() {
        utils.waitForUrlContains(ConfigReader.get("crustq.admin.login.expected.success.path"));
    }

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains(ConfigReader.get("crustq.admin.login.path"));
    }

    public ApplicationRole role() {
        return ROLE;
    }
}
