package com.crustq.pages.pwa.admin;

import com.crustq.config.ApplicationRole;
import com.crustq.pages.BasePage;
import org.openqa.selenium.WebDriver;

/**
 * Base Page Object for Admin (Dispatcher) PWA screens.
 */
public abstract class AdminBasePage extends BasePage {

    protected static final ApplicationRole ROLE = ApplicationRole.ADMIN;

    protected AdminBasePage(WebDriver driver) {
        super(driver);
    }

    protected void openAdminPath(String path) {
        navigateWithRetry(ROLE, path);
    }
}
