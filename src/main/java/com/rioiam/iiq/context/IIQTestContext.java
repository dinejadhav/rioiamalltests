package com.rioiam.iiq.context;

import com.rioiam.iiq.config.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.*;
import sailpoint.server.Environment;
import sailpoint.tools.GeneralException;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.mysql.cj.jdbc.MysqlDataSource;

/**
 * IIQ Test Context Manager using Direct Connection Approach
 * This implementation directly connects to IIQ database without Spring Starter
 * 
 * @author RIOIAM Framework
 */
@Component
public class IIQTestContext {
    
    private static final Logger logger = LoggerFactory.getLogger(IIQTestContext.class);
    
    @Autowired
    private EnvironmentConfig environmentConfig;
    
    @Value("${iiq.properties.location:classpath:environments/dev/iiq.properties}")
    private String iiqPropertiesPath;
    
    private SailPointContext context;
    private Properties iiqProperties;
    private DataSource dataSource;
    private boolean initialized = false;
    
    // Cache for frequently accessed objects
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private long contextCreationTime;
    
    /**
     * Initialize IIQ Test Context
     */
    @PostConstruct
    public void initialize() {
        logger.info("================================================");
        logger.info("    Initializing IIQ Test Context              ");
        logger.info("================================================");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Step 1: Load IIQ properties
            loadIIQProperties();
            
            // Step 2: Setup environment
            setupEnvironment();
            
            // Step 3: Initialize data source
            initializeDataSource();
            
            // Step 4: Initialize IIQ
            initializeIIQ();
            
            // Step 5: Create context
            createContext();
            
            // Step 6: Validate
            validateContext();
            
            contextCreationTime = System.currentTimeMillis() - startTime;
            initialized = true;
            
            logger.info("✓ IIQ Test Context initialized in {} ms", contextCreationTime);
            printSummary();
            
        } catch (Exception e) {
            logger.error("Failed to initialize IIQ Test Context", e);
            throw new RuntimeException("Initialization failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load IIQ properties file
     */
    private void loadIIQProperties() throws Exception {
        logger.info("Loading IIQ properties from: {}", iiqPropertiesPath);
        
        iiqProperties = new Properties();
        InputStream input = null;
        
        try {
            if (iiqPropertiesPath.startsWith("classpath:")) {
                String path = iiqPropertiesPath.substring("classpath:".length());
                input = getClass().getClassLoader().getResourceAsStream(path);
                if (input == null) {
                    throw new RuntimeException("Properties file not found: " + path);
                }
            } else {
                input = new FileInputStream(iiqPropertiesPath);
            }
            
            iiqProperties.load(input);
            logger.info("✓ Loaded {} properties", iiqProperties.size());
            
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
    
    /**
     * Setup IIQ environment
     */
    private void setupEnvironment() {
        logger.info("Setting up IIQ environment...");
        
        // Set all properties as system properties (IIQ reads from System properties)
        for (String key : iiqProperties.stringPropertyNames()) {
            System.setProperty(key, iiqProperties.getProperty(key));
        }
        
        // Set critical IIQ properties
        System.setProperty("sailpoint.home", "/Users/dineshjadhav/Sailpoint/iiq85/tomcat/webapps/identityiq");
        System.setProperty("sailpoint.environment", environmentConfig.getCurrentEnvironment().getProfile());
        System.setProperty("log4j.configuration", "log4j.properties");
        
        // Disable features not needed for testing
        System.setProperty("sailpoint.scheduler.enabled", "false");
        System.setProperty("sailpoint.plugins.enabled", "false");
        System.setProperty("sailpoint.server.hostName", "test-host");
        
        logger.info("✓ Environment configured for: {}", environmentConfig.getCurrentEnvironment());
    }
    
    /**
     * Initialize data source for direct database connection
     */
    private void initializeDataSource() throws SQLException {
        logger.info("Initializing database connection...");
        
        String dbUrl = iiqProperties.getProperty("dataSource.url");
        String username = iiqProperties.getProperty("dataSource.username");
        String password = iiqProperties.getProperty("dataSource.password");
        
        // Create MySQL data source
        MysqlDataSource mysqlDS = new MysqlDataSource();
        mysqlDS.setURL(dbUrl);
        mysqlDS.setUser(username);
        mysqlDS.setPassword(password);
        
        // Configure connection properties
        mysqlDS.setAutoReconnect(true);
        mysqlDS.setUseSSL(false);
        mysqlDS.setServerTimezone("UTC");
        mysqlDS.setAllowPublicKeyRetrieval(true);
        
        // Set connection pool properties
        mysqlDS.setConnectionAttributes("connectionTimeout:30000,socketTimeout:60000");
        
        this.dataSource = mysqlDS;
        
        // Test connection
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {
                logger.info("✓ Database connection successful");
            }
        }
    }
    
    /**
     * Initialize IIQ using Environment class
     */
    private void initializeIIQ() throws Exception {
        logger.info("Initializing IIQ Environment...");
        
        // In IIQ 8.5, Environment is initialized differently
        // Set the required system properties for IIQ
        System.setProperty("sailpoint.server.initialize", "true");
        
        // Get the singleton instance (it auto-initializes)
        Environment env = Environment.getEnvironment();
        
        if (env == null) {
            // If Environment is not available, we'll proceed without it
            logger.warn("IIQ Environment not available, proceeding with direct context creation");
        } else {
            logger.info("✓ IIQ Environment obtained");
        }
    }
    
    /**
     * Create SailPoint Context
     */
    private void createContext() throws GeneralException {
        logger.info("Creating SailPoint Context...");
        
        // Method 1: Try using SailPointFactory with default method
        try {
            context = SailPointFactory.createContext();
            if (context != null) {
                logger.info("✓ Context created using SailPointFactory");
                return;
            }
        } catch (Exception e) {
            logger.warn("SailPointFactory.createContext() failed: {}", e.getMessage());
        }
        
        // Method 2: Try getting current context
        try {
            context = SailPointFactory.getCurrentContext();
            if (context != null) {
                logger.info("✓ Context obtained from current context");
                return;
            }
        } catch (Exception e) {
            logger.warn("getCurrentContext failed: {}", e.getMessage());
        }
        
        // Method 3: For IIQ 8.5, try creating context with username
        try {
            // Try to create context as system user
            context = SailPointFactory.createContext("spadmin");
            if (context != null) {
                logger.info("✓ Context created as spadmin user");
                return;
            }
        } catch (Exception e) {
            logger.warn("createContext with username failed: {}", e.getMessage());
        }
        
        // Method 4: Use reflection to access internal methods if available
        try {
            // This is a fallback approach using reflection
            Class<?> factoryClass = SailPointFactory.class;
            java.lang.reflect.Method method = factoryClass.getDeclaredMethod("getContext");
            method.setAccessible(true);
            context = (SailPointContext) method.invoke(null);
            if (context != null) {
                logger.info("✓ Context obtained via reflection");
                return;
            }
        } catch (Exception e) {
            logger.warn("Reflection approach failed: {}", e.getMessage());
        }
        
        if (context == null) {
            throw new GeneralException("Failed to create SailPoint Context. Please ensure IIQ libraries are properly configured.");
        }
    }
    
    /**
     * Validate the context
     */
    private void validateContext() {
        logger.info("Validating context...");
        
        try {
            // Test 1: Get configuration
            Configuration config = context.getConfiguration();
            if (config != null) {
                logger.info("✓ System configuration accessible");
            }
            
            // Test 2: Count objects
            QueryOptions qo = new QueryOptions();
            qo.setResultLimit(1);
            
            int identityCount = context.countObjects(Identity.class, qo);
            logger.info("✓ Found {} identities", identityCount);
            
            int appCount = context.countObjects(Application.class, qo);
            logger.info("✓ Found {} applications", appCount);
            
            // Test 3: Check for admin user
            Identity admin = context.getObjectByName(Identity.class, "spadmin");
            if (admin != null) {
                logger.info("✓ Admin user found: {}", admin.getName());
            }
            
        } catch (GeneralException e) {
            logger.error("Validation failed: {}", e.getMessage());
        }
    }
    
    /**
     * Print initialization summary
     */
    private void printSummary() {
        String env = environmentConfig.getCurrentEnvironment().getDescription();
        String db = iiqProperties.getProperty("dataSource.username") + "@" + 
                   extractHost(iiqProperties.getProperty("dataSource.url"));
        
        logger.info("\n" +
            "┌────────────────────────────────────────────┐\n" +
            "│       IIQ TEST CONTEXT READY               │\n" +
            "├────────────────────────────────────────────┤\n" +
            "│ Environment: {:<30}│\n" +
            "│ Database:    {:<30}│\n" +
            "│ Init Time:   {:<27}ms│\n" +
            "└────────────────────────────────────────────┘",
            env, db, contextCreationTime
        );
    }
    
    // ===== Public API Methods =====
    
    /**
     * Get the SailPoint Context
     */
    public SailPointContext getContext() {
        if (!initialized) {
            throw new IllegalStateException("Context not initialized");
        }
        return context;
    }
    
    /**
     * Execute an operation with the context
     */
    public <T> T execute(ContextCallback<T> callback) throws GeneralException {
        if (!initialized) {
            throw new IllegalStateException("Context not initialized");
        }
        
        try {
            context.startTransaction();
            T result = callback.doInContext(context);
            
            if (environmentConfig.shouldRollbackTransactions()) {
                context.rollbackTransaction();
            } else {
                context.commitTransaction();
            }
            
            return result;
            
        } catch (Exception e) {
            context.rollbackTransaction();
            throw new GeneralException("Operation failed", e);
        }
    }
    
    /**
     * Get object by name with caching
     */
    @SuppressWarnings("unchecked")
    public <T extends SailPointObject> T getObject(Class<T> cls, String name) throws GeneralException {
        String key = cls.getSimpleName() + ":" + name;
        
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        }
        
        T object = context.getObjectByName(cls, name);
        if (object != null && environmentConfig.isDevelopment()) {
            cache.put(key, object);
        }
        
        return object;
    }
    
    /**
     * Search for objects
     */
    public <T extends SailPointObject> List<T> search(Class<T> cls, QueryOptions options) throws GeneralException {
        return context.getObjects(cls, options);
    }
    
    /**
     * Count objects
     */
    public <T extends SailPointObject> int count(Class<T> cls, QueryOptions options) throws GeneralException {
        return context.countObjects(cls, options);
    }
    
    /**
     * Save object
     */
    public void saveObject(SailPointObject object) throws GeneralException {
        context.saveObject(object);
        context.commitTransaction();
    }
    
    /**
     * Delete object
     */
    public void deleteObject(SailPointObject object) throws GeneralException {
        context.removeObject(object);
        context.commitTransaction();
    }
    
    /**
     * Clear cache
     */
    public void clearCache() {
        cache.clear();
        try {
            context.decache();
        } catch (GeneralException e) {
            logger.warn("Error clearing context cache", e);
        }
    }
    
    /**
     * Check if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("initialized", initialized);
        stats.put("environment", environmentConfig.getCurrentEnvironment().toString());
        stats.put("cacheSize", cache.size());
        stats.put("initTime", contextCreationTime + "ms");
        return stats;
    }
    
    /**
     * Cleanup resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up IIQ Test Context...");
        
        try {
            clearCache();
            
            if (context != null) {
                context.close();
            }
            
            logger.info("✓ Cleanup completed");
            
        } catch (Exception e) {
            logger.error("Cleanup failed", e);
        }
    }
    
    // ===== Helper Methods =====
    
    private String extractHost(String url) {
        try {
            return url.split("//")[1].split("/")[0].split(":")[0];
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Callback interface for context operations
     */
    @FunctionalInterface
    public interface ContextCallback<T> {
        T doInContext(SailPointContext context) throws GeneralException;
    }
}