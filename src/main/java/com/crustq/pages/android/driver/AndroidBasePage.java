package com.crustq.pages.android.driver;

import com.crustq.config.ConfigReader;
import com.crustq.utils.MobileGesturesUtil;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

/**
 * Base Page Object for Android native screens.
 * Initializes {@link PageFactory} with {@code @AndroidFindBy} locators in subclasses.
 */
public abstract class AndroidBasePage {

    protected final AppiumDriver driver;
    protected final MobileGesturesUtil gestures;

    protected AndroidBasePage(AppiumDriver driver) {
        this.driver = driver;
        this.gestures = new MobileGesturesUtil(driver);
        int waitSeconds = ConfigReader.getInt("explicit.wait.seconds", 20);
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(waitSeconds)), this);
    }

    protected AndroidBasePage(AppiumDriver driver, MobileGesturesUtil gestures) {
        this.driver = driver;
        this.gestures = gestures;
        int waitSeconds = ConfigReader.getInt("explicit.wait.seconds", 20);
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(waitSeconds)), this);
    }

    protected By byAccessibilityId(String id) {
        return gestures.accessibilityId(id);
    }

    protected By byAndroidId(String resourceId) {
        return By.id(resourceId);
    }
}
