package com.crustq.pages.drivermodule;

import com.crustq.config.ApplicationRole;
import com.crustq.pages.BasePage;
import org.openqa.selenium.WebDriver;

/**
 * Base Page Object for Driver (Delivery Agent) role screens.
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
