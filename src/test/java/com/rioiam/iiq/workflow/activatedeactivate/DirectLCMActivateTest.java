package com.rioiam.iiq.workflow.activatedeactivate;

import com.rioiam.iiq.base.BaseWorkflowTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningProject;
import sailpoint.object.WorkflowCase;
import sailpoint.tools.Util;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Direct LCM workflow test - bypasses UI forms entirely
 * Calls the LCM sub-workflow directly to generate Request ID
 */
public class DirectLCMActivateTest extends BaseWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(DirectLCMActivateTest.class);

    private static final String LAUNCHER_USER = "dinesh.jadhav1";
    private static final String LCM_WORKFLOW = "VF-Core-ActivateDeactivateIdentity-LCMCreateandUpdate";

    private static final List<String> USERS_TO_ACTIVATE = Arrays.asList(
        "944D25E7F42C7B46"
    );

    @Test
    public void testDirectLCMActivation() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Direct LCM Activation (Bypass Forms)");
        logger.info("========================================");
        logger.info("Launcher: {}", LAUNCHER_USER);
        logger.info("Users: {}", USERS_TO_ACTIVATE);

        // Get the identity to activate
        String userToActivate = USERS_TO_ACTIVATE.get(0);
        Identity identity = identityService.getIdentity(userToActivate);
        assertNotNull("User must exist", identity);

        logger.info("Activating user: {} ({})", identity.getName(), identity.getDisplayName());

        // Create provisioning plan for activation
        ProvisioningPlan plan = new ProvisioningPlan();
        plan.setIdentity(identity);

        // Add account request for activation
        ProvisioningPlan.AccountRequest accountReq = new ProvisioningPlan.AccountRequest();
        accountReq.setOperation(ProvisioningPlan.AccountRequest.Operation.Modify);
        accountReq.setApplication("IIQ");  // Assuming IIQ application

        // Set attributes for activation
        ProvisioningPlan.AttributeRequest attrReq1 = new ProvisioningPlan.AttributeRequest();
        attrReq1.setName("inactive");
        attrReq1.setValue(false);
        accountReq.add(attrReq1);

        ProvisioningPlan.AttributeRequest attrReq2 = new ProvisioningPlan.AttributeRequest();
        attrReq2.setName("vf_id_status");
        attrReq2.setValue("Active Adhoc");
        accountReq.add(attrReq2);

        plan.add(accountReq);

        // Launch LCM workflow directly with the plan
        Map<String, Object> lcmVariables = new HashMap<>();
        lcmVariables.put("plan", plan);
        lcmVariables.put("identityName", userToActivate);
        lcmVariables.put("launcher", LAUNCHER_USER);
        lcmVariables.put("flow", "Activate");
        lcmVariables.put("trace", true);

        logger.info("Launching LCM workflow directly...");
        String workflowCaseId = workflowExecutor.launchWorkflow(LCM_WORKFLOW, LAUNCHER_USER, lcmVariables);
        assertNotNull("LCM workflow should launch", workflowCaseId);
        logger.info("✓ LCM Workflow launched: {}", workflowCaseId);

        // Wait for workflow to process
        logger.info("Waiting for LCM workflow to complete...");
        WorkflowCase completedCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);

        if (completedCase != null) {
            // Extract request ID from task result
            String requestId = Util.otoa(completedCase.getTaskResult().get("identityRequestId"));

            logger.info("========================================");
            logger.info("✓ REQUEST ID CAPTURED");
            logger.info("========================================");
            logger.info("Request ID: {}", requestId);
            logger.info("Workflow Status: {}", completedCase.getCompletionStatus());
            logger.info("========================================");

            assertNotNull("Request ID should be generated", requestId);
        } else {
            logger.error("✗ Workflow did not complete");
            fail("Workflow did not complete");
        }
    }
}
