package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;

import java.util.List;

/**
 * Search for Bob identity using different methods
 */
public class SearchForBobTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchForBobTest.class);

    @Test
    public void testSearchForBob() throws Exception {
        logger.info("========================================");
        logger.info("Searching for Bob Identity");
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        // Method 1: Search by ID as name
        logger.info("\n1. Searching by name '944D25E7F42C7B46':");
        Identity bobByName = context.getObjectByName(Identity.class, "944D25E7F42C7B46");
        if (bobByName != null) {
            logger.info("  ✓ Found by name!");
            logger.info("  - ID: {}", bobByName.getId());
            logger.info("  - Name: {}", bobByName.getName());
            logger.info("  - Display Name: {}", bobByName.getDisplayName());
            logger.info("  - Email: {}", bobByName.getEmail());
            logger.info("  - Inactive: {}", bobByName.isInactive());
        } else {
            logger.info("  ✗ NOT found by name");
        }

        // Method 2: Search by display name pattern
        logger.info("\n2. Searching by display name containing 'Bob1':");
        QueryOptions qo = new QueryOptions();
        qo.addFilter(Filter.like("displayName", "%Bob1%", Filter.MatchMode.START));

        List<Identity> identities = context.getObjects(Identity.class, qo);
        logger.info("  Found {} identities matching 'Bob1'", identities != null ? identities.size() : 0);

        if (identities != null && !identities.isEmpty()) {
            for (Identity id : identities) {
                logger.info("\n  Identity:");
                logger.info("    - ID: {}", id.getId());
                logger.info("    - Name: {}", id.getName());
                logger.info("    - Display Name: {}", id.getDisplayName());
                logger.info("    - Email: {}", id.getEmail());
                logger.info("    - Inactive: {}", id.isInactive());
            }
        }

        // Method 3: Search by email
        logger.info("\n3. Searching by email 'bob1.testonoci11@vodafone-itc.com':");
        QueryOptions qo2 = new QueryOptions();
        qo2.addFilter(Filter.eq("email", "bob1.testonoci11@vodafone-itc.com"));

        List<Identity> identitiesByEmail = context.getObjects(Identity.class, qo2);
        logger.info("  Found {} identities", identitiesByEmail != null ? identitiesByEmail.size() : 0);

        if (identitiesByEmail != null && !identitiesByEmail.isEmpty()) {
            for (Identity id : identitiesByEmail) {
                logger.info("\n  Identity:");
                logger.info("    - ID: {}", id.getId());
                logger.info("    - Name: {}", id.getName());
                logger.info("    - Display Name: {}", id.getDisplayName());
                logger.info("    - Email: {}", id.getEmail());
                logger.info("    - Inactive: {}", id.isInactive());
            }
        }

        logger.info("\n========================================");
    }
}
