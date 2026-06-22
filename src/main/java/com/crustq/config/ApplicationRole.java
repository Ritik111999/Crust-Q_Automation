package com.crustq.config;

/**
 * Identifies which Crust & Q PWA role surface a page object targets.
 */
public enum ApplicationRole {

    USER("user.base.url", "01", "01 - User (Customer)"),
    ADMIN("admin.base.url", "02", "02 - Admin (Dispatcher)"),
    DRIVER("driver.base.url", "03", "03 - Driver (Delivery Agent)");

    private final String baseUrlKey;
    private final String moduleCode;
    private final String moduleName;

    ApplicationRole(String baseUrlKey, String moduleCode, String moduleName) {
        this.baseUrlKey = baseUrlKey;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
    }

    public String getBaseUrlKey() {
        return baseUrlKey;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getBaseUrl() {
        return ConfigReader.get(baseUrlKey);
    }
}
