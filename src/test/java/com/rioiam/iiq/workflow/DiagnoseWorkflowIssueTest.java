package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.*;
import sailpoint.tools.GeneralException;

import java.util.*;

/**
 * Comprehensive diagnosis of workflow execution issue
 */
public class DiagnoseWorkflowIssueTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(DiagnoseWorkflowIssueTest.class);

    @Test
    public void testDiagnoseWorkflowExecution() throws Exception {
        logger.info("========================================");
        logger.info("COMPREHENSIVE WORKFLOW DIAGNOSIS");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // 1. Check dinesh.jadhav1 user details
        logger.info("\n1. CHECKING USER: dinesh.jadhav1");
        Identity dinesh = context.getObjectByName(Identity.class, "dinesh.jadhav1");
        if (dinesh != null) {
            logger.info("  ✓ User exists");
            logger.info("  - Email: {}", dinesh.getEmail());
            logger.info("  - Inactive: {}", dinesh.isInactive());
            logger.info("  - Workgroup: {}", dinesh.isWorkgroup());

            // Capabilities
            List<Capability> caps = dinesh.getCapabilities();
            logger.info("  - Capabilities: {}", caps != null ? caps.size() : 0);
            if (caps != null && !caps.isEmpty()) {
                for (Capability cap : caps) {
                    logger.info("    * {}", cap.getName());
                }
            }

            // Check if user is a manager
            logger.info("  - Manager: {}", dinesh.getManager() != null ? dinesh.getManager().getName() : "None");

        } else {
            logger.error("  ✗ User NOT found!");
            return;
        }

        // 2. Check Bob user (identity to activate)
        logger.info("\n2. CHECKING TARGET USER: bob");
        Identity bob = context.getObjectByName(Identity.class, "bob");
        if (bob != null) {
            logger.info("  ✓ User exists");
            logger.info("  - Email: {}", bob.getEmail());
            logger.info("  - Inactive: {}", bob.isInactive());
            logger.info("  - Manager: {}", bob.getManager() != null ? bob.getManager().getName() : "None");
        } else {
            logger.error("  ✗ User NOT found!");
        }

        // 3. Check recent workflow cases
        logger.info("\n3. CHECKING RECENT WORKFLOW CASES");
        QueryOptions qo = new QueryOptions();
        qo.setOrderBy("created");
        qo.setOrderAscending(false);
        qo.setResultLimit(5);

        List<WorkflowCase> allCases = context.getObjects(WorkflowCase.class, qo);
        logger.info("  Total recent workflow cases: {}", allCases != null ? allCases.size() : 0);

        if (allCases != null && !allCases.isEmpty()) {
            for (WorkflowCase wc : allCases) {
                logger.info("\n  Workflow Case:");
                logger.info("    - Name: {}", wc.getName());
                logger.info("    - ID: {}", wc.getId());
                logger.info("    - Status: {}", wc.getCompletionStatus());
                logger.info("    - Created: {}", wc.getCreated());
                logger.info("    - Completed: {}", wc.getCompleted());

                // Check attributes
                Attributes<String, Object> attrs = wc.getAttributes();
                if (attrs != null) {
                    logger.info("    - Attributes:");
                    List<String> keys = attrs.getKeys();
                    if (keys != null) {
                        for (String key : keys) {
                            Object value = attrs.get(key);
                            if (value != null && !value.toString().isEmpty()) {
                                logger.info("      * {}: {}", key, value);
                            }
                        }
                    }
                }
            }
        }

        // 4. Check pending WorkItems
        logger.info("\n4. CHECKING ALL PENDING WORKITEMS");
        QueryOptions wiQo = new QueryOptions();
        wiQo.addFilter(Filter.eq("state", "Open"));

        List<WorkItem> pendingItems = context.getObjects(WorkItem.class, wiQo);
        logger.info("  Total pending WorkItems: {}", pendingItems != null ? pendingItems.size() : 0);

        if (pendingItems != null && !pendingItems.isEmpty()) {
            for (WorkItem wi : pendingItems) {
                logger.info("\n  WorkItem:");
                logger.info("    - ID: {}", wi.getId());
                logger.info("    - Name: {}", wi.getName());
                logger.info("    - Type: {}", wi.getType());
                logger.info("    - Owner: {}", wi.getOwner() != null ? wi.getOwner().getName() : "None");
                logger.info("    - Created: {}", wi.getCreated());

                WorkflowCase wc = wi.getWorkflowCase();
                if (wc != null) {
                    logger.info("    - Workflow Case: {}", wc.getName());
                }
            }
        }

        // 5. Check workflow configuration
        logger.info("\n5. CHECKING WORKFLOW CONFIGURATION");
        Workflow workflow = context.getObjectByName(Workflow.class, "VF-Core-ActivateDeactivateIdentity");
        if (workflow != null) {
            logger.info("  ✓ Workflow exists: {}", workflow.getName());
            logger.info("  - Description: {}", workflow.getDescription());
        } else {
            logger.error("  ✗ Workflow NOT found!");
        }

        // 6. Check for any workflow errors in TaskResults
        logger.info("\n6. CHECKING RECENT TASK RESULTS FOR ERRORS");
        QueryOptions trQo = new QueryOptions();
        trQo.setOrderBy("created");
        trQo.setOrderAscending(false);
        trQo.setResultLimit(5);
        trQo.addFilter(Filter.eq("completionStatus", TaskResult.CompletionStatus.Error));

        List<TaskResult> errorResults = context.getObjects(TaskResult.class, trQo);
        logger.info("  Recent error TaskResults: {}", errorResults != null ? errorResults.size() : 0);

        if (errorResults != null && !errorResults.isEmpty()) {
            for (TaskResult tr : errorResults) {
                logger.info("\n  Error TaskResult:");
                logger.info("    - Name: {}", tr.getName());
                logger.info("    - Created: {}", tr.getCreated());
                logger.info("    - Messages: {}", tr.getMessages());
            }
        }

        logger.info("\n========================================");
        logger.info("DIAGNOSIS COMPLETE");
        logger.info("========================================");
    }
}
