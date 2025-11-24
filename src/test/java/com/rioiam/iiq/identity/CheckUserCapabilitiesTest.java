package com.rioiam.iiq.identity;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;
import sailpoint.object.Capability;

import java.util.List;

/**
 * Test to check and compare user capabilities
 */
public class CheckUserCapabilitiesTest extends BaseIIQTest {
    private static final Logger logger = LoggerFactory.getLogger(CheckUserCapabilitiesTest.class);

    @Test
    public void testCheckCapabilities() {
        logger.info("========================================");
        logger.info("Checking User Capabilities");
        logger.info("========================================");

        // Check spadmin
        Identity spadmin = identityService.getIdentity("spadmin");
        if (spadmin != null) {
            logger.info("\nSPADMIN Capabilities:");
            List<Capability> spadminCaps = spadmin.getCapabilities();
            if (spadminCaps != null && !spadminCaps.isEmpty()) {
                logger.info("  Total capabilities: {}", spadminCaps.size());
                for (Capability cap : spadminCaps) {
                    logger.info("  - {}", cap.getName());
                }
            } else {
                logger.info("  No capabilities found");
            }
        } else {
            logger.error("  spadmin user not found!");
        }

        // Check dinesh.jadhav1
        Identity dinesh = identityService.getIdentity("dinesh.jadhav1");
        if (dinesh != null) {
            logger.info("\nDINESH.JADHAV1 Capabilities:");
            List<Capability> dineshCaps = dinesh.getCapabilities();
            if (dineshCaps != null && !dineshCaps.isEmpty()) {
                logger.info("  Total capabilities: {}", dineshCaps.size());
                for (Capability cap : dineshCaps) {
                    logger.info("  - {}", cap.getName());
                }
            } else {
                logger.info("  No capabilities found - THIS IS THE PROBLEM!");
                logger.info("  User needs capabilities to receive WorkItems");
            }
        } else {
            logger.error("  dinesh.jadhav1 user not found!");
        }

        logger.info("========================================");
    }
}
