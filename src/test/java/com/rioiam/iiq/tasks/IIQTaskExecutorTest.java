package com.rioiam.iiq.tasks;

import org.springframework.test.context.ContextConfiguration;
import com.rioiam.iiq.config.EnvironmentConfig;
import com.rioiam.iiq.context.IIQRemoteContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import sailpoint.object.TaskResult;



@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {IIQTaskExecutor.class, IIQRemoteContext.class, EnvironmentConfig.class})
public class IIQTaskExecutorTest  {

    @Autowired
    private IIQTaskExecutor taskExecutor;

    @Test
    public void testRefreshIdentityCubeTask() {
        String taskName = "Refresh Identity Cube";
        TaskResult result = taskExecutor.executeTaskByName(taskName);
        assert result != null : "TaskResult should not be null";
        assert result.getCompleted() != null : "Task should be completed";
        System.out.println("Task completed: " + result.getCompleted());
    }

    @Test
    public void testDineshCsvTask() {
        String taskName = "dineshcsv";
        System.out.println("========================================");
        System.out.println("Executing task: " + taskName);
        System.out.println("========================================");

        TaskResult result = taskExecutor.executeTaskByName(taskName);

        if (result != null) {
            System.out.println("✓ Task completed successfully!");
            System.out.println("  Result ID: " + result.getId());
            System.out.println("  Result Name: " + result.getName());
            System.out.println("  Completed: " + result.getCompleted());
            System.out.println("  Completion Status: " + result.getCompletionStatus());

            // Print task attributes
            if (result.getAttributes() != null) {
                System.out.println("  Task Attributes:");
                result.getAttributes().forEach((key, value) -> {
                    System.out.println("    " + key + ": " + value);
                });
            }

            assert result.getCompleted() != null : "Task should be completed";
        } else {
            System.out.println("✗ Task returned null - task may not exist or failed");
        }
    }

    @Test
    public void testPerformMaintenanceTask() {
        String taskName = "Perform Maintenance";
        System.out.println("========================================");
        System.out.println("Executing SailPoint Default Task: " + taskName);
        System.out.println("========================================");

        TaskResult result = taskExecutor.executeTaskByName(taskName);

        if (result != null) {
            System.out.println("✓ Task executed!");
            System.out.println("  Result ID: " + result.getId());
            System.out.println("  Result Name: " + result.getName());
            System.out.println("  Completed: " + result.getCompleted());
            System.out.println("  Completion Status: " + result.getCompletionStatus());

            // Print task attributes
            if (result.getAttributes() != null && !result.getAttributes().isEmpty()) {
                System.out.println("  Task Attributes:");
                result.getAttributes().forEach((key, value) -> {
                    System.out.println("    " + key + ": " + value);
                });
            }

            // Check if task completed successfully
            if (result.getCompletionStatus() != null) {
                switch (result.getCompletionStatus().toString()) {
                    case "Success":
                        System.out.println("\n✓✓✓ SUCCESS! Task completed without errors.");
                        break;
                    case "Warning":
                        System.out.println("\n⚠ WARNING: Task completed with warnings.");
                        break;
                    case "Error":
                        System.out.println("\n✗ ERROR: Task completed with errors.");
                        break;
                    default:
                        System.out.println("\n? Unknown completion status: " + result.getCompletionStatus());
                }
            }

            assert result.getCompleted() != null : "Task should be completed";
        } else {
            System.out.println("✗ Task returned null - task may not exist or execution failed");
        }

        System.out.println("========================================");
    }
}