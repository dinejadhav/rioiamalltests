package com.rioiam.iiq.base;

import com.rioiam.iiq.config.EnvironmentConfig;
import com.rioiam.iiq.context.IIQRemoteContext;
import com.rioiam.iiq.tasks.IIQTaskExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import sailpoint.api.SailPointContext;

/**
 * Base class for all IIQ tests.
 * Provides common setup, teardown, and utility methods.
 *
 * All test classes should extend this class to leverage:
 * - Shared context management (IIQRemoteContext)
 * - Environment configuration
 * - Common utilities
 * - Consistent logging
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
    IIQRemoteContext.class,
    EnvironmentConfig.class,
    IIQTaskExecutor.class,
    com.rioiam.iiq.identity.IdentityService.class,
    com.rioiam.iiq.identity.IdentityValidator.class,
    com.rioiam.iiq.workflow.WorkflowExecutor.class,
    com.rioiam.iiq.workflow.WorkItemHandler.class,
    com.rioiam.iiq.workflow.ServerSideWorkflowLauncher.class
    // Additional components will be added here as modules are created
})
public abstract class BaseIIQTest {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    // ========================================
    // EXISTING COMPONENTS (REUSED)
    // ========================================

    @Autowired
    protected IIQRemoteContext remoteContext;

    @Autowired
    protected EnvironmentConfig environmentConfig;

    @Autowired
    protected IIQTaskExecutor taskExecutor;

    // ========================================
    // NEW COMPONENTS (TO BE ADDED)
    // ========================================

    @Autowired
    protected com.rioiam.iiq.workflow.WorkflowExecutor workflowExecutor;

    @Autowired
    protected com.rioiam.iiq.workflow.WorkItemHandler workItemHandler;

    @Autowired
    protected com.rioiam.iiq.identity.IdentityService identityService;

    @Autowired
    protected com.rioiam.iiq.identity.IdentityValidator identityValidator;

    // @Autowired
    // protected DatabaseService databaseService;

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Get SailPoint context - reuses existing context
     */
    protected SailPointContext getContext() {
        return remoteContext.getContext();
    }

    /**
     * Get environment name
     */
    protected String getEnvironmentName() {
        // TODO: Add getEnvironmentName() method to EnvironmentConfig if needed
        return "dev"; // Default for now
    }

    // ========================================
    // LIFECYCLE METHODS
    // ========================================

    /**
     * Common setup - runs before each test
     */
    @Before
    public void baseSetup() {
        logger.info("========================================");
        logger.info("Starting test: {}", getTestName());
        logger.info("Environment: {}", getEnvironmentName());
        logger.info("========================================");
    }

    /**
     * Common cleanup - runs after each test
     */
    @After
    public void baseCleanup() {
        logger.info("========================================");
        logger.info("Test completed: {}", getTestName());
        logger.info("========================================");
    }

    /**
     * Get current test name
     */
    protected String getTestName() {
        return this.getClass().getSimpleName();
    }
}
