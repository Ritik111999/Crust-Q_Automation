package com.crustq.factory;

import com.crustq.config.ConfigReader;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import java.time.Duration;

/**
 * Manages a local Appium server for mobile test execution.
 * Auto-start is opt-in via {@code appium.server.auto.start=true}.
 */
public final class AppiumServerManager {

    private static AppiumDriverLocalService localService;

    private AppiumServerManager() {
    }

    public static synchronized void startIfRequired() {
        ConfigReader.init();
        if (!ConfigReader.getBoolean("appium.server.auto.start", false)) {
            return;
        }
        if (localService != null && localService.isRunning()) {
            return;
        }

        String host = ConfigReader.get("appium.server.host", "127.0.0.1");
        int port = ConfigReader.getInt("appium.server.port", 4723);
        Duration startupTimeout = Duration.ofSeconds(
                ConfigReader.getInt("appium.server.startup.timeout.seconds", 60));

        AppiumServiceBuilder builder = new AppiumServiceBuilder()
                .withIPAddress(host)
                .usingPort(port)
                .withTimeout(startupTimeout);

        localService = builder.build();
        localService.start();

        if (!localService.isRunning()) {
            throw new IllegalStateException("Failed to start local Appium server on " + host + ":" + port);
        }
    }

    public static synchronized void stopIfManaged() {
        if (!ConfigReader.getBoolean("appium.server.auto.stop", true)) {
            return;
        }
        if (localService != null && localService.isRunning()) {
            localService.stop();
        }
        localService = null;
    }

    public static boolean isManagedServerRunning() {
        return localService != null && localService.isRunning();
    }

    public static String getServerUrl() {
        if (localService != null && localService.isRunning()) {
            return localService.getUrl().toString();
        }
        return ConfigReader.getAppiumServerUrl();
    }
}
