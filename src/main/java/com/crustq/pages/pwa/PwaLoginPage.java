package com.crustq.pages.pwa;

import com.crustq.config.ApplicationRole;
import com.crustq.config.ConfigReader;
import com.crustq.utils.FlutterSemanticsHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Shared Flutter PWA auth page for User and Driver roles (/auth).
 */
public class PwaLoginPage extends PwaBasePage {

    private static final By EMAIL_INPUT = By.cssSelector("input[aria-label='Enter email address']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[aria-label='Enter password']");
    private static final By SIGN_IN_HEADING = By.xpath("//*[contains(normalize-space(), 'Sign In')]");

    public PwaLoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        openPwaPath(ApplicationRole.USER, ConfigReader.get("crustq.user.login.path"));
        waitForLoginPageReady();
    }

    public void openForRole(ApplicationRole role, String loginPath) {
        openPwaPath(role, loginPath);
        waitForLoginPageReady();
    }

    public void waitForLoginPageReady() {
        prepareFlutterSemantics();
        utils.waitForVisibility(SIGN_IN_HEADING);
        utils.waitForVisibility(EMAIL_INPUT);
        utils.waitForVisibility(PASSWORD_INPUT);
    }

    public boolean isSignInHeadingDisplayed() {
        return FlutterSemanticsHelper.isSignInHeadingVisible(utils);
    }

    public void enterEmail(String email) {
        utils.sendKeys(EMAIL_INPUT, email);
    }

    public void enterPassword(String password) {
        utils.sendKeys(PASSWORD_INPUT, password);
    }

    public void clickRememberMe() {
        FlutterSemanticsHelper.clickRememberMe(utils);
    }

    public boolean isRememberMeChecked() {
        return FlutterSemanticsHelper.isRememberMeChecked(utils);
    }

    public void clickSignIn() {
        FlutterSemanticsHelper.clickSemanticButton(utils, "Sign In");
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignIn();
    }

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains(ConfigReader.get("crustq.user.login.path"));
    }

    public void waitForSuccessfulLogin(String expectedPathFragment) {
        utils.waitForUrlContains(expectedPathFragment);
    }

    public boolean isValidationHintDisplayed(String expectedHint) {
        return utils.isDisplayed(By.xpath("//flt-semantics[contains(., '" + expectedHint + "')]"));
    }

    public void waitForValidationHint(String expectedHint) {
        utils.waitForVisibility(By.xpath("//flt-semantics[contains(., '" + expectedHint + "')]"));
    }
}
