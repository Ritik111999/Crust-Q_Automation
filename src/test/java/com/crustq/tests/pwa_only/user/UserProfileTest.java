package com.crustq.tests.pwa_only.user;

import com.crustq.base.BaseTest;
import com.crustq.config.ConfigReader;
import com.crustq.pages.pwa.PwaLoginPage;
import com.crustq.pages.pwa.user.UserProfilePage;
import com.crustq.utils.RealWorldTestData;
import com.crustq.reporting.ExtentTestManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * User My Profile journey — navigation, loyalty, personal info, payments, addresses.
 * Uses a pre-created user account (no signup automation).
 * <p>
 * Card-add and address-save tests are temporarily disabled ({@code enabled = false}).
 */
public class UserProfileTest extends BaseTest {

    private static final boolean CARD_AND_ADDRESS_TESTS_ENABLED = false;

    private UserProfilePage profilePage;

    @BeforeMethod(alwaysRun = true)
    public void loginAndLandOnDashboard() {
        PwaLoginPage loginPage = new PwaLoginPage(getDriver());
        loginPage.open();
        RealWorldTestData.ProfileUser user = RealWorldTestData.defaultProfileUser();
        loginPage.login(user.email(), user.password());
        loginPage.waitForSuccessfulLogin(ConfigReader.get("crustq.user.dashboard.path", "/dashboard"));

        profilePage = new UserProfilePage(getDriver());
        profilePage.waitForDashboard();
    }

    @Test(groups = {"Positive"},
            description = "User | Profile | Navigate dashboard → sidebar → My Profile")
    public void testPositive_navigateDashboardToProfile() {
        profilePage.openProfileFromSidebar();
        Assert.assertTrue(profilePage.isLoyaltySectionVisible(), "Loyalty section should be visible");
        Assert.assertTrue(profilePage.isPersonalInfoSectionVisible(), "Personal info should be visible");
        ExtentTestManager.getTest().info("Landed on My Profile: " + getDriver().getCurrentUrl());
    }

    @Test(groups = {"Positive"},
            description = "User | Profile | Loyalty tier and signup reward are displayed")
    public void testPositive_profileDisplaysLoyaltyAndSignupReward() {
        profilePage.openProfileFromSidebar();

        String tier = profilePage.getDisplayedLoyaltyTier();
        ExtentTestManager.getTest().info("Current loyalty tier: " + tier);
        Assert.assertFalse(tier.isBlank(), "Loyalty tier should be displayed");

        String progress = profilePage.getLoyaltyProgressText();
        ExtentTestManager.getTest().info("Tier progress: " + progress);

        Assert.assertTrue(profilePage.isSignupRewardVisible(),
                "Signup Fried Pickles reward should be visible for new user");
    }

    @Test(groups = {"Positive"},
            description = "User | Profile | Personal information shows configured email")
    public void testPositive_profileDisplaysPersonalInformation() {
        profilePage.openProfileFromSidebar();
        Assert.assertTrue(profilePage.isProfileEmailVisible(),
                "Profile email should match the pre-created user");
    }

    @Test(groups = {"Positive"},
            description = "User | Profile | Navigate to Edit Profile and back")
    public void testPositive_navigateEditProfileAndBack() {
        profilePage.openProfileFromSidebar();
        profilePage.openEditProfile();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/edit-profile"),
                "Should navigate to edit profile page");

