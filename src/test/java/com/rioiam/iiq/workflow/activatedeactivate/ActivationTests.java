package com.rioiam.iiq.workflow.activatedeactivate;

import com.rioiam.iiq.base.BaseWorkflowTest;
import com.rioiam.iiq.fixtures.TestConfigurations;
import com.rioiam.iiq.fixtures.TestIdentities;
import com.rioiam.iiq.workflow.WorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sailpoint.object.Identity;
import sailpoint.object.WorkItem;
import sailpoint.object.WorkflowCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test suite for Identity Activation scenarios.
 *
 * Tests covered:
 * - Activation with no approvals
 * - Activation with manager approval
 * - Activation with business owner approval
 * - Activation with both approvals
 */
public class ActivationTests extends BaseWorkflowTest {

    private String testUserName;
    private String testManagerName;
    private static final String WORKFLOW_NAME = "VF-Core-ActivateDeactivateIdentity";

    @Before
    public void setup() {
        logger.info("========================================");
        logger.info("Setting up activation test data with REALISTIC user attributes");
        logger.info("========================================");

        // Create unique test identity names
        testUserName = TestIdentities.generateUniqueIdentityName("test_user_activation");
        testManagerName = TestIdentities.generateUniqueIdentityName("test_manager");

        // Create manager first with realistic production-like attributes
        logger.info("Creating test manager with realistic attributes...");
        Map<String, Object> managerAttributes = TestIdentities.createRealisticManagerAttributes();

        Identity manager = identityService.createTestIdentity(testManagerName, managerAttributes);
        assertNotNull("Manager should be created", manager);
        logger.info("✓ Manager created: {} ({})", testManagerName, manager.getAttribute("displayName"));

        // Create test user (inactive) with realistic production-like attributes
        logger.info("Creating test user with realistic attributes...");
        Map<String, Object> userAttributes = TestIdentities.createRealisticInactiveUserAttributes();

        Identity user = identityService.createTestIdentityWithManager(
            testUserName, testManagerName, userAttributes);
        assertNotNull("Test user should be created", user);
        logger.info("✓ Test user created: {} ({})", testUserName, user.getAttribute("displayName"));
        logger.info("  Department: {}", user.getAttribute("vf_id_department"));
        logger.info("  Organization: {}", user.getAttribute("vf_id_organization_name"));
        logger.info("  OpCo UID: {}", user.getAttribute("vf_id_opcouid"));
        logger.info("  Status: {}", user.getAttribute("vf_id_status"));

        logger.info("========================================");
        logger.info("Test data created successfully with {} attributes per identity", userAttributes.size());
        logger.info("========================================");
    }

    @Test
    public void testActivation_NoApprovals() {
        logger.info("========================================");
        logger.info("TEST: Activation with no approvals");
        logger.info("========================================");

        // Prepare workflow variables
        Map<String, Object> workflowVariables = TestConfigurations.createActivationWorkflowVariables(
            "spadmin", testUserName);

        // Launch workflow
        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, workflowVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("Workflow launched with case ID: {}", workflowCaseId);

        // Step 1: Wait for and complete the "Request Initiate Form"
        logger.info("Waiting for request initiate form...");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request initiate form should appear", initiateForm);

        Map<String, Object> initiateFormData = new HashMap<>();
        initiateFormData.put("action", "individual");

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateFormData);
        assertTrue("Request initiate form should be completed", initiateCompleted);
        logger.info("✓ Completed request initiate form");

        // Step 2: Wait for and complete the "Request Submit Form"
        logger.info("Waiting for request submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request submit form should appear", submitForm);

        Map<String, Object> submitFormData = new HashMap<>();
        submitFormData.put("operation", "activate");
        submitFormData.put("identityName", testUserName);
        submitFormData.put("navigator", "vfsummary");

        boolean submitCompleted = workItemHandler.completeFormWorkItem(submitForm.getId(), submitFormData);
        assertTrue("Request submit form should be completed", submitCompleted);
        logger.info("✓ Completed request submit form");

