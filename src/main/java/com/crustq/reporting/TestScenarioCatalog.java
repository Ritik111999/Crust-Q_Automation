package com.crustq.reporting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue of QA scenarios with human-readable steps, preconditions, and expected results.
 * Lookup key format: ClassName#methodName[#scenarioParameter]
 */
public final class TestScenarioCatalog {

    private static final String PWA_AUTH_URL = "https://pwa.crustq.betaplanets.com/auth";

    private static final Map<String, TestScenarioDefinition> BY_KEY = new LinkedHashMap<>();

    static {
        registerFrameworkScenarios();
        registerUserLoginScenarios();
        registerDriverLoginScenarios();
        registerPwaLoginNegativeScenarios();
    }

    private TestScenarioCatalog() {
    }

    public static TestScenarioDefinition lookup(String lookupKey) {
        return BY_KEY.get(lookupKey);
    }

    private static void register(
            String lookupKey,
            String moduleCode,
            String moduleName,
            String scenarioId,
            String title,
            String type,
            String preconditions,
            List<String> steps,
            String expectedResult) {
        BY_KEY.put(lookupKey, new TestScenarioDefinition(
                moduleCode, moduleName, scenarioId, title, type,
                preconditions, steps, expectedResult));
    }

    private static List<String> steps(String... lines) {
        return List.of(lines);
    }

    private static void registerFrameworkScenarios() {
        register(
                "FrameworkSmokeTest#verifyFrameworkWiring",
                "00",
                "00 - Framework",
                "FW-001",
                "Framework wiring smoke check",
                "Positive",
                "Chrome browser available; config.properties on classpath",
                steps(
                        "Launch isolated Chrome via BaseTest",
                        "Verify ThreadLocal WebDriver is bound to the test thread",
                        "Confirm ConfigReader and reporting components initialize"
                ),
                "WebDriver and framework components initialize without error"
        );
    }

    private static void registerUserLoginScenarios() {
        register(
                "UserPwaLoginTest#testPositiveUserLogin_validCredentials_redirectsToDashboard",
                "01",
                "01 - User (Customer)",
                "USER-LOGIN-001",
                "Valid user credentials redirect to customer dashboard",
                "Positive",
                "PWA auth page is reachable; valid user account karlu@knoxweb.us exists",
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Enable Flutter accessibility and wait for Sign In form",
                        "Enter email: karlu@knoxweb.us",
                        "Enter password: sR@123456 (from config)",
                        "Click Sign In",
                        "Wait for URL to contain /dashboard"
                ),
                "User is authenticated and lands on the customer dashboard (/dashboard)"
        );

        register(
                "UserPwaLoginTest#testPositiveUserLogin_rememberMeCanBeSelected",
                "01",
                "01 - User (Customer)",
                "USER-LOGIN-002",
                "Remember Me checkbox can be toggled on auth page",
                "Positive",
                "PWA auth page is reachable at " + PWA_AUTH_URL,
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Enable Flutter accessibility and wait for Sign In form",
                        "Verify Remember Me checkbox is unchecked by default",
                        "Click Remember Me checkbox",
                        "Verify Remember Me checkbox becomes checked",
                        "Verify browser remains on /auth (no navigation)"
                ),
                "Remember Me is checked and user stays on the auth page without signing in"
        );
    }

    private static void registerDriverLoginScenarios() {
        register(
                "DriverPwaLoginTest#testPositiveDriverLogin_validCredentials_redirectsToDriverHome",
                "03",
                "03 - Driver (Delivery Agent)",
                "DRIVER-LOGIN-001",
                "Valid driver credentials redirect to driver home",
                "Positive",
                "PWA auth page is reachable; valid driver account karld@knoxweb.us exists",
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Enable Flutter accessibility and wait for Sign In form",
                        "Enter email: karld@knoxweb.us",
                        "Enter password: sR@123456 (from config)",
                        "Click Sign In",
                        "Wait for URL to contain /driver-home"
                ),
                "Driver is authenticated and lands on driver home (/driver-home)"
        );
    }

    private static void registerPwaLoginNegativeScenarios() {
        register(
                "PwaLoginNegativeTest#testNegativePwaLogin_scenarios#INVALID_CREDENTIALS",
                "01",
                "01 - PWA Auth (User & Driver)",
                "PWA-LOGIN-NEG-001",
                "Invalid email/password combination stays on auth page",
                "Negative",
                "PWA auth page is reachable at " + PWA_AUTH_URL,
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Enter email: karlu@knoxweb.us",
                        "Enter password: Wrong@123456 (incorrect password)",
                        "Click Sign In",
                        "Verify URL still contains /auth",
                        "Verify URL does not contain /dashboard or /driver-home"
                ),
                "Login is rejected; user remains on the auth page and is not redirected"
        );

        register(
                "PwaLoginNegativeTest#testNegativePwaLogin_scenarios#BLANK_EMAIL",
                "01",
                "01 - PWA Auth (User & Driver)",
                "PWA-LOGIN-NEG-002",
                "Blank email with password filled stays on auth page",
                "Negative",
                "PWA auth page is reachable at " + PWA_AUTH_URL,
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Leave email field empty",
                        "Enter password: sR@123456",
                        "Click Sign In",
                        "Verify URL still contains /auth",
                        "Verify URL does not contain /dashboard or /driver-home"
                ),
                "Login is blocked; user remains on the auth page"
        );

        register(
                "PwaLoginNegativeTest#testNegativePwaLogin_scenarios#BLANK_PASSWORD",
                "01",
                "01 - PWA Auth (User & Driver)",
                "PWA-LOGIN-NEG-003",
                "Blank password with email filled stays on auth page",
                "Negative",
                "PWA auth page is reachable at " + PWA_AUTH_URL,
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Enter email: karlu@knoxweb.us",
                        "Leave password field empty",
                        "Click Sign In",
                        "Verify URL still contains /auth",
                        "Verify URL does not contain /dashboard or /driver-home"
                ),
                "Login is blocked; user remains on the auth page"
        );

        register(
                "PwaLoginNegativeTest#testNegativePwaLogin_scenarios#BOTH_FIELDS_BLANK",
                "01",
                "01 - PWA Auth (User & Driver)",
                "PWA-LOGIN-NEG-004",
                "Both email and password blank stays on auth page",
                "Negative",
                "PWA auth page is reachable at " + PWA_AUTH_URL,
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Leave email field empty",
                        "Leave password field empty",
                        "Click Sign In",
                        "Verify URL still contains /auth",
                        "Verify URL does not contain /dashboard or /driver-home"
                ),
                "Login is blocked; user remains on the auth page"
        );

        register(
                "PwaLoginNegativeTest#testNegativePwaLogin_scenarios#WEAK_PASSWORD",
                "01",
                "01 - PWA Auth (User & Driver)",
                "PWA-LOGIN-NEG-005",
                "Weak password shows inline validation hint",
                "Negative",
                "PWA auth page is reachable at " + PWA_AUTH_URL,
                steps(
                        "Open " + PWA_AUTH_URL,
                        "Enter email: karlu@knoxweb.us",
                        "Enter password: weakpass (does not meet complexity rules)",
                        "Click Sign In",
                        "Verify validation hint is displayed: Include a capital letter, number and symbol in your password",
                        "Verify URL still contains /auth"
                ),
                "Password complexity validation message is shown and user stays on auth page"
        );
    }
}
