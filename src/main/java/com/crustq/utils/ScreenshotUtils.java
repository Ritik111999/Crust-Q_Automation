package com.crustq.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Captures WebDriver screenshots and returns the saved file path.
 */
public final class ScreenshotUtils {

    private ScreenshotUtils() {
    }

    public static String captureScreenshot(WebDriver driver, String testName) {
        if (!(driver instanceof TakesScreenshot takesScreenshot)) {
            return null;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String screenshotDir = System.getProperty("user.dir") + File.separator + "test-output" + File.separator + "screenshots";
        new File(screenshotDir).mkdirs();

        String filePath = screenshotDir + File.separator + sanitizedTestName + "_" + timestamp + ".png";
        File screenshotFile = takesScreenshot.getScreenshotAs(OutputType.FILE);

        try {
            FileUtils.copyFile(screenshotFile, new File(filePath));
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save screenshot: " + filePath, e);
        }
    }
}
