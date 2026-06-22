package com.crustq.tests.infrastructure;

import com.crustq.config.PlatformType;
import com.crustq.factory.AppiumServerManager;
import com.crustq.factory.CapabilitiesLoader;
import org.openqa.selenium.MutableCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies factory wiring without launching a mobile session.
 */
public class FactoryWiringTest {

    @Test(description = "Factory | Capabilities JSON loads and resolves config placeholders for Android")
    public void verifyAndroidCapabilitiesLoad() {
        MutableCapabilities capabilities = CapabilitiesLoader.loadMobileCapabilities(PlatformType.ANDROID);
        Assert.assertEquals(String.valueOf(capabilities.getCapability("platformName")).toLowerCase(), "android");
        Assert.assertEquals(capabilities.getCapability("appium:automationName"), "UiAutomator2");
    }

    @Test(description = "Factory | Appium server URL resolves from config when auto-start is disabled")
    public void verifyAppiumServerUrlResolution() {
        String url = AppiumServerManager.getServerUrl();
        Assert.assertTrue(url.startsWith("http://"), "Appium URL should be HTTP: " + url);
        Assert.assertTrue(url.contains(":4723"), "Appium URL should include default port: " + url);
    }
}
