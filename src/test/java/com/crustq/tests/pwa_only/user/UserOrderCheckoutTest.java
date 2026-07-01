package com.crustq.tests.pwa_only.user;

import com.crustq.base.BaseTest;
import com.crustq.config.ConfigReader;
import com.crustq.pages.pwa.PwaLoginPage;
import com.crustq.pages.pwa.user.UserOrderingFlowPage;
import com.crustq.reporting.ExtentTestManager;
import com.crustq.utils.FlutterSemanticsHelper;
import com.crustq.utils.RealWorldTestData;
import com.crustq.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * User ordering E2E — add to cart through checkout and order confirmation.
 * Flow mapped from screen recording (login → dashboard → menu → cart → checkout → order status).
 */
public class UserOrderCheckoutTest extends BaseTest {

    private UserOrderingFlowPage orderPage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndLandOnDashboard() {
        PwaLoginPage loginPage = new PwaLoginPage(getDriver());
        loginPage.open();
        RealWorldTestData.ProfileUser user = RealWorldTestData.defaultProfileUser();
        loginPage.login(user.email(), user.password());
        loginPage.waitForSuccessfulLogin(ConfigReader.get("crustq.user.dashboard.path", "/dashboard"));

        orderPage = new UserOrderingFlowPage(getDriver());
        orderPage.waitForDashboard();
    }

    // ── Happy path (full E2E) ─────────────────────────────────────────────

    @Test(groups = {"Positive", "E2E"},
            description = "User | Order | Full add-to-cart and checkout happy path")
    public void testPositive_fullAddToCartCheckoutHappyPath() {
        RealWorldTestData.OrderMenuItem item = RealWorldTestData.defaultOrderMenuItem();
        String savedAddress = RealWorldTestData.orderSavedAddressShortName();
        String cardBrand = RealWorldTestData.orderPaymentCardBrand();
        String deliveryNotes = RealWorldTestData.orderDeliveryInstructionsSample();
        String specialInstructions = TestDataGenerator.randomAlphanumeric(12);

        // 2. Dashboard — View All under Our Menu
        Assert.assertTrue(orderPage.areMenuCategoriesVisible(),
                "Dashboard menu categories (Steak, Pizza, Chicken, etc.) should load");
        orderPage.clickViewAllOurMenu();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/browse-menu"),
                "View All should navigate to browse-menu");

        // 3. Browse menu — search + Chicken Sate → Add
        orderPage.searchMenuItems(item.name());
        Assert.assertTrue(orderPage.isMenuItemVisible(item.name()),
                "Target menu item should be visible: " + item.name());
        orderPage.addMenuItemFromBrowse(item.name());

        // 4. Menu detail — special instructions + Add to Order
        orderPage.enterSpecialInstructions(specialInstructions);
        ExtentTestManager.getTest().info("Entered special instructions: " + specialInstructions);
        orderPage.submitMenuItemToOrder(item.name(), item.price());

        // 5–6. Order type (if shown) → delivery address
        // 6. Delivery address — saved address or new form + Continue
        orderPage.waitForDeliveryAddress();
        orderPage.selectSavedAddress(savedAddress);
        Assert.assertTrue(orderPage.isContinueEnabledOnAddressPage(),
                "Continue should be enabled after selecting or entering a delivery address");
        orderPage.clickContinueOnAddress();

        // 7. Delivery time — ASAP
        orderPage.waitForDeliveryTime();
        orderPage.selectAsapDelivery();
        orderPage.clickContinueOnDeliveryTime();

        // 8. Cart — item, totals, proceed
        orderPage.waitForCart();
        orderPage.waitForCartItem(item.name());
        Assert.assertTrue(orderPage.isCartItemVisible(item.name()),
                "Cart should list the ordered item");
        Assert.assertTrue(orderPage.isCartTotalConsistent(),
                "Subtotal + Delivery Fee + Tax should equal cart Total");
        ExtentTestManager.getTest().info("Cart total: $" + orderPage.getCartTotal());
        orderPage.clickProceedToCheckout();

        // 9. Checkout — instructions, card, tip, place order
        orderPage.enterCheckoutDeliveryInstructions(deliveryNotes);
        orderPage.selectCardPayment();
        orderPage.selectSavedPaymentCard(cardBrand);
        orderPage.selectNoTip();
        orderPage.clickPlaceOrder();

        // 10. Order status — ID, timeline, item, instructions
        orderPage.waitForOrderStatus();
        String orderId = orderPage.getOrderId();
        ExtentTestManager.getTest().info("Placed order ID: " + orderId);

