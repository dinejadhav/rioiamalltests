package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import com.rioiam.iiq.workflow.WorkflowExecutor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.*;
import sailpoint.tools.GeneralException;

import java.util.*;

/**
 * Track workflow execution to see what's happening
 */
public class TrackWorkflowExecutionTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(TrackWorkflowExecutionTest.class);

    @Test
    public void testTrackWorkflowExecution() throws Exception {
        logger.info("========================================");
        logger.info("TRACKING WORKFLOW EXECUTION");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // Prepare workflow variables
        Map<String, Object> variables = new HashMap<>();

        // Identity to activate
        List<String> identityIds = new ArrayList<>();
        identityIds.add("944D25E7F42C7B46");  // Bob

        variables.put("identityName", identityIds);
        variables.put("launcher", "dinesh.jadhav1");
        variables.put("flow", "");
        variables.put("requestID", "0");

        // Locale and timezone
        Locale userLocale = new Locale("en", "US");
        TimeZone clientTimeZone = TimeZone.getTimeZone("America/Chicago");

        variables.put("userLocale", userLocale);
        variables.put("clientTimeZone", clientTimeZone);

        logger.info("\n1. LAUNCHING WORKFLOW");
        logger.info("  Workflow: VF-Core-ActivateDeactivateIdentity");
        logger.info("  Launcher: dinesh.jadhav1");
        logger.info("  Identity: 944D25E7F42C7B46 (Bob)");

        // Launch workflow
        String workflowCaseId = workflowExecutor.launchWorkflow(
            "VF-Core-ActivateDeactivateIdentity",
            "dinesh.jadhav1",
            variables
        );

        logger.info("  Workflow Case ID: {}", workflowCaseId);

        if (workflowCaseId == null) {
            logger.error("✗ Workflow failed to launch!");
            return;
        }

        // Wait a bit for workflow to initialize
        logger.info("\n2. WAITING 5 seconds for workflow to initialize...");
        Thread.sleep(5000);

        // Check workflow case status immediately
        logger.info("\n3. CHECKING WORKFLOW CASE STATUS");
        WorkflowCase wfCase = context.getObjectByName(WorkflowCase.class, workflowCaseId);

        if (wfCase != null) {
            logger.info("  Workflow Case Found:");
            logger.info("    - ID: {}", wfCase.getId());
            logger.info("    - Name: {}", wfCase.getName());
            logger.info("    - Completion Status: {}", wfCase.getCompletionStatus());
            logger.info("    - Created: {}", wfCase.getCreated());
            logger.info("    - Completed: {}", wfCase.getCompleted());

            // Check if workflow completed already
            if (wfCase.getCompleted() != null) {
                logger.warn("  ⚠ WORKFLOW ALREADY COMPLETED!");
                logger.warn("  This means workflow finished without creating WorkItems");
            } else {
                logger.info("  ✓ Workflow is still running");
            }

            // Check workflow attributes
            Attributes<String, Object> attrs = wfCase.getAttributes();
            if (attrs != null) {
                logger.info("    - Workflow Attributes:");
                List<String> keys = attrs.getKeys();
                if (keys != null) {
                    for (String key : keys) {
                        Object value = attrs.get(key);
                        logger.info("      * {} = {}", key, value);
                    }
                }
            }
        } else {
            logger.error("  ✗ Workflow case NOT found!");
        }

        // Check for WorkItems
        logger.info("\n4. CHECKING FOR WORKITEMS");
        QueryOptions qo = new QueryOptions();
        Filter filter = Filter.or(
            Filter.eq("workflowCase.name", workflowCaseId),
            Filter.eq("workflowCase.id", workflowCaseId)
        );
        qo.addFilter(filter);

        List<WorkItem> workItems = context.getObjects(WorkItem.class, qo);
        logger.info("  Found {} WorkItems for workflow case '{}'", workItems != null ? workItems.size() : 0, workflowCaseId);

        if (workItems != null && !workItems.isEmpty()) {
            for (WorkItem wi : workItems) {
                logger.info("\n  WorkItem:");
                logger.info("    - ID: {}", wi.getId());
                logger.info("    - Name: {}", wi.getName());
                logger.info("    - Type: {}", wi.getType());
                logger.info("    - State: {}", wi.getState());
                logger.info("    - Owner: {}", wi.getOwner() != null ? wi.getOwner().getName() : "null");
                logger.info("    - Created: {}", wi.getCreated());
            }
        } else {
            logger.warn("  ⚠ NO WORKITEMS FOUND!");
            logger.warn("  This confirms workflow is not creating approval/form WorkItems");
        }

        // Check for any IdentityRequest created
        logger.info("\n5. CHECKING FOR IDENTITYREQUEST");
        QueryOptions irQo = new QueryOptions();
        irQo.setOrderBy("created");
        irQo.setOrderAscending(false);
        irQo.setResultLimit(5);

        List<IdentityRequest> requests = context.getObjects(IdentityRequest.class, irQo);
        logger.info("  Found {} recent IdentityRequests", requests != null ? requests.size() : 0);

        if (requests != null && !requests.isEmpty()) {
            for (IdentityRequest ir : requests) {
                logger.info("\n  IdentityRequest:");
                logger.info("    - ID: {}", ir.getId());
                logger.info("    - Name: {}", ir.getName());
                logger.info("    - State: {}", ir.getExecutionStatus());
                logger.info("    - Created: {}", ir.getCreated());
            }
        }

        // Wait longer and check again
        logger.info("\n6. WAITING ADDITIONAL 10 seconds...");
        Thread.sleep(10000);

        logger.info("\n7. FINAL CHECK FOR WORKITEMS");
        workItems = context.getObjects(WorkItem.class, qo);
        logger.info("  Found {} WorkItems (after 15 total seconds)", workItems != null ? workItems.size() : 0);

        // Final workflow case check
        wfCase = context.getObjectByName(WorkflowCase.class, workflowCaseId);
        if (wfCase != null) {
            logger.info("\n8. FINAL WORKFLOW CASE STATUS");
            logger.info("    - Completion Status: {}", wfCase.getCompletionStatus());
            logger.info("    - Completed: {}", wfCase.getCompleted());

            if (wfCase.getCompleted() != null) {
                logger.warn("  ⚠ WORKFLOW COMPLETED WITHOUT CREATING WORKITEMS!");
                logger.warn("  This indicates:");
                logger.warn("    1. Workflow may have auto-approve logic");
                logger.warn("    2. Workflow may skip approval for certain users");
                logger.warn("    3. Workflow configuration may have approvals disabled");
            }
        }

        logger.info("\n========================================");
        logger.info("TRACKING COMPLETE");
        logger.info("========================================");
    }
}
