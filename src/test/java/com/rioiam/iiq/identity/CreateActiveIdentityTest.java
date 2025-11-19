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
 * Test to create active identities for deactivation workflow testing.
 *
 * This test creates ACTIVE identities (inactive=false, vf_id_status="Active Adhoc")
 * that can be used for testing the deactivation workflow.
 */
public class CreateActiveIdentityTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateActiveIdentityTest.class);

    @Autowired
    private IdentityService identityService;

    /**
     * Create a single active identity for manual testing or deactivation workflow.
     */
    @Test
    public void testCreateSingleActiveIdentity() {
        logger.info("========================================");
        logger.info("Creating ACTIVE identity for deactivation testing");
        logger.info("========================================");

        // Generate unique identity name
        String identityName = TestIdentities.generateUniqueIdentityName("active_user");
        String managerName = TestIdentities.generateUniqueIdentityName("active_manager");

        // Create manager first (also active)
        logger.info("Creating active manager...");
        Map<String, Object> managerAttrs = TestIdentities.createRealisticManagerAttributes();
        Identity manager = identityService.createTestIdentity(managerName, managerAttrs);
        assertNotNull("Manager should be created", manager);
        logger.info("✓ Manager created: {}", managerName);
        logger.info("  Status: {}", manager.getAttribute("vf_id_status"));
        logger.info("  Inactive: {}", manager.getAttribute("inactive"));

        // Create active user
        logger.info("Creating active user...");
        Map<String, Object> userAttrs = TestIdentities.createRealisticActiveUserAttributes();
        Identity user = identityService.createTestIdentityWithManager(identityName, managerName, userAttrs);
        assertNotNull("User should be created", user);

        logger.info("========================================");
        logger.info("✓ ACTIVE Identity Created Successfully");
        logger.info("========================================");
        logger.info("Identity Name: {}", identityName);
        logger.info("Manager Name: {}", managerName);
        logger.info("User Status: {}", user.getAttribute("vf_id_status"));
        logger.info("User Inactive: {}", user.getAttribute("inactive"));
        logger.info("User Email: {}", user.getAttribute("email"));
        logger.info("User Department: {}", user.getAttribute("vf_id_department"));
        logger.info("User Organization: {}", user.getAttribute("vf_id_organization_name"));
        logger.info("========================================");
        logger.info("This identity can now be used for deactivation workflow testing");
        logger.info("========================================");
    }

    /**
     * Create multiple active identities at once for bulk testing.
     */
    @Test
    public void testCreateMultipleActiveIdentities() {
        logger.info("========================================");
        logger.info("Creating MULTIPLE ACTIVE identities");
        logger.info("========================================");

        int count = 5;  // Create 5 active identities

        for (int i = 1; i <= count; i++) {
            String identityName = TestIdentities.generateUniqueIdentityName("active_user_" + i);
            String managerName = TestIdentities.generateUniqueIdentityName("manager_" + i);

            // Create manager
            Map<String, Object> managerAttrs = TestIdentities.createRealisticManagerAttributes();
            Identity manager = identityService.createTestIdentity(managerName, managerAttrs);
            assertNotNull("Manager should be created", manager);

            // Create active user
            Map<String, Object> userAttrs = TestIdentities.createRealisticActiveUserAttributes();
            Identity user = identityService.createTestIdentityWithManager(identityName, managerName, userAttrs);
            assertNotNull("User should be created", user);

            logger.info("✓ Created identity {}/{}: {} (Status: {}, Inactive: {})",
                    i, count, identityName,
                    user.getAttribute("vf_id_status"),
                    user.getAttribute("inactive"));
        }

        logger.info("========================================");
        logger.info("✓ Created {} ACTIVE identities successfully", count);
        logger.info("========================================");
    }

    /**
     * Create an active identity with custom attributes for specific testing scenarios.
     */
    @Test
    public void testCreateActiveIdentityCustomAttributes() {
        logger.info("========================================");
        logger.info("Creating ACTIVE identity with custom attributes");
        logger.info("========================================");

        String identityName = "test_active_custom_" + System.currentTimeMillis();
        String managerName = "test_manager_custom_" + System.currentTimeMillis();

        // Create manager
        Map<String, Object> managerAttrs = TestIdentities.createRealisticManagerAttributes();
        Identity manager = identityService.createTestIdentity(managerName, managerAttrs);
        assertNotNull("Manager should be created", manager);

        // Create active user with custom OpCo
        Map<String, Object> userAttrs = TestIdentities.createRealisticActiveUserAttributes();

        // Customize for specific market
        userAttrs.put("vf_id_organization_name", "Vodafone Limited");
        userAttrs.put("vf_id_companylegalname", "Vodafone Limited");
        userAttrs.put("vf_id_country", "GB");
        userAttrs.put("vf_id_country_iso_code", "GB");
        userAttrs.put("vf_id_department", "IT Department");

        Identity user = identityService.createTestIdentityWithManager(identityName, managerName, userAttrs);
        assertNotNull("User should be created", user);

        logger.info("========================================");
        logger.info("✓ Custom ACTIVE Identity Created");
        logger.info("========================================");
        logger.info("Identity Name: {}", identityName);
        logger.info("Organization: {}", user.getAttribute("vf_id_organization_name"));
        logger.info("Country: {}", user.getAttribute("vf_id_country"));
        logger.info("Department: {}", user.getAttribute("vf_id_department"));
        logger.info("Status: {} (inactive={})",
                user.getAttribute("vf_id_status"),
                user.getAttribute("inactive"));
        logger.info("========================================");
    }
}