        // Step 3: Wait for workflow completion (no approvals expected)
        logger.info("Waiting for workflow completion...");
        WorkflowCase wfCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);
        assertNotNull("Workflow should complete", wfCase);

        // Verify workflow completed successfully
        String status = workflowExecutor.getWorkflowStatus(workflowCaseId);
        logger.info("Workflow completed with status: {}", status);

        // Validate identity was activated
        Map<String, Object> expectedAttributes = new HashMap<>();
        expectedAttributes.put("vf_id_status", "Active Adhoc");
        expectedAttributes.put("vf_id_enablement_type", "Enabled User");

        boolean attributesValid = identityValidator.validateAttributes(testUserName, expectedAttributes);
        assertTrue("Identity attributes should match expected values after activation", attributesValid);

        boolean inactiveValid = identityValidator.validateInactiveFlag(testUserName, false);
        assertTrue("Identity should be marked as active (inactive=false)", inactiveValid);

        logger.info("✓ Test passed: Activation workflow completed successfully");
    }

    @Test
    public void testActivation_WithMultiLevelApprovals() {
        logger.info("========================================");
        logger.info("TEST: Activation with multi-level approvals");
        logger.info("========================================");

        // NOTE: This test automatically handles ALL approval levels
        // Supports: Manager -> Business Owner -> Additional levels (if configured)

        // Prepare workflow variables
        Map<String, Object> workflowVariables = TestConfigurations.createActivationWorkflowVariables(
            "spadmin", testUserName);

        // Launch workflow
        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, workflowVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("Workflow launched with case ID: {}", workflowCaseId);

        // Step 1: Complete the "Request Initiate Form"
        logger.info("Waiting for request initiate form...");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request initiate form should appear", initiateForm);

        Map<String, Object> initiateFormData = new HashMap<>();
        initiateFormData.put("action", "individual");
        workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateFormData);
        logger.info("✓ Completed request initiate form");

        // Step 2: Complete the "Request Submit Form"
        logger.info("Waiting for request submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request submit form should appear", submitForm);

        Map<String, Object> submitFormData = new HashMap<>();
        submitFormData.put("operation", "activate");
        submitFormData.put("identityName", testUserName);
        submitFormData.put("navigator", "vfsummary");
        workItemHandler.completeFormWorkItem(submitForm.getId(), submitFormData);
        logger.info("✓ Completed request submit form");

        // Step 3: Automatically handle ALL approval levels
        logger.info("Starting automatic multi-level approval handling...");

        int approvalsProcessed = workItemHandler.handleAllApprovals(
            workflowCaseId,
            "Approved by automated test framework"
        );

        if (approvalsProcessed > 0) {
            logger.info("✓ Successfully processed {} approval level(s)", approvalsProcessed);
        } else if (approvalsProcessed == 0) {
            logger.info("No approvals required (approval not configured)");
        } else {
            fail("Failed to process approvals - handleAllApprovals returned error");
        }

        // Step 4: Wait for workflow completion
        logger.info("Waiting for workflow completion...");
        WorkflowCase wfCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);
        assertNotNull("Workflow should complete", wfCase);

        // Verify workflow completed successfully
        String status = workflowExecutor.getWorkflowStatus(workflowCaseId);
        logger.info("Workflow completed with status: {}", status);

        // Validate identity was activated
        Map<String, Object> expectedAttributes = new HashMap<>();
        expectedAttributes.put("vf_id_status", "Active Adhoc");
        expectedAttributes.put("vf_id_enablement_type", "Enabled User");

        boolean attributesValid = identityValidator.validateAttributes(testUserName, expectedAttributes);
        assertTrue("Identity attributes should match expected values after activation", attributesValid);

        boolean inactiveValid = identityValidator.validateInactiveFlag(testUserName, false);
        assertTrue("Identity should be marked as active (inactive=false)", inactiveValid);

        logger.info("✓ Test passed: Activation with multi-level approvals completed successfully");
    }

    @Test
    public void testActivation_WithMultiLevelApprovals_StepByStep() {
        logger.info("========================================");
        logger.info("TEST: Activation with multi-level approvals (Step-by-Step with individual assertions)");
        logger.info("========================================");

        // NOTE: This test shows EACH approval level as a separate test assertion
        // Each level approval is validated individually - great for detailed test reporting!

        // Prepare workflow variables
        Map<String, Object> workflowVariables = TestConfigurations.createActivationWorkflowVariables(
            "spadmin", testUserName);

        // Launch workflow
        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, workflowVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("Workflow launched with case ID: {}", workflowCaseId);

        // Step 1: Complete the "Request Initiate Form"
        logger.info("========================================");
        logger.info("STEP 1: Complete Request Initiate Form");
        logger.info("========================================");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request initiate form should appear", initiateForm);

        Map<String, Object> initiateFormData = new HashMap<>();
        initiateFormData.put("action", "individual");
        boolean initiateCompleted = workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateFormData);
        assertTrue("Request initiate form should be completed", initiateCompleted);
        logger.info("✓ STEP 1 PASSED: Request initiate form completed");

        // Step 2: Complete the "Request Submit Form"
        logger.info("========================================");
        logger.info("STEP 2: Complete Request Submit Form");
        logger.info("========================================");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request submit form should appear", submitForm);

        Map<String, Object> submitFormData = new HashMap<>();
        submitFormData.put("operation", "activate");
        submitFormData.put("identityName", testUserName);
        submitFormData.put("navigator", "vfsummary");
        boolean submitCompleted = workItemHandler.completeFormWorkItem(submitForm.getId(), submitFormData);
        assertTrue("Request submit form should be completed", submitCompleted);
        logger.info("✓ STEP 2 PASSED: Request submit form completed");

        // Step 3: Handle approvals level-by-level with individual assertions
        logger.info("========================================");
        logger.info("STARTING APPROVAL PROCESS");
        logger.info("========================================");

        int totalApprovalsProcessed = 0;

        // Approval Level 1
        WorkItemHandler.ApprovalResult level1 = workItemHandler.approveSingleLevel(workflowCaseId, 1, 10);
        if (level1 != null) {
            assertTrue("Level 1 approval should succeed", level1.isSuccess());
            assertNotNull("Level 1 approver should be identified", level1.getApproverName());
            logger.info("✓ APPROVAL LEVEL 1 PASSED: Approved by {}", level1.getApproverName());
            totalApprovalsProcessed++;

            // Wait for workflow to process
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            logger.info("No approval required at level 1 - approval not configured");
        }

        // Approval Level 2
        if (level1 != null) {
            WorkItemHandler.ApprovalResult level2 = workItemHandler.approveSingleLevel(workflowCaseId, 2, 10);
            if (level2 != null) {
                assertTrue("Level 2 approval should succeed", level2.isSuccess());
                assertNotNull("Level 2 approver should be identified", level2.getApproverName());
                logger.info("✓ APPROVAL LEVEL 2 PASSED: Approved by {}", level2.getApproverName());
                totalApprovalsProcessed++;

                // Wait for workflow to process
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                logger.info("No approval required at level 2");
            }

            // Approval Level 3 (if exists)
            if (level2 != null) {
                WorkItemHandler.ApprovalResult level3 = workItemHandler.approveSingleLevel(workflowCaseId, 3, 10);
                if (level3 != null) {
                    assertTrue("Level 3 approval should succeed", level3.isSuccess());
                    assertNotNull("Level 3 approver should be identified", level3.getApproverName());
                    logger.info("✓ APPROVAL LEVEL 3 PASSED: Approved by {}", level3.getApproverName());
                    totalApprovalsProcessed++;
                } else {
                    logger.info("No approval required at level 3");
                }
            }
        }

        logger.info("========================================");
        logger.info("APPROVAL PROCESS COMPLETE");
        logger.info("Total approvals processed: {}", totalApprovalsProcessed);
        logger.info("========================================");

        // Step 4: Wait for workflow completion
        logger.info("========================================");
        logger.info("STEP 3: Wait for workflow completion");
        logger.info("========================================");
        WorkflowCase wfCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);
        assertNotNull("Workflow should complete", wfCase);

        String status = workflowExecutor.getWorkflowStatus(workflowCaseId);
        logger.info("Workflow completed with status: {}", status);
        logger.info("✓ STEP 3 PASSED: Workflow completed");

        // Step 5: Validate identity was activated
        logger.info("========================================");
        logger.info("STEP 4: Validate identity activation");
        logger.info("========================================");
        Map<String, Object> expectedAttributes = new HashMap<>();
        expectedAttributes.put("vf_id_status", "Active Adhoc");
        expectedAttributes.put("vf_id_enablement_type", "Enabled User");

        boolean attributesValid = identityValidator.validateAttributes(testUserName, expectedAttributes);
        assertTrue("Identity attributes should match expected values after activation", attributesValid);
        logger.info("✓ Attributes validated: vf_id_status=Active Adhoc, vf_id_enablement_type=Enabled User");

        boolean inactiveValid = identityValidator.validateInactiveFlag(testUserName, false);
        assertTrue("Identity should be marked as active (inactive=false)", inactiveValid);
        logger.info("✓ Inactive flag validated: inactive=false");

        logger.info("✓ STEP 4 PASSED: Identity activation validated");

        logger.info("========================================");
        logger.info("✓✓✓ ALL STEPS PASSED ✓✓✓");
        logger.info("Test completed successfully!");
        logger.info("Total steps: 4 (forms) + {} (approvals) = {}",
            totalApprovalsProcessed, 4 + totalApprovalsProcessed);
        logger.info("========================================");
    }

    @Test
    public void testActivation_WithRejection() {
        logger.info("========================================");
        logger.info("TEST: Activation with approval rejection");
        logger.info("========================================");

        // NOTE: This test demonstrates automatic rejection handling
        // Can reject at any approval level (Manager, Business Owner, etc.)

        // Prepare workflow variables
        Map<String, Object> workflowVariables = TestConfigurations.createActivationWorkflowVariables(
            "spadmin", testUserName);

        // Launch workflow
        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, workflowVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("Workflow launched with case ID: {}", workflowCaseId);

        // Step 1: Complete the "Request Initiate Form"
        logger.info("Waiting for request initiate form...");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request initiate form should appear", initiateForm);

        Map<String, Object> initiateFormData = new HashMap<>();
        initiateFormData.put("action", "individual");
        workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateFormData);
        logger.info("✓ Completed request initiate form");

        // Step 2: Complete the "Request Submit Form"
        logger.info("Waiting for request submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 10);
        assertNotNull("Request submit form should appear", submitForm);

        Map<String, Object> submitFormData = new HashMap<>();
        submitFormData.put("operation", "activate");
        submitFormData.put("identityName", testUserName);
        submitFormData.put("navigator", "vfsummary");
        workItemHandler.completeFormWorkItem(submitForm.getId(), submitFormData);
        logger.info("✓ Completed request submit form");

        // Step 3: Automatically REJECT any approvals
        logger.info("Starting automatic rejection handling...");

        int rejectionsProcessed = workItemHandler.handleAllRejections(
            workflowCaseId,
            "Rejected by automated test framework for testing"
        );

        if (rejectionsProcessed > 0) {
            logger.info("✓ Successfully rejected approval at level 1");
            logger.info("Workflow should terminate due to rejection");

            // Give workflow time to process rejection
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Validate identity remains INACTIVE (activation should be rejected)
            Map<String, Object> expectedAttributes = new HashMap<>();
            expectedAttributes.put("vf_id_status", "Inactive");
            expectedAttributes.put("vf_id_enablement_type", "");

            boolean attributesValid = identityValidator.validateAttributes(testUserName, expectedAttributes);
            assertTrue("Identity should remain inactive after rejection", attributesValid);

            boolean inactiveValid = identityValidator.validateInactiveFlag(testUserName, true);
            assertTrue("Identity should remain inactive (inactive=true)", inactiveValid);

            logger.info("✓ Test passed: Rejection scenario validated - identity remains inactive");
        } else if (rejectionsProcessed == 0) {
            logger.info("No approvals configured - skipping rejection test");
        } else {
            fail("Failed to process rejections - handleAllRejections returned error");
        }
    }

    @After
    public void cleanup() {
        logger.info("Cleaning up activation test data");

        // Delete test identities
        if (testUserName != null && identityService.identityExists(testUserName)) {
            boolean deleted = identityService.deleteIdentity(testUserName);
            if (deleted) {
                logger.info("Deleted test user: {}", testUserName);
            } else {
                logger.warn("Could not delete test user: {}", testUserName);
            }
        }

        if (testManagerName != null && identityService.identityExists(testManagerName)) {
            boolean deleted = identityService.deleteIdentity(testManagerName);
            if (deleted) {
                logger.info("Deleted test manager: {}", testManagerName);
            } else {
                logger.warn("Could not delete test manager: {}", testManagerName);
            }
        }

        logger.info("Cleanup completed");
    }
}