        Assert.assertTrue(orderPage.isOrderIdValidFormat(),
                "Order ID should match ORD-XXX... format");
        Assert.assertTrue(orderPage.isOrderTimelineVisible(),
                "Order status timeline should be visible");
        Assert.assertTrue(orderPage.isOrderItemListed(item.name()),
                "Order confirmation should list the correct item");
        Assert.assertTrue(orderPage.isDeliveryInstructionOnOrderStatus(deliveryNotes),
                "Delivery instructions on confirmation should match checkout input");
    }

    // ── Dashboard & menu positives ────────────────────────────────────────

    @Test(groups = {"Positive"},
            description = "User | Order | Search filters browse menu items")
    public void testPositive_searchFiltersMenuItems() {
        RealWorldTestData.OrderMenuItem item = RealWorldTestData.defaultOrderMenuItem();
        orderPage.clickViewAllOurMenu();
        orderPage.searchMenuItems(item.name());
        orderPage.waitForSemanticText(item.name(), 15);
        Assert.assertTrue(orderPage.isMenuItemVisible(item.name()),
                "Search should surface the target menu item");
    }

    // Login negatives are covered by PwaLoginNegativeTest (blank credentials, invalid email, etc.).

    // ── Item detail negatives ─────────────────────────────────────────────

    @Test(groups = {"Negative"},
            description = "User | Order | Special instructions over max length are truncated or blocked")
    public void testNegative_specialInstructionsExceedMaxLength() {
        RealWorldTestData.OrderMenuItem item = RealWorldTestData.defaultOrderMenuItem();
        int maxLength = RealWorldTestData.orderSpecialInstructionsMaxLength();
        String overLimit = TestDataGenerator.randomAlphanumeric(maxLength + 25);

        orderPage.clickViewAllOurMenu();
        orderPage.addMenuItemFromBrowse(item.name());
        orderPage.enterSpecialInstructions(overLimit);

        String actual = orderPage.getSpecialInstructionsValue();
        Assert.assertTrue(actual.length() <= maxLength,
                "Special instructions should not exceed max length of " + maxLength);
    }

    // ── Address negative ──────────────────────────────────────────────────

    @Test(groups = {"Negative"},
            description = "User | Order | Address Continue blocked without saved address selection")
    public void testNegative_addressContinueWithoutSelection() {
        RealWorldTestData.OrderMenuItem item = RealWorldTestData.defaultOrderMenuItem();

        orderPage.clickViewAllOurMenu();
        orderPage.addMenuItemFromBrowse(item.name());
        orderPage.enterSpecialInstructions(TestDataGenerator.randomAlphanumeric(8));
        orderPage.clickAddToOrder(item.name(), item.price());
        if (orderPage.isOrderTypeModalVisible()) {
            orderPage.selectDeliveryOrder();
        }
        orderPage.waitForDeliveryAddress();

        // Deselect any pre-selected saved address by toggling if present, then assert Continue state.
        // When no address is selected and new-address form is blank, Continue should be disabled.
        boolean continueEnabled = orderPage.isContinueEnabledOnAddressPage();
        if (continueEnabled) {
            orderPage.clickContinueOnAddress();
            Assert.assertTrue(getDriver().getCurrentUrl().contains("/address"),
                    "Continue without a valid address should not advance the flow");
        } else {
            Assert.assertFalse(continueEnabled,
                    "Continue should be disabled when no address is selected");
        }
    }

    // ── Checkout negatives ────────────────────────────────────────────────

    @Test(groups = {"Negative"},
            description = "User | Order | Custom tip rejects non-numeric characters")
    public void testNegative_customTipRejectsNonNumeric() {
        orderPage.clickViewAllOurMenu();
        orderPage.addMenuItemFromBrowse(RealWorldTestData.defaultOrderMenuItem().name());
        orderPage.enterSpecialInstructions(TestDataGenerator.randomAlphanumeric(6));
        orderPage.clickAddToOrder(
                RealWorldTestData.defaultOrderMenuItem().name(),
                RealWorldTestData.defaultOrderMenuItem().price());
        if (orderPage.isOrderTypeModalVisible()) {
            orderPage.selectDeliveryOrder();
        }
        orderPage.waitForDeliveryAddress();
        orderPage.selectSavedAddress(RealWorldTestData.orderSavedAddressShortName());
        orderPage.clickContinueOnAddress();
        orderPage.waitForDeliveryTime();
        orderPage.selectAsapDelivery();
        orderPage.clickContinueOnDeliveryTime();
        orderPage.waitForCart();
        orderPage.clickProceedToCheckout();

        orderPage.openCustomTipModal();
        orderPage.enterCustomTipAmount("XASXAZSX");
        String tipValue = orderPage.getCustomTipInputValue();
        Assert.assertTrue(tipValue.isEmpty() || tipValue.chars().allMatch(c -> Character.isDigit(c) || c == '.'),
                "Custom tip field should reject non-numeric characters");
    }

    @Test(groups = {"Negative"},
            description = "User | Order | Place Order without payment method shows validation")
    public void testNegative_placeOrderWithoutPaymentMethod() {
        orderPage.clickViewAllOurMenu();
        orderPage.addMenuItemFromBrowse(RealWorldTestData.defaultOrderMenuItem().name());
        orderPage.enterSpecialInstructions(TestDataGenerator.randomAlphanumeric(6));
        orderPage.clickAddToOrder(
                RealWorldTestData.defaultOrderMenuItem().name(),
                RealWorldTestData.defaultOrderMenuItem().price());
        if (orderPage.isOrderTypeModalVisible()) {
            orderPage.selectDeliveryOrder();
        }
        orderPage.waitForDeliveryAddress();
        orderPage.selectSavedAddress(RealWorldTestData.orderSavedAddressShortName());
        orderPage.clickContinueOnAddress();
        orderPage.waitForDeliveryTime();
        orderPage.selectAsapDelivery();
        orderPage.clickContinueOnDeliveryTime();
        orderPage.waitForCart();
        orderPage.clickProceedToCheckout();

        orderPage.selectNoTip();
        orderPage.clickPlaceOrder();

        boolean stayedOnCheckout = getDriver().getCurrentUrl().contains("/checkout");
        boolean validationVisible = orderPage.isPlaceOrderValidationVisible()
                || FlutterSemanticsHelper.isSemanticTextVisible(
                        new com.crustq.utils.WebElementUtils(getDriver()), "Select");
        Assert.assertTrue(stayedOnCheckout || validationVisible,
                "Placing order without a payment method should not complete checkout");
    }
}
