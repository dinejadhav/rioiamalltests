package com.rioiam.iiq.fixtures;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Test configuration data for workflows and approvals.
 *
 * Provides predefined configurations for common test scenarios.
 */
public class TestConfigurations {

    /**
     * Create test service configuration for VF-Core-Global.
     */
    public static Map<String, Object> createTestServiceConfig() {
        Map<String, Object> serviceConfig = new HashMap<>();
        serviceConfig.put("managerApprovalRequired", "true");
        serviceConfig.put("businessApprovalRequired", "true");
        serviceConfig.put("businessOwnerApprover", "businessowner@test.com");
        return serviceConfig;
    }

    /**
     * Create test market configuration for VF-Core-Global.
     */
    public static Map<String, Object> createTestMarketConfig() {
        Map<String, Object> marketConfig = new HashMap<>();
        marketConfig.put("opcoDisplayName", "Test Market");
        marketConfig.put("managerApprovalRequired", "true");
        marketConfig.put("businessApprovalRequired", "true");
        marketConfig.put("businessOwnerApprover", "marketowner@test.com");
        return marketConfig;
    }

    /**
     * Create workflow variables for activation test.
     * Includes all required variables for VF-Core-ActivateDeactivateIdentity workflow.
     */
    public static Map<String, Object> createActivationWorkflowVariables(String launcher, String identityName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("launcher", launcher);
        variables.put("identityName", identityName);
        variables.put("operation", "activate");

        // Required by workflow Initialize step - prevents NullPointerException
        variables.put("userLocale", Locale.ENGLISH);
        variables.put("clientTimeZone", TimeZone.getDefault());

        // Initialize maModel with local market configuration
        Map<String, Object> maModel = new HashMap<>();
        maModel.put("vfMarket", "Vodafone Limited");
        maModel.put("sponsorScope", "Local Market");
        variables.put("maModel", maModel);

        return variables;
    }

    /**
     * Create workflow variables for deactivation test.
     * Includes all required variables for VF-Core-ActivateDeactivateIdentity workflow.
     */
    public static Map<String, Object> createDeactivationWorkflowVariables(String launcher, String identityName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("launcher", launcher);
        variables.put("identityName", identityName);
        variables.put("operation", "deactivate");

        // Required by workflow Initialize step - prevents NullPointerException
        variables.put("userLocale", Locale.ENGLISH);
        variables.put("clientTimeZone", TimeZone.getDefault());

        // Initialize maModel with local market configuration
        Map<String, Object> maModel = new HashMap<>();
        maModel.put("vfMarket", "Vodafone Limited");
        maModel.put("sponsorScope", "Local Market");
        variables.put("maModel", maModel);

        return variables;
    }
}
