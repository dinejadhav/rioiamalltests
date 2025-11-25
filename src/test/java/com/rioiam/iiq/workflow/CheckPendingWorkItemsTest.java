package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.WorkItem;

import java.util.List;

/**
 * Quick test to check pending WorkItems for a user
 */
public class CheckPendingWorkItemsTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckPendingWorkItemsTest.class);

    @Test
    public void testCheckPendingWorkItems() {
        String userName = "dinesh.jadhav1";

        logger.info("========================================");
        logger.info("Checking Pending WorkItems");
        logger.info("========================================");
        logger.info("User: {}", userName);

        List<WorkItem> workItems = workItemHandler.getWorkItemsByOwner(userName);

        logger.info("Found {} pending WorkItems", workItems != null ? workItems.size() : 0);

        if (workItems != null && !workItems.isEmpty()) {
            logger.info("WorkItems:");
            for (WorkItem wi : workItems) {
                logger.info("  - ID: {}", wi.getId());
                logger.info("    Type: {}", wi.getType());
                logger.info("    Name: {}", wi.getName());
                logger.info("    State: {}", wi.getState());
                logger.info("    Created: {}", wi.getCreated());
                logger.info("    Workflow Case: {}", wi.getWorkflowCase() != null ? wi.getWorkflowCase().getId() : "null");
                logger.info("");
            }
        } else {
            logger.info("No pending WorkItems found for user: {}", userName);
        }

        logger.info("========================================");
    }
}
