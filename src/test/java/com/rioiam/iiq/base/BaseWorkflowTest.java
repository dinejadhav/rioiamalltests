package com.rioiam.iiq.base;

import org.junit.After;
import org.junit.Before;

/**
 * Base class for workflow-specific tests.
 * Extends BaseIIQTest and adds workflow-specific setup/teardown.
 *
 * Use this for tests that involve:
 * - Workflow execution
 * - Work item approvals
 * - Provisioning plans
 */
public abstract class BaseWorkflowTest extends BaseIIQTest {

    // ========================================
    // WORKFLOW-SPECIFIC FIELDS
    // ========================================

    // Will be populated as we create workflow modules

    // ========================================
    // LIFECYCLE METHODS
    // ========================================

    /**
     * Workflow-specific setup
     */
    @Before
    public void workflowSetup() {
        logger.info("Setting up workflow test environment");
        // Workflow-specific initialization
    }

    /**
     * Workflow-specific cleanup
     */
    @After
    public void workflowCleanup() {
        logger.info("Cleaning up workflow test environment");
        // Clean up workflows, work items, etc.
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Wait for workflow completion with timeout
     */
    protected void waitForWorkflowCompletion(String workflowCaseId, int timeoutSeconds) {
        // To be implemented when WorkflowExecutor is created
        logger.debug("Waiting for workflow {} to complete (timeout: {}s)", workflowCaseId, timeoutSeconds);
    }

    /**
     * Get workflow status
     */
    protected String getWorkflowStatus(String workflowCaseId) {
        // To be implemented when WorkflowExecutor is created
        return "Unknown";
    }
}
