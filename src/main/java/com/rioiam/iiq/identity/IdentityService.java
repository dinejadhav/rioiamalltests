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
 * Service for managing SailPoint identities in tests.
 *
 * This component handles:
 * - Creating test identities
 * - Retrieving identities
 * - Updating identity attributes
 * - Deleting test identities
 *
 * Reuses existing IIQRemoteContext for SailPoint connectivity.
 */
@Component
public class IdentityService {

    private static final Logger logger = LoggerFactory.getLogger(IdentityService.class);

    @Autowired
    private IIQRemoteContext remoteContext;

    /**
     * Create a test identity with the given attributes.
     *
     * @param name Identity name
     * @param attributes Map of attribute name/value pairs
     * @return Created Identity object, or null if failed
     */
    public Identity createTestIdentity(String name, Map<String, Object> attributes) {
        logger.info("========================================");
        logger.info("Creating test identity: {}", name);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Check if identity already exists
            Identity existing = context.getObjectByName(Identity.class, name);
            if (existing != null) {
                logger.warn("Identity already exists: {}. Returning existing identity.", name);
                return existing;
            }

            // Create new identity
            Identity identity = new Identity();
            identity.setName(name);

            // Set attributes
            if (attributes != null && !attributes.isEmpty()) {
                logger.debug("Setting {} attributes", attributes.size());
                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                    logger.debug("  {}: {}", entry.getKey(), entry.getValue());
                    identity.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            // Save identity
            context.startTransaction();
            context.saveObject(identity);
            context.commitTransaction();

            logger.info("✓ Test identity created successfully");
            logger.info("  ID: {}", identity.getId());
            logger.info("  Name: {}", identity.getName());

            return identity;

        } catch (GeneralException e) {
            logger.error("✗ Error creating test identity: {}", name, e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return null;
        }
    }

    /**
     * Get an identity by name.
     *
     * @param name Identity name
     * @return Identity object, or null if not found
     */
    public Identity getIdentity(String name) {
        logger.debug("Getting identity: {}", name);

        SailPointContext context = remoteContext.getContext();

        try {
            Identity identity = context.getObjectByName(Identity.class, name);

            if (identity != null) {
                logger.debug("✓ Identity found: {} (ID: {})", name, identity.getId());
            } else {
                logger.debug("✗ Identity not found: {}", name);
            }

            return identity;

        } catch (GeneralException e) {
            logger.error("✗ Error getting identity: {}", name, e);
            return null;
        }
    }

    /**
     * Update identity attributes.
     *
     * @param name Identity name
     * @param attributes Map of attribute name/value pairs to update
     * @return true if updated successfully, false otherwise
     */
    public boolean updateIdentity(String name, Map<String, Object> attributes) {
        logger.info("Updating identity: {}", name);

        SailPointContext context = remoteContext.getContext();

        try {
            // Get identity
            Identity identity = context.getObjectByName(Identity.class, name);
            if (identity == null) {
                logger.error("✗ Identity not found: {}", name);
                return false;
            }

            // Update attributes
            if (attributes != null && !attributes.isEmpty()) {
                logger.debug("Updating {} attributes", attributes.size());
                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                    logger.debug("  {}: {}", entry.getKey(), entry.getValue());
                    identity.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            // Save changes
            context.startTransaction();
            context.saveObject(identity);
            context.commitTransaction();

            logger.info("✓ Identity updated successfully: {}", name);
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error updating identity: {}", name, e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return false;
        }
    }

    /**
     * Delete a test identity.
     *
     * @param name Identity name
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteIdentity(String name) {
        logger.info("Deleting test identity: {}", name);

        SailPointContext context = remoteContext.getContext();

        try {
            // Get identity
            Identity identity = context.getObjectByName(Identity.class, name);
            if (identity == null) {
                logger.warn("✗ Identity not found for deletion: {}", name);
                return false;
            }

            // Delete identity
            context.startTransaction();
            context.removeObject(identity);
            context.commitTransaction();

            logger.info("✓ Test identity deleted successfully: {}", name);
            return true;

        } catch (GeneralException e) {
            logger.error("✗ Error deleting identity: {}", name, e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return false;
        }
    }

    /**
     * Check if an identity exists.
     *
     * @param name Identity name
     * @return true if identity exists, false otherwise
     */
    public boolean identityExists(String name) {
        logger.debug("Checking if identity exists: {}", name);
        return getIdentity(name) != null;
    }

    /**
     * Create a test identity with manager relationship.
     *
     * @param name Identity name
     * @param managerName Manager's identity name
     * @param attributes Additional attributes
     * @return Created Identity object, or null if failed
     */
    public Identity createTestIdentityWithManager(String name, String managerName, Map<String, Object> attributes) {
        logger.info("Creating test identity with manager: {} -> {}", name, managerName);

        SailPointContext context = remoteContext.getContext();

        try {
            // Get or create manager
            Identity manager = context.getObjectByName(Identity.class, managerName);
            if (manager == null) {
                logger.error("✗ Manager not found: {}", managerName);
                return null;
            }

            // Create identity
            Identity identity = createTestIdentity(name, attributes);
            if (identity == null) {
                return null;
            }

            // Set manager
            identity.setManager(manager);

            // Save changes
            context.startTransaction();
            context.saveObject(identity);
            context.commitTransaction();

            logger.info("✓ Test identity created with manager");
            return identity;

        } catch (GeneralException e) {
            logger.error("✗ Error creating identity with manager", e);
            try {
                context.rollbackTransaction();
            } catch (GeneralException ex) {
                logger.error("Error rolling back transaction", ex);
            }
            return null;
        }
    }
}
