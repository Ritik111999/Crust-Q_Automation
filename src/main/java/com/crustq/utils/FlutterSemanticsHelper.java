package com.crustq.utils;

import com.crustq.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Interaction helpers for Flutter web semantics (CanvasKit PWA).
 */
public final class FlutterSemanticsHelper {

    private FlutterSemanticsHelper() {
    }

    public static void enableAccessibility(WebElementUtils utils) {
        enableAccessibilityForFlutterPage(utils, "input[aria-label='Enter email address']");
    }

    /**
     * Enables Flutter semantics on any PWA screen (dashboard, profile, etc.).
     * After the placeholder is activated, the tree exposes interactive semantics nodes.
     */
    public static void enableAccessibilityForFlutterPage(WebElementUtils utils) {
        enableAccessibilityForFlutterPage(utils, "flt-semantics[role='button']");
    }

    private static void enableAccessibilityForFlutterPage(WebElementUtils utils, String readySelector) {
        utils.waitForPresence(By.cssSelector("flutter-view"));
        int maxAttempts = ConfigReader.getInt("navigation.retry.count", 2) + 1;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                utils.waitForPresence(By.cssSelector("flutter-view"));
                if (utils.isDisplayed(By.cssSelector(readySelector))) {
                    return;
                }
                utils.waitForPresence(By.cssSelector("flt-semantics-placeholder"));
                utils.executeScript("document.querySelector('flt-semantics-placeholder')?.click();");
                utils.waitForPresence(By.cssSelector(readySelector));
                return;
            } catch (RuntimeException e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                utils.executeScript("location.reload();");
                utils.waitForPresence(By.cssSelector("flutter-view"));
            }
        }
    }

    public static boolean clickSidebarProfileEntry(WebElementUtils utils, String displayName, String email) {
        Object result = utils.executeScript("""
            const displayName = arguments[0];
            const email = arguments[1];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const match = nodes.find(n => {
              const text = (n.textContent || '').trim();
              if (displayName && text.includes(displayName)) {
                return true;
              }
              return email && text.includes(email) && text.includes('@');
            });
            if (!match) {
              return false;
            }
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || match).click();
            return true;
            """, displayName, email);
        return Boolean.TRUE.equals(result);
    }

    public static boolean clickSemanticTextIfPresent(WebElementUtils utils, String text) {
        Object result = utils.executeScript("""
            const text = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const match = nodes.find(n => n.textContent && n.textContent.includes(text));
            if (!match) return false;
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || match).click();
            return true;
            """, text);
        return Boolean.TRUE.equals(result);
    }

    public static void clickSemanticText(WebElementUtils utils, String text) {
        Object result = utils.executeScript("""
            const text = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const match = nodes.find(n => n.textContent && n.textContent.includes(text));
            if (!match) throw new Error('Semantic text not found: ' + text);
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || match).click();
            return true;
            """, text);
        if (result == null) {
            throw new IllegalStateException("Failed to click semantic text: " + text);
        }
    }

    public static boolean isSemanticTextVisible(WebElementUtils utils, String text) {
        return utils.isDisplayed(By.xpath("//flt-semantics[contains(., '" + text + "')]"));
    }

    public static String readSemanticTextContaining(WebElementUtils utils, String text) {
        Object value = utils.executeScript("""
            const text = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const match = nodes.find(n => n.textContent && n.textContent.includes(text));
            return match ? match.textContent.trim() : '';
            """, text);
        return value == null ? "" : String.valueOf(value);
    }

    public static void clickSemanticButton(WebElementUtils utils, String label) {
        Object result = utils.executeScript("""
            const label = arguments[0];
            const buttons = Array.from(document.querySelectorAll('flt-semantics[role="button"]'));
            const match = buttons.find(b => b.textContent.trim() === label);
            if (!match) throw new Error('Button not found: ' + label);
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || match).click();
            return true;
            """, label);
        if (result == null) {
            throw new IllegalStateException("Failed to click semantic button: " + label);
        }
    }

    public static boolean isSemanticButtonEnabled(WebElementUtils utils, String label) {
        Object enabled = utils.executeScript("""
            const label = arguments[0];
            const buttons = Array.from(document.querySelectorAll('flt-semantics[role="button"]'));
            const match = buttons.find(b => b.textContent.trim() === label);
            if (!match) return false;
            return match.getAttribute('aria-disabled') !== 'true';
            """, label);
        return Boolean.TRUE.equals(enabled);
    }

    public static void clickRememberMe(WebElementUtils utils) {
        Object result = utils.executeScript("""
            const checkbox = document.querySelector('flt-semantics[role="checkbox"]');
            if (!checkbox) throw new Error('Remember Me checkbox not found');
            let target = checkbox;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || checkbox).click();
            return true;
            """);
        if (result == null) {
            throw new IllegalStateException("Failed to click Remember Me checkbox");
        }
    }

    public static boolean isRememberMeChecked(WebElementUtils utils) {
        Object checked = utils.executeScript("""
            const checkbox = document.querySelector('flt-semantics[role="checkbox"]');
            return checkbox != null && checkbox.getAttribute('aria-checked') === 'true';
            """);
        return Boolean.TRUE.equals(checked);
    }

    public static boolean isSignInHeadingVisible(WebElementUtils utils) {
        return utils.isDisplayed(By.xpath("//*[contains(normalize-space(), 'Sign In')]"));
    }

    /**
     * Opens a Flutter dropdown button and selects an exact option from the overlay list.
     * US state picker: button "Select State" → overlay items (Alabama, Alaska, …).
     */
    public static void selectDropdownOption(WebElementUtils utils, String buttonLabel, String optionLabel) {
        clickSemanticButton(utils, buttonLabel);
        waitForSemanticText(utils, "Alabama", 20);

        if (clickExactSemanticOption(utils, optionLabel)) {
            return;
        }

        new Actions(utils.getDriver()).sendKeys(optionLabel).perform();
        if (clickExactSemanticOption(utils, optionLabel)) {
            return;
        }

        Actions actions = new Actions(utils.getDriver());
        for (int i = 0; i < 40; i++) {
            if (clickExactSemanticOption(utils, optionLabel)) {
                return;
            }
            actions.sendKeys(Keys.ARROW_DOWN).perform();
        }

        throw new IllegalStateException("Dropdown option not found: " + optionLabel);
    }

    public static void waitForSemanticText(WebElementUtils utils, String text, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(utils.getDriver(), Duration.ofSeconds(timeoutSeconds));
        wait.until(driver -> isSemanticTextVisible(utils, text));
    }

    private static boolean clickExactSemanticOption(WebElementUtils utils, String optionLabel) {
        Object clicked = utils.executeScript("""
            const optionLabel = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            for (let i = nodes.length - 1; i >= 0; i--) {
              const text = (nodes[i].textContent || '').trim();
              if (text !== optionLabel) {
                continue;
              }
              let target = nodes[i];
              while (target && !target.hasAttribute('flt-tappable')) {
                target = target.parentElement;
              }
              if (!target) {
                continue;
              }
              target.scrollIntoView({block: 'center'});
              target.click();
              return true;
            }
            return false;
            """, optionLabel);
        return Boolean.TRUE.equals(clicked);
    }
}
