package com.rioiam.iiq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Environment Configuration Manager
 * Handles environment detection, validation, and provides environment-specific utilities
 * 
 * @author RIOIAM Framework
 */
@Component
@Configuration
public class EnvironmentConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    
    @Autowired
    private Environment springEnvironment;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Value("${environment.name:UNKNOWN}")
    private String environmentName;
    
    @Value("${iiq.properties.location:classpath:iiq.properties}")
    private String iiqPropertiesLocation;
    
    @Value("${iiq.server.url:http://localhost:8080/identityiq}")
    private String iiqServerUrl;
    
    @Value("${iiq.server.username:spadmin}")
    private String iiqUsername;
    
    @Value("${iiq.server.password:admin}")
    private String iiqPassword;
    
    // Environment types
    public enum EnvironmentType {
        DEV("dev", "Development"),
        TEST("test", "Testing"),
        UAT("uat", "User Acceptance Testing"),
        PROD("prod", "Production"),
        LOCAL("local", "Local Development");
        
        private final String profile;
        private final String description;
        
        EnvironmentType(String profile, String description) {
            this.profile = profile;
            this.description = description;
        }
        
        public String getProfile() {
            return profile;
        }
        
        public String getDescription() {
            return description;
        }
        
        public static EnvironmentType fromProfile(String profile) {
            for (EnvironmentType env : values()) {
                if (env.profile.equalsIgnoreCase(profile)) {
                    return env;
                }
            }
            return DEV; // Default to DEV if not found
        }
    }
    
    private Properties iiqProperties;
    private Map<String, String> databaseInfo;
    private boolean initialized = false;
    
    @PostConstruct
    public void init() {
        logger.info("========================================");
        logger.info("Initializing Environment Configuration");
        logger.info("========================================");
        logger.info("Active Profile: {}", activeProfile);
        logger.info("Environment Name: {}", environmentName);
        logger.info("IIQ Server URL: {}", iiqServerUrl);
        logger.info("IIQ Properties Location: {}", iiqPropertiesLocation);
        logger.info("========================================");
        
        // Load IIQ properties
        loadIiqProperties();
        
        // Extract database information
        extractDatabaseInfo();
        
        // Validate environment
        validateEnvironment();
        
        initialized = true;
        
        // Print environment summary
        printEnvironmentSummary();
    }
    
    /**
     * Load IIQ properties file based on environment
     */
    private void loadIiqProperties() {
        iiqProperties = new Properties();
        try {
            InputStream input = null;
            
            if (iiqPropertiesLocation.startsWith("classpath:")) {
                String path = iiqPropertiesLocation.substring("classpath:".length());
                input = getClass().getClassLoader().getResourceAsStream(path);
                if (input == null) {
                    logger.warn("IIQ properties file not found in classpath: {}", path);
                    return;
                }
            } else if (iiqPropertiesLocation.startsWith("file:")) {
                String path = iiqPropertiesLocation.substring("file:".length());
                input = new FileInputStream(new File(path));
            } else {
                input = new FileInputStream(new File(iiqPropertiesLocation));
            }
            
            iiqProperties.load(input);
            logger.info("Successfully loaded IIQ properties from: {}", iiqPropertiesLocation);
            
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            logger.error("Error loading IIQ properties from: " + iiqPropertiesLocation, e);
        }
    }
    
    /**
     * Extract database connection information from IIQ properties
     */
    private void extractDatabaseInfo() {
        databaseInfo = new HashMap<>();
        
        // Main database
        String dbUrl = iiqProperties.getProperty("dataSource.url", "");
        databaseInfo.put("main.url", dbUrl);
        databaseInfo.put("main.username", iiqProperties.getProperty("dataSource.username", ""));
        databaseInfo.put("main.driver", iiqProperties.getProperty("dataSource.driverClassName", ""));
        
        // Plugin database
        String pluginUrl = iiqProperties.getProperty("pluginsDataSource.url", "");
        databaseInfo.put("plugin.url", pluginUrl);
        databaseInfo.put("plugin.username", iiqProperties.getProperty("pluginsDataSource.username", ""));
        
        // Access History database
        String ahUrl = iiqProperties.getProperty("dataSourceAccessHistory.url", "");
        databaseInfo.put("accessHistory.url", ahUrl);
        databaseInfo.put("accessHistory.username", iiqProperties.getProperty("dataSourceAccessHistory.username", ""));
        
        // Extract host from main URL
        if (!dbUrl.isEmpty()) {
            try {
                String host = dbUrl.split("//")[1].split("/")[0].split(":")[0];
                databaseInfo.put("main.host", host);
                logger.debug("Extracted database host: {}", host);
            } catch (Exception e) {
                logger.warn("Could not extract host from database URL: {}", dbUrl);
            }
        }
    }
    
    /**
     * Validate environment configuration
     */
    private void validateEnvironment() {
        logger.info("Validating environment configuration...");
        
        boolean valid = true;
        
        // Check IIQ properties
        if (iiqProperties.isEmpty()) {
            logger.warn("⚠ IIQ properties are empty. Check if file exists: {}", iiqPropertiesLocation);
            valid = false;
        }
        
        // Check database URL
        String dbUrl = databaseInfo.get("main.url");
        if (dbUrl == null || dbUrl.isEmpty()) {
            logger.error("✗ Database URL is not configured");
            valid = false;
        } else {
            logger.info("✓ Database URL configured: {}", maskConnectionString(dbUrl));
        }
        
        // Check IIQ server connectivity (optional)
        if (iiqServerUrl != null && !iiqServerUrl.isEmpty()) {
            logger.info("✓ IIQ Server URL configured: {}", iiqServerUrl);
        }
        
        // Warn for production environment
        if (isProduction()) {
            logger.warn("⚠ WARNING: Running in PRODUCTION environment!");
            logger.warn("⚠ Test rollback is disabled in production by default");
        }
        
        if (valid) {
            logger.info("✓ Environment validation successful");
        } else {
            logger.error("✗ Environment validation failed - some configurations are missing");
        }
    }
    
    /**
     * Print environment summary
     */
    private void printEnvironmentSummary() {
        logger.info("\n" +
            "╔════════════════════════════════════════════╗\n" +
            "║        ENVIRONMENT CONFIGURATION           ║\n" +
            "╠════════════════════════════════════════════╣\n" +
            "║ Profile:     {:30}║\n" +
            "║ Environment: {:30}║\n" +
            "║ IIQ Server:  {:30}║\n" +
            "║ Database:    {:30}║\n" +
            "║ User:        {:30}║\n" +
            "╚════════════════════════════════════════════╝",
            activeProfile.toUpperCase(),
            environmentName,
            maskUrl(iiqServerUrl),
            maskConnectionString(databaseInfo.get("main.url")),
            databaseInfo.get("main.username")
        );
    }
    
    // ===== Public Methods =====
    
    /**
     * Get current environment type
     */
    public EnvironmentType getCurrentEnvironment() {
        return EnvironmentType.fromProfile(activeProfile);
    }
    
    /**
     * Check if current environment is development
     */
    public boolean isDevelopment() {
        return "dev".equalsIgnoreCase(activeProfile) || "local".equalsIgnoreCase(activeProfile);
    }
    
    /**
     * Check if current environment is test
     */
    public boolean isTest() {
        return "test".equalsIgnoreCase(activeProfile);
    }
    
    /**
     * Check if current environment is UAT
     */
    public boolean isUAT() {
        return "uat".equalsIgnoreCase(activeProfile);
    }
    
    /**
     * Check if current environment is production
     */
    public boolean isProduction() {
        return "prod".equalsIgnoreCase(activeProfile);
    }
    
    /**
     * Get IIQ property value
     */
    public String getIiqProperty(String key) {
        return iiqProperties.getProperty(key);
    }
    
    /**
     * Get IIQ property value with default
     */
    public String getIiqProperty(String key, String defaultValue) {
        return iiqProperties.getProperty(key, defaultValue);
    }
    
    /**
     * Get all IIQ properties
     */
    public Properties getIiqProperties() {
        return (Properties) iiqProperties.clone();
    }
    
    /**
     * Get database connection URL
     */
    public String getDatabaseUrl() {
        return databaseInfo.get("main.url");
    }
    
    /**
     * Get database username
     */
    public String getDatabaseUsername() {
        return databaseInfo.get("main.username");
    }
    
    /**
     * Get database password (masked)
     */
    public String getDatabasePassword() {
        String password = iiqProperties.getProperty("dataSource.password");
        return password;
    }
    
    /**
     * Get IIQ server URL
     */
    public String getIiqServerUrl() {
        return iiqServerUrl;
    }
    
    /**
     * Get IIQ username
     */
    public String getIiqUsername() {
        return iiqUsername;
    }
    
    /**
     * Get IIQ password
     */
    public String getIiqPassword() {
        return iiqPassword;
    }
    
    /**
     * Check if environment is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get environment display name
     */
    public String getEnvironmentDisplayName() {
        return String.format("[%s] %s", activeProfile.toUpperCase(), environmentName);
    }
    
    /**
     * Should rollback transactions (based on environment)
     */
    public boolean shouldRollbackTransactions() {
        // Never rollback in production
        if (isProduction()) {
            return false;
        }
        
        // Check Spring property
        String rollback = springEnvironment.getProperty("test.transaction.rollback", "true");
        return Boolean.parseBoolean(rollback);
    }
    
    /**
     * Get max retry count based on environment
     */
    public int getMaxRetryCount() {
        if (isDevelopment()) {
            return 1; // Less retries in dev for faster feedback
        } else if (isProduction()) {
            return 5; // More retries in production
        }
        return 3; // Default
    }
    
    // ===== Utility Methods =====
    
    /**
     * Mask sensitive information in URLs
     */
    private String maskUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Not configured";
        }
        if (url.contains("@")) {
            return url.replaceAll("://[^@]+@", "://****@");
        }
        return url;
    }
    
    /**
     * Mask connection string
     */
    private String maskConnectionString(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            return "Not configured";
        }
        try {
            String[] parts = connectionString.split("\\?")[0].split("//");
            if (parts.length > 1) {
                String dbPart = parts[1];
                String[] dbParts = dbPart.split("/");
                if (dbParts.length > 0) {
                    return "jdbc:mysql://" + dbParts[0] + "/****";
                }
            }
        } catch (Exception e) {
            // If parsing fails, return partially masked string
        }
        return connectionString.substring(0, Math.min(30, connectionString.length())) + "****";
    }
    
    /**
     * Get environment-specific timeout
     */
    public long getTimeoutMillis() {
        if (isDevelopment()) {
            return 60000; // 1 minute for dev
        } else if (isProduction()) {
            return 300000; // 5 minutes for production
        }
        return 120000; // 2 minutes default
    }
}