package com.crustq.pages;

import com.crustq.config.ApplicationRole;
import com.crustq.config.ConfigReader;
import com.crustq.utils.WebElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Abstract base for all Page Object classes.
 * Locators (By) and actions stay inside page classes — never in test classes.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebElementUtils utils;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.utils = new WebElementUtils(driver);
    }

    protected WebElement waitForVisible(By locator) {
        return utils.waitForVisibility(locator);
    }

    protected WebElement waitForClickable(By locator) {
        return utils.waitForClickability(locator);
    }

    protected boolean waitForInvisible(By locator) {
        return utils.waitForInvisibility(locator);
    }

    protected void navigateWithRetry(ApplicationRole role, String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String url = role.getBaseUrl() + normalizedPath;
        int maxAttempts = ConfigReader.getInt("navigation.retry.count", 2);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                driver.get(url);
                return;
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
            }
        }
    }

    protected By orderRowLocator(String orderId) {
        return By.xpath("//tr[contains(., '" + orderId + "')]");
    }

    protected By orderRowButtonLocator(String orderId, String buttonLabel) {
        return By.xpath("//tr[contains(., '" + orderId + "')]//button[contains(normalize-space(), '" + buttonLabel + "')]");
    }
}
