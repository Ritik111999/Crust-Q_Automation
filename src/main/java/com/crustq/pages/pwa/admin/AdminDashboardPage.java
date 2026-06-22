package com.crustq.pages.pwa.admin;

import com.crustq.config.ConfigReader;
import com.crustq.utils.WebActionsUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Admin portal dashboard Page Object (Selenium only — no Appium dependencies).
 * <p>
 * Used as a fast, headless prerequisite step to grant driver permissions before
 * mobile cross-functional tests execute.
 */
public class AdminDashboardPage extends AdminBasePage {

    @FindBy(css = "input[type='email'], input[name='email'], #email")
    private WebElement emailInput;

    @FindBy(css = "input[type='password'], input[name='password'], #password")
    private WebElement passwordInput;

    @FindBy(xpath = "//button[contains(normalize-space(), 'Sign In') or contains(normalize-space(), 'Login') or contains(normalize-space(), 'Log In')]")
    private WebElement signInButton;

    @FindBy(xpath = "//a[contains(@href, 'driver') or contains(normalize-space(), 'Drivers')]")
    private WebElement driversNavLink;

    @FindBy(xpath = "//input[@type='search' or contains(@placeholder, 'Search') or contains(@aria-label, 'Search')]")
    private WebElement driverSearchInput;

    @FindBy(xpath = "//button[normalize-space()='Save' or contains(normalize-space(), 'Save Changes') or contains(normalize-space(), 'Update')]")
    private WebElement saveButton;

    @FindBy(xpath = "//*[contains(@class, 'toast') or contains(@class, 'Toast') or @role='alert' or contains(@class, 'snackbar') or contains(@class, 'Snackbar')]")
    private WebElement successToast;

    private final WebActionsUtil actions;

    public AdminDashboardPage(WebDriver driver) {
        super(driver);
        this.actions = new WebActionsUtil(driver);
        PageFactory.initElements(driver, this);
    }

    public void waitForDashboardReady() {
        actions.waitForUrlContains(ConfigReader.get("crustq.admin.login.expected.success.path"));
    }

    public void loginIfRequired(String email, String password) {
        if (!driver.getCurrentUrl().contains(ConfigReader.get("crustq.admin.login.path"))) {
            waitForDashboardReady();
            return;
        }
        actions.waitForElementToBeClickable(emailInput);
        actions.type(By.cssSelector("input[type='email'], input[name='email'], #email"), email);
        actions.type(By.cssSelector("input[type='password'], input[name='password'], #password"), password);
        actions.click(signInButton);
        waitForDashboardReady();
    }

    /**
     * Grants a permission toggle for the target driver and blocks until a success
     * confirmation toast is visible — ensuring downstream mobile tests start from
     * a known-good Admin state.
     *
     * @param driverId unique driver identifier shown in the Admin drivers table
     */
    public void grantDriverPermission(String driverId) {
        actions.click(driversNavLink);
        actions.waitForUrlContains(driversPathFragment());

        if (actions.getDriver().findElements(driverSearchInputLocator()).size() > 0) {
            actions.type(driverSearchInputLocator(), driverId);
        }

        WebElement permissionToggle = actions.waitForElementToBeClickable(driverPermissionToggleLocator(driverId));
        if (!isPermissionEnabled(permissionToggle)) {
            actions.click(permissionToggle);
        }

        actions.click(saveButton);
        waitForSuccessConfirmation();
    }

    public void waitForSuccessConfirmation() {
        String expectedMessage = ConfigReader.get(
                "crustq.admin.driver.permission.success.message",
                "success"
        );
        actions.waitForElementToBeClickable(successToast);
        actions.waitForTextInElement(successToastLocator(), expectedMessage);
    }

    private String driversPathFragment() {
        return ConfigReader.get("crustq.admin.drivers.path", "/drivers");
    }

    private By driverSearchInputLocator() {
        return By.xpath("//input[@type='search' or contains(@placeholder, 'Search') or contains(@aria-label, 'Search')]");
    }

    private By driverPermissionToggleLocator(String driverId) {
        return By.xpath("//tr[contains(., '" + driverId + "')]//input[@type='checkbox' or @role='switch' or contains(@class, 'switch')]"
                + " | //*[contains(., '" + driverId + "')]//button[@role='switch']");
    }

    private By successToastLocator() {
        return By.xpath("//*[contains(@class, 'toast') or contains(@class, 'Toast') or @role='alert'"
                + " or contains(@class, 'snackbar') or contains(@class, 'Snackbar')]");
    }

    private boolean isPermissionEnabled(WebElement toggle) {
        String tagName = toggle.getTagName();
        if ("input".equalsIgnoreCase(tagName)) {
            String checked = toggle.getAttribute("checked");
            return checked != null && (checked.equals("true") || checked.equals("checked"));
        }
        String ariaChecked = toggle.getAttribute("aria-checked");
        return "true".equalsIgnoreCase(ariaChecked);
    }
}
