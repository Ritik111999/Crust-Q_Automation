package com.crustq.utils;

import com.crustq.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates isolated Google Chrome instances for parallel TestNG execution.
 */
public final class BrowserFactory {

    private static final String MAC_CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
    private static final boolean IS_MAC = System.getProperty("os.name", "").toLowerCase().contains("mac");

    private BrowserFactory() {
    }

    public static WebDriver createChromeDriver() {
        return new ChromeDriver(buildChromeOptions());
    }

    private static ChromeOptions buildChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        List<String> arguments = new ArrayList<>();
        arguments.add("--disable-notifications");
        arguments.add("--remote-allow-origins=*");
        arguments.add("--disable-popup-blocking");
        arguments.add("--no-sandbox");
        arguments.add("--disable-dev-shm-usage");

        if (ConfigReader.getBoolean("chrome.headless", false)) {
            arguments.add("--headless=new");
            arguments.add("--window-size=1920,1080");
        }

        options.addArguments(arguments);

        if (IS_MAC && new File(MAC_CHROME).exists()) {
            options.setBinary(MAC_CHROME);
        }

        return options;
    }
}
