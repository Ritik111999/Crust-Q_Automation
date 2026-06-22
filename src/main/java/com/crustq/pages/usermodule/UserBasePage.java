package com.crustq.pages.usermodule;

import com.crustq.config.ApplicationRole;
import com.crustq.pages.BasePage;
import org.openqa.selenium.WebDriver;

/**
 * Base Page Object for User (Customer) role screens.
 */
public abstract class UserBasePage extends BasePage {

    protected static final ApplicationRole ROLE = ApplicationRole.USER;

    protected UserBasePage(WebDriver driver) {
        super(driver);
    }

    protected void openUserPath(String path) {
        navigateWithRetry(ROLE, path);
    }
}
