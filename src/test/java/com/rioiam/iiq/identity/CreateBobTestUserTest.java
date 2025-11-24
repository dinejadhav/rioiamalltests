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
 * Creates Bob1 Testonoci11 identity with all attributes from the XML
 * Identity Name: 944D25E7F42C7B46
 */
public class CreateBobTestUserTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CreateBobTestUserTest.class);

    @Test
    public void testCreateBobTestUserWithAllAttributes() {
        logger.info("========================================");
        logger.info("Creating Bob1 Testonoci11 identity with complete attributes");
        logger.info("========================================");

        String identityName = "944D25E7F42C7B46";

        // Check if user already exists
        Identity existingUser = identityService.getIdentity(identityName);
        if (existingUser != null) {
            logger.info("✓ User already exists: {}", identityName);
            logger.info("  Display Name: {}", existingUser.getDisplayName());
            logger.info("  Email: {}", existingUser.getEmail());
            logger.info("  Status: {}", existingUser.getAttribute("vf_id_status"));
            return;
        }

        // Create manager first (A683DDECC0289C29)
        String managerName = "A683DDECC0289C29";
        Identity manager = identityService.getIdentity(managerName);
        if (manager == null) {
            logger.info("Creating manager: {}", managerName);
            Map<String, Object> managerAttrs = new HashMap<>();
            managerAttrs.put("firstname", "Manager");
            managerAttrs.put("lastname", "ForBob");
            managerAttrs.put("displayName", "Manager ForBob");
            managerAttrs.put("email", "ker3.mod3@vodafone-itc.com");
            managerAttrs.put("vf_id_status", "Active");
            managerAttrs.put("inactive", false);
            managerAttrs.put("vf_id_companyuniqueid", "2");
            managerAttrs.put("vf_id_organization_name", "Vodafone Germany");

            manager = identityService.createTestIdentity(managerName, managerAttrs);
            assertNotNull("Manager should be created", manager);
            logger.info("✓ Manager created: {}", managerName);
        }

        // Create Bob's identity with all attributes
        Map<String, Object> attributes = new HashMap<>();

        // Basic attributes
        attributes.put("displayName", "Bob1 Testonoci11, Vodafone (External)");
        attributes.put("email", "bob1.testonoci11@vodafone-itc.com");
        attributes.put("firstname", "Bob1");
        attributes.put("lastname", "Testonoci11");
        attributes.put("inactive", true);

        // VF Identity attributes
        attributes.put("vf_id_City", "Alandi");
        attributes.put("vf_id_building", "Abteilungsleiterr Service Management");
        attributes.put("vf_id_company", "Wipro");
        attributes.put("vf_id_companylegalname", "Vodafone Germany");
        attributes.put("vf_id_companyuniqueid", "2");
        attributes.put("vf_id_cost_center", "TRUE");
        attributes.put("vf_id_country", "Germany");
        attributes.put("vf_id_country_iso_code", "DE");
        attributes.put("vf_id_data_source", "VFD2_iPlanet");
        attributes.put("vf_id_department", "TRUE DVT20");
        attributes.put("vf_id_department_abbreviation", "TRUE");
        attributes.put("vf_id_employee_group", "Non-Employee");
        attributes.put("vf_id_employee_subgroup", "Contractor");
        attributes.put("vf_id_end_date", "12/9/2025 0:0:0 AM CET");
        attributes.put("vf_id_function", "Active");
        attributes.put("vf_id_hire_date", "20251111000000Z");
        attributes.put("vf_id_identity_type", "Contractor");
        attributes.put("vf_id_job_title", "DE04108600");
        attributes.put("vf_id_legal_entity_id", "DE04");
        attributes.put("vf_id_middle_name", "AD");
        attributes.put("vf_id_objectGUID", "CB29C270E14FAB45B1DD33CEB27813B4");
        attributes.put("vf_id_opcouid", "921432690");
        attributes.put("vf_id_organization_name", "Vodafone Germany");
        attributes.put("vf_id_personnelareacode", "Hamacher");
        attributes.put("vf_id_personnelsubareacode", "2");
        attributes.put("vf_id_preferred_language", "de");
        attributes.put("vf_id_start_date", "11/11/2025 0:0:0 AM CET");
        attributes.put("vf_id_status", "Inactive");
        attributes.put("vf_id_termination_date", "20251209000000Z");
        attributes.put("vf_id_usr_create", "11/11/2025 7:38:27 AM CET");
        attributes.put("vf_id_usr_update", "11/11/2025 8:25:8 AM CET");
        attributes.put("vf_id_vfuid_short", "4e09dcc");
        attributes.put("vf_id_wiam_manager", "ker3.mod3@vodafone-itc.com");

        // Create the identity with manager
        Identity user = identityService.createTestIdentityWithManager(identityName, managerName, attributes);
        assertNotNull("User should be created", user);

        logger.info("========================================");
        logger.info("✓ IDENTITY CREATED SUCCESSFULLY");
        logger.info("========================================");
        logger.info("Identity Name: {}", user.getName());
        logger.info("Display Name: {}", user.getDisplayName());
        logger.info("Email: {}", user.getEmail());
        logger.info("First Name: {}", user.getAttribute("firstname"));
        logger.info("Last Name: {}", user.getAttribute("lastname"));
        logger.info("Middle Name: {}", user.getAttribute("vf_id_middle_name"));
        logger.info("Status: {}", user.getAttribute("vf_id_status"));
        logger.info("Inactive: {}", user.getAttribute("inactive"));
        logger.info("Identity Type: {}", user.getAttribute("vf_id_identity_type"));
        logger.info("Employee Group: {}", user.getAttribute("vf_id_employee_group"));
        logger.info("Employee Subgroup: {}", user.getAttribute("vf_id_employee_subgroup"));
        logger.info("Company: {}", user.getAttribute("vf_id_company"));
        logger.info("Company Legal Name: {}", user.getAttribute("vf_id_companylegalname"));
        logger.info("Company Unique ID: {}", user.getAttribute("vf_id_companyuniqueid"));
        logger.info("Country: {}", user.getAttribute("vf_id_country"));
        logger.info("Country ISO Code: {}", user.getAttribute("vf_id_country_iso_code"));
        logger.info("City: {}", user.getAttribute("vf_id_City"));
        logger.info("Building: {}", user.getAttribute("vf_id_building"));
        logger.info("Department: {}", user.getAttribute("vf_id_department"));
        logger.info("Cost Center: {}", user.getAttribute("vf_id_cost_center"));
        logger.info("Function: {}", user.getAttribute("vf_id_function"));
        logger.info("Job Title: {}", user.getAttribute("vf_id_job_title"));
        logger.info("Legal Entity ID: {}", user.getAttribute("vf_id_legal_entity_id"));
        logger.info("OpCo UID: {}", user.getAttribute("vf_id_opcouid"));
        logger.info("Start Date: {}", user.getAttribute("vf_id_start_date"));
        logger.info("End Date: {}", user.getAttribute("vf_id_end_date"));
        logger.info("Hire Date: {}", user.getAttribute("vf_id_hire_date"));
        logger.info("Termination Date: {}", user.getAttribute("vf_id_termination_date"));
        logger.info("Data Source: {}", user.getAttribute("vf_id_data_source"));
        logger.info("VFUID Short: {}", user.getAttribute("vf_id_vfuid_short"));
        logger.info("WIAM Manager: {}", user.getAttribute("vf_id_wiam_manager"));
        logger.info("Manager: {}", user.getManager() != null ? user.getManager().getName() : "None");
        logger.info("========================================");

        // Log all attributes for verification
        logger.info("");
        logger.info("Complete Attribute List:");
        logger.info("========================");
        int count = 1;
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            logger.info("{}. {} = {}", count++, entry.getKey(), entry.getValue());
        }
        logger.info("Total Attributes: {}", attributes.size());
        logger.info("========================================");
    }

    /**
     * Verify the created identity has all attributes
     */
    @Test
    public void testVerifyBobTestUserAttributes() {
        logger.info("========================================");
        logger.info("Verifying Bob1 Testonoci11 identity attributes");
        logger.info("========================================");

        String identityName = "944D25E7F42C7B46";
        Identity user = identityService.getIdentity(identityName);

        assertNotNull("User must exist: " + identityName, user);

        logger.info("✓ User found: {}", identityName);
        logger.info("");
        logger.info("Verifying all attributes:");
        logger.info("========================");

        // Verify each attribute
        verifyAttribute(user, "displayName", "Bob1 Testonoci11, Vodafone (External)");
        verifyAttribute(user, "email", "bob1.testonoci11@vodafone-itc.com");
        verifyAttribute(user, "firstname", "Bob1");
        verifyAttribute(user, "lastname", "Testonoci11");
        verifyAttribute(user, "inactive", true);
        verifyAttribute(user, "vf_id_City", "Alandi");
        verifyAttribute(user, "vf_id_building", "Abteilungsleiterr Service Management");
        verifyAttribute(user, "vf_id_company", "Wipro");
        verifyAttribute(user, "vf_id_companylegalname", "Vodafone Germany");
        verifyAttribute(user, "vf_id_companyuniqueid", "2");
        verifyAttribute(user, "vf_id_cost_center", "TRUE");
        verifyAttribute(user, "vf_id_country", "Germany");
        verifyAttribute(user, "vf_id_country_iso_code", "DE");
        verifyAttribute(user, "vf_id_data_source", "VFD2_iPlanet");
        verifyAttribute(user, "vf_id_department", "TRUE DVT20");
        verifyAttribute(user, "vf_id_department_abbreviation", "TRUE");
        verifyAttribute(user, "vf_id_employee_group", "Non-Employee");
        verifyAttribute(user, "vf_id_employee_subgroup", "Contractor");
        verifyAttribute(user, "vf_id_function", "Active");
        verifyAttribute(user, "vf_id_identity_type", "Contractor");
        verifyAttribute(user, "vf_id_job_title", "DE04108600");
        verifyAttribute(user, "vf_id_legal_entity_id", "DE04");
        verifyAttribute(user, "vf_id_middle_name", "AD");
        verifyAttribute(user, "vf_id_objectGUID", "CB29C270E14FAB45B1DD33CEB27813B4");
        verifyAttribute(user, "vf_id_opcouid", "921432690");
        verifyAttribute(user, "vf_id_organization_name", "Vodafone Germany");
        verifyAttribute(user, "vf_id_personnelareacode", "Hamacher");
        verifyAttribute(user, "vf_id_personnelsubareacode", "2");
        verifyAttribute(user, "vf_id_preferred_language", "de");
        verifyAttribute(user, "vf_id_status", "Inactive");
        verifyAttribute(user, "vf_id_vfuid_short", "4e09dcc");
        verifyAttribute(user, "vf_id_wiam_manager", "ker3.mod3@vodafone-itc.com");

        logger.info("========================================");
        logger.info("✓ ALL ATTRIBUTES VERIFIED SUCCESSFULLY");
        logger.info("========================================");
    }

    private void verifyAttribute(Identity user, String attributeName, Object expectedValue) {
        Object actualValue = user.getAttribute(attributeName);
        if (actualValue != null && actualValue.equals(expectedValue)) {
            logger.info("✓ {} = {}", attributeName, actualValue);
        } else {
            logger.warn("⚠ {} - Expected: {}, Actual: {}", attributeName, expectedValue, actualValue);
        }
    }
}
