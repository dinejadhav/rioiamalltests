package com.rioiam.iiq.workflow;

import com.rioiam.iiq.base.BaseIIQTest;
import com.rioiam.iiq.session.MockHttpSessionProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Check if WorkItems are stored in the session instead of database
 */
public class CheckSessionWorkItemsTest extends BaseIIQTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckSessionWorkItemsTest.class);

    @Test
    public void testCheckSessionForWorkItems() throws InterruptedException {
        logger.info("========================================");
        logger.info("Checking Session for WorkItems");
        logger.info("========================================");

        String LAUNCHER_USER = "dinesh.jadhav1";
        String WORKFLOW_NAME = "VF-Core-ActivateDeactivateIdentity";

        // Verify launcher identity exists
        Identity launcherIdentity = identityService.getIdentity(LAUNCHER_USER);
        logger.info("Launcher identity loaded: {}", launcherIdentity.getName());

        // Create mock HTTP session
        HttpSession httpSession = MockHttpSessionProvider.createMockSession(LAUNCHER_USER);
        logger.info("Mock HTTP session created: {}", httpSession.getId());

        Map<String, Object> initialVariables = new HashMap<>();
        initialVariables.put("launcher", LAUNCHER_USER);
        initialVariables.put("identityName", LAUNCHER_USER);
        initialVariables.put("userLocale", Locale.ENGLISH);
        initialVariables.put("clientTimeZone", TimeZone.getDefault());
        initialVariables.put("httpSession", httpSession);
        initialVariables.put("trace", true);

        // Launch workflow
        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, initialVariables);
        logger.info("✓ Workflow launched with case ID: {}", workflowCaseId);

        // Wait a bit for workflow to execute
        Thread.sleep(2000);

        // Check what's in the session
        logger.info("\n========================================");
        logger.info("CHECKING HTTP SESSION ATTRIBUTES:");
        logger.info("========================================");

        Enumeration<String> attrNames = httpSession.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = attrNames.nextElement();
            Object attrValue = httpSession.getAttribute(attrName);
            logger.info("Session attribute: {} = {}", attrName, attrValue);

            // If it's a Map, show its contents
            if (attrValue instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) attrValue;
                logger.info("  Map contents ({} entries):", map.size());
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    logger.info("    {} = {}", entry.getKey(), entry.getValue());
                }
            }
        }

        logger.info("========================================");
    }
}
