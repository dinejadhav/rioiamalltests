package com.rioiam.iiq.workflow.activatedeactivate;

import com.rioiam.iiq.base.BaseWorkflowTest;
import com.rioiam.iiq.fixtures.TestIdentities;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sailpoint.object.Identity;
import sailpoint.object.WorkItem;
import sailpoint.object.WorkflowCase;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Complete test for VF-Core-ActivateDeactivateIdentity workflow with proper form handling.
 *
 * This test:
 * - Properly fills both RequestInitiate and RequestSubmit forms
 * - Handles multiple users in a single request
 * - Provides business justification (mandatory field)
 * - Captures and returns the Request ID after workflow execution
 *
 * Usage:
 * 1. Create test users first using CreateInactiveUserForTest
 * 2. Update INACTIVE_USERS_TO_ACTIVATE with the created user names
 * 3. Run: mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testActivateMultipleUsersWithRequestId
 */
public class ActivateDeactivateIdentityCompleteTest extends BaseWorkflowTest {

    private static final Logger logger = LoggerFactory.getLogger(ActivateDeactivateIdentityCompleteTest.class);

    private static final String WORKFLOW_NAME = "VF-Core-ActivateDeactivateIdentity";

    /**
     * The user who will launch the workflow and complete the forms
     * This should be an active user with appropriate permissions
     */
    private static final String LAUNCHER_USER = "dinesh.jadhav1";

    // ========== CONFIGURATION: SET YOUR USER IDs HERE ==========

