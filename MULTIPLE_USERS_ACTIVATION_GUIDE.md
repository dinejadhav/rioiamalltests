# Multiple Users Activation/Deactivation Guide

This guide shows how to activate or deactivate **multiple users at once** using the test framework.

## Quick Start

### Step 1: Create Multiple Test Users

Run one of these commands to create test users:

```bash
# Create 5 inactive users for activation testing
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#createMultipleInactiveUsersForTest

# Create 5 active users for deactivation testing
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#createMultipleActiveUsersForTest
```

**Want to create more users?**
- Open the test file and change the `userCount` variable (default is 5)

### Step 2: Copy the User Names

After the test runs, you'll see output like this in the logs:

```
========================================
✓ 5 USERS CREATED SUCCESSFULLY
========================================

COPY THE FOLLOWING TO INACTIVE_USERS_TO_ACTIVATE:

Arrays.asList(
    "test_user_activation_1763557890145",
    "test_user_activation_1763557890146",
    "test_user_activation_1763557890147",
    "test_user_activation_1763557890148",
    "test_user_activation_1763557890149"
)

========================================
```

### Step 3: Paste User Names into Test

Open `ActivateDeactivateIdentityCompleteTest.java` and paste the user names:

```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "test_user_activation_1763557890145",
    "test_user_activation_1763557890146",
    "test_user_activation_1763557890147",
    "test_user_activation_1763557890148",
    "test_user_activation_1763557890149"
);
```

### Step 4: Run the Activation Test

```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testActivateMultipleUsersWithRequestId
```

**That's it!** The workflow will:
1. Launch once
2. Fill the initiate form with all 5 users
3. Fill the submit form with justification
4. Process all users together
5. Return the Request ID

---

## How It Works

The workflow handles multiple users efficiently:

1. **Single Workflow Launch**: One workflow handles all users
2. **Single Request Initiate Form**: All users are added to the `requestees` field
3. **Single Request Submit Form**: One justification for all users
4. **Multiple Sub-Workflows**: The main workflow creates one sub-workflow per user
5. **Single Request ID**: You get one request ID that tracks all users

---

## Examples

### Example 1: Activate 1 User

```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "test_user_activation_1763557890145"
);
```

### Example 2: Activate 3 Users

```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "test_user_activation_1763557890145",
    "test_user_activation_1763557890146",
    "test_user_activation_1763557890147"
);
```

### Example 3: Activate 10 Users

```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "test_user_activation_1763557890145",
    "test_user_activation_1763557890146",
    "test_user_activation_1763557890147",
    "test_user_activation_1763557890148",
    "test_user_activation_1763557890149",
    "test_user_activation_1763557890150",
    "test_user_activation_1763557890151",
    "test_user_activation_1763557890152",
    "test_user_activation_1763557890153",
    "test_user_activation_1763557890154"
);
```

---

## Demo Test

Want to see it in action first? Run the demo test:

```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#exampleActivateThreeUsersAtOnce
```

This will:
1. Create 3 test users automatically
2. Activate all 3 together
3. Show you exactly how it works

---

## Configuration Options

### Change the Launcher User

The user who launches the workflow (default: `dinesh.jadhav1`):

```java
private static final String LAUNCHER_USER = "dinesh.jadhav1";
```

### Change the Market

The market/OpCo for activation (default: `Vodafone Limited`):

```java
initiateData.put("vfMarket", "Vodafone Limited");  // Change to your market
```

### Change Business Justification

Update the justification text:

```java
submitData.put("busJustification", "Your custom justification here");
```

---

## Workflow Form Fields

The test automatically fills these fields:

### Request Initiate Form
- `operation`: "Activate" or "Deactivate"
- `sponsorScope`: "Local Market" or "Service"
- `vfMarket`: Market name (e.g., "UK")
- `requestees`: **List of user names** ← This is where multiple users go!
- `action`: "individual"

### Request Submit Form
- `busJustification`: Business justification text (required, max 512 chars)
- `navigator`: "vfsummary" (to submit)

---

## Tips and Best Practices

### 1. Start Small
- Test with 1 user first to ensure everything works
- Then increase to 2-3 users
- Finally test with your target number

### 2. Check User Status First
All users must be:
- **For activation**: `inactive=true` and `vf_id_status="Inactive"`
- **For deactivation**: `inactive=false` and `vf_id_status="Active Adhoc"`

### 3. Monitor Logs
The test logs show detailed progress:
```
Launcher user: dinesh.jadhav1
Users to activate: [user1, user2, user3]
Step 1: Launching workflow...
✓ Request Initiate form completed with 3 users
✓ Request Submit form completed
```

### 4. Request ID Tracking
The test captures the Request ID from the workflow:
```
========================================
✓ REQUEST ID CAPTURED
========================================
Request ID: 12345
========================================
```

---

## Troubleshooting

### Issue: "User not found"
**Solution**: Run `createMultipleInactiveUsersForTest()` first to create users

### Issue: "User must be inactive"
**Solution**: Check user status with:
```bash
mvn test -Dtest=CheckUserExistsTest
```

### Issue: "Workflow should be launched"
**Solution**: This is a known issue with workflow case ID being null. The workflow still launches successfully - check IIQ UI to verify.

### Issue: "Form timeout"
**Solution**: Increase the timeout in `waitForWorkItem()`:
```java
WorkItem form = workItemHandler.waitForWorkItem(workflowCaseId, WorkItem.Type.Form, 60);  // 60 seconds
```

---

## Advanced Usage

### Programmatically Add Users

Instead of hardcoding user names, you can generate them programmatically:

```java
@Test
public void activateDynamicUsers() throws InterruptedException {
    List<String> users = new ArrayList<>();

    // Create and activate 10 users on the fly
    for (int i = 1; i <= 10; i++) {
        String userName = TestIdentities.generateUniqueIdentityName("bulk_user_" + i);
        // Create user...
        users.add(userName);
    }

    // Activate all users together
    // ... rest of test code
}
```

### Mix and Match Operations

You can run different operations in separate tests:

```bash
# Activate users
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testActivateMultipleUsersWithRequestId

# Deactivate users
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testDeactivateMultipleUsersWithRequestId
```

---

## Test File Location

```
src/test/java/com/rioiam/iiq/workflow/activatedeactivate/ActivateDeactivateIdentityCompleteTest.java
```

---

## Summary

✅ **Multiple users supported out of the box**
✅ **Simple configuration** - just paste user names into a list
✅ **Helper methods** to create test users automatically
✅ **Demo test** included to see it in action
✅ **Request ID tracking** for all users
✅ **Full form handling** with business justification

**The workflow is designed to handle multiple users efficiently in a single execution!**
