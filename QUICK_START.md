# Quick Start Guide - Multiple Users Activation

## 🚀 3-Step Process

### Step 1: Create Test Users (5 users)
```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#createMultipleInactiveUsersForTest
```

### Step 2: Copy Output and Paste into Test File

Look for this in the logs:
```
COPY THE FOLLOWING TO INACTIVE_USERS_TO_ACTIVATE:

Arrays.asList(
    "test_user_activation_1763557890145",
    "test_user_activation_1763557890146",
    "test_user_activation_1763557890147",
    "test_user_activation_1763557890148",
    "test_user_activation_1763557890149"
)
```

Paste into `ActivateDeactivateIdentityCompleteTest.java`:
```java
private static final List<String> INACTIVE_USERS_TO_ACTIVATE = Arrays.asList(
    "test_user_activation_1763557890145",
    "test_user_activation_1763557890146",
    "test_user_activation_1763557890147",
    "test_user_activation_1763557890148",
    "test_user_activation_1763557890149"
);
```

### Step 3: Run the Test
```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#testActivateMultipleUsersWithRequestId
```

---

## ✅ What Happens

1. **Workflow launches** as user `dinesh.jadhav1`
2. **Request Initiate Form** filled with:
   - Operation: Activate
   - Market: Vodafone Limited
   - Users: All 5 users in the list
3. **Request Submit Form** filled with:
   - Business Justification: "Automated test - Activating users..."
4. **Request ID returned** in the logs

---

## 📝 Current Configuration

| Setting | Value |
|---------|-------|
| Launcher User | `dinesh.jadhav1` |
| Market (vfMarket) | `Vodafone Limited` |
| Sponsor Scope | `Local Market` |
| Operation | `Activate` or `Deactivate` |

---

## 🎯 Want to Activate More/Fewer Users?

Edit this line in `createMultipleInactiveUsersForTest()`:
```java
int userCount = 5;  // Change to 10, 20, etc.
```

---

## 🔍 Test Multiple Users Example

Already included! Run:
```bash
mvn test -Dtest=ActivateDeactivateIdentityCompleteTest#exampleActivateThreeUsersAtOnce
```

This creates 3 users and activates them together automatically.

---

## 📖 Full Documentation

See `MULTIPLE_USERS_ACTIVATION_GUIDE.md` for complete details.

---

## ⚡ Key Features

✅ **Multiple users in one workflow**
✅ **Automatic form filling**
✅ **Business justification included**
✅ **Request ID tracking**
✅ **Runs as dinesh.jadhav1 (not spadmin)**
✅ **Market: Vodafone Limited**

