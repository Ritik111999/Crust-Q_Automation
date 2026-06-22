package com.crustq.tests.infrastructure;

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
}
