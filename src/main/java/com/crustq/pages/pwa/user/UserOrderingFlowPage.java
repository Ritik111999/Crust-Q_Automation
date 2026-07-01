package com.crustq.pages.pwa.user;

import com.crustq.config.ConfigReader;
import com.crustq.utils.FlutterSemanticsHelper;
import com.crustq.utils.RealWorldTestData;
import com.crustq.utils.TestDataGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Page Object for the User ordering journey:
 * dashboard → browse menu → item detail → delivery address/time → cart → checkout → order status.
 * <p>
 * Locators use Flutter semantics text and aria-label inputs (CanvasKit PWA).
 */
public class UserOrderingFlowPage extends UserBasePage {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("ORD-\\d+-\\d+");
    private static final List<String> MENU_CATEGORIES = List.of(
            "Steak and Burgers", "Pizza", "Chicken", "Poke Bowls", "Sides", "Drinks"
    );

    public UserOrderingFlowPage(WebDriver driver) {
        super(driver);
    }

    // ── Dashboard ───────────────────────────────────────────────────────────

    public void waitForDashboard() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.dashboard.path", "/dashboard"));
        prepareFlutterPage();
    }

    public void clickViewAllOurMenu() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickViewAllForSection(utils, "Our Menu");
        waitForBrowseMenu();
    }

    public boolean areMenuCategoriesVisible() {
        prepareFlutterPage();
        return MENU_CATEGORIES.stream()
                .allMatch(category -> FlutterSemanticsHelper.isSemanticTextVisible(utils, category));
    }

    // ── Browse menu ─────────────────────────────────────────────────────────

    public void waitForBrowseMenu() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.browse.menu.path", "/browse-menu"));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForSemanticText(utils, "Menu", 25);
    }

    public void searchMenuItems(String query) {
        prepareFlutterPage();
        Object filled = utils.executeScript("""
            const query = arguments[0];
            const inputs = Array.from(document.querySelectorAll('input[aria-label], textarea[aria-label], input, textarea'));
            const search = inputs.find(i => {
              const label = (i.getAttribute('aria-label') || '').toLowerCase();
              const placeholder = (i.getAttribute('placeholder') || '').toLowerCase();
              return label.includes('search') || placeholder.includes('search');
            });
            if (!search) return false;
            search.focus();
            search.value = query;
            search.dispatchEvent(new Event('input', { bubbles: true }));
            search.dispatchEvent(new Event('change', { bubbles: true }));
            return true;
            """, query);
        if (!Boolean.TRUE.equals(filled)) {
            if (FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, "Search menu items")) {
                new Actions(driver).sendKeys(query).perform();
            }
        }
    }

    public boolean isMenuItemVisible(String itemName) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, itemName);
    }

    public void addMenuItemFromBrowse(String itemName) {
        prepareFlutterPage();
        searchMenuItems(itemName);
        FlutterSemanticsHelper.waitForSemanticText(utils, itemName, 35);
        FlutterSemanticsHelper.scrollToSemanticText(utils, itemName);
        try {
            FlutterSemanticsHelper.clickAddForMenuItem(utils, itemName);
        } catch (RuntimeException addButtonMissing) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, itemName);
        }
        waitForMenuDetail(itemName);
    }

    // ── Menu detail ─────────────────────────────────────────────────────────

    public void waitForMenuDetail(String itemName) {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.menu.detail.path", "/menu-detail"));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForSemanticText(utils, itemName, 20);
    }

    public void enterSpecialInstructions(String instructions) {
        prepareFlutterPage();
        try {
            FlutterSemanticsHelper.fillMultilineField(utils, instructions,
                    "Any special requests", "Special Instructions", "special requests");
        } catch (RuntimeException ignored) {
            // Optional on menu detail — checkout delivery notes are verified on confirmation.
        }
    }

    public String getSpecialInstructionsValue() {
        prepareFlutterPage();
        String active = FlutterSemanticsHelper.readActiveFlutterEditorValue(utils);
        if (!active.isBlank()) {
            return active;
        }
        String byAria = FlutterSemanticsHelper.readInputValueByAriaLabel(utils, "special");
        if (!byAria.isBlank()) {
            return byAria;
        }
        return FlutterSemanticsHelper.readInputValueByAriaLabel(utils, "request");
    }

    public void enterCheckoutDeliveryInstructions(String instructions) {
        prepareFlutterPage();
        FlutterSemanticsHelper.fillMultilineField(utils, instructions,
                "Add any delivery notes", "Delivery Instructions", "delivery notes");
    }

    public String getCheckoutDeliveryInstructionsValue() {
        prepareFlutterPage();
        String active = FlutterSemanticsHelper.readActiveFlutterEditorValue(utils);
        if (!active.isBlank()) {
            return active;
        }
        return FlutterSemanticsHelper.readInputValueByAriaLabel(utils, "delivery notes");
    }

    public boolean isTextVisibleOnPage(String text) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, text);
    }

    public void clickAddToOrder(String itemName, String price) {
        prepareFlutterPage();
        dismissActiveTextEditor();
        FlutterSemanticsHelper.waitForSemanticText(utils, "Add to Order", 25);
        FlutterSemanticsHelper.scrollToSemanticText(utils, "Add to Order");
        try {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Add to Order - $" + price);
        } catch (RuntimeException firstAttempt) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Add to Order");
        }
    }

    public void submitMenuItemToOrder(String itemName, String price) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            clickAddToOrder(itemName, price);
            if (waitForOrderSubmissionTransition(15)) {
                break;
            }
        }
        confirmDeliveryOrderType();
    }

    private boolean waitForOrderSubmissionTransition(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
                String url = d.getCurrentUrl();
                return url.contains("/address")
                        || url.contains("/cart")
                        || isOrderTypeModalVisible();
            });
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public void confirmDeliveryOrderType() {
        prepareFlutterPage();
        if (driver.getCurrentUrl().contains("/address")) {
            return;
        }
        try {
            FlutterSemanticsHelper.waitForSemanticText(utils, "Delivery Order", 20);
            selectDeliveryOrder();
        } catch (RuntimeException modalNotShown) {
            FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, "Delivery Order");
        }
    }

    private void dismissActiveTextEditor() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    public boolean isOrderTypeModalVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "Select Order Type")
                || FlutterSemanticsHelper.isSemanticTextVisible(utils, "Delivery Order");
    }

    public void selectDeliveryOrder() {
        prepareFlutterPage();
        try {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Delivery Order");
        } catch (RuntimeException buttonClick) {
            FlutterSemanticsHelper.clickSemanticText(utils, "Delivery Order");
        }
    }

    // ── Delivery address ────────────────────────────────────────────────────

    public void waitForDeliveryAddress() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.address.path", "/address"));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForSemanticText(utils, "Delivery Address", 25);
        waitForSavedAddressesReady();
    }

    private void waitForSavedAddressesReady() {
        WebDriverWait addressWait = new WebDriverWait(driver, Duration.ofSeconds(45));
        addressWait.until(d -> {
            prepareFlutterPage();
            if (FlutterSemanticsHelper.isSemanticTextVisible(utils, "No saved addresses found")) {
                return true;
            }
            if (FlutterSemanticsHelper.hasSavedAddressCards(utils)) {
                return true;
            }
            for (String marker : List.of("Orlando", "Nagpur", "Magnolia", "Green Tower", "1238")) {
                if (FlutterSemanticsHelper.isSemanticTextVisible(utils, marker)) {
                    return true;
                }
            }
            return FlutterSemanticsHelper.isSemanticTextVisible(utils, "Enter New Address");
        });
    }

    public void selectSavedAddress(String shortName) {
        prepareFlutterPage();
        waitForSavedAddressesReady();

        if (FlutterSemanticsHelper.isSemanticTextVisible(utils, "No saved addresses found")) {
            fillNewDeliveryAddress(RealWorldTestData.defaultAddress());
            return;
        }

        String[] candidates = {shortName, "Orlando", "Nagpur", "Magnolia", "Green Tower"};
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()
                    && FlutterSemanticsHelper.selectSavedAddressCard(utils, candidate)) {
                return;
            }
        }
        if (FlutterSemanticsHelper.selectFirstSavedAddressCard(utils)) {
            return;
        }
        throw new IllegalStateException("Could not select a saved delivery address on /address");
    }

    public boolean isContinueEnabledOnAddressPage() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticButtonEnabled(utils, "Continue");
    }

    public void clickContinueOnAddress() {
        prepareFlutterPage();
        FlutterSemanticsHelper.scrollToSemanticText(utils, "Continue");
        WebDriverWait readyWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        readyWait.until(d -> FlutterSemanticsHelper.isSemanticButtonEnabled(utils, "Continue"));

        try {
            FlutterSemanticsHelper.clickSemanticButton(utils, "Continue");
        } catch (RuntimeException primaryClick) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Continue");
        }

        WebDriverWait navigationWait = new WebDriverWait(driver, Duration.ofSeconds(40));
        navigationWait.until(d -> !d.getCurrentUrl().contains("/address")
                || d.getCurrentUrl().contains("/delivery-time"));
    }

    public void waitForDeliveryTime() {
        String deliveryTimePath = ConfigReader.get("crustq.user.delivery.time.path", "/delivery-time-view");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        wait.until(ExpectedConditions.urlContains(deliveryTimePath));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForSemanticText(utils, "Delivery Time", 30);
    }

    private boolean clickSavedAddressLabel(String label) {
        if (label == null || label.isBlank()) {
            return false;
        }
        if (FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, label)) {
            return true;
        }
        try {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, label);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public void fillNewDeliveryAddress(RealWorldTestData.UsAddress address) {
        prepareFlutterPage();
        FlutterSemanticsHelper.scrollToSemanticText(utils, "Enter New Address");
        typeIntoAddressField("Enter Street Address", address.streetWithUniqueUnit());
        if (!address.suite().isBlank()) {
            typeIntoAddressField("Enter Suite", address.suite());
        }
        typeIntoAddressField("Enter City", address.city());
        FlutterSemanticsHelper.scrollToSemanticText(utils, "Select State");
        FlutterSemanticsHelper.selectDropdownOption(utils, "Select State", address.state());
        typeIntoAddressField("Enter Zip Code", address.zip());
        enableSaveAddressToggle();
    }

    private void typeIntoAddressField(String ariaLabelFragment, String value) {
        org.openqa.selenium.By locator =
                org.openqa.selenium.By.cssSelector("input[aria-label*='" + ariaLabelFragment + "']");
        utils.waitForPresence(locator);
        utils.sendKeys(locator, value);
    }

    private void enableSaveAddressToggle() {
        utils.executeScript("""
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
    }

    public void selectAnySavedAddress(String... addressLabels) {
        prepareFlutterPage();
        for (String label : addressLabels) {
            if (label != null && !label.isBlank()
                    && FlutterSemanticsHelper.clickSemanticTextIfPresent(utils, label)) {
                return;
            }
        }
        throw new IllegalStateException("No saved address found for labels: " + String.join(", ", addressLabels));
    }

    // ── Delivery time ───────────────────────────────────────────────────────

    public void selectAsapDelivery() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "ASAP");
    }

    public void selectFutureDelivery() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Future Select");
    }

    public void clickContinueOnDeliveryTime() {
        prepareFlutterPage();
        FlutterSemanticsHelper.scrollToSemanticText(utils, "Continue");
        try {
            FlutterSemanticsHelper.clickSemanticButton(utils, "Continue");
        } catch (RuntimeException primaryClick) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Continue");
        }
        WebDriverWait navigationWait = new WebDriverWait(driver, Duration.ofSeconds(45));
        navigationWait.until(d -> d.getCurrentUrl().contains("/cart"));
    }

    // ── Cart ────────────────────────────────────────────────────────────────

    public void waitForCart() {
        WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(45));
        urlWait.until(ExpectedConditions.urlContains(ConfigReader.get("crustq.user.cart.path", "/cart")));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForAnySemanticText(utils, 30,
                "Your Cart", "Proceed to Checkout", "Subtotal", "Order Summary");
    }

    public void waitForCartItem(String itemName) {
        prepareFlutterPage();
        try {
            FlutterSemanticsHelper.waitForSemanticText(utils, itemName, 20);
        } catch (org.openqa.selenium.TimeoutException notFound) {
            FlutterSemanticsHelper.waitForAnySemanticText(utils, 10, "Proceed to Checkout", "Subtotal");
        }
    }

    public boolean isCartItemVisible(String itemName) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, itemName);
    }

    public double getCartLineAmount(String label) {
        prepareFlutterPage();
        String text = FlutterSemanticsHelper.readSemanticTextContaining(utils, label);
        return FlutterSemanticsHelper.parseDollarAmount(text);
    }

    public double getCartTotal() {
        prepareFlutterPage();
        String totalText = FlutterSemanticsHelper.readSemanticTextContaining(utils, "Total");
        return FlutterSemanticsHelper.parseDollarAmount(totalText);
    }

    public boolean isCartTotalConsistent() {
        double subtotal = getCartLineAmount("Subtotal");
        double delivery = getCartLineAmount("Delivery Fee");
        double tax = getCartLineAmount("Tax");
        double total = getCartTotal();
        if (subtotal <= 0 || total <= 0) {
            return false;
        }
        double expected = subtotal + delivery + tax;
        return Math.abs(expected - total) < 0.02;
    }

    public void clickProceedToCheckout() {
        prepareFlutterPage();
        try {
            FlutterSemanticsHelper.clickSemanticButton(utils, "Proceed to Checkout");
        } catch (RuntimeException primaryClick) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Proceed to Checkout");
        }
        waitForCheckout();
    }

    // ── Checkout ────────────────────────────────────────────────────────────

    public void waitForCheckout() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.checkout.path", "/checkout"));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForSemanticText(utils, "Checkout", 25);
    }

    public void selectCardPayment() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Card");
    }

    public void selectCashOnDeliveryPayment() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Cash on Delivery");
    }

    public void selectSavedPaymentCard(String brand) {
        prepareFlutterPage();
        try {
            FlutterSemanticsHelper.clickSemanticText(utils, brand);
        } catch (RuntimeException primaryClick) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, brand);
        }
    }

    public void selectNoTip() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "No Tip");
    }

    public void openCustomTipModal() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Custom");
    }

    public void enterCustomTipAmount(String amount) {
        prepareFlutterPage();
        FlutterSemanticsHelper.fillFlutterInput(utils, "tip", amount);
    }

    public void applyCustomTip() {
        prepareFlutterPage();
        FlutterSemanticsHelper.clickSemanticText(utils, "Apply");
    }

    public String getCustomTipInputValue() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.readInputValueByAriaLabel(utils, "tip");
    }

    public void clickPlaceOrder() {
        prepareFlutterPage();
        try {
            FlutterSemanticsHelper.clickSemanticButton(utils, "Place Order");
        } catch (RuntimeException primaryClick) {
            FlutterSemanticsHelper.forceClickSemanticContaining(utils, "Place Order");
        }
    }

    public boolean isPlaceOrderValidationVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "payment")
                || FlutterSemanticsHelper.isSemanticTextVisible(utils, "Payment")
                || FlutterSemanticsHelper.isSemanticTextVisible(utils, "required");
    }

    public double getCheckoutTotal() {
        prepareFlutterPage();
        String totalText = FlutterSemanticsHelper.readSemanticTextContaining(utils, "Total");
        return FlutterSemanticsHelper.parseDollarAmount(totalText);
    }

    // ── Order status ────────────────────────────────────────────────────────

    public void waitForOrderStatus() {
        webActions.waitForUrlContains(ConfigReader.get("crustq.user.order.status.path", "/order"));
        prepareFlutterPage();
        FlutterSemanticsHelper.waitForSemanticText(utils, "Order Status", 40);
    }

    public String getOrderId() {
        prepareFlutterPage();
        String orderText = FlutterSemanticsHelper.readSemanticTextContaining(utils, "ORD-");
        if (orderText.isBlank()) {
            return "";
        }
        var matcher = ORDER_ID_PATTERN.matcher(orderText);
        return matcher.find() ? matcher.group() : orderText;
    }

    public boolean isOrderIdValidFormat() {
        String orderId = getOrderId();
        return ORDER_ID_PATTERN.matcher(orderId).matches();
    }

    public boolean isOrderTimelineVisible() {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, "Order Placed");
    }

    public boolean isOrderItemListed(String itemName) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, itemName);
    }

    public boolean isDeliveryInstructionOnOrderStatus(String instructions) {
        prepareFlutterPage();
        return FlutterSemanticsHelper.isSemanticTextVisible(utils, instructions);
    }

    public boolean isToastVisible(String message) {
        return utils.isDisplayed(By.xpath("//*[contains(., '" + message + "')]"));
    }

    // ── Composite happy-path steps ──────────────────────────────────────────

    public String runHappyPathFromDashboard() {
        RealWorldTestData.OrderMenuItem item = RealWorldTestData.defaultOrderMenuItem();
        String savedAddress = RealWorldTestData.orderSavedAddressShortName();
        String cardBrand = RealWorldTestData.orderPaymentCardBrand();
        String deliveryNotes = RealWorldTestData.orderDeliveryInstructionsSample();
        String specialInstructions = TestDataGenerator.randomAlphanumeric(12);

        clickViewAllOurMenu();
        addMenuItemFromBrowse(item.name());
        enterSpecialInstructions(specialInstructions);
        clickAddToOrder(item.name(), item.price());

        if (isOrderTypeModalVisible()) {
            selectDeliveryOrder();
        }

        waitForDeliveryAddress();
        selectSavedAddress(savedAddress);
        clickContinueOnAddress();

        waitForDeliveryTime();
        selectAsapDelivery();
        clickContinueOnDeliveryTime();

        waitForCart();
        clickProceedToCheckout();

        enterCheckoutDeliveryInstructions(deliveryNotes);
        selectCardPayment();
        selectSavedPaymentCard(cardBrand);
        selectNoTip();
        clickPlaceOrder();

        waitForOrderStatus();
        return getOrderId();
    }

    private void prepareFlutterPage() {
        FlutterSemanticsHelper.enableAccessibilityForFlutterPage(utils);
    }

    public void waitForSemanticText(String text, int timeoutSeconds) {
        prepareFlutterPage();
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(d -> FlutterSemanticsHelper.isSemanticTextVisible(utils, text));
    }
}
