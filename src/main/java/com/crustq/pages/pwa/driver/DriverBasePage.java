package com.crustq.pages.pwa.driver;

import com.crustq.config.ApplicationRole;
import com.crustq.pages.BasePage;
import org.openqa.selenium.WebDriver;

/**
 * Base Page Object for Driver (Delivery Agent) PWA screens.
 */
public abstract class DriverBasePage extends BasePage {

    protected static final ApplicationRole ROLE = ApplicationRole.DRIVER;

    protected DriverBasePage(WebDriver driver) {
        super(driver);
    }

    protected void openDriverPath(String path) {
        navigateWithRetry(ROLE, path);
    }
}
