package com.crustq.factory;

import com.crustq.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates isolated Selenium WebDriver instances for PWA / Web execution.
 * <p>
 * Admin prerequisite flows should call {@link #createChromeDriver(boolean)} with
 * {@code isHeadless = true} to avoid rendering overhead and shorten setup time.
 */
public final class WebDriverFactory {

    private static final String MAC_CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
    private static final boolean IS_MAC = System.getProperty("os.name", "").toLowerCase().contains("mac");

    private WebDriverFactory() {
    }

    public static WebDriver createDriver() {
        ConfigReader.init();
        String browser = ConfigReader.get("browser", "chrome").trim().toLowerCase();
        if (!"chrome".equals(browser)) {
            throw new IllegalArgumentException("Unsupported browser for PWA execution: " + browser);
        }
        return createChromeDriver(ConfigReader.getBoolean("chrome.headless", false));
    }

    /**
     * Initializes a Selenium {@link ChromeDriver} with explicit headless control.
     *
     * @param isHeadless {@code true} for headless Chrome (recommended for fast Admin setup prerequisites);
     *                   {@code false} for headed execution and interactive debugging
     */
    public static WebDriver createChromeDriver(boolean isHeadless) {
        return new ChromeDriver(buildChromeOptions(isHeadless));
    }

    public static WebDriver createChromeDriver() {
        ConfigReader.init();
        return createChromeDriver(ConfigReader.getBoolean("chrome.headless", false));
    }

    private static ChromeOptions buildChromeOptions(boolean isHeadless) {
        ChromeOptions options = new ChromeOptions();
        List<String> arguments = new ArrayList<>();
        arguments.add("--disable-notifications");
        arguments.add("--remote-allow-origins=*");
        arguments.add("--disable-popup-blocking");
        arguments.add("--no-sandbox");
        arguments.add("--disable-dev-shm-usage");

        if (isHeadless) {
            arguments.add("--headless=new");
            arguments.add("--window-size=1920,1080");
            arguments.add("--disable-gpu");
        }

        options.addArguments(arguments);

        if (IS_MAC && new File(MAC_CHROME).exists()) {
            options.setBinary(MAC_CHROME);
        }

        return options;
    }
}
