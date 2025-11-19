package com.rioiam.iiq.identity;

import com.rioiam.iiq.context.IIQRemoteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sailpoint.api.SailPointContext;
import sailpoint.object.Identity;
import sailpoint.tools.GeneralException;

import java.util.Map;

/**
 * Service for validating SailPoint identity state in tests.
 *
 * This component handles:
 * - Validating identity attributes
 * - Validating identity status (active/inactive)
 * - Validating identity links and entitlements
 *
 * Reuses existing IIQRemoteContext for SailPoint connectivity.
 */
@Component
public class IdentityValidator {

    private static final Logger logger = LoggerFactory.getLogger(IdentityValidator.class);

    @Autowired
    private IIQRemoteContext remoteContext;

    /**
     * Validate that an identity has the expected attributes.
     *
     * @param identityName Name of the identity to validate
     * @param expectedAttributes Map of expected attribute name/value pairs
     * @return true if all attributes match, false otherwise
     */
    public boolean validateAttributes(String identityName, Map<String, Object> expectedAttributes) {
        logger.info("========================================");
        logger.info("Validating attributes for identity: {}", identityName);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Get the identity
            Identity identity = context.getObjectByName(Identity.class, identityName);

            if (identity == null) {
                logger.error("✗ Identity not found: {}", identityName);
                return false;
            }

            boolean allMatch = true;

            // Validate each expected attribute
            for (Map.Entry<String, Object> entry : expectedAttributes.entrySet()) {
                String attrName = entry.getKey();
                Object expectedValue = entry.getValue();
                Object actualValue = identity.getAttribute(attrName);

                logger.debug("Checking attribute: {}", attrName);
                logger.debug("  Expected: {}", expectedValue);
                logger.debug("  Actual: {}", actualValue);

                if (expectedValue == null) {
                    if (actualValue != null) {
                        logger.warn("✗ Attribute '{}' mismatch: expected null, got '{}'", attrName, actualValue);
                        allMatch = false;
                    }
                } else if (!expectedValue.equals(actualValue)) {
                    logger.warn("✗ Attribute '{}' mismatch: expected '{}', got '{}'", attrName, expectedValue, actualValue);
                    allMatch = false;
                } else {
                    logger.debug("✓ Attribute '{}' matches", attrName);
                }
            }

            if (allMatch) {
                logger.info("✓ All attributes validated successfully");
            } else {
                logger.warn("✗ Some attributes did not match");
            }

            return allMatch;

        } catch (GeneralException e) {
            logger.error("✗ Error validating attributes", e);
            return false;
        }
    }

    /**
     * Validate identity status (active/inactive).
     *
     * @param identityName Name of the identity to validate
     * @param expectedStatus Expected status (e.g., "Active Adhoc", "Inactive")
     * @return true if status matches, false otherwise
     */
    public boolean validateStatus(String identityName, String expectedStatus) {
        logger.info("Validating status for identity: {} (expected: {})", identityName, expectedStatus);

        SailPointContext context = remoteContext.getContext();

        try {
            Identity identity = context.getObjectByName(Identity.class, identityName);

            if (identity == null) {
                logger.error("✗ Identity not found: {}", identityName);
                return false;
            }

            String actualStatus = (String) identity.getAttribute("vf_id_status");
            logger.debug("  Expected status: {}", expectedStatus);
            logger.debug("  Actual status: {}", actualStatus);

            if (expectedStatus == null) {
                if (actualStatus != null) {
                    logger.warn("✗ Status mismatch: expected null, got '{}'", actualStatus);
                    return false;
                }
            } else if (!expectedStatus.equals(actualStatus)) {
                logger.warn("✗ Status mismatch: expected '{}', got '{}'", expectedStatus, actualStatus);
                return false;
            }

            logger.info("✓ Status validated successfully");
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error validating status", e);
            return false;
        }
    }

    /**
     * Validate that identity exists.
     *
     * @param identityName Name of the identity
     * @return true if identity exists, false otherwise
     */
    public boolean identityExists(String identityName) {
        logger.debug("Checking if identity exists: {}", identityName);

        SailPointContext context = remoteContext.getContext();

        try {
            Identity identity = context.getObjectByName(Identity.class, identityName);
            boolean exists = (identity != null);

            if (exists) {
                logger.debug("✓ Identity exists: {}", identityName);
            } else {
                logger.debug("✗ Identity does not exist: {}", identityName);
            }

            return exists;

        } catch (GeneralException e) {
            logger.error("✗ Error checking identity existence", e);
            return false;
        }
    }

    /**
     * Validate identity inactive flag.
     *
     * @param identityName Name of the identity
     * @param expectedInactive Expected inactive flag value
     * @return true if inactive flag matches, false otherwise
     */
    public boolean validateInactiveFlag(String identityName, boolean expectedInactive) {
        logger.info("Validating inactive flag for identity: {} (expected: {})", identityName, expectedInactive);

        SailPointContext context = remoteContext.getContext();

        try {
            Identity identity = context.getObjectByName(Identity.class, identityName);

            if (identity == null) {
                logger.error("✗ Identity not found: {}", identityName);
                return false;
            }

            Boolean actualInactive = identity.isInactive();
            logger.debug("  Expected inactive: {}", expectedInactive);
            logger.debug("  Actual inactive: {}", actualInactive);

            if (expectedInactive != (actualInactive != null && actualInactive)) {
                logger.warn("✗ Inactive flag mismatch: expected '{}', got '{}'", expectedInactive, actualInactive);
                return false;
            }

            logger.info("✓ Inactive flag validated successfully");
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error validating inactive flag", e);
            return false;
        }
    }
}
