package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.Identity;

/**
 * Check Bob identity details
 */
public class CheckBobIdentityTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckBobIdentityTest.class);

    @Test
    public void testCheckBobIdentity() throws Exception {
        logger.info("========================================");
        logger.info("Checking Bob Identity");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // Check by ID
        String bobId = "944D25E7F42C7B46";
        Identity bobById = context.getObjectById(Identity.class, bobId);

        if (bobById != null) {
            logger.info("✓ Found identity by ID: {}", bobId);
            logger.info("  - Name: {}", bobById.getName());
            logger.info("  - Display Name: {}", bobById.getDisplayName());
            logger.info("  - Email: {}", bobById.getEmail());
            logger.info("  - Inactive: {}", bobById.isInactive());
            logger.info("  - Manager: {}", bobById.getManager() != null ? bobById.getManager().getName() : "None");
        } else {
            logger.error("✗ Identity NOT found by ID: {}", bobId);
        }

        logger.info("========================================");
    }
}
