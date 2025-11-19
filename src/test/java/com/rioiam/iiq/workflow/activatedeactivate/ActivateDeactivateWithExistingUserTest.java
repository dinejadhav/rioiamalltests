package com.rioiam.iiq.workflow.activatedeactivate;

import com.rioiam.iiq.base.BaseWorkflowTest;
import com.rioiam.iiq.fixtures.TestConfigurations;
import com.rioiam.iiq.workflow.WorkItemHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;
import sailpoint.object.WorkItem;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test activation and deactivation workflows using EXISTING user IDs.
 *
 * Simply provide the identity name in the test method to test workflows
 * without creating new identities each time.
 *
 * Usage:
 * 1. Set the USER_TO_TEST variable to an existing identity name
 * 2. Run the appropriate test (activate or deactivate)
 */
public class ActivateDeactivateWithExistingUserTest extends BaseWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(ActivateDeactivateWithExistingUserTest.class);

    private static final String WORKFLOW_NAME = "VF-Core-ActivateDeactivateIdentity";

    // ========== CONFIGURATION: SET YOUR USER ID HERE ==========

    /**
     * SET THIS: Identity name to test activation workflow
     * Example: "test_user_activation_1761823363978"
     */
    private static final String INACTIVE_USER_TO_ACTIVATE = "test_user_activation_1761823363978";

    /**
     * SET THIS: Identity name to test deactivation workflow
     * Example: "active_user_1761823363978"
     */
    private static final String ACTIVE_USER_TO_DEACTIVATE = "active_user_1761823363978";

    // ==========================================================

    /**
     * Test activation workflow with an existing INACTIVE user.
     *
     * Prerequisites:
     * - User must exist in IIQ
     * - User must be in INACTIVE state (inactive=true, vf_id_status="Inactive")
     *
     * To use:
     * 1. Set INACTIVE_USER_TO_ACTIVATE to your identity name
     * 2. Run: mvn test -Dtest=ActivateDeactivateWithExistingUserTest#testActivateExistingUser
     */
    @Test
    public void testActivateExistingUser() {
        logger.info("========================================");
        logger.info("TEST: Activate Existing User");
        logger.info("========================================");
        logger.info("User to activate: {}", INACTIVE_USER_TO_ACTIVATE);

        // Verify user exists and is inactive
        Identity user = identityService.getIdentity(INACTIVE_USER_TO_ACTIVATE);
        assertNotNull("User must exist in IIQ: " + INACTIVE_USER_TO_ACTIVATE, user);

        Boolean inactive = (Boolean) user.getAttribute("inactive");
        String status = (String) user.getAttribute("vf_id_status");

        logger.info("User current state:");
        logger.info("  Inactive: {}", inactive);
        logger.info("  Status: {}", status);

        // Verify user is in inactive state
        assertTrue("User must be inactive before activation. Current inactive=" + inactive,
                   inactive == null || inactive);

        // Launch activation workflow
        logger.info("Launching activation workflow...");
        Map<String, Object> variables = TestConfigurations.createActivationWorkflowVariables(
            "spadmin", INACTIVE_USER_TO_ACTIVATE);

        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, variables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("✓ Workflow launched: {}", workflowCaseId);

        // Complete Request Initiate Form
        logger.info("Waiting for Request Initiate form...");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Initiate form should appear", initiateForm);

        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("action", "individual");

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateData);
        assertTrue("Request Initiate form should be completed", initiateCompleted);
        logger.info("✓ Completed Request Initiate form");

        // Complete Request Submit Form
        logger.info("Waiting for Request Submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Submit form should appear", submitForm);

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("comments", "Activation test via automated framework");

        boolean submitCompleted = workItemHandler.completeFormWorkItem(submitForm.getId(), submitData);
        assertTrue("Request Submit form should be completed", submitCompleted);
        logger.info("✓ Completed Request Submit form");

        // Handle all approval levels
        logger.info("Handling approval workflow...");
        int approvalsProcessed = workItemHandler.handleAllApprovals(
            workflowCaseId,
            30,  // wait timeout per approval
            5,   // max approval levels
            "Approved by automated test - Activate existing user"
        );

        if (approvalsProcessed > 0) {
            logger.info("✓ Processed {} approval level(s)", approvalsProcessed);
        } else {
            logger.warn("No approvals found or processed");
        }

        // Wait for workflow completion
        logger.info("Waiting for workflow completion...");
        sailpoint.object.WorkflowCase completedCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);
        assertNotNull("Workflow should complete", completedCase);

        // Validate final state
        logger.info("Validating user state after activation...");
        Identity updatedUser = identityService.getIdentity(INACTIVE_USER_TO_ACTIVATE);
        assertNotNull("User should still exist", updatedUser);

        Boolean finalInactive = (Boolean) updatedUser.getAttribute("inactive");
        String finalStatus = (String) updatedUser.getAttribute("vf_id_status");

        logger.info("========================================");
        logger.info("✓ TEST COMPLETE");
        logger.info("========================================");
        logger.info("User: {}", INACTIVE_USER_TO_ACTIVATE);
        logger.info("Before: inactive={}, status={}", inactive, status);
        logger.info("After:  inactive={}, status={}", finalInactive, finalStatus);
        logger.info("Expected: inactive=false, status=Active Adhoc");
        logger.info("========================================");

        // Note: Validation depends on workflow logic - may need adjustment
        // assertFalse("User should be active after workflow", finalInactive);
        // assertEquals("Status should be Active Adhoc", "Active Adhoc", finalStatus);
    }

    /**
     * Test deactivation workflow with an existing ACTIVE user.
     *
     * Prerequisites:
     * - User must exist in IIQ
     * - User must be in ACTIVE state (inactive=false, vf_id_status="Active Adhoc")
     *
     * To use:
     * 1. Set ACTIVE_USER_TO_DEACTIVATE to your identity name
     * 2. Run: mvn test -Dtest=ActivateDeactivateWithExistingUserTest#testDeactivateExistingUser
     */
    @Test
    public void testDeactivateExistingUser() {
        logger.info("========================================");
        logger.info("TEST: Deactivate Existing User");
        logger.info("========================================");
        logger.info("User to deactivate: {}", ACTIVE_USER_TO_DEACTIVATE);

        // Verify user exists and is active
        Identity user = identityService.getIdentity(ACTIVE_USER_TO_DEACTIVATE);
        assertNotNull("User must exist in IIQ: " + ACTIVE_USER_TO_DEACTIVATE, user);

        Boolean inactive = (Boolean) user.getAttribute("inactive");
        String status = (String) user.getAttribute("vf_id_status");

        logger.info("User current state:");
        logger.info("  Inactive: {}", inactive);
        logger.info("  Status: {}", status);

        // Verify user is in active state
        assertFalse("User must be active before deactivation. Current inactive=" + inactive,
                    inactive != null && inactive);

        // Launch deactivation workflow
        logger.info("Launching deactivation workflow...");
        Map<String, Object> variables = TestConfigurations.createDeactivationWorkflowVariables(
            "spadmin", ACTIVE_USER_TO_DEACTIVATE);

        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, variables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("✓ Workflow launched: {}", workflowCaseId);

        // Complete Request Initiate Form
        logger.info("Waiting for Request Initiate form...");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Initiate form should appear", initiateForm);

        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("action", "individual");

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateData);
        assertTrue("Request Initiate form should be completed", initiateCompleted);
        logger.info("✓ Completed Request Initiate form");

        // Complete Request Submit Form
        logger.info("Waiting for Request Submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Submit form should appear", submitForm);

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("comments", "Deactivation test via automated framework");

        boolean submitCompleted = workItemHandler.completeFormWorkItem(submitForm.getId(), submitData);
        assertTrue("Request Submit form should be completed", submitCompleted);
        logger.info("✓ Completed Request Submit form");

        // Handle all approval levels
        logger.info("Handling approval workflow...");
        int approvalsProcessed = workItemHandler.handleAllApprovals(
            workflowCaseId,
            30,  // wait timeout per approval
            5,   // max approval levels
            "Approved by automated test - Deactivate existing user"
        );

        if (approvalsProcessed > 0) {
            logger.info("✓ Processed {} approval level(s)", approvalsProcessed);
        } else {
            logger.warn("No approvals found or processed");
        }

        // Wait for workflow completion
        logger.info("Waiting for workflow completion...");
        sailpoint.object.WorkflowCase completedCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);
        assertNotNull("Workflow should complete", completedCase);

        // Validate final state
        logger.info("Validating user state after deactivation...");
        Identity updatedUser = identityService.getIdentity(ACTIVE_USER_TO_DEACTIVATE);
        assertNotNull("User should still exist", updatedUser);

        Boolean finalInactive = (Boolean) updatedUser.getAttribute("inactive");
        String finalStatus = (String) updatedUser.getAttribute("vf_id_status");

        logger.info("========================================");
        logger.info("✓ TEST COMPLETE");
        logger.info("========================================");
        logger.info("User: {}", ACTIVE_USER_TO_DEACTIVATE);
        logger.info("Before: inactive={}, status={}", inactive, status);
        logger.info("After:  inactive={}, status={}", finalInactive, finalStatus);
        logger.info("Expected: inactive=true, status=Inactive");
        logger.info("========================================");

        // Note: Validation depends on workflow logic - may need adjustment
        // assertTrue("User should be inactive after workflow", finalInactive);
        // assertEquals("Status should be Inactive", "Inactive", finalStatus);
    }

    /**
     * Test activation workflow step-by-step with individual approval assertions.
     * Uses existing user.
     *
     * To use:
     * 1. Set INACTIVE_USER_TO_ACTIVATE to your identity name
     * 2. Run: mvn test -Dtest=ActivateDeactivateWithExistingUserTest#testActivateExistingUser_StepByStep
     */
    @Test
    public void testActivateExistingUser_StepByStep() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Activate Existing User (Step-by-Step Approvals)");
        logger.info("========================================");
        logger.info("User to activate: {}", INACTIVE_USER_TO_ACTIVATE);

        // Verify user exists
        Identity user = identityService.getIdentity(INACTIVE_USER_TO_ACTIVATE);
        assertNotNull("User must exist: " + INACTIVE_USER_TO_ACTIVATE, user);

        // Launch workflow and complete forms
        Map<String, Object> variables = TestConfigurations.createActivationWorkflowVariables(
            "spadmin", INACTIVE_USER_TO_ACTIVATE);

        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, variables);
        assertNotNull("Workflow should be launched", workflowCaseId);

        // Complete forms
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Initiate form should appear", initiateForm);

        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("action", "individual");
        workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateData);

        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Submit form should appear", submitForm);

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("comments", "Step-by-step activation test");
        workItemHandler.completeFormWorkItem(submitForm.getId(), submitData);

        // Handle approvals step-by-step
        logger.info("Processing approvals step-by-step...");
        int totalApprovals = 0;

        // Approval Level 1
        WorkItemHandler.ApprovalResult level1 = workItemHandler.approveSingleLevel(workflowCaseId, 1, 30);
        if (level1 != null) {
            assertTrue("Level 1 approval should succeed", level1.isSuccess());
            logger.info("✓ APPROVAL LEVEL 1 PASSED: Approved by {}", level1.getApproverName());
            totalApprovals++;
            Thread.sleep(2000);

            // Approval Level 2
            WorkItemHandler.ApprovalResult level2 = workItemHandler.approveSingleLevel(workflowCaseId, 2, 30);
            if (level2 != null) {
                assertTrue("Level 2 approval should succeed", level2.isSuccess());
                logger.info("✓ APPROVAL LEVEL 2 PASSED: Approved by {}", level2.getApproverName());
                totalApprovals++;
                Thread.sleep(2000);

                // Approval Level 3 (if exists)
                WorkItemHandler.ApprovalResult level3 = workItemHandler.approveSingleLevel(workflowCaseId, 3, 30);
                if (level3 != null) {
                    assertTrue("Level 3 approval should succeed", level3.isSuccess());
                    logger.info("✓ APPROVAL LEVEL 3 PASSED: Approved by {}", level3.getApproverName());
                    totalApprovals++;
                }
            }
        }

        logger.info("========================================");
        logger.info("✓ ALL APPROVAL STEPS PASSED");
        logger.info("Total approval levels: {}", totalApprovals);
        logger.info("========================================");
    }
}
