package com.crustq.pages.pwa;

import com.crustq.config.ApplicationRole;
import com.crustq.pages.BasePage;
import com.crustq.utils.FlutterSemanticsHelper;
import com.crustq.utils.WebActionsUtil;
import org.openqa.selenium.WebDriver;

/**
 * Base Page Object for Flutter CanvasKit PWA screens.
 */
public abstract class PwaBasePage extends BasePage {

    protected PwaBasePage(WebDriver driver) {
        super(driver);
    }

    protected void openPwaPath(ApplicationRole role, String path) {
        navigateWithRetry(role, path);
    }

    protected void prepareFlutterSemantics() {
        FlutterSemanticsHelper.enableAccessibility(utils);
    }

    protected void clickSemanticButton(String label) {
        FlutterSemanticsHelper.clickSemanticButton(utils, label);
    }

    protected boolean isSemanticButtonEnabled(String label) {
        return FlutterSemanticsHelper.isSemanticButtonEnabled(utils, label);
    }

    protected WebActionsUtil web() {
        return webActions;
    }
}
