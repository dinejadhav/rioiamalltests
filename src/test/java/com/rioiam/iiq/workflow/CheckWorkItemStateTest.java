package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.WorkItem;

import java.util.List;

/**
 * Check WorkItem state values
 */
public class CheckWorkItemStateTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckWorkItemStateTest.class);

    @Test
    public void testCheckWorkItemStates() throws Exception {
        logger.info("========================================");
        logger.info("Checking WorkItem State Values");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // Check what WorkItem.State values are available
        logger.info("\n1. WorkItem.State enum values:");
        try {
            for (WorkItem.State state : WorkItem.State.values()) {
                logger.info("  - {}", state);
            }
        } catch (Exception e) {
            logger.error("Error getting WorkItem.State values", e);
        }

        // Try different state filters
        logger.info("\n2. Testing different state filters:");

        // Test with "Open"
        logger.info("\n  A. Searching with state = 'Open' (String):");
        QueryOptions qo1 = new QueryOptions();
        qo1.addFilter(Filter.eq("state", "Open"));
        List<WorkItem> items1 = context.getObjects(WorkItem.class, qo1);
        logger.info("    Found {} WorkItems", items1 != null ? items1.size() : 0);

        // Test with "Pending"
        logger.info("\n  B. Searching with state = 'Pending' (String):");
        QueryOptions qo2 = new QueryOptions();
        qo2.addFilter(Filter.eq("state", "Pending"));
        List<WorkItem> items2 = context.getObjects(WorkItem.class, qo2);
        logger.info("    Found {} WorkItems", items2 != null ? items2.size() : 0);

        // Test with WorkItem.State.Pending
        logger.info("\n  C. Searching with state = WorkItem.State.Pending:");
        QueryOptions qo3 = new QueryOptions();
        qo3.addFilter(Filter.eq("state", WorkItem.State.Pending));
        List<WorkItem> items3 = context.getObjects(WorkItem.class, qo3);
        logger.info("    Found {} WorkItems", items3 != null ? items3.size() : 0);

        // Get all WorkItems and check their actual state values
        logger.info("\n3. Checking actual state values of all WorkItems:");
        QueryOptions qoAll = new QueryOptions();
        qoAll.setResultLimit(10);
        List<WorkItem> allItems = context.getObjects(WorkItem.class, qoAll);

        if (allItems != null && !allItems.isEmpty()) {
            logger.info("  Found {} WorkItems (showing up to 10):", allItems.size());
            for (WorkItem item : allItems) {
                logger.info("\n  WorkItem:");
                logger.info("    - ID: {}", item.getId());
                logger.info("    - Name: {}", item.getName());
                logger.info("    - State: {}", item.getState());
                logger.info("    - State Class: {}", item.getState() != null ? item.getState().getClass().getName() : "null");
                logger.info("    - Type: {}", item.getType());
                logger.info("    - Owner: {}", item.getOwner() != null ? item.getOwner().getName() : "null");
            }
        } else {
            logger.info("  No WorkItems found in database");
        }

        logger.info("\n========================================");
    }
}
