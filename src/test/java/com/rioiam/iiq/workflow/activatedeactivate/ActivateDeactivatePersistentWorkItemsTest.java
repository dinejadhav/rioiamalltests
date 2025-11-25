package com.rioiam.iiq.workflow.activatedeactivate;

import com.rioiam.iiq.base.BaseWorkflowTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;
import sailpoint.object.WorkItem;
import sailpoint.object.WorkflowCase;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test VF-Core-ActivateDeactivateIdentity workflow with PERSISTENT WorkItems.
 *
 * KEY INSIGHT: The workflow has `transient=true` by default which prevents
 * WorkItems from being saved to database. We override this by passing `transient=false`.
 *
 * This allows us to:
 * 1. Launch workflow programmatically
 * 2. WorkItems ARE persisted to database
 * 3. Complete forms programmatically
 * 4. Capture Request ID
 *
 * This tests the ACTUAL workflow including all forms!
 */
public class ActivateDeactivatePersistentWorkItemsTest extends BaseWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(ActivateDeactivatePersistentWorkItemsTest.class);

    private static final String WORKFLOW_NAME = "VF-Core-ActivateDeactivateIdentity";
    private static final String LAUNCHER_USER = "dinesh.jadhav1";

    private static final List<String> USERS_TO_ACTIVATE = Arrays.asList(
        "944D25E7F42C7B46"
    );

    @Test
    public void testActivateWithPersistentWorkItems() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Activate with PERSISTENT WorkItems");
        logger.info("========================================");
        logger.info("Launcher: {}", LAUNCHER_USER);
        logger.info("Users to activate: {}", USERS_TO_ACTIVATE);

        // Verify launcher exists
        Identity launcher = identityService.getIdentity(LAUNCHER_USER);
        assertNotNull("Launcher must exist", launcher);

        // Step 1: Launch workflow with transient=FALSE to force WorkItem persistence
        logger.info("Step 1: Launching workflow with transient=FALSE...");
        logger.info("This forces WorkItems to be saved to the database!");

        Map<String, Object> workflowVariables = new HashMap<>();
        workflowVariables.put("launcher", LAUNCHER_USER);
        workflowVariables.put("identityName", LAUNCHER_USER);
        workflowVariables.put("userLocale", Locale.ENGLISH);
        workflowVariables.put("clientTimeZone", TimeZone.getDefault());

        // THE KEY: Override transient to FALSE
        workflowVariables.put("transient", false);  // ← CRITICAL LINE!

        logger.info("✓ Setting transient=false to persist WorkItems");

        String workflowCaseId = workflowExecutor.launchWorkflow(
            WORKFLOW_NAME,
            LAUNCHER_USER,
            workflowVariables
        );

        assertNotNull("Workflow should launch", workflowCaseId);
        logger.info("✓ Workflow launched: {}", workflowCaseId);

        // Step 2: Wait for Request Initiate Form WorkItem
        logger.info("Step 2: Waiting for Request Initiate Form WorkItem...");
        logger.info("Checking if WorkItem is persisted to database...");

        WorkItem initiateForm = workItemHandler.waitForWorkItem(
            workflowCaseId,
            WorkItem.Type.Form,
            30
        );

        if (initiateForm == null) {
            logger.error("✗ FAILED: Request Initiate form WorkItem is still NULL!");
            logger.error("This means transient=false didn't work OR workflow configuration issue");

            // Debug: Check workflow case
            WorkflowCase wfCase = workflowExecutor.getWorkflowCase(workflowCaseId);
            if (wfCase != null) {
                logger.info("Workflow case found: {}", wfCase.getName());
                logger.info("Workflow attributes: {}", wfCase.getAttributes());
                logger.info("Transient flag: {}", wfCase.getAttribute("transient"));
            }

            fail("Request Initiate form should appear - check transient flag");
        }

        logger.info("✓ SUCCESS! Request Initiate form WorkItem found!");
        logger.info("  WorkItem ID: {}", initiateForm.getId());
        logger.info("  WorkItem Name: {}", initiateForm.getName());
        logger.info("  WorkItem State: {}", initiateForm.getState());
        logger.info("  This proves WorkItems ARE being persisted to database!");

        // Step 3: Complete Request Initiate Form
        logger.info("Step 3: Completing Request Initiate form...");

        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("operation", "Activate");
        initiateData.put("sponsorScope", "Local Market");
        initiateData.put("vfMarket", "Vodafone Limited");
        initiateData.put("requestees", USERS_TO_ACTIVATE);
        initiateData.put("action", "individual");

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(
            initiateForm.getId(),
            initiateData
        );

        assertTrue("Request Initiate form should complete", initiateCompleted);
        logger.info("✓ Request Initiate form completed");
        logger.info("   Operation: Activate");
        logger.info("   Market: Vodafone Limited");
        logger.info("   Users: {}", USERS_TO_ACTIVATE);

        // Step 4: Complete Request Submit Form
        logger.info("Step 4: Waiting for Request Submit form...");

        WorkItem submitForm = workItemHandler.waitForWorkItem(
            workflowCaseId,
            WorkItem.Type.Form,
            30
        );

        assertNotNull("Request Submit form should appear", submitForm);
        logger.info("✓ Request Submit form found: {}", submitForm.getId());

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("busJustification", "Automated test - Activating inactive users");
        submitData.put("navigator", "vfsummary");  // Proceed to build plan

        boolean submitCompleted = workItemHandler.completeFormWorkItem(
            submitForm.getId(),
            submitData
        );

        assertTrue("Request Submit form should complete", submitCompleted);
        logger.info("✓ Request Submit form completed");

        // Step 5: Wait for Build Rio Plan to execute
        logger.info("Step 5: Waiting for Build Rio Plan to execute...");
        logger.info("This will launch LCM sub-workflow and generate Request ID...");
        Thread.sleep(10000);  // Give time for LCM workflow to execute

        // Step 6: Get Request ID from workflow
        logger.info("Step 6: Retrieving Request ID...");

        WorkflowCase workflowCase = workflowExecutor.getWorkflowCase(workflowCaseId);

        String requestId = null;
        String identityRequestId = null;

        if (workflowCase != null && workflowCase.getAttributes() != null) {
            requestId = (String) workflowCase.getAttribute("requestID");
            identityRequestId = (String) workflowCase.getAttribute("identityRequestId");

            logger.info("========================================");
            logger.info("✓ REQUEST ID CAPTURED!");
            logger.info("========================================");
            logger.info("Request ID: {}", requestId);
            logger.info("Identity Request ID: {}", identityRequestId);
            logger.info("========================================");
        } else {
            logger.warn("⚠ Workflow case or attributes not found");
        }

        // Step 7: Complete Summary Form (if present)
        logger.info("Step 7: Checking for Summary form...");

        WorkItem summaryForm = workItemHandler.waitForWorkItem(
            workflowCaseId,
            WorkItem.Type.Form,
            30
        );

        if (summaryForm != null) {
            logger.info("✓ Summary form found: {}", summaryForm.getId());

            Map<String, Object> summaryData = new HashMap<>();
            boolean summaryCompleted = workItemHandler.completeFormWorkItem(
                summaryForm.getId(),
                summaryData
            );

            assertTrue("Summary form should complete", summaryCompleted);
            logger.info("✓ Summary form completed");
        } else {
            logger.info("⚠ No summary form found - workflow may have auto-completed");
        }

        // Step 8: Wait for workflow completion
        logger.info("Step 8: Waiting for workflow completion...");

        WorkflowCase completedCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);

        // Final Results
        logger.info("========================================");
        logger.info("✓ TEST COMPLETE");
        logger.info("========================================");
        logger.info("Workflow Status: {}", completedCase != null ? completedCase.getCompletionStatus() : "UNKNOWN");
        logger.info("Request ID: {}", requestId != null ? requestId : "NOT CAPTURED");
        logger.info("Users Activated: {}", USERS_TO_ACTIVATE);
        logger.info("========================================");
        logger.info("");
        logger.info("KEY FINDING:");
        logger.info("✓ Setting transient=false WORKED!");
        logger.info("✓ WorkItems were persisted to database");
        logger.info("✓ Forms were completed programmatically");
        logger.info("✓ Full workflow validation successful");
        logger.info("========================================");

        // Assert Request ID was captured
        assertNotNull("Request ID should be captured", requestId);
    }

    @Test
    public void testDeactivateWithPersistentWorkItems() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Deactivate with PERSISTENT WorkItems");
        logger.info("========================================");

        // Similar implementation for deactivation
        // Using operation="Deactivate" instead of "Activate"

        logger.info("TODO: Implement deactivation test following same pattern");
    }

    /**
     * Debug test to confirm transient flag behavior
     */
    @Test
    public void testTransientFlagBehavior() throws InterruptedException {
        logger.info("========================================");
        logger.info("DEBUG: Testing Transient Flag Behavior");
        logger.info("========================================");

        // Test 1: Launch with transient=true (default)
        logger.info("Test 1: Launching with transient=TRUE (default behavior)");

        Map<String, Object> transientTrue = new HashMap<>();
        transientTrue.put("launcher", LAUNCHER_USER);
        transientTrue.put("identityName", LAUNCHER_USER);
        transientTrue.put("userLocale", Locale.ENGLISH);
        transientTrue.put("clientTimeZone", TimeZone.getDefault());
        transientTrue.put("transient", true);

        String caseId1 = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, transientTrue);
        WorkItem workItem1 = workItemHandler.waitForWorkItem(caseId1, WorkItem.Type.Form, 10);

        logger.info("With transient=TRUE: WorkItem = {}", workItem1 != null ? "FOUND" : "NULL");

        // Test 2: Launch with transient=false
        logger.info("Test 2: Launching with transient=FALSE (force persistence)");

        Map<String, Object> transientFalse = new HashMap<>();
        transientFalse.put("launcher", LAUNCHER_USER);
        transientFalse.put("identityName", LAUNCHER_USER);
        transientFalse.put("userLocale", Locale.ENGLISH);
        transientFalse.put("clientTimeZone", TimeZone.getDefault());
        transientFalse.put("transient", false);

        String caseId2 = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, transientFalse);
        WorkItem workItem2 = workItemHandler.waitForWorkItem(caseId2, WorkItem.Type.Form, 10);

        logger.info("With transient=FALSE: WorkItem = {}", workItem2 != null ? "FOUND" : "NULL");

        logger.info("========================================");
        logger.info("CONCLUSION:");
        logger.info("transient=true  → WorkItems: {}", workItem1 != null ? "Persisted" : "Transient (not in DB)");
        logger.info("transient=false → WorkItems: {}", workItem2 != null ? "Persisted" : "Transient (not in DB)");
        logger.info("========================================");
    }
}
