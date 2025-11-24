package com.rioiam.iiq.identity;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Create dinesh.jadhav1 user in IIQ for testing
 */
public class CreateDineshJadhavUserTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateDineshJadhavUserTest.class);

    @Test
    public void testCreateDineshJadhavUser() {
        logger.info("========================================");
        logger.info("Creating dinesh.jadhav1 user");
        logger.info("========================================");

        // Check if user already exists
        Identity existingUser = identityService.getIdentity("dinesh.jadhav1");
        if (existingUser != null) {
            logger.info("✓ User already exists: {}", existingUser.getName());
            logger.info("  Display Name: {}", existingUser.getDisplayName());
            logger.info("  Email: {}", existingUser.getEmail());
            return;
        }

        // Create user attributes
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("firstname", "Dinesh");
        attributes.put("lastname", "Jadhav");
        attributes.put("displayName", "Dinesh Jadhav");
        attributes.put("email", "dinesh.jadhav1@vodafone.com");

        // VF specific attributes
        attributes.put("vf_id_status", "Active");
        attributes.put("inactive", false);
        attributes.put("vf_id_employeenumber", "EMP123456");
        attributes.put("vf_id_companyuniqueid", "UK");
        attributes.put("vf_id_company", "Vodafone UK");
        attributes.put("vf_id_department", "IT");
        attributes.put("vf_id_title", "Test Engineer");
        attributes.put("vf_id_location", "London");
        attributes.put("vf_id_enablement_type", "Enabled Manager");

        // Create the user
        Identity user = identityService.createTestIdentity("dinesh.jadhav1", attributes);
        assertNotNull("User should be created", user);

        logger.info("========================================");
        logger.info("✓ USER CREATED SUCCESSFULLY");
        logger.info("========================================");
        logger.info("User Name: {}", user.getName());
        logger.info("Display Name: {}", user.getDisplayName());
        logger.info("Email: {}", user.getEmail());
        logger.info("Status: {}", user.getAttribute("vf_id_status"));
        logger.info("Inactive: {}", user.getAttribute("inactive"));
        logger.info("Enablement Type: {}", user.getAttribute("vf_id_enablement_type"));
        logger.info("========================================");
    }
}
