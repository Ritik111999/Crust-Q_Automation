package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ConfigReader;

public class FrameworkSmokeTest extends BaseTest {

    @Test(description = "Verifies framework wiring: driver, config, and navigation")
    public void verifyFrameworkWiring() {
        String userBaseUrl = ConfigReader.getProperty("user.base.url", "");
        if (userBaseUrl.isBlank()) {
            Assert.assertNotNull(getDriver(), "WebDriver should be initialized per thread");
            return;
        }

        getDriver().get(userBaseUrl);
        Assert.assertNotNull(getDriver().getTitle());
    }
}
