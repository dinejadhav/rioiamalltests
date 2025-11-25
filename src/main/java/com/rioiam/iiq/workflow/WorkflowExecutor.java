package com.rioiam.iiq.workflow;

import com.rioiam.iiq.context.IIQRemoteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sailpoint.api.SailPointContext;
import sailpoint.api.Workflower;
import sailpoint.object.TaskResult;
import sailpoint.object.WorkflowCase;
import sailpoint.object.WorkflowLaunch;
import sailpoint.tools.GeneralException;

import java.util.Map;

/**
 * Service for executing SailPoint workflows remotely.
 *
 * This component handles:
 * - Launching workflows programmatically
 * - Monitoring workflow execution
 * - Retrieving workflow status and results
 *
 * Reuses existing IIQRemoteContext for SailPoint connectivity.
 */
@Component
public class WorkflowExecutor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutor.class);

    @Autowired
    private IIQRemoteContext remoteContext;

    /**
     * Launch a workflow with the given name and input variables.
     *
     * @param workflowName Name of the workflow to launch
     * @param variables Input variables for the workflow
     * @return Workflow case ID if successful, null if failed
     */
    public String launchWorkflow(String workflowName, Map<String, Object> variables) {
        return launchWorkflow(workflowName, "spadmin", variables);
    }

    /**
     * Launch a workflow with the given name, launcher, and input variables.
     *
     * @param workflowName Name of the workflow to launch
     * @param launcher Identity name who launches the workflow (e.g., "dinesh.jadhav1", "spadmin")
     * @param variables Input variables for the workflow
     * @return Workflow case ID if successful, null if failed
     */
    public String launchWorkflow(String workflowName, String launcher, Map<String, Object> variables) {
        logger.info("========================================");
        logger.info("Launching workflow: {}", workflowName);
        logger.info("Launcher: {}", launcher);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Create Workflower instance
            Workflower workflower = new Workflower(context);

            // Log input variables
            if (variables != null && !variables.isEmpty()) {
                logger.debug("Workflow variables:");
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    logger.debug("  {}: {}", entry.getKey(), entry.getValue());
                }
            }

            // Create WorkflowLaunch object for proper configuration
            WorkflowLaunch wfLaunch = new WorkflowLaunch();
            wfLaunch.setWorkflowName(workflowName);
            wfLaunch.setWorkflowRef(workflowName);
            wfLaunch.setCaseName(launcher + " - " + System.currentTimeMillis()); // Unique case name
            wfLaunch.setLauncher(launcher); // CRITICAL: Set the launcher identity
            wfLaunch.setVariables(variables);

            logger.info("Launching workflow with WorkflowLaunch object...");
            logger.info("  Case Name: {}", wfLaunch.getCaseName());
            logger.info("  Launcher: {}", launcher);
            logger.info("  Using WorkflowLaunch for proper WorkItem creation");

            // Launch the workflow using WorkflowLaunch object
            WorkflowLaunch launch = workflower.launch(wfLaunch);

            if (launch != null) {
                WorkflowCase wfCase = launch.getWorkflowCase();

                if (wfCase != null) {
                    logger.info("✓ Workflow launched successfully");
                    logger.info("  Workflow Case Name: {}", wfCase.getName());
                    logger.info("  Workflow Case ID: {}", wfCase.getId());
                    logger.info("  Initial Status: {}", wfCase.getCompletionStatus());

                    // CRITICAL DEBUG: Check if workflow completed immediately or is waiting
                    if (wfCase.getCompletionStatus() != null) {
                        logger.warn("⚠️  Workflow completed immediately with status: {}", wfCase.getCompletionStatus());
                        logger.warn("  This usually means workflow finished without waiting for WorkItems");
                        logger.warn("  Workflow may have errored or taken unexpected path");
                    } else {
                        logger.info("  Workflow is executing (status is null - workflow running)");
                    }

                    // Return the workflow case name instead of ID since ID may not be populated yet
                    // The workflow case name is unique and can be used to track the workflow
                    return wfCase.getName();
                } else {
                    logger.error("✗ Workflow launch returned WorkflowCase = null");
                    logger.error("  Launch object exists but WorkflowCase is null");
                    logger.error("  This usually means the workflow failed during initialization");
                    if (launch.getTaskResult() != null) {
                        logger.error("  Task Result Messages: {}", launch.getTaskResult().getMessages());
                    }
                    return null;
                }
            } else {
                logger.error("✗ Workflow launch returned null");
                return null;
            }

        } catch (GeneralException e) {
            logger.error("✗ Error launching workflow: {}", workflowName, e);
            return null;
        }
    }

    /**
     * Wait for a workflow to complete with timeout.
     *
     * @param workflowCaseId ID of the workflow case
     * @param timeoutSeconds Maximum time to wait in seconds
     * @return WorkflowCase with final status, or null if timeout
     */
    public WorkflowCase waitForCompletion(String workflowCaseId, int timeoutSeconds) {
        logger.info("Waiting for workflow {} to complete (timeout: {}s)", workflowCaseId, timeoutSeconds);

        SailPointContext context = remoteContext.getContext();

        try {
            long startTime = System.currentTimeMillis();
            long timeoutMillis = timeoutSeconds * 1000L;
            int pollInterval = 2000; // Poll every 2 seconds

            while (true) {
                // Get current workflow case
                WorkflowCase wfCase = context.getObjectById(WorkflowCase.class, workflowCaseId);

                if (wfCase == null) {
                    logger.error("✗ Workflow case not found: {}", workflowCaseId);
                    return null;
                }

                TaskResult.CompletionStatus status = wfCase.getCompletionStatus();
                logger.debug("Workflow status: {}", status);

                // Check if workflow is complete
                // If status is not null, the workflow has completed (Success, Error, Warning, or Terminated)
                // If status is null, the workflow is still executing
                if (status != null) {
                    logger.info("✓ Workflow completed with status: {}", status);
                    return wfCase;
                }

                // Check timeout
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= timeoutMillis) {
                    logger.warn("✗ Workflow timed out after {} seconds", timeoutSeconds);
                    return wfCase; // Return current state even on timeout
                }

                // Wait before next poll
                Thread.sleep(pollInterval);
            }

        } catch (GeneralException e) {
            logger.error("✗ Error waiting for workflow completion", e);
            return null;
        } catch (InterruptedException e) {
            logger.error("✗ Interrupted while waiting for workflow", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Get the current status of a workflow case.
     *
     * @param workflowCaseId ID of the workflow case
     * @return Status string (e.g., "Executing", "Completed", "Terminated")
     */
    public String getWorkflowStatus(String workflowCaseId) {
        logger.debug("Getting status for workflow: {}", workflowCaseId);

        SailPointContext context = remoteContext.getContext();

        try {
            WorkflowCase wfCase = context.getObjectById(WorkflowCase.class, workflowCaseId);

            if (wfCase == null) {
                logger.warn("Workflow case not found: {}", workflowCaseId);
                return "Not Found";
            }

            TaskResult.CompletionStatus status = wfCase.getCompletionStatus();
            logger.debug("Workflow {} status: {}", workflowCaseId, status);

            return status != null ? status.toString() : "Unknown";

        } catch (GeneralException e) {
            logger.error("Error getting workflow status", e);
            return "Error";
        }
    }

    /**
     * Get the workflow case by ID or NAME.
     * This method tries both ID lookup and name lookup.
     *
     * @param workflowCaseIdOrName ID or NAME of the workflow case
     * @return WorkflowCase object, or null if not found
     */
    public WorkflowCase getWorkflowCase(String workflowCaseIdOrName) {
        logger.debug("Getting workflow case: {}", workflowCaseIdOrName);

        SailPointContext context = remoteContext.getContext();

        try {
            // Try by ID first
            WorkflowCase wfCase = context.getObjectById(WorkflowCase.class, workflowCaseIdOrName);

            if (wfCase != null) {
                logger.debug("✓ Workflow case found by ID: {} (Status: {})", wfCase.getName(), wfCase.getCompletionStatus());
                return wfCase;
            }

            // Try by name
            wfCase = context.getObjectByName(WorkflowCase.class, workflowCaseIdOrName);

            if (wfCase != null) {
                logger.debug("✓ Workflow case found by NAME: {} (Status: {})", wfCase.getName(), wfCase.getCompletionStatus());
            } else {
                logger.debug("✗ Workflow case not found by ID or NAME: {}", workflowCaseIdOrName);
            }

            return wfCase;

        } catch (GeneralException e) {
            logger.error("Error getting workflow case", e);
            return null;
        }
    }

    /**
     * Cancel a running workflow.
     *
     * @param workflowCaseId ID of the workflow case
     * @return true if cancelled successfully, false otherwise
     */
    public boolean cancelWorkflow(String workflowCaseId) {
        logger.info("Cancelling workflow: {}", workflowCaseId);

        SailPointContext context = remoteContext.getContext();

        try {
            WorkflowCase wfCase = context.getObjectById(WorkflowCase.class, workflowCaseId);

            if (wfCase == null) {
                logger.error("✗ Workflow case not found: {}", workflowCaseId);
                return false;
            }

            // Terminate the workflow
            wfCase.setCompletionStatus(TaskResult.CompletionStatus.Terminated);

            context.startTransaction();
            context.saveObject(wfCase);
            context.commitTransaction();

            logger.info("✓ Workflow cancelled successfully");
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error cancelling workflow", e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return false;
        }
    }
}
