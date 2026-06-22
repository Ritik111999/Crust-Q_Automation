package com.crustq.utils;

import com.crustq.config.ConfigReader;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable touch gestures and mobile element waits for Appium sessions.
 * Uses explicit waits and W3C actions — no Thread.sleep().
 */
public class MobileGesturesUtil {

    private final AppiumDriver driver;
    private final WebDriverWait wait;

    public MobileGesturesUtil(AppiumDriver driver) {
        this.driver = driver;
        int explicitWaitSeconds = ConfigReader.getInt("explicit.wait.seconds", 20);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
    }

    public AppiumDriver getDriver() {
        return driver;
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public boolean isDisplayed(By locator) {
        try {
            return waitForVisible(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void tap(By locator) {
        WebElement element = waitForClickable(locator);
        tap(element);
    }

    public void tap(WebElement element) {
        Point center = centerOf(element);
        performTap(center.getX(), center.getY());
    }

    public void tapAt(int x, int y) {
        performTap(x, y);
    }

    public void longPress(By locator) {
        WebElement element = waitForVisible(locator);
        Point center = centerOf(element);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longPress = new Sequence(finger, 1);
        longPress.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), center.getX(), center.getY()));
        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(new Pause(finger, Duration.ofMillis(900)));
        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(longPress));
    }

    public void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        if (text != null && !text.isEmpty()) {
            element.sendKeys(text);
        }
    }

    public String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    public void hideKeyboard() {
        try {
            driver.executeScript("mobile: hideKeyboard", Collections.emptyMap());
        } catch (Exception ignored) {
            // Keyboard may not be visible
        }
    }

    public void swipeUp() {
        swipe("up");
    }

    public void swipeDown() {
        swipe("down");
    }

    public void swipeLeft() {
        swipe("left");
    }

    public void swipeRight() {
        swipe("right");
    }

    public void scrollToText(String text) {
        driver.findElement(AppiumBy.androidUIAutomator(
                "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().textContains(\""
                        + text.replace("\"", "\\\"") + "\"));"));
    }

    public void scrollToElement(By locator) {
        WebElement element = waitForPresence(locator);
        Map<String, Object> params = new HashMap<>();
        params.put("elementId", ((org.openqa.selenium.remote.RemoteWebElement) element).getId());
        params.put("direction", "down");
        driver.executeScript("mobile: scrollGesture", params);
    }

    public By accessibilityId(String id) {
        return AppiumBy.accessibilityId(id);
    }

    private void swipe(String direction) {
        Dimension size = driver.manage().window().getSize();
        int left = (int) (size.width * 0.1);
        int top = (int) (size.height * 0.2);
        int width = (int) (size.width * 0.8);
        int height = (int) (size.height * 0.6);

        Map<String, Object> params = new HashMap<>();
        params.put("left", left);
        params.put("top", top);
        params.put("width", width);
        params.put("height", height);
        params.put("direction", direction);
        params.put("percent", 0.75);
        driver.executeScript("mobile: swipeGesture", params);
    }

    private void performTap(int x, int y) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(tap));
    }

    private Point centerOf(WebElement element) {
        Point location = element.getLocation();
        Dimension size = element.getSize();
        return new Point(location.getX() + size.width / 2, location.getY() + size.height / 2);
    }
}