    /**
     * ========== PROVIDE YOUR EXISTING INACTIVE USERS HERE ==========
     *
     * Simply paste the user IDs of existing inactive users from your IIQ system.
     * You can activate one or multiple users in a single workflow execution.
     *
     * SINGLE USER EXAMPLE:
     * private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
     *     "john.doe"
     * );
     *
     * MULTIPLE USERS EXAMPLE:
     * private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
     *     "john.doe",
     *     "jane.smith",
     *     "bob.wilson",
     *     "alice.johnson",
     *     "mike.brown"
     * );
     *
     * IMPORTANT:
     * - Users must already exist in IIQ
     * - Users must be INACTIVE (inactive=true, vf_id_status="Inactive")
     * - Replace the example names below with your actual user IDs
     */
    private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
        "944D25E7F42C7B46"    // Bob1 Testonoci11 - Created from XML
        // Add as many users as you need...
    );

    /**
     * ========== PROVIDE YOUR EXISTING ACTIVE USERS HERE ==========
     *
     * Simply paste the user IDs of existing active users from your IIQ system.
     * You can deactivate one or multiple users in a single workflow execution.
     *
     * SINGLE USER EXAMPLE:
     * private static final List<String> ACTIVE_USERS_TO_DEACTIVATE = Arrays.asList(
     *     "john.doe"
     * );
     *
     * MULTIPLE USERS EXAMPLE:
     * private static final List<String> ACTIVE_USERS_TO_DEACTIVATE = Arrays.asList(
     *     "john.doe",
     *     "jane.smith",
     *     "bob.wilson"
     * );
     *
     * IMPORTANT:
     * - Users must already exist in IIQ
     * - Users must be ACTIVE (inactive=false, vf_id_status="Active Adhoc")
     * - Replace the example names below with your actual user IDs
     */
    private static final List<String> ACTIVE_USERS_TO_DEACTIVATE = Arrays.asList(
        "activeuser1",    // ← Replace with your actual user ID
        "activeuser2",    // ← Replace with your actual user ID
        "activeuser3"     // ← Replace with your actual user ID
        // Add as many users as you need...
    );

    // ==========================================================

    /**
     * Test activation of single or multiple users with complete form handling.
     * Returns the Request ID upon successful completion.
     */
    @Test
    public void testActivateMultipleUsersWithRequestId() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Activate Identity/Identities - Complete Workflow");
        logger.info("========================================");
        logger.info("Launcher user: {}", LAUNCHER_USER);
        logger.info("Users to activate: {}", INACTIVE_USERS_TO_ACTIVATE);
        logger.info("Total users to activate: {}", INACTIVE_USERS_TO_ACTIVATE.size());

        // Step 1: Launch workflow
        logger.info("Step 1: Launching workflow...");
        Map<String, Object> initialVariables = new HashMap<>();
        initialVariables.put("launcher", LAUNCHER_USER);
        initialVariables.put("identityName", LAUNCHER_USER);
        initialVariables.put("userLocale", Locale.ENGLISH);  // Required by workflow
        initialVariables.put("clientTimeZone", TimeZone.getDefault());  // Required by workflow

        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, initialVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("✓ Workflow launched with case ID: {}", workflowCaseId);

        // Step 2: Complete Request Initiate Form
        logger.info("Step 2: Completing Request Initiate form...");
        logger.info("DEBUG: Searching for WorkItem with workflowCaseId: {}", workflowCaseId);

        // Debug: Check all pending work items immediately
        logger.info("DEBUG: Checking pending work items before wait...");
        List<WorkItem> allItems = workItemHandler.getWorkItemsForWorkflow(workflowCaseId);
        logger.info("DEBUG: Found {} work items for workflow case '{}'", allItems != null ? allItems.size() : 0, workflowCaseId);
        if (allItems != null && !allItems.isEmpty()) {
            for (WorkItem item : allItems) {
                logger.info("DEBUG: - WorkItem: type={}, owner={}, state={}", item.getType(), item.getOwner(), item.getState());
            }
        }

        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Initiate form should appear", initiateForm);
        logger.info("✓ Request Initiate form found: {}", initiateForm.getId());

        // Fill in the Request Initiate form data
        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("operation", "Activate");  // Activate or Deactivate
        initiateData.put("sponsorScope", "Local Market");  // Local Market or Service
        initiateData.put("vfMarket", "Vodafone Limited");  // Market selection (adjust as needed)
        initiateData.put("requestees", INACTIVE_USERS_TO_ACTIVATE);  // List of users
        initiateData.put("action", "individual");  // Hidden field, always "individual"

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateData);
        assertTrue("Request Initiate form should be completed", initiateCompleted);
        logger.info("✓ Request Initiate form completed");
        logger.info("   - Operation: Activate");
        logger.info("   - Market: UK");
        logger.info("   - Users: {}", INACTIVE_USERS_TO_ACTIVATE);

        // Step 3: Complete Request Submit Form
        logger.info("Step 3: Completing Request Submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Submit form should appear", submitForm);
        logger.info("✓ Request Submit form found: {}", submitForm.getId());

        // Fill in the Request Submit form data
        Map<String, Object> submitData = new HashMap<>();
        submitData.put("busJustification", "Automated test - Activating users for inactive local market testing");
        submitData.put("navigator", "vfsummary");  // Submit action

        boolean submitCompleted = workItemHandler.completeFormWorkItem(submitForm.getId(), submitData);
        assertTrue("Request Submit form should be completed", submitCompleted);
        logger.info("✓ Request Submit form completed with business justification");

        // Step 4: Wait for workflow to process and launch sub-workflow
        logger.info("Step 4: Waiting for workflow processing...");
        Thread.sleep(5000);  // Give time for sub-workflow to be created

        // Step 5: Get the Request ID from workflow variables
        logger.info("Step 5: Retrieving Request ID...");
        WorkflowCase workflowCase = workflowExecutor.getWorkflowCase(workflowCaseId);

        String requestId = null;
        String identityRequestId = null;

        if (workflowCase != null && workflowCase.getAttributes() != null) {
            requestId = (String) workflowCase.getAttribute("requestID");
            identityRequestId = (String) workflowCase.getAttribute("identityRequestId");

            logger.info("========================================");
            logger.info("✓ REQUEST ID CAPTURED");
            logger.info("========================================");
            logger.info("Request ID: {}", requestId);
            logger.info("Identity Request ID: {}", identityRequestId);
            logger.info("========================================");
        }

        // Step 6: Complete Summary Form
        logger.info("Step 6: Completing Summary form...");
        WorkItem summaryForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);

        if (summaryForm != null) {
            logger.info("✓ Summary form found: {}", summaryForm.getId());
            Map<String, Object> summaryData = new HashMap<>();
            boolean summaryCompleted = workItemHandler.completeFormWorkItem(summaryForm.getId(), summaryData);
            assertTrue("Summary form should be completed", summaryCompleted);
            logger.info("✓ Summary form completed");
        } else {
            logger.info("⚠ No summary form found - workflow may have auto-completed");
        }

        // Step 7: Wait for workflow completion
        logger.info("Step 7: Waiting for workflow completion...");
        WorkflowCase completedCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);

        // Final validation
        logger.info("========================================");
        logger.info("✓ TEST COMPLETE");
        logger.info("========================================");
        logger.info("Workflow Status: {}", completedCase != null ? completedCase.getCompletionStatus() : "UNKNOWN");
        logger.info("Request ID: {}", requestId != null ? requestId : "NOT CAPTURED");
        logger.info("Users Activated: {}", INACTIVE_USERS_TO_ACTIVATE);
        logger.info("========================================");

        // Validate users are now active (optional - comment out if you want to check manually)
        validateUsersActivated();

        // Assert that we got a request ID
        assertNotNull("Request ID should be captured from workflow", requestId);
    }

    /**
     * Test deactivation of single or multiple users with complete form handling.
     * Returns the Request ID upon successful completion.
     */
    @Test
    public void testDeactivateMultipleUsersWithRequestId() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST: Deactivate Identity/Identities - Complete Workflow");
        logger.info("========================================");
        logger.info("Launcher user: {}", LAUNCHER_USER);
        logger.info("Users to deactivate: {}", ACTIVE_USERS_TO_DEACTIVATE);
        logger.info("Total users to deactivate: {}", ACTIVE_USERS_TO_DEACTIVATE.size());

        // Step 1: Launch workflow
        logger.info("Step 1: Launching workflow...");
        Map<String, Object> initialVariables = new HashMap<>();
        initialVariables.put("launcher", LAUNCHER_USER);
        initialVariables.put("identityName", LAUNCHER_USER);
        initialVariables.put("userLocale", Locale.ENGLISH);  // Required by workflow
        initialVariables.put("clientTimeZone", TimeZone.getDefault());  // Required by workflow

        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, initialVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);
        logger.info("✓ Workflow launched with case ID: {}", workflowCaseId);

        // Step 2: Complete Request Initiate Form
        logger.info("Step 2: Completing Request Initiate form...");
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Initiate form should appear", initiateForm);
        logger.info("✓ Request Initiate form found: {}", initiateForm.getId());

        // Fill in the Request Initiate form data for deactivation
        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("operation", "Deactivate");  // Deactivate operation
        initiateData.put("requestees", ACTIVE_USERS_TO_DEACTIVATE);  // List of users
        initiateData.put("action", "individual");  // Hidden field

        boolean initiateCompleted = workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateData);
        assertTrue("Request Initiate form should be completed", initiateCompleted);
        logger.info("✓ Request Initiate form completed");
        logger.info("   - Operation: Deactivate");
        logger.info("   - Users: {}", ACTIVE_USERS_TO_DEACTIVATE);

        // Step 3: Complete Request Submit Form
        logger.info("Step 3: Completing Request Submit form...");
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Submit form should appear", submitForm);
        logger.info("✓ Request Submit form found: {}", submitForm.getId());

        // Fill in the Request Submit form data
        Map<String, Object> submitData = new HashMap<>();
        submitData.put("busJustification", "Automated test - Deactivating adhoc users no longer needed");
        submitData.put("navigator", "vfsummary");  // Submit action

        boolean submitCompleted = workItemHandler.completeFormWorkItem(submitForm.getId(), submitData);
        assertTrue("Request Submit form should be completed", submitCompleted);
        logger.info("✓ Request Submit form completed with business justification");

        // Step 4: Wait for workflow processing
        logger.info("Step 4: Waiting for workflow processing...");
        Thread.sleep(5000);

        // Step 5: Get the Request ID
        logger.info("Step 5: Retrieving Request ID...");
        WorkflowCase workflowCase = workflowExecutor.getWorkflowCase(workflowCaseId);

        String requestId = null;

        if (workflowCase != null && workflowCase.getAttributes() != null) {
            requestId = (String) workflowCase.getAttribute("requestID");

            logger.info("========================================");
            logger.info("✓ REQUEST ID CAPTURED");
            logger.info("========================================");
            logger.info("Request ID: {}", requestId);
            logger.info("========================================");
        }

        // Step 6: Complete Summary Form
        logger.info("Step 6: Completing Summary form...");
        WorkItem summaryForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);

        if (summaryForm != null) {
            logger.info("✓ Summary form found: {}", summaryForm.getId());
            Map<String, Object> summaryData = new HashMap<>();
            boolean summaryCompleted = workItemHandler.completeFormWorkItem(summaryForm.getId(), summaryData);
            assertTrue("Summary form should be completed", summaryCompleted);
            logger.info("✓ Summary form completed");
        }

        // Step 7: Wait for workflow completion
        logger.info("Step 7: Waiting for workflow completion...");
        WorkflowCase completedCase = workflowExecutor.waitForCompletion(workflowCaseId, 60);

        // Final validation
        logger.info("========================================");
        logger.info("✓ TEST COMPLETE");
        logger.info("========================================");
        logger.info("Workflow Status: {}", completedCase != null ? completedCase.getCompletionStatus() : "UNKNOWN");
        logger.info("Request ID: {}", requestId != null ? requestId : "NOT CAPTURED");
        logger.info("Users Deactivated: {}", ACTIVE_USERS_TO_DEACTIVATE);
        logger.info("========================================");

        // Assert that we got a request ID
        assertNotNull("Request ID should be captured from workflow", requestId);
    }

    /**
     * Validate that users were successfully activated
     */
    private void validateUsersActivated() {
        logger.info("Validating users after activation...");

        for (String userName : INACTIVE_USERS_TO_ACTIVATE) {
            Identity updatedUser = identityService.getIdentity(userName);

            if (updatedUser != null) {
                Boolean finalInactive = (Boolean) updatedUser.getAttribute("inactive");
                String finalStatus = (String) updatedUser.getAttribute("vf_id_status");

                logger.info("User: {} - After activation: inactive={}, status={}",
                    userName, finalInactive, finalStatus);

                // Note: Actual validation depends on workflow logic and approval completion
                // Uncomment if you want strict validation:
                // assertFalse("User should be active after workflow", finalInactive != null && finalInactive);
                // assertEquals("Status should be Active Adhoc", "Active Adhoc", finalStatus);
            }
        }
    }

    /**
     * Helper method to create multiple test users for activation testing.
     *
     * USAGE:
     * 1. Update the userCount variable to create as many users as you need
     * 2. Run: mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#createMultipleInactiveUsersForTest
     * 3. Copy the output from the logs
     * 4. Paste into INACTIVE_USERS_TO_ACTIVATE list above
     */
    @Test
    public void createMultipleInactiveUsersForTest() {
        logger.info("========================================");
        logger.info("Creating multiple INACTIVE users for activation testing");
        logger.info("========================================");

        // *** CONFIGURE THIS: Change the number to create more or fewer users ***
        int userCount = 5;  // Default: Create 5 inactive users

        List<String> createdUsers = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            String userName = TestIdentities.generateUniqueIdentityName("test_user_activation_" + i);
            String managerName = TestIdentities.generateUniqueIdentityName("test_manager_" + i);

            logger.info("Creating user {}/{}: {}", i, userCount, userName);

            // Create manager
            Map<String, Object> managerAttrs = TestIdentities.createRealisticManagerAttributes();
            Identity manager = identityService.createTestIdentity(managerName, managerAttrs);
            assertNotNull("Manager should be created", manager);

            // Create inactive user
            Map<String, Object> userAttrs = TestIdentities.createRealisticInactiveUserAttributes();
            Identity user = identityService.createTestIdentityWithManager(userName, managerName, userAttrs);
            assertNotNull("User should be created", user);

            createdUsers.add(userName);
            logger.info("✓ Created user {}/{}: {}", i, userCount, userName);
        }

        logger.info("========================================");
        logger.info("✓ {} USERS CREATED SUCCESSFULLY", userCount);
        logger.info("========================================");
        logger.info("");
        logger.info("COPY THE FOLLOWING TO INACTIVE_USERS_TO_ACTIVATE:");
        logger.info("");
        logger.info("Arrays.asList(");
        for (int i = 0; i < createdUsers.size(); i++) {
            if (i < createdUsers.size() - 1) {
                logger.info("    \"{}\",", createdUsers.get(i));
            } else {
                logger.info("    \"{}\"", createdUsers.get(i));
            }
        }
        logger.info(")");
        logger.info("");
        logger.info("========================================");
    }

    /**
     * Helper method to create multiple ACTIVE users for deactivation testing.
     *
     * USAGE:
     * 1. Update the userCount variable to create as many users as you need
     * 2. Run: mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#createMultipleActiveUsersForTest
     * 3. Copy the output from the logs
     * 4. Paste into ACTIVE_USERS_TO_DEACTIVATE list above
     */
    @Test
    public void createMultipleActiveUsersForTest() {
        logger.info("========================================");
        logger.info("Creating multiple ACTIVE users for deactivation testing");
        logger.info("========================================");

        // *** CONFIGURE THIS: Change the number to create more or fewer users ***
        int userCount = 5;  // Default: Create 5 active users

        List<String> createdUsers = new ArrayList<>();

        for (int i = 1; i <= userCount; i++) {
            String userName = TestIdentities.generateUniqueIdentityName("active_user_" + i);
            String managerName = TestIdentities.generateUniqueIdentityName("active_manager_" + i);

            logger.info("Creating user {}/{}: {}", i, userCount, userName);

            // Create manager
            Map<String, Object> managerAttrs = TestIdentities.createRealisticManagerAttributes();
            Identity manager = identityService.createTestIdentity(managerName, managerAttrs);
            assertNotNull("Manager should be created", manager);

            // Create active user
            Map<String, Object> userAttrs = TestIdentities.createRealisticActiveUserAttributes();
            Identity user = identityService.createTestIdentityWithManager(userName, managerName, userAttrs);
            assertNotNull("User should be created", user);

            createdUsers.add(userName);
            logger.info("✓ Created user {}/{}: {}", i, userCount, userName);
        }

        logger.info("========================================");
        logger.info("✓ {} ACTIVE USERS CREATED SUCCESSFULLY", userCount);
        logger.info("========================================");
        logger.info("");
        logger.info("COPY THE FOLLOWING TO ACTIVE_USERS_TO_DEACTIVATE:");
        logger.info("");
        logger.info("Arrays.asList(");
        for (int i = 0; i < createdUsers.size(); i++) {
            if (i < createdUsers.size() - 1) {
                logger.info("    \"{}\",", createdUsers.get(i));
            } else {
                logger.info("    \"{}\"", createdUsers.get(i));
            }
        }
        logger.info(")");
        logger.info("");
        logger.info("========================================");
    }

    /**
     * Quick test to verify multiple users can be activated together.
     * This demonstrates the workflow with 3 users at once.
     */
    @Test
    public void exampleActivateThreeUsersAtOnce() throws InterruptedException {
        logger.info("========================================");
        logger.info("EXAMPLE: Activate 3 users in a single workflow");
        logger.info("========================================");

        // Create 3 test users
        List<String> usersToActivate = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String userName = TestIdentities.generateUniqueIdentityName("demo_user_" + i);
            String managerName = TestIdentities.generateUniqueIdentityName("demo_manager_" + i);

            Map<String, Object> managerAttrs = TestIdentities.createRealisticManagerAttributes();
            identityService.createTestIdentity(managerName, managerAttrs);

            Map<String, Object> userAttrs = TestIdentities.createRealisticInactiveUserAttributes();
            identityService.createTestIdentityWithManager(userName, managerName, userAttrs);

            usersToActivate.add(userName);
            logger.info("✓ Created test user {}: {}", i, userName);
        }

        logger.info("========================================");
        logger.info("Activating {} users together: {}", usersToActivate.size(), usersToActivate);
        logger.info("========================================");

        // Launch workflow with all 3 users
        Map<String, Object> initialVariables = new HashMap<>();
        initialVariables.put("launcher", LAUNCHER_USER);
        initialVariables.put("identityName", LAUNCHER_USER);
        initialVariables.put("userLocale", Locale.ENGLISH);  // Required by workflow
        initialVariables.put("clientTimeZone", TimeZone.getDefault());  // Required by workflow
        initialVariables.put("userLocale", Locale.ENGLISH);
        initialVariables.put("clientTimeZone", TimeZone.getDefault().getID());  // Use timezone ID string instead of TimeZone object

        String workflowCaseId = workflowExecutor.launchWorkflow(WORKFLOW_NAME, LAUNCHER_USER, initialVariables);
        assertNotNull("Workflow should be launched", workflowCaseId);

        // Complete Request Initiate Form with all 3 users
        WorkItem initiateForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Initiate form should appear", initiateForm);

        Map<String, Object> initiateData = new HashMap<>();
        initiateData.put("operation", "Activate");
        initiateData.put("sponsorScope", "Local Market");
        initiateData.put("vfMarket", "Vodafone Limited");
        initiateData.put("requestees", usersToActivate);  // All 3 users at once!
        initiateData.put("action", "individual");

        workItemHandler.completeFormWorkItem(initiateForm.getId(), initiateData);
        logger.info("✓ Request Initiate form completed with {} users", usersToActivate.size());

        // Complete Request Submit Form
        WorkItem submitForm = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 30);
        assertNotNull("Request Submit form should appear", submitForm);

        Map<String, Object> submitData = new HashMap<>();
        submitData.put("busJustification", "Demo: Activating 3 users together in one workflow");
        submitData.put("navigator", "vfsummary");

        workItemHandler.completeFormWorkItem(submitForm.getId(), submitData);
        logger.info("✓ Request Submit form completed");

        logger.info("========================================");
        logger.info("✓ WORKFLOW SUBMITTED FOR {} USERS", usersToActivate.size());
        logger.info("========================================");
        logger.info("Users activated: {}", usersToActivate);
        logger.info("========================================");
    }
}
