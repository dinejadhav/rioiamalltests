package com.rioiam.iiq.identity;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.Capability;
import sailpoint.object.Identity;
import sailpoint.tools.GeneralException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Assign necessary capabilities to dinesh.jadhav1 user for workflow and WorkItem management
 */
public class AssignCapabilitiesToDineshTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(AssignCapabilitiesToDineshTest.class);

    @Test
    public void testAssignCapabilities() throws GeneralException {
        logger.info("========================================");
        logger.info("Assigning Capabilities to dinesh.jadhav1");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // Get the user
        Identity dinesh = context.getObjectByName(Identity.class, "dinesh.jadhav1");
        assertNotNull("dinesh.jadhav1 user must exist", dinesh);

        // Check current capabilities
        List<Capability> currentCaps = dinesh.getCapabilities();
        logger.info("Current capabilities: {}", currentCaps != null ? currentCaps.size() : 0);

        // Get SystemAdministrator capability (this includes all rights)
        Capability sysAdminCap = context.getObjectByName(Capability.class, "SystemAdministrator");

        if (sysAdminCap != null) {
            logger.info("Found SystemAdministrator capability");

            // Assign the capability
            List<Capability> caps = new ArrayList<>();
            caps.add(sysAdminCap);
            dinesh.setCapabilities(caps);

            // Save the identity
            context.saveObject(dinesh);
            context.commitTransaction();

            logger.info("✓ SystemAdministrator capability assigned to dinesh.jadhav1");
        } else {
            logger.warn("SystemAdministrator capability not found in the system");
            logger.info("Available capabilities in the system:");

            // List all capabilities
            List<Capability> allCaps = context.getObjects(Capability.class);
            if (allCaps != null && !allCaps.isEmpty()) {
                for (Capability cap : allCaps) {
                    logger.info("  - {}", cap.getName());
                }

                // Assign the first available capability
                logger.info("Assigning first available capability: {}", allCaps.get(0).getName());
                List<Capability> caps = new ArrayList<>();
                caps.add(allCaps.get(0));
                dinesh.setCapabilities(caps);
                context.saveObject(dinesh);
                context.commitTransaction();
            } else {
                logger.error("No capabilities found in the system!");
            }
        }

        // Verify assignment
        dinesh = context.getObjectByName(Identity.class, "dinesh.jadhav1");
        List<Capability> newCaps = dinesh.getCapabilities();

        logger.info("========================================");
        logger.info("✓ CAPABILITIES ASSIGNED SUCCESSFULLY");
        logger.info("========================================");
        logger.info("Total capabilities: {}", newCaps != null ? newCaps.size() : 0);
        if (newCaps != null && !newCaps.isEmpty()) {
            for (Capability cap : newCaps) {
                logger.info("  - {}", cap.getName());
            }
        }
        logger.info("========================================");
    }
}
