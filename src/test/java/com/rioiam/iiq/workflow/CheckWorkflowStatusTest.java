package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.QueryOptions;
import sailpoint.object.WorkflowCase;
import sailpoint.object.Filter;

import java.util.List;

/**
 * Check workflow case status
 */
public class CheckWorkflowStatusTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckWorkflowStatusTest.class);

    @Test
    public void testCheckRecentWorkflows() throws Exception {
        logger.info("========================================");
        logger.info("Checking Recent Workflow Cases");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // Query for recent workflow cases
        QueryOptions qo = new QueryOptions();
        qo.setOrderBy("created");
        qo.setOrderAscending(false);
        qo.addFilter(Filter.eq("name", "dinesh.jadhav1"));

        List<WorkflowCase> workflowCases = context.getObjects(WorkflowCase.class, qo);

        if (workflowCases != null && !workflowCases.isEmpty()) {
            logger.info("Found {} workflow cases for dinesh.jadhav1", workflowCases.size());

            for (WorkflowCase wfCase : workflowCases) {
                logger.info("\nWorkflow Case: {}", wfCase.getName());
                logger.info("  ID: {}", wfCase.getId());
                logger.info("  Status: {}", wfCase.getCompletionStatus());
                logger.info("  Created: {}", wfCase.getCreated());
                logger.info("  Completed: {}", wfCase.getCompleted());

                // Get workflow attributes if available
                if (wfCase.getAttributes() != null) {
                    logger.info("  Attributes: {}", wfCase.getAttributes());
                }
            }
        } else {
            logger.info("No workflow cases found for dinesh.jadhav1");
        }

        logger.info("========================================");
    }
}
