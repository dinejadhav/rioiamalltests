package com.rioiam.iiq.context;

import com.rioiam.iiq.config.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.*;
import sailpoint.spring.SpringStarter;
import sailpoint.tools.GeneralException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * IIQ Remote Context Manager
 * Manages SailPointContext initialization and lifecycle for remote testing
 * 
 * This class provides:
 * - Remote IIQ context initialization
 * - Connection pooling and management
 * - Thread-safe context operations
 * - Automatic cleanup
 * 
 * @author RIOIAM Framework
 */
@Component
public class IIQRemoteContext {
    
    private static final Logger logger = LoggerFactory.getLogger(IIQRemoteContext.class);
    
    @Autowired
    private EnvironmentConfig environmentConfig;
    
    private SailPointContext context;
    private SpringStarter starter;
    private Properties iiqProperties;
    private boolean initialized = false;
    private final ReentrantLock contextLock = new ReentrantLock();
    
    // Cache for frequently accessed objects
    private final Map<String, Identity> identityCache = new ConcurrentHashMap<>();
    private final Map<String, Application> applicationCache = new ConcurrentHashMap<>();
    private final Map<String, TaskDefinition> taskCache = new ConcurrentHashMap<>();
    
    // Statistics
    private long contextCreationTime;
    private int operationCount = 0;
    private final Map<String, Integer> operationStats = new HashMap<>();
    