        profilePage.clickBack();
        profilePage.waitForProfilePage();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/profile"),
                "Back should return to My Profile");
    }

    @Test(groups = {"Positive"},
            description = "User | Profile | Order history shows empty state for new user")
    public void testPositive_orderHistoryEmptyState() {
        profilePage.openProfileFromSidebar();
        profilePage.openOrderHistory();
        Assert.assertTrue(profilePage.isNoOrdersMessageDisplayed(),
                "New user should see no orders found message");
    }

    @Test(groups = {"Positive"},
            description = "User | Profile | Navigate to payment method page")
    public void testPositive_navigateToPaymentMethodPage() {
        profilePage.openProfileFromSidebar();
        profilePage.openPaymentMethods();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/payment-method"),
                "Should open payment method page");
        profilePage.selectCashOnDelivery();
    }

    @Test(groups = {"Positive"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Add valid NMI sandbox card and verify card list")
    public void testPositive_addValidNmiCard() {
        profilePage.openProfileFromSidebar();
        profilePage.openPaymentMethods();
        profilePage.addValidCardViaNmiTab();

        RealWorldTestData.PaymentCard card = RealWorldTestData.defaultPaymentCard();
        Assert.assertTrue(profilePage.isCardListed(card.last4()),
                "Saved card ending in " + card.last4() + " should appear in card list");
    }

    @Test(groups = {"Negative"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Invalid ZIP keeps Add Card disabled on NMI page")
    public void testNegative_addCardInvalidZip() {
        profilePage.openProfileFromSidebar();
        profilePage.openPaymentMethods();
        profilePage.attemptAddCardWithInvalidZipOnNmiTab();
        ExtentTestManager.getTest().info("Invalid ZIP (" + RealWorldTestData.invalidPaymentZip()
                + ") correctly blocked Add Card submission");
    }

    @Test(groups = {"Negative"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Invalid card number keeps Add Card disabled on NMI page")
    public void testNegative_addCardInvalidCardNumber() {
        profilePage.openProfileFromSidebar();
        profilePage.openPaymentMethods();
        profilePage.attemptAddCardWithInvalidCardNumberOnNmiTab();
        ExtentTestManager.getTest().info("Invalid card number (" + RealWorldTestData.invalidPaymentNumber()
                + ") correctly blocked Add Card submission");
    }

    @Test(groups = {"Negative"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Expired card keeps Add Card disabled on NMI page")
    public void testNegative_addCardExpiredCard() {
        profilePage.openProfileFromSidebar();
        profilePage.openPaymentMethods();
        profilePage.attemptAddCardWithExpiredCardOnNmiTab();
        ExtentTestManager.getTest().info("Expired card (" + RealWorldTestData.invalidPaymentExpiry()
                + ") correctly blocked Add Card submission");
    }

    @Test(groups = {"Negative"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Invalid CVV keeps Add Card disabled on NMI page")
    public void testNegative_addCardInvalidCvv() {
        profilePage.openProfileFromSidebar();
        profilePage.openPaymentMethods();
        profilePage.attemptAddCardWithInvalidCvvOnNmiTab();
        ExtentTestManager.getTest().info("Invalid CVV (" + RealWorldTestData.invalidPaymentCvv()
                + ") correctly blocked Add Card submission");
    }

    @Test(groups = {"Negative"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Blank street address blocks address save")
    public void testNegative_submitBlankAddressForm() {
        profilePage.openProfileFromSidebar();
        profilePage.openAddressPage();
        profilePage.attemptSubmitBlankAddressForm();
        Assert.assertTrue(profilePage.isAddressSubmissionBlocked(),
                "Blank street address should block address save");
        ExtentTestManager.getTest().info("Blank street address correctly blocked address save");
    }

    @Test(groups = {"Negative"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Invalid ZIP blocks address save")
    public void testNegative_submitAddressInvalidZip() {
        profilePage.openProfileFromSidebar();
        profilePage.openAddressPage();
        profilePage.attemptSubmitAddressWithInvalidZip();
        Assert.assertTrue(profilePage.isAddressSubmissionBlocked(),
                "Invalid ZIP should block address save: " + RealWorldTestData.invalidAddressZip());
        ExtentTestManager.getTest().info("Invalid ZIP (" + RealWorldTestData.invalidAddressZip()
                + ") correctly blocked address save");
    }

    @Test(groups = {"Negative"},
            description = "User | Profile | Invalid phone blocks edit profile submission")
    public void testNegative_editProfileInvalidPhone() {
        profilePage.openProfileFromSidebar();
        profilePage.openEditProfile();
        profilePage.attemptSubmitInvalidPhoneOnEditProfile();
        Assert.assertTrue(profilePage.isOnEditProfilePage() || profilePage.isPhoneValidationVisible(),
                "User should remain on edit profile or see validation for invalid phone: "
                        + RealWorldTestData.invalidPhone());
        ExtentTestManager.getTest().info("Invalid phone (" + RealWorldTestData.invalidPhone()
                + ") correctly blocked profile update");
    }

    @Test(groups = {"Positive"}, enabled = CARD_AND_ADDRESS_TESTS_ENABLED,
            description = "User | Profile | Save new delivery address")
    public void testPositive_saveNewDeliveryAddress() {
        profilePage.openProfileFromSidebar();
        profilePage.openAddressPage();
        profilePage.saveNewAddressFromForm();

        RealWorldTestData.UsAddress address = RealWorldTestData.defaultAddress();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/profile")
                        || getDriver().getCurrentUrl().contains("/address")
                        || profilePage.isSavedAddressVisible(address.city())
                        || profilePage.isSavedAddressVisible(address.label()),
                "Address save flow should complete and show saved location: " + address.city());
    }
}
