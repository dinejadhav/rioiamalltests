package com.rioiam.iiq.workflow;

import com.rioiam.iiq.context.IIQRemoteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sailpoint.api.SailPointContext;
import sailpoint.object.*;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for handling SailPoint work items (approvals and forms).
 *
 * This component handles:
 * - Retrieving work items for approvers
 * - Approving/rejecting work items
 * - Completing form-based work items
 *
 * Reuses existing IIQRemoteContext for SailPoint connectivity.
 */
@Component
public class WorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(WorkItemHandler.class);

    @Autowired
    private IIQRemoteContext remoteContext;

    /**
     * Get all open work items for a specific workflow case.
     *
     * @param workflowCaseId ID or name of the workflow case
     * @return List of open work items for the workflow
     */
    public List<WorkItem> getWorkItemsForWorkflow(String workflowCaseId) {
        logger.debug("Getting work items for workflow case: {}", workflowCaseId);

        SailPointContext context = remoteContext.getContext();

        try {
            QueryOptions qo = new QueryOptions();
            // Try both workflowCase.name and workflowCase.id since ID may be null in some cases
            Filter filter = Filter.and(
                Filter.or(
                    Filter.eq("workflowCase.name", workflowCaseId),
                    Filter.eq("workflowCase.id", workflowCaseId)
                ),
                Filter.eq("state", WorkItem.State.Pending)
            );
            qo.addFilter(filter);

            List<WorkItem> items = context.getObjects(WorkItem.class, qo);

            if (items != null && !items.isEmpty()) {
                logger.debug("✓ Found {} work items for workflow {}", items.size(), workflowCaseId);
                for (WorkItem item : items) {
                    logger.debug("  - WorkItem: {} (Type: {}, Owner: {})",
                        item.getId(), item.getType(), item.getOwner());
                }
            } else {
                logger.debug("✗ No work items found for workflow {}", workflowCaseId);
            }

            return items != null ? items : new ArrayList<>();

        } catch (GeneralException e) {
            logger.error("✗ Error getting work items for workflow", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get all open work items for a specific approver.
     *
     * @param approverName Name of the approver (identity name or workgroup)
     * @return List of pending work items
     */
    public List<WorkItem> getWorkItemsByOwner(String approverName) {
        logger.debug("Getting work items for approver: {}", approverName);

        SailPointContext context = remoteContext.getContext();

        try {
            QueryOptions qo = new QueryOptions();
            Filter filter = Filter.and(
                Filter.eq("owner.name", approverName),
                Filter.eq("state", WorkItem.State.Pending)
            );
            qo.addFilter(filter);

            List<WorkItem> items = context.getObjects(WorkItem.class, qo);

            if (items != null && !items.isEmpty()) {
                logger.debug("✓ Found {} work items for approver {}", items.size(), approverName);
            } else {
                logger.debug("✗ No work items found for approver {}", approverName);
            }

            return items != null ? items : new ArrayList<>();

        } catch (GeneralException e) {
            logger.error("✗ Error getting work items for approver", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get work item by ID.
     *
     * @param workItemId ID of the work item
     * @return WorkItem object, or null if not found
     */
    public WorkItem getWorkItemById(String workItemId) {
        logger.debug("Getting work item by ID: {}", workItemId);

        SailPointContext context = remoteContext.getContext();

        try {
            WorkItem item = context.getObjectById(WorkItem.class, workItemId);

            if (item != null) {
                logger.debug("✓ WorkItem found: {} (Type: {}, State: {})",
                    item.getId(), item.getType(), item.getState());
            } else {
                logger.debug("✗ WorkItem not found: {}", workItemId);
            }

            return item;

        } catch (GeneralException e) {
            logger.error("✗ Error getting work item", e);
            return null;
        }
    }

    /**
     * Get form-type work items for a workflow.
     *
     * @param workflowCaseId ID of the workflow case
     * @return List of form work items
     */
    public List<WorkItem> getFormWorkItems(String workflowCaseId) {
        logger.debug("Getting form work items for workflow: {}", workflowCaseId);

        List<WorkItem> allItems = getWorkItemsForWorkflow(workflowCaseId);
        List<WorkItem> formItems = new ArrayList<>();

        for (WorkItem item : allItems) {
            if (WorkItem.Type.Form.equals(item.getType())) {
                formItems.add(item);
            }
        }

        logger.debug("Found {} form work items", formItems.size());
        return formItems;
    }

    /**
     * Get approval-type work items for a workflow.
     *
     * @param workflowCaseId ID of the workflow case
     * @return List of approval work items
     */
    public List<WorkItem> getApprovalWorkItems(String workflowCaseId) {
        logger.debug("Getting approval work items for workflow: {}", workflowCaseId);

        List<WorkItem> allItems = getWorkItemsForWorkflow(workflowCaseId);
        List<WorkItem> approvalItems = new ArrayList<>();

        for (WorkItem item : allItems) {
            if (WorkItem.Type.Approval.equals(item.getType())) {
                approvalItems.add(item);
            }
        }

        logger.debug("Found {} approval work items", approvalItems.size());
        return approvalItems;
    }

    /**
     * Complete a form-based work item with form data.
     *
     * @param workItemId ID of the work item
     * @param formData Form field values (e.g., maModel data)
     * @return true if completed successfully, false otherwise
     */
    public boolean completeFormWorkItem(String workItemId, Map<String, Object> formData) {
        logger.info("========================================");
        logger.info("Completing form work item: {}", workItemId);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Get the work item
            WorkItem workItem = context.getObjectById(WorkItem.class, workItemId);

            if (workItem == null) {
                logger.error("✗ Work item not found: {}", workItemId);
                return false;
            }

            // Verify it's a form work item
            if (!WorkItem.Type.Form.equals(workItem.getType())) {
                logger.error("✗ Work item is not a form type: {}", workItem.getType());
                return false;
            }

            logger.debug("Form work item details:");
            logger.debug("  Type: {}", workItem.getType());
            logger.debug("  State: {}", workItem.getState());
            logger.debug("  Owner: {}", workItem.getOwner());

            // Set form data into work item
            if (formData != null && !formData.isEmpty()) {
                logger.debug("Setting form data:");
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    logger.debug("  {}: {}", entry.getKey(), entry.getValue());
                    workItem.put(entry.getKey(), entry.getValue());
                }
            }

            // Mark work item as complete
            workItem.setState(WorkItem.State.Finished);
            workItem.setCompletionComments("Completed programmatically by test framework");

            // Save the work item
            context.startTransaction();
            context.saveObject(workItem);
            context.commitTransaction();

            // Trigger workflow continuation
            WorkflowCase wfCase = workItem.getWorkflowCase();
            if (wfCase != null) {
                logger.debug("Advancing workflow by processing completed WorkItem");
                logger.debug("  WorkItem: {}", workItem.getId());
                logger.debug("  Workflow Case: {}", wfCase.getName());

                // CRITICAL: Must advance the workflow after completing WorkItem
                // Use Workflower to process the completed WorkItem
                try {
                    sailpoint.api.Workflower workflower = new sailpoint.api.Workflower(context);
                    // Process the WorkItem with owner checking enabled
                    workflower.process(workItem, true);
                    logger.debug("✓ WorkItem processed, workflow should advance");
                } catch (Exception e) {
                    logger.error("✗ Error processing WorkItem to advance workflow", e);
                    throw new GeneralException("Failed to advance workflow", e);
                }
            }

            logger.info("✓ Form work item completed successfully");
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error completing form work item", e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return false;
        }
    }

    /**
     * Complete the first form work item for a workflow with the given form data.
     * This is a convenience method that finds and completes the first form in one call.
     *
     * @param workflowCaseId ID of the workflow case
     * @param formData Form field values
     * @return true if completed successfully, false otherwise
     */
    public boolean completeFormWorkItemForWorkflow(String workflowCaseId, Map<String, Object> formData) {
        logger.info("Completing form work item for workflow: {}", workflowCaseId);

        List<WorkItem> formItems = getFormWorkItems(workflowCaseId);

        if (formItems.isEmpty()) {
            logger.warn("✗ No form work items found for workflow {}", workflowCaseId);
            return false;
        }

        // Complete the first form work item
        WorkItem formItem = formItems.get(0);
        logger.debug("Found form work item: {}", formItem.getId());

        return completeFormWorkItem(formItem.getId(), formData);
    }

    /**
     * Approve a work item with comments.
     *
     * @param workItemId ID of the work item to approve
     * @param comments Approval comments
     * @return true if approved successfully, false otherwise
     */
    public boolean approveWorkItem(String workItemId, String comments) {
        logger.info("========================================");
        logger.info("Approving work item: {}", workItemId);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Get the work item
            WorkItem workItem = context.getObjectById(WorkItem.class, workItemId);

            if (workItem == null) {
                logger.error("✗ Work item not found: {}", workItemId);
                return false;
            }

            logger.debug("Work item details:");
            logger.debug("  Type: {}", workItem.getType());
            logger.debug("  State: {}", workItem.getState());
            logger.debug("  Owner: {}", workItem.getOwner());

            // Get the approval set
            ApprovalSet approvalSet = (ApprovalSet) workItem.get("approvalSet");

            if (approvalSet != null) {
                logger.debug("Approving all items in approval set");

                // Approve all approval items
                List<ApprovalItem> items = approvalSet.getItems();
                if (items != null) {
                    for (ApprovalItem item : items) {
                        item.approve();
                        item.setState(WorkItem.State.Finished);
                        logger.debug("  Approved item: {}", item.getName());
                    }
                }
            }

            // Set completion state
            workItem.setState(WorkItem.State.Finished);
            workItem.setCompletionComments(comments != null ? comments : "Approved programmatically by test framework");

            // Save the work item
            context.startTransaction();
            context.saveObject(workItem);
            context.commitTransaction();

            logger.info("✓ Work item approved successfully");
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error approving work item", e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return false;
        }
    }

    /**
     * Reject a work item with comments.
     *
     * @param workItemId ID of the work item to reject
     * @param comments Rejection comments
     * @return true if rejected successfully, false otherwise
     */
    public boolean rejectWorkItem(String workItemId, String comments) {
        logger.info("========================================");
        logger.info("Rejecting work item: {}", workItemId);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Get the work item
            WorkItem workItem = context.getObjectById(WorkItem.class, workItemId);

            if (workItem == null) {
                logger.error("✗ Work item not found: {}", workItemId);
                return false;
            }

            logger.debug("Work item details:");
            logger.debug("  Type: {}", workItem.getType());
            logger.debug("  State: {}", workItem.getState());
            logger.debug("  Owner: {}", workItem.getOwner());

            // Get the approval set
            ApprovalSet approvalSet = (ApprovalSet) workItem.get("approvalSet");

            if (approvalSet != null) {
                logger.debug("Rejecting all items in approval set");

                // Reject all approval items
                List<ApprovalItem> items = approvalSet.getItems();
                if (items != null) {
                    for (ApprovalItem item : items) {
                        item.reject();
                        item.setState(WorkItem.State.Finished);
                        logger.debug("  Rejected item: {}", item.getName());
                    }
                }
            }

            // Set completion state
            workItem.setState(WorkItem.State.Finished);
            workItem.setCompletionComments(comments != null ? comments : "Rejected programmatically by test framework");

            // Save the work item
            context.startTransaction();
            context.saveObject(workItem);
            context.commitTransaction();

            logger.info("✓ Work item rejected successfully");
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error rejecting work item", e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return false;
        }
    }

    /**
     * Wait for a work item to appear for a workflow case.
     * Polls for up to maxWaitSeconds.
     *
     * @param workflowCaseId ID of the workflow case
     * @param workItemType Type of work item to wait for (Form or Approval)
     * @param maxWaitSeconds Maximum seconds to wait
     * @return WorkItem if found, null if timeout
     */
    public WorkItem waitForWorkItem(String workflowCaseId, WorkItem.Type workItemType, int maxWaitSeconds) {
        logger.debug("Waiting for {} work item for workflow {} (max {}s)",
            workItemType, workflowCaseId, maxWaitSeconds);

        long startTime = System.currentTimeMillis();
        long timeoutMillis = maxWaitSeconds * 1000L;
        int pollInterval = 1000; // Poll every 1 second

        while (true) {
            List<WorkItem> items = getWorkItemsForWorkflow(workflowCaseId);

            for (WorkItem item : items) {
                if (workItemType.equals(item.getType())) {
                    logger.debug("✓ Found {} work item: {}", workItemType, item.getId());
                    return item;
                }
            }

            // Check timeout
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMillis) {
                logger.warn("✗ Timeout waiting for {} work item", workItemType);
                return null;
            }

            // Wait before next poll
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for work item", e);
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    /**
     * Automatically handle ALL approval work items for a workflow.
     * This method continuously polls for approval work items and approves them until workflow completes.
     *
     * Supports multi-level approvals (e.g., Manager -> Business Owner -> Additional levels)
     *
     * @param workflowCaseId ID of the workflow case
     * @param maxWaitSeconds Maximum seconds to wait for each approval level
     * @param maxApprovalLevels Maximum number of approval levels to handle (safety limit)
     * @param comments Comments to add for each approval
     * @return Number of approvals processed, -1 if error
     */
    public int handleAllApprovals(String workflowCaseId, int maxWaitSeconds, int maxApprovalLevels, String comments) {
        logger.info("========================================");
        logger.info("Starting automatic multi-level approval handling");
        logger.info("Workflow Case ID: {}", workflowCaseId);
        logger.info("Max wait per level: {}s, Max levels: {}", maxWaitSeconds, maxApprovalLevels);
        logger.info("========================================");

        int approvalsProcessed = 0;
        long overallStartTime = System.currentTimeMillis();
        long overallTimeout = maxApprovalLevels * maxWaitSeconds * 1000L;

        try {
            for (int level = 1; level <= maxApprovalLevels; level++) {
                // Check overall timeout
                if (System.currentTimeMillis() - overallStartTime >= overallTimeout) {
                    logger.warn("Overall timeout reached after processing {} approvals", approvalsProcessed);
                    break;
                }

                logger.info("----------------------------------------");
                logger.info("Checking for approval level {}", level);
                logger.info("----------------------------------------");

                // Wait for approval work item
                WorkItem approvalItem = waitForWorkItem(workflowCaseId, WorkItem.Type.Approval, maxWaitSeconds);

                if (approvalItem == null) {
                    logger.info("No more approval work items found - all approvals completed");
                    break;
                }

                // Log approval details
                String owner = approvalItem.getOwner() != null ? approvalItem.getOwner().getName() : "Unknown";
                logger.info("✓ Found approval work item at level {}", level);
                logger.info("  Work Item ID: {}", approvalItem.getId());
                logger.info("  Owner: {}", owner);
                logger.info("  Type: {}", approvalItem.getType());

                // Approve the work item
                boolean approved = approveWorkItem(approvalItem.getId(),
                    comments != null ? comments : "Approved automatically by test framework - Level " + level);

                if (approved) {
                    approvalsProcessed++;
                    logger.info("✓ Successfully approved level {} by {}", level, owner);
                } else {
                    logger.error("✗ Failed to approve level {} - stopping approval chain", level);
                    return -1;
                }

                // Give workflow time to process and create next approval if needed
                logger.debug("Waiting for workflow to process approval and create next level...");
                try {
                    Thread.sleep(2000); // Wait 2 seconds between approval levels
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while waiting between approval levels");
                    return -1;
                }
            }

            logger.info("========================================");
            logger.info("✓ Automatic approval handling completed");
            logger.info("Total approvals processed: {}", approvalsProcessed);
            logger.info("========================================");

            return approvalsProcessed;

        } catch (Exception e) {
            logger.error("✗ Error during automatic approval handling", e);
            return -1;
        }
    }

    /**
     * Automatically handle all approval work items for a workflow with default settings.
     * Uses 10 second timeout per level and max 5 approval levels.
     *
     * @param workflowCaseId ID of the workflow case
     * @param comments Comments to add for each approval
     * @return Number of approvals processed, -1 if error
     */
    public int handleAllApprovals(String workflowCaseId, String comments) {
        return handleAllApprovals(workflowCaseId, 10, 5, comments);
    }

    /**
     * Automatically reject ALL approval work items for a workflow.
     * This method continuously polls for approval work items and rejects them.
     *
     * Useful for testing rejection scenarios at any approval level.
     *
     * @param workflowCaseId ID of the workflow case
     * @param maxWaitSeconds Maximum seconds to wait for each approval level
     * @param maxApprovalLevels Maximum number of approval levels to handle
     * @param comments Comments to add for each rejection
     * @return Number of rejections processed, -1 if error
     */
    public int handleAllRejections(String workflowCaseId, int maxWaitSeconds, int maxApprovalLevels, String comments) {
        logger.info("========================================");
        logger.info("Starting automatic multi-level rejection handling");
        logger.info("Workflow Case ID: {}", workflowCaseId);
        logger.info("Max wait per level: {}s, Max levels: {}", maxWaitSeconds, maxApprovalLevels);
        logger.info("========================================");

        int rejectionsProcessed = 0;
        long overallStartTime = System.currentTimeMillis();
        long overallTimeout = maxApprovalLevels * maxWaitSeconds * 1000L;

        try {
            for (int level = 1; level <= maxApprovalLevels; level++) {
                // Check overall timeout
                if (System.currentTimeMillis() - overallStartTime >= overallTimeout) {
                    logger.warn("Overall timeout reached after processing {} rejections", rejectionsProcessed);
                    break;
                }

                logger.info("----------------------------------------");
                logger.info("Checking for approval level {} to reject", level);
                logger.info("----------------------------------------");

                // Wait for approval work item
                WorkItem approvalItem = waitForWorkItem(workflowCaseId, WorkItem.Type.Approval, maxWaitSeconds);

                if (approvalItem == null) {
                    logger.info("No more approval work items found");
                    break;
                }

                // Log approval details
                String owner = approvalItem.getOwner() != null ? approvalItem.getOwner().getName() : "Unknown";
                logger.info("✓ Found approval work item at level {}", level);
                logger.info("  Work Item ID: {}", approvalItem.getId());
                logger.info("  Owner: {}", owner);
                logger.info("  Type: {}", approvalItem.getType());

                // Reject the work item
                boolean rejected = rejectWorkItem(approvalItem.getId(),
                    comments != null ? comments : "Rejected automatically by test framework - Level " + level);

                if (rejected) {
                    rejectionsProcessed++;
                    logger.info("✓ Successfully rejected level {} by {}", level, owner);
                    // After first rejection in serial mode, workflow typically terminates
                    logger.info("First rejection completed - workflow will typically terminate now");
                    break;
                } else {
                    logger.error("✗ Failed to reject level {}", level);
                    return -1;
                }
            }

            logger.info("========================================");
            logger.info("✓ Automatic rejection handling completed");
            logger.info("Total rejections processed: {}", rejectionsProcessed);
            logger.info("========================================");

            return rejectionsProcessed;

        } catch (Exception e) {
            logger.error("✗ Error during automatic rejection handling", e);
            return -1;
        }
    }

    /**
     * Automatically reject all approval work items with default settings.
     *
     * @param workflowCaseId ID of the workflow case
     * @param comments Comments to add for each rejection
     * @return Number of rejections processed, -1 if error
     */
    public int handleAllRejections(String workflowCaseId, String comments) {
        return handleAllRejections(workflowCaseId, 10, 5, comments);
    }

    /**
     * Get the current approval level (how many approvals have been completed).
     * Useful for tracking progress in multi-level approval scenarios.
     *
     * @param workflowCaseId ID of the workflow case
     * @return Number of completed approval work items
     */
    public int getCompletedApprovalCount(String workflowCaseId) {
        SailPointContext context = remoteContext.getContext();

        try {
            QueryOptions qo = new QueryOptions();
            Filter filter = Filter.and(
                Filter.eq("workflowCase.id", workflowCaseId),
                Filter.eq("type", WorkItem.Type.Approval),
                Filter.eq("state", WorkItem.State.Finished)
            );
            qo.addFilter(filter);

            List<WorkItem> completedApprovals = context.getObjects(WorkItem.class, qo);
            int count = completedApprovals != null ? completedApprovals.size() : 0;

            logger.debug("Found {} completed approvals for workflow {}", count, workflowCaseId);
            return count;

        } catch (GeneralException e) {
            logger.error("Error getting completed approval count", e);
            return 0;
        }
    }

    /**
     * Check if there are any pending approval work items for the workflow.
     *
     * @param workflowCaseId ID of the workflow case
     * @return true if pending approvals exist, false otherwise
     */
    public boolean hasPendingApprovals(String workflowCaseId) {
        List<WorkItem> approvals = getApprovalWorkItems(workflowCaseId);
        boolean hasPending = !approvals.isEmpty();

        if (hasPending) {
            logger.debug("Workflow {} has {} pending approval(s)", workflowCaseId, approvals.size());
        } else {
            logger.debug("Workflow {} has no pending approvals", workflowCaseId);
        }

        return hasPending;
    }

    /**
     * Approve a single approval level and return detailed result.
     * Useful for step-by-step approval with individual test assertions.
     *
     * @param workflowCaseId ID of the workflow case
     * @param expectedLevel Expected approval level number (for logging/validation)
     * @param maxWaitSeconds Maximum seconds to wait for this approval level
     * @return ApprovalResult with details of the approval, null if no approval found
     */
    public ApprovalResult approveSingleLevel(String workflowCaseId, int expectedLevel, int maxWaitSeconds) {
        logger.info("========================================");
        logger.info("Approval Level {}: Waiting for work item", expectedLevel);
        logger.info("========================================");

        // Wait for approval work item
        WorkItem approvalItem = waitForWorkItem(workflowCaseId, WorkItem.Type.Approval, maxWaitSeconds);

        if (approvalItem == null) {
            logger.info("No approval work item found at level {}", expectedLevel);
            return null;
        }

        // Get approval details
        String owner = approvalItem.getOwner() != null ? approvalItem.getOwner().getName() : "Unknown";
        String workItemId = approvalItem.getId();

        logger.info("✓ Found approval work item at level {}", expectedLevel);
        logger.info("  Work Item ID: {}", workItemId);
        logger.info("  Owner: {}", owner);

        // Approve the work item
        boolean approved = approveWorkItem(workItemId, "Approved by test framework - Level " + expectedLevel);

        if (approved) {
            logger.info("========================================");
            logger.info("✓ LEVEL {} APPROVED by {}", expectedLevel, owner);
            logger.info("========================================");

            return new ApprovalResult(expectedLevel, owner, workItemId, true, null);
        } else {
            logger.error("========================================");
            logger.error("✗ LEVEL {} APPROVAL FAILED", expectedLevel);
            logger.error("========================================");

            return new ApprovalResult(expectedLevel, owner, workItemId, false, "Approval operation failed");
        }
    }

    /**
     * Result of a single approval level operation.
     * Contains all details needed for test assertions.
     */
    public static class ApprovalResult {
        private final int level;
        private final String approverName;
        private final String workItemId;
        private final boolean success;
        private final String errorMessage;

        public ApprovalResult(int level, String approverName, String workItemId, boolean success, String errorMessage) {
            this.level = level;
            this.approverName = approverName;
            this.workItemId = workItemId;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public int getLevel() {
            return level;
        }

        public String getApproverName() {
            return approverName;
        }

        public String getWorkItemId() {
            return workItemId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return String.format("ApprovalResult[level=%d, approver=%s, success=%s]",
                level, approverName, success);
        }
    }
}