    /**
     * Initialize IIQ context after Spring context is ready
     */
    @PostConstruct
    public void initialize() {
        logger.info("========================================");
        logger.info("Initializing IIQ Remote Context");
        logger.info("========================================");
        
        try {
            // Wait for environment config to be ready
            if (!environmentConfig.isInitialized()) {
                logger.info("Waiting for environment configuration...");
                Thread.sleep(1000);
            }
            
            long startTime = System.currentTimeMillis();
            
            // Initialize IIQ properties
            initializeProperties();
            
            // Initialize Spring Starter
            initializeSpringStarter();
            
            // Create SailPoint Context
            createContext();
            
            // Validate context
            validateContext();
            
            // Initialize caches
            initializeCaches();
            
            contextCreationTime = System.currentTimeMillis() - startTime;
            initialized = true;
            
            logger.info("✓ IIQ Remote Context initialized successfully in {} ms", contextCreationTime);
            printContextSummary();
            
        } catch (Exception e) {
            logger.error("Failed to initialize IIQ Remote Context", e);
            throw new RuntimeException("Failed to initialize IIQ Remote Context: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize IIQ properties from environment config
     */
    private void initializeProperties() {
        logger.info("Loading IIQ properties...");
        iiqProperties = environmentConfig.getIiqProperties();
        // Set system properties required by IIQ
        String iiqHome = System.getenv("SAILPOINT_HOME");
        if (iiqHome == null || iiqHome.isEmpty()) {
            iiqHome = environmentConfig.getIiqProperty("sailpoint.home", "/Users/dineshjadhav/Sailpoint/iiq85/tomcat/webapps/identityiq");
        }
        System.setProperty("sailpoint.home", iiqHome);
        logger.info("sailpoint.home set to: {}", iiqHome);
        System.setProperty("sailpoint.environment", environmentConfig.getCurrentEnvironment().getProfile());
        // Override any properties if needed for remote execution
        iiqProperties.setProperty("scheduler.enabled", "false"); // Disable scheduler for testing
        iiqProperties.setProperty("plugins.enabled", "false");   // Disable plugins for testing
        logger.debug("Loaded {} IIQ properties", iiqProperties.size());
    }
    
    /**
     * Initialize Spring Starter (SST Helper)
     */
    private void initializeSpringStarter() throws Exception {
        logger.info("Initializing Spring Starter...");
        
        // Set properties file path
        String propertiesPath = "iiq.properties";
        
        // Create Spring Starter - correct constructor for IIQ 8.5
        starter = new SpringStarter("iiqBeans.xml");
        
        // Set properties using the properties we loaded
        for (String key : iiqProperties.stringPropertyNames()) {
            System.setProperty(key, iiqProperties.getProperty(key));
        }
        
        // Start Spring context
        starter.start();
        
        logger.info("✓ Spring Starter initialized");
    }
    
    /**
     * Create SailPoint Context
     */
    private void createContext() throws GeneralException {
        logger.info("Creating SailPoint Context...");
        
        contextLock.lock();
        try {
            // Get context from factory
            context = SailPointFactory.createContext();
            
            if (context == null) {
                throw new GeneralException("Failed to create SailPoint Context - null returned");
            }
            
            // Set debug logging if in development
            if (environmentConfig.isDevelopment()) {
                // Enable debug through logging configuration
                Logger sailpointLogger = LoggerFactory.getLogger("sailpoint");
                if (sailpointLogger instanceof ch.qos.logback.classic.Logger) {
                    ((ch.qos.logback.classic.Logger) sailpointLogger).setLevel(ch.qos.logback.classic.Level.DEBUG);
                }
            }
            
            logger.info("✓ SailPoint Context created");
            
        } finally {
            contextLock.unlock();
        }
    }
    
    /**
     * Validate context by performing test operations
     */
    private void validateContext() throws GeneralException {
        logger.info("Validating SailPoint Context...");
        
        contextLock.lock();
        try {
            // Test 1: Check database connectivity
            testDatabaseConnection();
            
            // Test 2: Load system configuration
            Configuration systemConfig = context.getConfiguration();
            if (systemConfig == null) {
                logger.warn("⚠ System configuration not found - may need to import init.xml");
            } else {
                logger.info("✓ System configuration loaded: {}", systemConfig.getName());
            }
            
            // Test 3: Count identities
            QueryOptions qo = new QueryOptions();
            qo.setResultLimit(1);
            int identityCount = context.countObjects(Identity.class, qo);
            logger.info("✓ Found {} identities in the system", identityCount);
            
            // Test 4: Check for spadmin
            Identity spadmin = context.getObjectByName(Identity.class, "spadmin");
            if (spadmin != null) {
                logger.info("✓ Administrator account found: {}", spadmin.getName());
            } else {
                logger.warn("⚠ Administrator account 'spadmin' not found");
            }
            
        } finally {
            contextLock.unlock();
        }
    }
    
    /**
     * Test database connection
     */
    private void testDatabaseConnection() {
        try {
            String url = environmentConfig.getDatabaseUrl();
            String username = environmentConfig.getDatabaseUsername();
            String password = environmentConfig.getDatabasePassword();
            
            Connection conn = DriverManager.getConnection(url, username, password);
            if (conn != null && !conn.isClosed()) {
                logger.info("✓ Database connection successful");
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("✗ Database connection failed: {}", e.getMessage());
        }
    }
    
    /**
     * Initialize object caches for performance
     */
    private void initializeCaches() {
        logger.info("Initializing object caches...");
        
        if (environmentConfig.isDevelopment() || environmentConfig.isTest()) {
            // Preload common objects in dev/test environments
            try {
                // Cache common applications
                QueryOptions qo = new QueryOptions();
                qo.setResultLimit(10);
                List<Application> apps = context.getObjects(Application.class, qo);
                for (Application app : apps) {
                    applicationCache.put(app.getName(), app);
                }
                logger.debug("Cached {} applications", applicationCache.size());
                
                // Cache task definitions
                List<TaskDefinition> tasks = context.getObjects(TaskDefinition.class, qo);
                for (TaskDefinition task : tasks) {
                    taskCache.put(task.getName(), task);
                }
                logger.debug("Cached {} task definitions", taskCache.size());
                
            } catch (GeneralException e) {
                logger.warn("Could not initialize caches: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Print context summary
     */
    private void printContextSummary() {
        logger.info("\n" +
            "╔════════════════════════════════════════════╗\n" +
            "║         IIQ CONTEXT INITIALIZED            ║\n" +
            "╠════════════════════════════════════════════╣\n" +
            "║ Environment:  {:29}║\n" +
            "║ Database:     {:29}║\n" +
            "║ Init Time:    {:26}ms ║\n" +
            "║ Debug Mode:   {:29}║\n" +
            "║ Caching:      {:29}║\n" +
            "╚════════════════════════════════════════════╝",
            environmentConfig.getCurrentEnvironment().getDescription(),
            environmentConfig.getDatabaseUsername() + "@" + extractHost(environmentConfig.getDatabaseUrl()),
            contextCreationTime,
            environmentConfig.isDevelopment() ? "ENABLED" : "DISABLED",
            !applicationCache.isEmpty() ? "ENABLED (" + applicationCache.size() + " apps)" : "DISABLED"
        );
    }
    
    // ===== Public API Methods =====
    
    /**
     * Get the SailPoint Context
     */
    public SailPointContext getContext() {
        if (!initialized) {
            throw new IllegalStateException("IIQ Context not initialized");
        }
        operationCount++;
        return context;
    }
    
    /**
     * Execute operation with context (auto-handles transactions)
     */
    public <T> T execute(ContextOperation<T> operation) throws GeneralException {
        return execute(operation, true);
    }
    
    /**
     * Execute operation with context
     * @param operation The operation to execute
     * @param autoCommit Whether to auto-commit
     */
    public <T> T execute(ContextOperation<T> operation, boolean autoCommit) throws GeneralException {
        if (!initialized) {
            throw new IllegalStateException("IIQ Context not initialized");
        }
        
        contextLock.lock();
        try {
            context.startTransaction();
            
            T result = operation.execute(context);
            
            if (autoCommit && !environmentConfig.shouldRollbackTransactions()) {
                context.commitTransaction();
                context.decache();
            } else {
                context.rollbackTransaction();
            }
            
            trackOperation(operation.getClass().getSimpleName());
            return result;
            
        } catch (Exception e) {
            context.rollbackTransaction();
            throw new GeneralException("Operation failed: " + e.getMessage(), e);
        } finally {
            contextLock.unlock();
        }
    }
    
    /**
     * Get Identity by name (with caching)
     */
    public Identity getIdentity(String name) throws GeneralException {
        if (identityCache.containsKey(name)) {
            return identityCache.get(name);
        }
        
        Identity identity = context.getObjectByName(Identity.class, name);
        if (identity != null && environmentConfig.isDevelopment()) {
            identityCache.put(name, identity);
        }
        return identity;
    }
    
    /**
     * Get Application by name (with caching)
     */
    public Application getApplication(String name) throws GeneralException {
        if (applicationCache.containsKey(name)) {
            return applicationCache.get(name);
        }
        
        Application app = context.getObjectByName(Application.class, name);
        if (app != null) {
            applicationCache.put(name, app);
        }
        return app;
    }
    
    /**
     * Get TaskDefinition by name (with caching)
     */
    public TaskDefinition getTaskDefinition(String name) throws GeneralException {
        if (taskCache.containsKey(name)) {
            return taskCache.get(name);
        }
        
        TaskDefinition task = context.getObjectByName(TaskDefinition.class, name);
        if (task != null) {
            taskCache.put(name, task);
        }
        return task;
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        identityCache.clear();
        applicationCache.clear();
        taskCache.clear();
        logger.info("All caches cleared");
    }
    
    /**
     * Refresh context
     */
    public void refresh() throws GeneralException {
        contextLock.lock();
        try {
            context.decache();
            clearCaches();
            logger.info("Context refreshed");
        } finally {
            contextLock.unlock();
        }
    }
    
    /**
     * Get context statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("initialized", initialized);
        stats.put("environment", environmentConfig.getCurrentEnvironment().toString());
        stats.put("operationCount", operationCount);
        stats.put("contextCreationTime", contextCreationTime + "ms");
        stats.put("cacheSize", identityCache.size() + applicationCache.size() + taskCache.size());
        stats.put("operations", new HashMap<>(operationStats));
        return stats;
    }
    
    /**
     * Check if context is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Cleanup resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up IIQ Remote Context...");
        
        try {
            clearCaches();
            
            if (context != null) {
                context.close();
            }
            
            if (starter != null) {
                starter.close();
            }
            
            logger.info("✓ IIQ Remote Context cleaned up successfully");
            logger.info("Total operations performed: {}", operationCount);
            
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }
    
    // ===== Helper Methods =====
    
    private void trackOperation(String operationName) {
        operationStats.merge(operationName, 1, Integer::sum);
    }
    
    private String extractHost(String url) {
        try {
            return url.split("//")[1].split("/")[0];
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Functional interface for context operations
     */
    @FunctionalInterface
    public interface ContextOperation<T> {
        T execute(SailPointContext context) throws GeneralException;
    }
}
