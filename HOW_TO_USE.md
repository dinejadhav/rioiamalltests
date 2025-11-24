# How to Use - Activate/Deactivate Existing Users

## Simple 2-Step Process

### Step 1: Add Your User IDs to the Test

Open: `src/test/java/com/rioiam/iiq/workflow/activatedeactivate/ActivateDeactivateIdentityCompleteTest.java`

Find this section and replace with your actual user IDs:

```java
// ========== PROVIDE YOUR EXISTING INACTIVE USERS HERE ==========
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "user1",    // ← Replace with your actual user ID
    "user2",    // ← Replace with your actual user ID
    "user3"     // ← Replace with your actual user ID
    // Add as many users as you need...
);
```

**Example with real user IDs:**
```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "john.doe",
    "jane.smith",
    "bob.wilson",
    "alice.johnson",
    "mike.brown"
);
```

### Step 2: Run the Test

```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testActivateMultipleUsersWithRequestId
```

**That's it!** The test will:
- Launch the workflow as `dinesh.jadhav1`
- Fill the initiate form with all your users
- Fill the submit form with business justification
- Submit the workflow
- Return the Request ID

---

## For Deactivation

Same process, just use different users:

```java
// ========== PROVIDE YOUR EXISTING ACTIVE USERS HERE ==========
private static final List<String> ACTIVE_USERS_TO_DEACTIVATE = Arrays.asList(
    "activeuser1",
    "activeuser2",
    "activeuser3"
);
```

Run:
```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testDeactivateMultipleUsersWithRequestId
```

---

## Configuration

All settings are in the test file:

| Setting | Value | Where to Change |
|---------|-------|----------------|
| Launcher User | `dinesh.jadhav1` | `LAUNCHER_USER` constant |
| Market | `Vodafone Limited` | In the test code: `initiateData.put("vfMarket", "Vodafone Limited")` |
| Sponsor Scope | `Local Market` | In the test code: `initiateData.put("sponsorScope", "Local Market")` |
| Business Justification | Auto-generated | In the test code: `submitData.put("busJustification", "...")` |

---

## Examples

### Activate 1 User
```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "john.doe"
);
```

### Activate 3 Users
```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "john.doe",
    "jane.smith",
    "bob.wilson"
);
```

### Activate 10 Users
```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "user01",
    "user02",
    "user03",
    "user04",
    "user05",
    "user06",
    "user07",
    "user08",
    "user09",
    "user10"
);
```

---

## Important Notes

✅ **Users must already exist in IIQ** - The test does NOT create users
✅ **For activation**: Users must be INACTIVE (`inactive=true`, `vf_id_status="Inactive"`)
✅ **For deactivation**: Users must be ACTIVE (`inactive=false`, `vf_id_status="Active Adhoc"`)
✅ **All operations run as dinesh.jadhav1** - Not spadmin
✅ **Market is Vodafone Limited** - Not UK

---

## What the Test Does

1. **Launches workflow** with `dinesh.jadhav1` as launcher
2. **Waits for Initiate Form** (automatically)
3. **Fills Initiate Form** with:
   - Operation: "Activate" or "Deactivate"
   - Sponsor Scope: "Local Market"
   - Market: "Vodafone Limited"
   - Requestees: Your list of users
   - Action: "individual"
4. **Waits for Submit Form** (automatically)
5. **Fills Submit Form** with:
   - Business Justification: "Automated test - Activating users for inactive local market testing"
   - Navigator: "vfsummary" (to submit)
6. **Captures Request ID** from workflow variables
7. **Completes Summary Form** if it appears
8. **Logs the Request ID**

---

## Output Example

```
========================================
TEST: Activate Identity/Identities - Complete Workflow
========================================
Launcher user: dinesh.jadhav1
Users to activate: [john.doe, jane.smith, bob.wilson]
Total users to activate: 3

Step 1: Launching workflow...
✓ Workflow launched with case ID: 12345

Step 2: Completing Request Initiate form...
✓ Request Initiate form completed
   - Operation: Activate
   - Market: Vodafone Limited
   - Users: [john.doe, jane.smith, bob.wilson]

Step 3: Completing Request Submit form...
✓ Request Submit form completed with business justification

========================================
✓ REQUEST ID CAPTURED
========================================
Request ID: 67890
========================================
```

---

## Troubleshooting

**Issue**: Test fails with "User must exist in IIQ"
**Solution**: Check that your user IDs are correct and exist in IIQ

**Issue**: Test fails with "Workflow should be launched"
**Solution**: This is a known issue with workflow case ID. Check IIQ UI to verify the workflow actually launched successfully.

**Issue**: Form timeout
**Solution**: The workflow might be slow. Check IIQ UI for the workflow status.

---

## Quick Reference

### Activate Users
1. Edit `INACTIVE_USERS_TO_ACTIVATE` list
2. Run: `mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testActivateMultipleUsersWithRequestId`

### Deactivate Users
1. Edit `ACTIVE_USERS_TO_DEACTIVATE` list
2. Run: `mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testDeactivateMultipleUsersWithRequestId`

---

**No user creation needed - just provide existing user IDs and run the test!**
