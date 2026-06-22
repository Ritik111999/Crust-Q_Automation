package com.crustq.utils;

import com.crustq.config.ConfigReader;
import org.openqa.selenium.By;

/**
 * Interaction helpers for Flutter web semantics (CanvasKit PWA).
 */
public final class FlutterSemanticsHelper {

    private FlutterSemanticsHelper() {
    }

    public static void enableAccessibility(WebElementUtils utils) {
        utils.waitForPresence(By.cssSelector("flutter-view"));
        int maxAttempts = ConfigReader.getInt("navigation.retry.count", 2) + 1;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                utils.waitForPresence(By.cssSelector("flt-semantics-placeholder"));
                utils.executeScript("document.querySelector('flt-semantics-placeholder')?.click();");
                utils.waitForVisibility(By.cssSelector("input[aria-label='Enter email address']"));
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
}
