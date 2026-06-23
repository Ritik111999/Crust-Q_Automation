package com.crustq.pages.pwa.user;

import com.crustq.config.ConfigReader;
import com.crustq.utils.FlutterSemanticsHelper;
import com.crustq.utils.RealWorldTestData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * Single Page Object for the User My Profile journey:
 * dashboard navigation, profile, edit profile, payment methods (incl. NMI add-card tab),
 * order history, and saved addresses.
 */
public class UserProfilePage extends UserBasePage {

    private String pwaWindowHandle;

    public UserProfilePage(WebDriver driver) {
        super(driver);
    }

    // ── Session bootstrap (pre-created user — no signup flow) ─────────────────

    public void openDashboardForLoggedInUser() {
        openUserPath(ConfigReader.get("crustq.user.dashboard.path", "/dashboard"));
        prepareFlutterPage();
        waitForDashboard();
    }

    public void waitForDashboard() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.dashboard.path", "/dashboard"));
        FlutterSemanticsHelper.enableAccessibilityForFlutterPage(utils);
    }

    public void waitForProfilePage() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.profile.path", "/profile"));
        FlutterSemanticsHelper.enableAccessibilityForFlutterPage(utils);
    }

    // ── Sidebar / navigation ────────────────────────────────────────────────

    public void openHamburgerMenu() {
        ensureSidebarDrawerOpen();
    }

    private void ensureSidebarDrawerOpen() {
        prepareFlutterPage();
        if (FlutterSemanticsHelper.isSemanticTextVisible(utils, "Logout")) {
            return;
        }
        clickHamburgerButton();
        if (!FlutterSemanticsHelper.isSemanticTextVisible(utils, "Logout")) {
            throw new IllegalStateException("Sidebar drawer did not open");
        }
    }

    private void clickHamburgerButton() {
        prepareFlutterPage();
        Object clicked = utils.executeScript("""
            const buttons = Array.from(document.querySelectorAll('flt-semantics[role="button"]'));
            if (!buttons.length) throw new Error('No header buttons found for hamburger menu');
            let target = buttons[0];
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || buttons[0]).click();
            return true;
            """);
        if (clicked == null) {
            throw new IllegalStateException("Failed to open hamburger menu");
        }
    }

    public void openProfileFromSidebar() {
        pwaWindowHandle = driver.getWindowHandle();
        openHamburgerMenu();
        if (!openProfileFromOpenMenu()) {
            openUserPath(ConfigReader.get("crustq.user.profile.path", "/profile"));
            prepareFlutterPage();
        }
        waitForProfilePage();
    }

    private boolean openProfileFromOpenMenu() {
        prepareFlutterPage();
        RealWorldTestData.ProfileUser user = RealWorldTestData.defaultProfileUser();
        String profilePath = ConfigReader.get("crustq.user.profile.path", "/profile");

        if (FlutterSemanticsHelper.clickSidebarProfileEntry(utils, user.sidebarTapText(), user.email())
                && waitForProfileUrl(profilePath, 8)) {
            return true;
        }

        String[] candidates = {
                user.sidebarTapText(),
                user.firstName() + " " + user.lastName(),
                user.email(),
                "My Profile",
                "Profile"
        };
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            ensureSidebarDrawerOpen();
            if (FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, candidate)
                    && waitForProfileUrl(profilePath, 8)) {
                return true;
            }
        }
        return false;
    }

    private boolean waitForProfileUrl(String profilePath, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.urlContains(profilePath));
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public void clickBack() {
        prepareFlutterPage();
        if (!FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, "Back")) {
            Object clicked = utils.executeScript("""
                const buttons = Array.from(document.querySelectorAll('flt-semantics[role="button"]'));
                if (!buttons.length) return false;
                let target = buttons[0];
                while (target && !target.hasAttribute('flt-tappable')) {
                  target = target.parentElement;
                }
                (target || buttons[0]).click();
                return true;
                """);
            if (!Boolean.TRUE.equals(clicked)) {
                driver.navigate().back();
            }
        }
    }

    public void openEditProfile() {
        navigateToProfileSubPage(
                "Edit",
                ConfigReader.get("crustq.user.edit.profile.path", "/edit-profile"),
                "Phone", "Submit"
        );
    }

    public void openOrderHistory() {
        navigateToProfileSubPage(
                "View All Orders",
                ConfigReader.get("crustq.user.order.history.path", "/order-detail"),
                "No orders found", "Order History"
        );
    }

    public void openPaymentMethods() {
        navigateToProfileSubPage(
                "Add Payment Method",
                ConfigReader.get("crustq.user.payment.method.path", "/payment-method"),
                "Cash on Delivery", "Add New Card"
        );
    }

    public void openAddressPage() {
        navigateToProfileSubPage(
                "New Address",
                ConfigReader.get("crustq.user.address.path", "/address"),
                "Enter Street Address", "Enter City", "Continue"
        );
    }

    public void waitForPaymentMethodPage() {
        waitForProfileSubPage(
                ConfigReader.get("crustq.user.payment.method.path", "/payment-method"),
                "Cash on Delivery", "Add New Card"
        );
    }

    public void waitForAddressPage() {
        waitForProfileSubPage(
                ConfigReader.get("crustq.user.address.path", "/address"),
                "Enter Street Address", "Enter City", "Continue"
        );
    }

    public void waitForEditProfilePage() {
        waitForProfileSubPage(
                ConfigReader.get("crustq.user.edit.profile.path", "/edit-profile"),
                "Phone", "Submit"
        );
    }

    public void waitForOrderHistoryPage() {
        waitForProfileSubPage(
                ConfigReader.get("crustq.user.order.history.path", "/order-detail"),
                "No orders found", "Order History"
        );
    }

    private void navigateToProfileSubPage(String linkText, String path, String... contentMarkers) {
        prepareFlutterPage();
        scrollToSemanticText(linkText);
        FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, linkText);
        if (!waitForProfileUrl(path, 6)) {
            openUserPath(path);
            prepareFlutterPage();
        }
        waitForProfileSubPage(path, contentMarkers);
    }

    private void waitForProfileSubPage(String path, String... contentMarkers) {
        if (driver.getCurrentUrl().contains(path)) {
            prepareFlutterPage();
            return;
        }
        prepareFlutterPage();
        for (String marker : contentMarkers) {
            if (marker != null && !marker.isBlank()
                    && (FlutterSemanticsHelper.isSemanticTextVisible(utils, marker)
                    || utils.isDisplayed(By.cssSelector("input[aria-label*='" + marker + "']")))) {
                return;
            }
        }
        webActions.waitForUrlContains(path);
        prepareFlutterPage();
    }

    private void scrollToSemanticText(String text) {
        utils.executeScript("""
            const text = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const match = nodes.find(n => n.textContent && n.textContent.includes(text));
            if (match) {
              match.scrollIntoView({block: 'center'});
            }
            """, text);
    }

    // ── Profile assertions (loyalty + personal) ─────────────────────────────

    public boolean isLoyaltySectionVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "My Crust & Q Rewards");
    }

    public String getDisplayedLoyaltyTier() {
        prepareFlutterPage();
        String expectedTier = RealWorldTestData.defaultProfileUser().loyaltyTier();
        if (FlutterSemanticsHelper.isSemanticTextVisible(utils, expectedTier)) {
            return expectedTier;
        }
        return readFirstMatchingTier();
    }

    public boolean isSignupRewardVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "Fried Pickles");
    }

    public boolean isPersonalInfoSectionVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "Personal Information");
    }

    public boolean isProfileEmailVisible() {
        String email = RealWorldTestData.defaultProfileUser().email();
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, email);
    }

    public String getLoyaltyProgressText() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.readSemanticTextContaining(utils, "away from");
    }

    // ── Edit profile ────────────────────────────────────────────────────────

    public void fillPhoneOnEditProfile(String phone) {
        waitForEditProfilePage();
        By phoneInput = By.cssSelector("input[aria-label*='Phone'], input[aria-label*='phone']");
        utils.sendKeys(phoneInput, phone);
    }

    public void submitEditProfile() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticButton(utils, "Submit");
    }

    public boolean isEditProfileSubmitEnabled() {
        waitForEditProfilePage();
        return FlutterSemanticsHelper.isSemanticButtonEnabled(utils, "Submit");
    }

    public boolean isOnEditProfilePage() {
        return driver.getCurrentUrl().contains(ConfigReader.get("crustq.user.edit.profile.path", "/edit-profile"));
    }

    public void attemptSubmitInvalidPhoneOnEditProfile() {
        fillPhoneOnEditProfile(RealWorldTestData.invalidPhone());
        if (isEditProfileSubmitEnabled()) {
            submitEditProfile();
        }
    }

    public boolean isPhoneValidationVisible() {
        prepareFlutterPage();
        String hint = RealWorldTestData.phoneValidationHint();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, hint)
                || isInputValidationVisible("phone");
    }

    public void updatePhoneOnEditProfile(String phone) {
        fillPhoneOnEditProfile(phone);
        submitEditProfile();
    }

    // ── Payment methods + NMI add-card (new tab) ────────────────────────────

    public void clickAddNewCard() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Add New Card");
    }

    public void addValidCardViaNmiTab() {
        addCardViaNmiTab(RealWorldTestData.defaultPaymentCard());
    }

    public void addCardViaNmiTab(RealWorldTestData.PaymentCard card) {
        rememberPwaWindow();
        clickAddNewCard();
        switchToAddCardWindow();
        fillNmiCardForm(card.zip(), card.number(), card.expiry(), card.cvv());
        submitAddCardOnNmiPage();
        switchBackToPwaWindow();
        waitForPaymentMethodPage();
    }

    public void attemptAddCardWithInvalidZipOnNmiTab() {
        assertAddCardBlockedOnNmiTab(
                RealWorldTestData.invalidPaymentZip(),
                null,
                null,
                null
        );
    }

    public void attemptAddCardWithInvalidCardNumberOnNmiTab() {
        RealWorldTestData.PaymentCard valid = RealWorldTestData.defaultPaymentCard();
        assertAddCardBlockedOnNmiTab(
                valid.zip(),
                RealWorldTestData.invalidPaymentNumber(),
                valid.expiry(),
                valid.cvv()
        );
    }

    public void attemptAddCardWithExpiredCardOnNmiTab() {
        RealWorldTestData.PaymentCard valid = RealWorldTestData.defaultPaymentCard();
        assertAddCardBlockedOnNmiTab(
                valid.zip(),
                valid.number(),
                RealWorldTestData.invalidPaymentExpiry(),
                valid.cvv()
        );
    }

    public void attemptAddCardWithInvalidCvvOnNmiTab() {
        RealWorldTestData.PaymentCard valid = RealWorldTestData.defaultPaymentCard();
        assertAddCardBlockedOnNmiTab(
                valid.zip(),
                valid.number(),
                valid.expiry(),
                RealWorldTestData.invalidPaymentCvv()
        );
    }

    private void assertAddCardBlockedOnNmiTab(String zip, String cardNumber, String expiry, String cvv) {
        rememberPwaWindow();
        clickAddNewCard();
        switchToAddCardWindow();

        if (cardNumber == null && expiry == null && cvv == null) {
            fillNmiZipOnly(zip);
        } else {
            fillNmiCardForm(zip, cardNumber, expiry, cvv);
        }

        boolean enabled = isAddCardButtonEnabledOnNmiPage();
        switchBackToPwaWindow();
        if (enabled) {
            throw new IllegalStateException("Add Card button should stay disabled for invalid payment details");
        }
    }

    public boolean isCardListed(String lastFourDigits) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, lastFourDigits);
    }

    public void selectCashOnDelivery() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Cash on Delivery");
    }

    // ── Order history ───────────────────────────────────────────────────────

    public boolean isNoOrdersMessageDisplayed() {
        waitForOrderHistoryPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "No orders found")
                || FlutterSemanticsHelper.isSemanticTextVisible(utils, "No order history found");
    }

    // ── Saved addresses ───────────────────────────────────────────────────

    public void saveNewAddressFromForm() {
        saveNewAddressFromForm(RealWorldTestData.nextAddress());
    }

    public void saveNewAddressFromForm(RealWorldTestData.UsAddress address) {
        waitForAddressPage();
        typeIntoFlutterField("Enter Street Address", address.streetWithUniqueUnit());

        if (!address.suite().isBlank()) {
            typeIntoFlutterField("Enter Suite", address.suite());
        }

        typeIntoFlutterField("Enter City", address.city());
        typeIntoFlutterField("Enter Zip Code", address.zip());
        selectState(address.state(), address.stateCode());

        if (!address.deliveryInstructions().isBlank()) {
            typeIntoFlutterField("Enter the Delivery Instructions", address.deliveryInstructions());
        }

        enableSaveAddressToggle();
        FlutterSemanticsHelper.clickSemanticButton(utils, "Continue");
    }

    private void selectState(String stateName, String stateCode) {
        prepareFlutterPage();
        scrollToSemanticText("State");
        FlutterSemanticsHelper.selectDropdownOption(utils, "Select State", stateName);
    }

    public void attemptSubmitBlankAddressForm() {
        waitForAddressPage();
        RealWorldTestData.UsAddress address = RealWorldTestData.defaultAddress();
        typeIntoFlutterField("Enter City", address.city());
        clickAddressContinue();
    }

    public void attemptSubmitAddressWithInvalidZip() {
        waitForAddressPage();
        RealWorldTestData.UsAddress address = RealWorldTestData.defaultAddress();
        typeIntoFlutterField("Enter Street Address", address.streetWithUniqueUnit());
        typeIntoFlutterField("Enter City", address.city());
        typeIntoFlutterField("Enter Zip Code", RealWorldTestData.invalidAddressZip());
        selectState(address.state(), address.stateCode());
        clickAddressContinue();
    }

    public void clickAddressContinue() {
        prepareFlutterPage();
        if (FlutterSemanticsHelper.isSemanticButtonEnabled(utils, "Continue")) {
            FlutterSemanticsHelper.clickSemanticButton(utils, "Continue");
        }
    }

    /**
     * True when address save did not complete (still on form, validation shown, or URL unchanged).
     */
    public boolean isAddressSubmissionBlocked() {
        prepareFlutterPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInt("explicit.wait.seconds", 20)));
        try {
            return wait.until(driver -> {
                if (isAddressValidationVisible()) {
                    return true;
                }
                return driver.getCurrentUrl().contains(ConfigReader.get("crustq.user.address.path", "/address"))
                        && isAddressFormVisible();
            });
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean isAddressFormVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "Delivery Address")
                || FlutterSemanticsHelper.isSemanticTextVisible(utils, "Street Address")
                || utils.isDisplayed(By.cssSelector("input[aria-label*='Street']"));
    }

    public boolean isAddressContinueEnabled() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticButtonEnabled(utils, "Continue");
    }

    public boolean isAddressValidationVisible() {
        prepareFlutterPage();
        String hint = RealWorldTestData.addressValidationHint();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, hint)
                || isInputValidationVisible("zip");
    }

    public boolean isSavedAddressVisible(String addressLabel) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, addressLabel);
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private void prepareFlutterPage() {
        FlutterSemanticsHelper.enableAccessibilityForFlutterPage(utils);
    }

    private void rememberPwaWindow() {
        if (pwaWindowHandle == null) {
            pwaWindowHandle = driver.getWindowHandle();
        }
    }

    private void switchToAddCardWindow() {
        WebDriverWait windowWait = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInt("explicit.wait.seconds", 20)));
        windowWait.until(d -> d.getWindowHandles().size() > 1);

        String fragment = ConfigReader.get("crustq.add.card.url.fragment", "add-card-details");
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            if (driver.getCurrentUrl().contains(fragment)) {
                return;
            }
        }
        throw new IllegalStateException("Add Card tab did not open for URL fragment: " + fragment);
    }

    private void switchBackToPwaWindow() {
        if (pwaWindowHandle != null) {
            driver.switchTo().window(pwaWindowHandle);
            return;
        }
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            if (driver.getCurrentUrl().contains(ConfigReader.get("user.base.url"))) {
                return;
            }
        }
    }

    private void fillNmiCardForm(String zip, String cardNumber, String expiry, String cvv) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInt("explicit.wait.seconds", 20)));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addCardForm")));

        WebElement zipInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("zip")));
        zipInput.clear();
        zipInput.sendKeys(zip);

        typeIntoNmiIframe("CollectJSInlineccnumber", cardNumber);
        typeIntoNmiIframe("CollectJSInlineccexp", expiry);
        typeIntoNmiIframe("CollectJSInlinecvv", cvv);
    }

    private void fillNmiZipOnly(String zip) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInt("explicit.wait.seconds", 20)));
        WebElement zipInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("zip")));
        zipInput.clear();
        zipInput.sendKeys(zip);
    }

    private void typeIntoNmiIframe(String iframeId, String value) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInt("explicit.wait.seconds", 20)));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id(iframeId)));
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input")));
        input.clear();
        input.sendKeys(value);
        driver.switchTo().defaultContent();
    }

    private void submitAddCardOnNmiPage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(45));
        WebElement addCardButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("demoPayButton")));
        addCardButton.click();
        wait.until(d -> d.getWindowHandles().size() == 1 || d.getCurrentUrl().contains("payment-method"));
    }

    private boolean isAddCardButtonEnabledOnNmiPage() {
        WebElement addCardButton = driver.findElement(By.id("demoPayButton"));
        return addCardButton.isEnabled();
    }

    private void typeIntoFlutterField(String ariaLabelFragment, String value) {
        By locator = By.cssSelector("input[aria-label*='" + ariaLabelFragment + "']");
        utils.sendKeys(locator, value);
    }

    private boolean isInputValidationVisible(String ariaLabelFragment) {
        Object invalid = utils.executeScript("""
            const fragment = arguments[0].toLowerCase();
            const inputs = Array.from(document.querySelectorAll('input[aria-label]'));
            return inputs.some(input => {
              const label = (input.getAttribute('aria-label') || '').toLowerCase();
              if (!label.includes(fragment)) return false;
              return input.getAttribute('aria-invalid') === 'true'
                || input.matches(':invalid');
            });
            """, ariaLabelFragment);
        return Boolean.TRUE.equals(invalid);
    }

    private void enableSaveAddressToggle() {
        if (!ConfigReader.getBoolean("crustq.user.address.save.toggle", true)) {
            return;
        }
        Object result = utils.executeScript("""
            const toggle = Array.from(document.querySelectorAll('flt-semantics'))
              .find(n => n.textContent && n.textContent.includes('Save this address'));
            if (!toggle) return false;
            let target = toggle;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || toggle).click();
            return true;
            """);
        if (!Boolean.TRUE.equals(result)) {
            FlutterSemanticsHelper.clickSemanticText(utils, "Save this address");
        }
    }

    private String readFirstMatchingTier() {
        for (RealWorldTestData.LoyaltyTier tier : RealWorldTestData.allLoyaltyTiers()) {
            if (FlutterSemanticsHelper.isSemanticTextVisible(utils, tier.name())) {
                return tier.name();
            }
        }
        return "";
    }

    public String getPwaWindowHandle() {
        return pwaWindowHandle != null ? pwaWindowHandle : driver.getWindowHandle();
    }

    public Set<String> getOpenWindowHandles() {
        return driver.getWindowHandles();
    }
}
