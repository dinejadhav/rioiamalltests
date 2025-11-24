package com.rioiam.iiq.identity;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;

import static org.junit.Assert.assertNotNull;

/**
 * Quick test to check if a user exists in IIQ
 */
public class CheckUserExistsTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckUserExistsTest.class);

    @Test
    public void testCheckDineshJadhavUser() {
        logger.info("========================================");
        logger.info("Checking if dinesh.jadhav1 user exists");
        logger.info("========================================");

        Identity user = identityService.getIdentity("dinesh.jadhav1");

        if (user != null) {
            logger.info("✓ User found: {}", user.getName());
            logger.info("  Display Name: {}", user.getDisplayName());
            logger.info("  Email: {}", user.getEmail());
            logger.info("  Status: {}", user.getAttribute("vf_id_status"));
            logger.info("  Inactive: {}", user.getAttribute("inactive"));
            logger.info("  Manager: {}", user.getManager() != null ? user.getManager().getName() : "None");
        } else {
            logger.error("✗ User NOT found: dinesh.jadhav1");
            logger.info("Available alternatives:");
            logger.info("  - Use 'spadmin' (system admin)");
            logger.info("  - Create dinesh.jadhav1 user first");
            logger.info("  - Use another existing user");
        }

        assertNotNull("dinesh.jadhav1 user must exist in IIQ", user);
    }
}
