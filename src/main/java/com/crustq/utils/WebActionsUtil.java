package com.crustq.utils;

import com.crustq.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Lightweight Selenium action helper focused on stable explicit waits.
 * <p>
 * Admin prerequisite flows rely on {@link #waitForElementToBeClickable(By)} to
 * synchronize on DOM readiness before every interaction — no implicit waits, no sleeps.
 */
public class WebActionsUtil {

    private final WebDriver driver;
    private final FluentWait<WebDriver> wait;

    public WebActionsUtil(WebDriver driver) {
        this.driver = driver;
        int explicitWaitSeconds = ConfigReader.getInt("explicit.wait.seconds", 20);
        this.wait = buildRobustWait(driver, explicitWaitSeconds);
    }

    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Bulletproof clickable wait: re-resolves the locator on every poll and tolerates
     * transient stale/intercepted states common in SPAs during Admin setup.
     */
    public WebElement waitForElementToBeClickable(By locator) {
        return wait.until(clickableIgnoringTransientFailures(locator));
    }

    /**
     * Waits until a PageFactory-managed element is visible, enabled, and clickable.
     */
    public WebElement waitForElementToBeClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public boolean waitForElementInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public void waitForTextInElement(By locator, String textFragment) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, textFragment));
    }

    public void click(By locator) {
        waitForElementToBeClickable(locator).click();
    }

    public void click(WebElement element) {
        waitForElementToBeClickable(element).click();
    }

    public void type(By locator, String text) {
        WebElement element = waitForElementVisible(locator);
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    public void openUrl(String url) {
        driver.get(url);
    }

    public void refreshPage() {
        driver.navigate().refresh();
    }

    public void navigateBack() {
        driver.navigate().back();
    }

    public void navigateForward() {
        driver.navigate().forward();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }

    public void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    public void maximizeWindow() {
        driver.manage().window().maximize();
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    public void switchToFrame(int index) {
        driver.switchTo().frame(index);
    }

    public void switchToFrame(By locator) {
        WebElement frame = waitForElementVisible(locator);
        driver.switchTo().frame(frame);
    }

    public void switchToWindow(String windowHandle) {
        driver.switchTo().window(windowHandle);
    }

    public void switchToWindowContainingUrl(String urlFragment) {
        String targetHandle = findWindowHandleContaining(urlFragment);
        if (targetHandle == null) {
            throw new IllegalStateException("No window found containing URL fragment: " + urlFragment);
        }
        driver.switchTo().window(targetHandle);
    }

    public void switchToNewestWindow() {
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        if (handles.isEmpty()) {
            throw new IllegalStateException("No browser windows available");
        }
        driver.switchTo().window(handles.get(handles.size() - 1));
    }

    public void closeCurrentWindowAndSwitchTo(String windowHandle) {
        driver.close();
        driver.switchTo().window(windowHandle);
    }

    public void uploadFile(By fileInputLocator, String absoluteFilePath) {
        WebElement input = waitForElementPresence(fileInputLocator);
        input.sendKeys(absoluteFilePath);
    }

    private static FluentWait<WebDriver> buildRobustWait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class);
    }

    private static ExpectedCondition<WebElement> clickableIgnoringTransientFailures(By locator) {
        return driver -> {
            try {
                WebElement element = ExpectedConditions.elementToBeClickable(locator).apply(driver);
                if (element == null) {
                    return null;
                }
                return element.isDisplayed() && element.isEnabled() ? element : null;
            } catch (StaleElementReferenceException | ElementClickInterceptedException ignored) {
                return null;
            }
        };
    }

    private String findWindowHandleContaining(String urlFragment) {
        String currentHandle = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            driver.switchTo().window(handle);
            if (driver.getCurrentUrl().contains(urlFragment)) {
                return handle;
            }
        }
        driver.switchTo().window(currentHandle);
        return null;
    }
}
