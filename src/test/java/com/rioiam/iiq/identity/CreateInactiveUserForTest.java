package com.rioiam.iiq.identity;

import com.rioiam.iiq.base.BaseIIQTest;
import com.rioiam.iiq.fixtures.TestIdentities;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sailpoint.object.Identity;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Simple test to create an INACTIVE user and an ACTIVE user for testing activation/deactivation workflows.
 *
 * Run this test FIRST to create test users, then copy the identity names to ActivateDeactivateWithExistingUserTest.
 */
public class CreateInactiveUserForTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateInactiveUserForTest.class);

    @Autowired
    private IdentityService identityService;

    @Test
    public void createInactiveAndActiveUsers() {
        logger.info("========================================");
        logger.info("Creating TEST USERS for Activation/Deactivation Workflows");
        logger.info("========================================");

        // Create INACTIVE user for ACTIVATION testing
        String inactiveUserName = TestIdentities.generateUniqueIdentityName("test_user_activation");
        String inactiveManagerName = TestIdentities.generateUniqueIdentityName("test_manager_inactive");

        logger.info("Creating INACTIVE manager...");
        Map<String, Object> inactiveManagerAttrs = TestIdentities.createRealisticManagerAttributes();
        Identity inactiveManager = identityService.createTestIdentity(inactiveManagerName, inactiveManagerAttrs);
        assertNotNull("Inactive manager should be created", inactiveManager);
        logger.info("✓ Inactive manager created: {}", inactiveManagerName);

        logger.info("Creating INACTIVE user...");
        Map<String, Object> inactiveUserAttrs = TestIdentities.createRealisticInactiveUserAttributes();
        Identity inactiveUser = identityService.createTestIdentityWithManager(inactiveUserName, inactiveManagerName, inactiveUserAttrs);
        assertNotNull("Inactive user should be created", inactiveUser);

        logger.info("========================================");
        logger.info("✓ INACTIVE User Created for ACTIVATION Testing");
        logger.info("========================================");
        logger.info("Identity Name: {}", inactiveUserName);
        logger.info("Manager Name: {}", inactiveManagerName);
        logger.info("Status: {}", inactiveUser.getAttribute("vf_id_status"));
        logger.info("Inactive: {}", inactiveUser.getAttribute("inactive"));
        logger.info("========================================");

        // Create ACTIVE user for DEACTIVATION testing
        String activeUserName = TestIdentities.generateUniqueIdentityName("active_user");
        String activeManagerName = TestIdentities.generateUniqueIdentityName("active_manager");

        logger.info("Creating ACTIVE manager...");
        Map<String, Object> activeManagerAttrs = TestIdentities.createRealisticManagerAttributes();
        Identity activeManager = identityService.createTestIdentity(activeManagerName, activeManagerAttrs);
        assertNotNull("Active manager should be created", activeManager);
        logger.info("✓ Active manager created: {}", activeManagerName);

        logger.info("Creating ACTIVE user...");
        Map<String, Object> activeUserAttrs = TestIdentities.createRealisticActiveUserAttributes();
        Identity activeUser = identityService.createTestIdentityWithManager(activeUserName, activeManagerName, activeUserAttrs);
        assertNotNull("Active user should be created", activeUser);

        logger.info("========================================");
        logger.info("✓ ACTIVE User Created for DEACTIVATION Testing");
        logger.info("========================================");
        logger.info("Identity Name: {}", activeUserName);
        logger.info("Manager Name: {}", activeManagerName);
        logger.info("Status: {}", activeUser.getAttribute("vf_id_status"));
        logger.info("Inactive: {}", activeUser.getAttribute("inactive"));
        logger.info("========================================");

        logger.info("");
        logger.info("╔══════════════════════════════════════════════════════════════╗");
        logger.info("║  COPY THESE VALUES TO ActivateDeactivateWithExistingUserTest ║");
        logger.info("╠══════════════════════════════════════════════════════════════╣");
        logger.info("║  INACTIVE_USER_TO_ACTIVATE   = \"{}\" ", inactiveUserName);
        logger.info("║  ACTIVE_USER_TO_DEACTIVATE   = \"{}\" ", activeUserName);
        logger.info("╚══════════════════════════════════════════════════════════════╝");
        logger.info("");
    }
}
