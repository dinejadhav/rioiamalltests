package com.rioiam.iiq.workflow.activatedeactivate;

import com.rioiam.iiq.base.BaseWorkflowTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.Identity;
import sailpoint.object.WorkItem;
import sailpoint.object.WorkflowCase;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test with FULL TRACE enabled to diagnose form submission issues.
 *
 * This test will:
 * 1. Enable trace=true in workflow
 * 2. Launch workflow
 * 3. Wait for WorkItem
 * 4. Complete form with detailed logging
 * 5. Check workflow advancement
 */
public class ActivateDeactivateWithTraceTest extends BaseWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(ActivateDeactivateWithTraceTest.class);

    private static final String WORKFLOW_NAME = "VF-Core-ActivateDeactivateIdentity";
    private static final String LAUNCHER_USER = "dinesh.jadhav1";

    private static final List<String> USERS_TO_ACTIVATE = Arrays.asList(
        "944D25E7F42C7B46"
    );

    @Test
    public void testActivateWithFullTrace() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Activate with FULL TRACE");
        logger.info("========================================");
        logger.info("This test enables trace=true to see detailed workflow execution logs");
        logger.info("Check IIQ logs: /Users/dineshjadhav/Sailpoint/iiq85/tomcat/logs/sailpoint.log");
        logger.info("========================================");

        // Verify launcher exists
        Identity launcher = identityService.getIdentity(LAUNCHER_USER);
        assertNotNull("Launcher must exist", launcher);
        logger.info("✓ Launcher: {} ({})", launcher.getName(), launcher.getDisplayName());

        // Step 1: Launch workflow with TRACE enabled
        logger.info("\nStep 1: Launching workflow with trace=TRUE...");

        Map<String, Object> workflowVariables = new HashMap<>();
        workflowVariables.put("launcher", LAUNCHER_USER);
        workflowVariables.put("identityName", LAUNCHER_USER);
        workflowVariables.put("userLocale", Locale.ENGLISH);
        workflowVariables.put("clientTimeZone", TimeZone.getDefault());

        // ENABLE TRACE!
        workflowVariables.put("trace", true);  // ← THIS ENABLES DETAILED LOGGING

        // Also try forcing non-transient
        workflowVariables.put("transient", false);

        logger.info("✓ trace=true (detailed workflow logging enabled)");
        logger.info("✓ transient=false (attempting to persist WorkItems)");

        String workflowCaseId = workflowExecutor.launchWorkflow(
            WORKFLOW_NAME,
            LAUNCHER_USER,
            workflowVariables
        );

        assertNotNull("Workflow should launch", workflowCaseId);
        logger.info("✓ Workflow launched: {}", workflowCaseId);
        logger.info("CHECK IIQ LOGS NOW: grep 'VF-Core-ActivateDeactivateIdentity' /Users/dineshjadhav/Sailpoint/iiq85/tomcat/logs/sailpoint.log");

        // Step 2: Wait for WorkItem
        logger.info("\nStep 2: Waiting for Request Initiate Form WorkItem...");
        logger.info("Waiting 15 seconds for WorkItem to be created...");

        WorkItem initiateForm = workItemHandler.waitForWorkItem(
            workflowCaseId,
            WorkItem.Type.Form,
            15
        );

        if (initiateForm == null) {
            logger.error("\n✗✗✗ CRITICAL: Request Initiate form WorkItem is NULL!");
            logger.error("This confirms WorkItems are NOT persisted to database.");
            logger.error("");
            logger.error("Debug Information:");

            // Check workflow case status
            WorkflowCase wfCase = workflowExecutor.getWorkflowCase(workflowCaseId);
            if (wfCase != null) {
                logger.error("  Workflow Case: {}", wfCase.getName());
                logger.error("  Workflow Status: {}", wfCase.getCompletionStatus());
                logger.error("  Workflow Attributes:");
                if (wfCase.getAttributes() != null) {
                    for (Map.Entry<String, Object> entry : wfCase.getAttributes().entrySet()) {
                        logger.error("    {}: {}", entry.getKey(), entry.getValue());
                    }
                }
            } else {
                logger.error("  Workflow Case NOT FOUND!");
            }

            logger.error("");
            logger.error("NEXT STEPS TO DEBUG:");
            logger.error("1. Check IIQ logs: tail -f /Users/dineshjadhav/Sailpoint/iiq85/tomcat/logs/sailpoint.log");
            logger.error("2. Look for workflow trace messages");
            logger.error("3. Check if workflow reached 'Show Request Initiate Form' step");
            logger.error("4. Look for any errors or exceptions");
            logger.error("");

            fail("Request Initiate form WorkItem should appear - check trace logs");
        }

        logger.info("\n✓✓✓ SUCCESS! WorkItem found!");
        logger.info("  WorkItem ID: {}", initiateForm.getId());
        logger.info("  WorkItem Type: {}", initiateForm.getType());
        logger.info("  WorkItem Name: {}", initiateForm.getName());
        logger.info("  WorkItem State: {}", initiateForm.getState());
        logger.info("  WorkItem Owner: {}", initiateForm.getOwner());
        logger.info("  WorkItem Created: {}", initiateForm.getCreated());

        // Step 3: Examine WorkItem attributes
        logger.info("\nStep 3: Examining WorkItem attributes...");

        Map<String, Object> wiAttributes = initiateForm.getAttributes();
        if (wiAttributes != null) {
            logger.info("WorkItem has {} attributes:", wiAttributes.size());
            for (Map.Entry<String, Object> entry : wiAttributes.entrySet()) {
                logger.info("  {}: {}", entry.getKey(), entry.getValue());
            }
        } else {
            logger.warn("WorkItem has NO attributes");
        }

        // Step 4: Complete the form
        logger.info("\nStep 4: Completing Request Initiate form...");

        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("operation", "Activate");
        initiateData.put("sponsorScope", "Local Market");
        initiateData.put("vfMarket", "Vodafone Limited");
        initiateData.put("requestees", USERS_TO_ACTIVATE);
        initiateData.put("action", "individual");

        logger.info("Form data to submit:");
        for (Map.Entry<String, Object> entry : initiateData.entrySet()) {
            logger.info("  {}: {}", entry.getKey(), entry.getValue());
        }

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(
            initiateForm.getId(),
            initiateData
        );

        assertTrue("Request Initiate form should complete", initiateCompleted);
        logger.info("✓ Form completion API call returned: {}", initiateCompleted);

        // Step 5: Wait and check if workflow advanced
        logger.info("\nStep 5: Checking if workflow advanced...");
        logger.info("Waiting 10 seconds for workflow to process form completion...");
        Thread.sleep(10000);

        // Check workflow case again
        WorkflowCase wfCaseAfter = workflowExecutor.getWorkflowCase(workflowCaseId);
        if (wfCaseAfter != null) {
            logger.info("Workflow case after form completion:");
            logger.info("  Name: {}", wfCaseAfter.getName());
            logger.info("  Status: {}", wfCaseAfter.getCompletionStatus());

            logger.info("  Attributes after form:");
            if (wfCaseAfter.getAttributes() != null) {
                for (Map.Entry<String, Object> entry : wfCaseAfter.getAttributes().entrySet()) {
                    logger.info("    {}: {}", entry.getKey(), entry.getValue());
                }
            }
        }

        // Step 6: Look for next WorkItem
        logger.info("\nStep 6: Looking for Request Submit form...");

        WorkItem submitForm = workItemHandler.waitForWorkItem(
            workflowCaseId,
            WorkItem.Type.Form,
            15
        );

        if (submitForm != null) {
            logger.info("✓✓✓ REQUEST SUBMIT FORM FOUND!");
            logger.info("  WorkItem ID: {}", submitForm.getId());
            logger.info("  This means workflow DID advance after form completion!");
            logger.info("  Form submission is WORKING!");
        } else {
            logger.warn("✗ Request Submit form NOT found");
            logger.warn("Workflow may not have advanced");
            logger.warn("CHECK TRACE LOGS to see why workflow didn't continue");
        }

        logger.info("\n========================================");
        logger.info("TEST COMPLETE");
        logger.info("========================================");
        logger.info("Check IIQ logs for detailed trace:");
        logger.info("  tail -f /Users/dineshjadhav/Sailpoint/iiq85/tomcat/logs/sailpoint.log");
        logger.info("========================================");
    }

    /**
     * Simpler test - just check if workflow launches and trace works
     */
    @Test
    public void testWorkflowLaunchWithTrace() {
        logger.info("========================================");
        logger.info("SIMPLE TEST: Launch workflow with trace");
        logger.info("========================================");

        Map<String, Object> vars = new HashMap<>();
        vars.put("launcher", LAUNCHER_USER);
        vars.put("identityName", LAUNCHER_USER);
        vars.put("userLocale", Locale.ENGLISH);
        vars.put("clientTimeZone", TimeZone.getDefault());
        vars.put("trace", true);

        String caseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, vars);
        assertNotNull("Workflow should launch", caseId);

        logger.info("✓ Workflow launched: {}", caseId);
        logger.info("");
        logger.info("NOW CHECK IIQ LOGS:");
        logger.info("  tail -100 /Users/dineshjadhav/Sailpoint/iiq85/tomcat/logs/sailpoint.log | grep -A 5 -B 5 '{}'", caseId);
        logger.info("");
        logger.info("Look for:");
        logger.info("  - Workflow steps being executed");
        logger.info("  - 'Show Request Initiate Form' step");
        logger.info("  - Any errors or warnings");
        logger.info("========================================");
    }
}
