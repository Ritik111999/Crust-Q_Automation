package com.crustq.utils;

import com.crustq.factory.WebDriverFactory;
import org.openqa.selenium.WebDriver;

/**
 * @deprecated Use {@link WebDriverFactory} from the {@code factory} package.
 */
@Deprecated
public final class BrowserFactory {

    private BrowserFactory() {
    }

    @Deprecated
    public static WebDriver createChromeDriver() {
        return WebDriverFactory.createChromeDriver();
    }
}
