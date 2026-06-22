package com.crustq.tests.infrastructure;

import com.crustq.base.BaseTest;
import com.crustq.config.PlatformType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies platform-aware {@link BaseTest} lifecycle for PWA execution.
 */
public class BaseLifecycleTest extends BaseTest {

    @Test(description = "BaseTest | PWA | WebDriver initializes when platform.active=pwa")
    public void verifyPwaWebDriverLifecycle() {
        Assert.assertEquals(getActivePlatform(), PlatformType.PWA);
        Assert.assertNotNull(getDriver());
        Assert.assertNotNull(getUtils());
    }
}
