package com.crustq.tests.infrastructure;

import com.crustq.utils.RealWorldTestData;
import com.crustq.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Verifies shared utilities without launching browsers or Appium sessions.
 */
public class UtilsWiringTest {

    @Test(description = "Utils | TestDataGenerator produces unique order IDs")
    public void verifyUniqueOrderIds() {
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < 25; i++) {
            String orderId = TestDataGenerator.uniqueOrderId();
            Assert.assertTrue(orderId.startsWith("ORD-"), "Order ID should start with ORD-: " + orderId);
            Assert.assertTrue(generated.add(orderId), "Duplicate order ID generated: " + orderId);
        }
    }

    @Test(description = "Utils | TestDataGenerator builds unique emails and references")
    public void verifyUniqueEmailAndReference() {
        String emailOne = TestDataGenerator.uniqueEmail("driver.qa");
        String emailTwo = TestDataGenerator.uniqueEmail("driver.qa");
        Assert.assertNotEquals(emailOne, emailTwo);
        Assert.assertTrue(emailOne.contains("driver.qa+"));
        Assert.assertTrue(emailOne.endsWith("@knoxweb.us"));

        String referenceOne = TestDataGenerator.uniqueReference("dispatch");
        String referenceTwo = TestDataGenerator.uniqueReference("dispatch");
        Assert.assertNotEquals(referenceOne, referenceTwo);
        Assert.assertTrue(referenceOne.startsWith("DISPATCH-"));
    }

    @Test(description = "Utils | RealWorldTestData loads multiple USA datasets")
    public void verifyRealWorldTestDataCatalog() {
        RealWorldTestData.ProfileUser user = RealWorldTestData.defaultProfileUser();
        Assert.assertFalse(user.email().isBlank());
        Assert.assertFalse(user.firstName().isBlank());
        Assert.assertEquals(user.loyaltyTier(), "Pizza Cutter");

        Assert.assertTrue(RealWorldTestData.addressIds().size() >= 4,
                "Expected at least 4 real-world USA addresses");
        RealWorldTestData.UsAddress orlando = RealWorldTestData.address(1);
        Assert.assertEquals(orlando.city(), "Orlando");
        Assert.assertFalse(orlando.streetWithUniqueUnit().equals(orlando.street()));

        Assert.assertTrue(RealWorldTestData.paymentCardIds().size() >= 3,
                "Expected at least 3 NMI sandbox cards");
        Assert.assertEquals(RealWorldTestData.defaultPaymentCard().brand(), "Visa");

        Assert.assertEquals(RealWorldTestData.allLoyaltyTiers().size(), 5);
        Assert.assertEquals(RealWorldTestData.invalidPaymentZip(), "12");
        Assert.assertEquals(RealWorldTestData.invalidPhone(), "123");
        Assert.assertEquals(RealWorldTestData.invalidAddressZip(), "ABCDE");

        Assert.assertEquals(RealWorldTestData.nextAddress().id(),
                RealWorldTestData.address(1).id());
    }
}
