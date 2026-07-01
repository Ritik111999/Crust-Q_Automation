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

    public static boolean selectFirstSavedAddressCard(WebElementUtils utils) {
        Object result = utils.executeScript("""
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const sectionIndex = nodes.findIndex(n =>
              (n.textContent || '').includes('Select Saved Address'));
            const scoped = sectionIndex >= 0 ? nodes.slice(sectionIndex) : nodes;
            const checkbox = scoped.find(n =>
              n.getAttribute('role') === 'checkbox' || n.hasAttribute('aria-checked'));
            if (!checkbox) return false;
            let target = checkbox;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || checkbox).scrollIntoView({block: 'center'});
            (target || checkbox).click();
            return true;
            """);
        return Boolean.TRUE.equals(result);
    }

    public static boolean hasSavedAddressCards(WebElementUtils utils) {
        Object result = utils.executeScript("""
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const sectionIndex = nodes.findIndex(n =>
              (n.textContent || '').includes('Select Saved Address'));
            const scoped = sectionIndex >= 0 ? nodes.slice(sectionIndex) : nodes;
            return scoped.some(n =>
              n.getAttribute('role') === 'checkbox' || n.hasAttribute('aria-checked'));
            """);
        return Boolean.TRUE.equals(result);
    }

    public static boolean selectSavedAddressCard(WebElementUtils utils, String shortLabel) {
        Object result = utils.executeScript("""
            const shortLabel = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const sectionIndex = nodes.findIndex(n =>
              (n.textContent || '').includes('Select Saved Address'));
            const scoped = sectionIndex >= 0 ? nodes.slice(sectionIndex) : nodes;
            const labelNode = scoped.find(n => {
              const text = (n.textContent || '').trim();
              return text === shortLabel
                || text.startsWith(shortLabel + '\\n')
                || (text.includes(shortLabel) && text.length < shortLabel.length + 80);
            });
            if (!labelNode) return false;
            let card = labelNode;
            for (let depth = 0; depth < 12 && card; depth++) {
              const checkbox = Array.from(card.querySelectorAll('flt-semantics'))
                .find(n => n.getAttribute('role') === 'checkbox'
                  || n.hasAttribute('aria-checked'));
              if (checkbox) {
                let target = checkbox;
                while (target && !target.hasAttribute('flt-tappable')) {
                  target = target.parentElement;
                }
                (target || checkbox).scrollIntoView({block: 'center'});
                (target || checkbox).click();
                return checkbox.getAttribute('aria-checked') === 'true' || true;
              }
              card = card.parentElement;
            }
            let target = labelNode;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || labelNode).scrollIntoView({block: 'center'});
            (target || labelNode).click();
            return true;
            """, shortLabel);
        return Boolean.TRUE.equals(result);
    }

    public static boolean isSavedAddressVisible(WebElementUtils utils, String shortLabel) {
        return utils.executeScript("""
            const shortLabel = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const sectionIndex = nodes.findIndex(n =>
              (n.textContent || '').includes('Select Saved Address'));
            const scoped = sectionIndex >= 0 ? nodes.slice(sectionIndex) : nodes;
            return scoped.some(n => (n.textContent || '').includes(shortLabel));
            """, shortLabel) == Boolean.TRUE;
    }

    public static void forceClickSemanticContaining(WebElementUtils utils, String partialLabel) {
        Object result = utils.executeScript("""
            const partial = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const matches = nodes.filter(n => (n.textContent || '').includes(partial));
            const match = matches.length ? matches[matches.length - 1] : null;
            if (!match) throw new Error('Semantic node not found containing: ' + partial);
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            const clickTarget = target || match;
            clickTarget.scrollIntoView({block: 'center'});
            clickTarget.click();
            clickTarget.dispatchEvent(new MouseEvent('click', {bubbles: true, cancelable: true, view: window}));
            return true;
            """, partialLabel);
        if (result == null) {
            throw new IllegalStateException("Failed to force-click semantic text containing: " + partialLabel);
        }
    }

    public static void clickSemanticButtonContaining(WebElementUtils utils, String partialLabel) {
        Object result = utils.executeScript("""
            const partial = arguments[0];
            const buttons = Array.from(document.querySelectorAll('flt-semantics[role="button"]'));
            const match = buttons.find(b => (b.textContent || '').includes(partial));
            if (!match) throw new Error('Button not found containing: ' + partial);
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || match).click();
            return true;
            """, partialLabel);
        if (result == null) {
            throw new IllegalStateException("Failed to click semantic button containing: " + partialLabel);
        }
    }

    public static void clickAddForMenuItem(WebElementUtils utils, String itemName) {
        Object result = utils.executeScript("""
            const itemName = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const itemNode = nodes.find(n => {
              const text = (n.textContent || '').trim();
              return text === itemName || text.startsWith(itemName + '\\n') || text.startsWith(itemName);
            });
            if (!itemNode) throw new Error('Menu item not found: ' + itemName);
            let card = itemNode;
            for (let depth = 0; depth < 12 && card; depth++) {
              const addBtn = Array.from(card.querySelectorAll('flt-semantics[role="button"]'))
                .find(b => (b.textContent || '').trim() === 'Add');
              if (addBtn) {
                let target = addBtn;
                while (target && !target.hasAttribute('flt-tappable')) {
                  target = target.parentElement;
                }
                (target || addBtn).click();
                return true;
              }
              card = card.parentElement;
            }
            throw new Error('Add button not found for menu item: ' + itemName);
            """, itemName);
        if (result == null) {
            throw new IllegalStateException("Failed to click Add for menu item: " + itemName);
        }
    }

    public static void clickViewAllForSection(WebElementUtils utils, String sectionHeading) {
        Object result = utils.executeScript("""
            const heading = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const headingNode = nodes.find(n => (n.textContent || '').trim() === heading);
            if (!headingNode) throw new Error('Section heading not found: ' + heading);
            let container = headingNode.parentElement;
            for (let depth = 0; depth < 8 && container; depth++) {
              const viewAll = Array.from(container.querySelectorAll('flt-semantics'))
                .find(n => (n.textContent || '').trim() === 'View All');
              if (viewAll) {
                let target = viewAll;
                while (target && !target.hasAttribute('flt-tappable')) {
                  target = target.parentElement;
                }
                (target || viewAll).click();
                return true;
              }
              container = container.parentElement;
            }
            throw new Error('View All not found for section: ' + heading);
            """, sectionHeading);
        if (result == null) {
            throw new IllegalStateException("Failed to click View All for section: " + sectionHeading);
        }
    }

    public static void fillFlutterInput(WebElementUtils utils, String ariaLabelFragment, String text) {
        By locator = By.cssSelector(
                "input[aria-label*='" + ariaLabelFragment + "'], textarea[aria-label*='" + ariaLabelFragment + "']");
        utils.sendKeys(locator, text);
    }

    public static void typeIntoActiveFlutterEditor(WebElementUtils utils, String text) {
        Object filled = utils.executeScript("""
            const text = arguments[0];
            const host = document.querySelector('flt-text-editing-host');
            const input = host ? host.querySelector('input, textarea') : null;
            if (!input) return false;
            input.focus();
            input.value = text;
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
            return true;
            """, text);
        if (!Boolean.TRUE.equals(filled)) {
            By editor = By.cssSelector("flt-text-editing-host input, flt-text-editing-host textarea");
            utils.sendKeys(editor, text);
        }
    }

    public static void scrollToSemanticText(WebElementUtils utils, String text) {
        utils.executeScript("""
            const text = arguments[0];
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const match = nodes.find(n => n.textContent && n.textContent.includes(text));
            if (match) {
              match.scrollIntoView({block: 'center'});
            }
            """, text);
    }

    public static void fillMultilineField(WebElementUtils utils, String text, String... hints) {
        for (String hint : hints) {
            scrollToSemanticText(utils, hint);
        }

        for (String hint : hints) {
            Object filled = utils.executeScript("""
                const hint = arguments[0];
                const text = arguments[1];
                const inputs = Array.from(document.querySelectorAll('input[aria-label], textarea[aria-label]'));
                const match = inputs.find(i => (i.getAttribute('aria-label') || '').includes(hint));
                if (!match) return false;
                match.focus();
                match.value = text;
                match.dispatchEvent(new Event('input', { bubbles: true }));
                match.dispatchEvent(new Event('change', { bubbles: true }));
                return true;
                """, hint, text);
            if (Boolean.TRUE.equals(filled)) {
                return;
            }
        }

        Object focused = utils.executeScript("""
            const hints = Array.prototype.slice.call(arguments);
            const nodes = Array.from(document.querySelectorAll('flt-semantics'));
            const lowered = hints.map(h => String(h).toLowerCase());
            let match = nodes.find(n => {
              const label = (n.getAttribute('aria-label') || '').toLowerCase();
              const content = (n.textContent || '').toLowerCase();
              return lowered.some(h => label.includes(h) || content.includes(h));
            });
            if (!match) {
              match = nodes.find(n => n.getAttribute('role') === 'textbox');
            }
            if (!match) return false;
            let target = match;
            while (target && !target.hasAttribute('flt-tappable')) {
              target = target.parentElement;
            }
            (target || match).scrollIntoView({block: 'center'});
            (target || match).click();
            return true;
            """, (Object[]) hints);

        if (Boolean.TRUE.equals(focused)) {
            new Actions(utils.getDriver()).sendKeys(text).perform();
            return;
        }

        for (String hint : hints) {
            if (clickSemanticTextIfPresent(utils, hint)) {
                new Actions(utils.getDriver()).sendKeys(text).perform();
                return;
            }
        }

        throw new IllegalStateException("Could not fill field using hints: " + String.join(", ", hints));
    }

    public static String readActiveFlutterEditorValue(WebElementUtils utils) {
        Object value = utils.executeScript("""
            const host = document.querySelector('flt-text-editing-host');
            if (host) {
              const input = host.querySelector('input, textarea');
              if (input && input.value) return input.value;
            }
            const inputs = Array.from(document.querySelectorAll('input[aria-label], textarea[aria-label]'));
            const match = inputs.find(i => i.value && i.value.length > 0);
            return match ? match.value : '';
            """);
        return value == null ? "" : String.valueOf(value);
    }

    public static String readInputValueByAriaLabel(WebElementUtils utils, String ariaLabelFragment) {
        Object value = utils.executeScript("""
            const fragment = arguments[0].toLowerCase();
            const inputs = Array.from(document.querySelectorAll('input[aria-label], textarea[aria-label]'));
            const match = inputs.find(i => (i.getAttribute('aria-label') || '').toLowerCase().includes(fragment));
            return match ? (match.value || '') : '';
            """, ariaLabelFragment);
        return value == null ? "" : String.valueOf(value);
    }

    public static double parseDollarAmount(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        String normalized = text.replaceAll("[^0-9.]", "");
        if (normalized.isBlank()) {
            return 0.0;
        }
        return Double.parseDouble(normalized);
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

    public static void waitForAnySemanticText(WebElementUtils utils, int timeoutSeconds, String... texts) {
        WebDriverWait wait = new WebDriverWait(utils.getDriver(), Duration.ofSeconds(timeoutSeconds));
        wait.until(driver -> {
            for (String text : texts) {
                if (isSemanticTextVisible(utils, text)) {
                    return true;
                }
            }
            return false;
        });
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
