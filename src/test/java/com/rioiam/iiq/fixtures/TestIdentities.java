package com.rioiam.iiq.fixtures;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Test identity definitions and templates.
 *
 * Provides predefined identity configurations for common test scenarios.
 * Includes both simple test data and realistic production-like data based on
 * actual Vodafone user exports.
 */
public class TestIdentities {

    private static final Random RANDOM = new Random();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("M/d/yyyy h:m:s a z");

    // ========== SIMPLE TEST DATA (Backward Compatibility) ==========

    /**
     * Create attributes for a standard test user.
     * Simple test data with minimal attributes.
     */
    public static Map<String, Object> createStandardUserAttributes() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("firstname", "Test");
        attrs.put("lastname", "User");
        attrs.put("email", "testuser@test.com");
        attrs.put("inactive", true);
        attrs.put("vf_id_status", "Inactive");
        return attrs;
    }

    /**
     * Create attributes for a test manager.
     * Simple test data with minimal attributes.
     */
    public static Map<String, Object> createManagerAttributes() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("firstname", "Test");
        attrs.put("lastname", "Manager");
        attrs.put("email", "testmanager@test.com");
        attrs.put("inactive", false);
        attrs.put("vf_id_status", "Active Adhoc");
        attrs.put("vf_id_enablement_type", "Enabled Manager");
        return attrs;
    }

    /**
     * Create attributes for an active adhoc user.
     * Simple test data with minimal attributes.
     */
    public static Map<String, Object> createActiveAdhocUserAttributes() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("firstname", "Test");
        attrs.put("lastname", "ActiveUser");
        attrs.put("email", "activeuser@test.com");
        attrs.put("inactive", false);
        attrs.put("vf_id_status", "Active Adhoc");
        attrs.put("vf_id_enablement_type", "Enabled User");
        return attrs;
    }

    // ========== REALISTIC TEST DATA (Based on Production Exports) ==========

    /**
     * Create attributes for a realistic employee with all production-like attributes.
     * Based on actual Vodafone user data exports (response.json).
     *
     * Includes 18+ attributes:
     * - Core: firstname, lastname, email, displayName
     * - Status: vf_id_status, inactive, vf_id_identity_type
     * - Organization: vf_id_department, vf_id_organization_name, vf_id_companylegalname
     * - IDs: vf_id_opcouid, vf_id_companyuniqueid, vf_id_vfuid_short, vf_id_NTUSERNAME
     * - Dates: vf_id_start_date, vf_id_end_date, vf_id_usr_create, vf_id_usr_update
     * - Other: vf_id_wiam_manager, vf_id_data_source, vf_id_preferred_language, isManager
     */
    public static Map<String, Object> createRealisticEmployeeAttributes() {
        Map<String, Object> attrs = new HashMap<>();

        // Generate unique identifiers
        String uniqueId = String.format("%08d", 90000000 + RANDOM.nextInt(10000000));
        String shortId = String.format("4df%04x", RANDOM.nextInt(65536));
        String ntUsername = "TEST" + RANDOM.nextInt(1000);

        // Core attributes
        attrs.put("firstname", "TestUser");
        attrs.put("lastname", "Automation" + RANDOM.nextInt(1000));
        attrs.put("email", "test.automation" + RANDOM.nextInt(1000) + "@vodafone-itc.com");
        attrs.put("displayName", "TestUser Automation, Vodafone Test");

        // Status attributes
        attrs.put("inactive", true);
        attrs.put("vf_id_status", "Inactive");
        attrs.put("vf_id_identity_type", "Employee");

        // Organization attributes
        attrs.put("vf_id_department", "Test Automation Department");
        attrs.put("vf_id_organization_name", "Test OpCo");
        attrs.put("vf_id_companylegalname", "Vodafone Test");
        attrs.put("vf_id_companyuniqueid", "100");

        // ID attributes
        attrs.put("vf_id_opcouid", uniqueId);
        attrs.put("vf_id_vfuid_short", shortId);
        attrs.put("vf_id_NTUSERNAME", ntUsername);

        // Date attributes
        String currentDate = DATE_FORMAT.format(new Date());
        attrs.put("vf_id_start_date", "1/1/2024 0:0:0 AM CET");
        attrs.put("vf_id_end_date", "12/31/2050 0:0:0 AM CET");
        attrs.put("vf_id_usr_create", currentDate);
        attrs.put("vf_id_usr_update", currentDate);

        // Other attributes
        attrs.put("vf_id_wiam_manager", "testmanager@vodafone.com");
        attrs.put("vf_id_data_source", "Test_XML");
        attrs.put("vf_id_preferred_language", "en");
        attrs.put("isManager", false);

        return attrs;
    }

    /**
     * Create attributes for a realistic manager with all production-like attributes.
     * Similar to realistic employee but configured as a manager.
     */
    public static Map<String, Object> createRealisticManagerAttributes() {
        Map<String, Object> attrs = createRealisticEmployeeAttributes();

        // Override specific attributes for manager
        attrs.put("firstname", "TestManager");
        attrs.put("lastname", "Automation" + RANDOM.nextInt(1000));
        attrs.put("email", "test.manager" + RANDOM.nextInt(1000) + "@vodafone-itc.com");
        attrs.put("displayName", "TestManager Automation, Vodafone Test");
        attrs.put("inactive", false);
        attrs.put("vf_id_status", "Active Adhoc");
        attrs.put("vf_id_enablement_type", "Enabled Manager");
        attrs.put("isManager", true);
        attrs.put("vf_id_department", "Test Management Department");

        return attrs;
    }

    /**
     * Create attributes for a realistic active employee ready for activation workflow.
     * Uses inactive status that will be activated during the test.
     */
    public static Map<String, Object> createRealisticInactiveUserAttributes() {
        Map<String, Object> attrs = createRealisticEmployeeAttributes();

        // Ensure inactive for activation workflow
        attrs.put("firstname", "TestInactive");
        attrs.put("lastname", "User" + RANDOM.nextInt(1000));
        attrs.put("email", "test.inactive" + RANDOM.nextInt(1000) + "@vodafone-itc.com");
        attrs.put("displayName", "TestInactive User, Vodafone Test");
        attrs.put("inactive", true);
        attrs.put("vf_id_status", "Inactive");
        attrs.put("vf_id_department", "Test Department Pending Activation");

        return attrs;
    }

    /**
     * Create attributes for a realistic user from various OpCos (markets).
     * Randomly selects from common Vodafone markets.
     *
     * @param opcoName OpCo name (e.g., "VOIS Turkey", "Vodafone UK", "Vodafone Germany")
     */
    public static Map<String, Object> createRealisticOpCoUserAttributes(String opcoName) {
        Map<String, Object> attrs = createRealisticEmployeeAttributes();

        // OpCo-specific configurations
        Map<String, String[]> opcoConfig = new HashMap<>();
        opcoConfig.put("VOIS Turkey", new String[]{"TR", "TR20", "VOIS TURKEY Intelligent Solutions Limited Company"});
        opcoConfig.put("Vodafone UK", new String[]{"GB", "UK10", "Vodafone Limited"});
        opcoConfig.put("Vodafone Germany", new String[]{"DE", "DE10", "Vodafone GmbH"});
        opcoConfig.put("Test OpCo", new String[]{"XX", "100", "Vodafone Test"});

        String[] config = opcoConfig.getOrDefault(opcoName, opcoConfig.get("Test OpCo"));

        attrs.put("vf_id_organization_name", opcoName);
        attrs.put("vf_id_country", config[0]);
        attrs.put("vf_id_country_iso_code", config[0]);
        attrs.put("vf_id_legal_entity_id", config[1]);
        attrs.put("vf_id_companylegalname", config[2]);
        attrs.put("vf_id_companyuniqueid", config[1]);

        return attrs;
    }

    /**
     * Create attributes for a realistic terminated/inactive user.
     * Includes termination date and inactive status based on real data patterns.
     */
    public static Map<String, Object> createRealisticTerminatedUserAttributes() {
        Map<String, Object> attrs = createRealisticEmployeeAttributes();

        attrs.put("firstname", "TestTerminated");
        attrs.put("lastname", "User" + RANDOM.nextInt(1000));
        attrs.put("email", "test.terminated" + RANDOM.nextInt(1000) + "@vodafone-itc.com");
        attrs.put("displayName", "TestTerminated User, Vodafone Test");
        attrs.put("inactive", true);
        attrs.put("vf_id_status", "Inactive");
        attrs.put("vf_id_termination_date", "1/15/2024 23:59:0 PM CET");
        attrs.put("vf_id_end_date", "1/15/2024 0:0:0 AM CET");

        return attrs;
    }

    /**
     * Create attributes for a realistic ACTIVE user ready for deactivation workflow.
     * User is in Active Adhoc status and can be deactivated during tests.
     */
    public static Map<String, Object> createRealisticActiveUserAttributes() {
        Map<String, Object> attrs = createRealisticEmployeeAttributes();

        // Override to make user ACTIVE
        attrs.put("firstname", "TestActive");
        attrs.put("lastname", "User" + RANDOM.nextInt(1000));
        attrs.put("email", "test.active" + RANDOM.nextInt(1000) + "@vodafone-itc.com");
        attrs.put("displayName", "TestActive User, Vodafone Test");
        attrs.put("inactive", false);  // ACTIVE
        attrs.put("vf_id_status", "Active Adhoc");  // ACTIVE STATUS
        attrs.put("vf_id_enablement_type", "Enabled User");
        attrs.put("vf_id_department", "Test Department Active");

        return attrs;
    }

    // ========== UTILITY METHODS ==========

    /**
     * Generate unique identity name for testing.
     */
    public static String generateUniqueIdentityName(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    /**
     * Get a description of all available attributes in realistic user data.
     * Useful for documentation and debugging.
     */
    public static String getRealisticAttributesList() {
        return "Realistic User Attributes (18+ fields):\n" +
               "  Core: firstname, lastname, email, displayName\n" +
               "  Status: vf_id_status, inactive, vf_id_identity_type\n" +
               "  Organization: vf_id_department, vf_id_organization_name, vf_id_companylegalname, vf_id_companyuniqueid\n" +
               "  IDs: vf_id_opcouid, vf_id_vfuid_short, vf_id_NTUSERNAME\n" +
               "  Dates: vf_id_start_date, vf_id_end_date, vf_id_usr_create, vf_id_usr_update\n" +
               "  Other: vf_id_wiam_manager, vf_id_data_source, vf_id_preferred_language, isManager";
    }
}
