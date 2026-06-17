package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.WaitUtils;

public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebElement waitForVisible(By locator) {
        return WaitUtils.waitForVisibility(driver, locator);
    }

    protected WebElement waitForClickable(By locator) {
        return WaitUtils.waitForClickability(driver, locator);
    }

    protected boolean waitForInvisible(By locator) {
        return WaitUtils.waitForInvisibility(driver, locator);
    }
}
