package com.rioiam.iiq.tasks;

import com.rioiam.iiq.config.EnvironmentConfig;
import com.rioiam.iiq.context.IIQRemoteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sailpoint.api.SailPointContext;
import sailpoint.api.TaskManager;
import sailpoint.object.*;
import sailpoint.tools.GeneralException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes any IIQ Task by name using the remote context.
 * Compatible with SailPoint IIQ 8.5.
 *
 * FIXED VERSION: Uses TaskManager.runSync() for immediate synchronous execution
 */
@Component
public class IIQTaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(IIQTaskExecutor.class);

    @Autowired
    private IIQRemoteContext remoteContext;

    @Autowired
    private EnvironmentConfig environmentConfig;

    /**
     * Executes a task by its name and waits for completion.
     * This method directly executes the task using Tasker.runTask() instead of scheduling it.
     *
     * @param taskName the name of the IIQ TaskDefinition
     * @return TaskResult if execution is successful, null otherwise
     */
    public TaskResult executeTaskByName(String taskName) {
        return executeTaskByName(taskName, null);
    }

    /**
     * Executes a task by its name with custom arguments and waits for completion.
     *
     * @param taskName the name of the IIQ TaskDefinition
     * @param taskArguments optional map of arguments to pass to the task
     * @return TaskResult if execution is successful, null otherwise
     */
    public TaskResult executeTaskByName(String taskName, Map<String, Object> taskArguments) {
        logger.info("========================================");
        logger.info("Starting DIRECT execution for IIQ Task: {}", taskName);
        logger.info("========================================");

        TaskResult taskResult = null;

        try {
            SailPointContext context = remoteContext.getContext();
            logger.info("✓ Obtained SailPointContext");

            // Get the task definition
            TaskDefinition taskDef = context.getObjectByName(TaskDefinition.class, taskName);
            if (taskDef == null) {
                logger.error("✗ TaskDefinition not found: {}", taskName);
                return null;
            }

            logger.info("✓ TaskDefinition found: {} (ID: {})", taskDef.getName(), taskDef.getId());
            logger.info("  Task Type: {}", taskDef.getType());
            logger.info("  Executor: {}", taskDef.getExecutor());

            // Create task arguments if not provided
            if (taskArguments == null) {
                taskArguments = new HashMap<>();
            }

            // Create a unique result name
            String resultName = taskName + "_Result_" + System.currentTimeMillis();

            // Create Attributes object for task arguments
            Attributes<String, Object> taskArgs = new Attributes<>();
            taskArgs.putAll(taskArguments);

            logger.info("Executing task directly using TaskManager.runSync()...");
            logger.info("Result name: {}", resultName);

            // Start transaction
            context.startTransaction();

            try {
                // Use TaskManager.runSync() for synchronous execution
                logger.info("Using TaskManager.runSync() for immediate execution...");

                // Create TaskManager and execute task synchronously
                TaskManager taskManager = new TaskManager(context);

                // Convert Attributes to Map for runSync
                Map<String, Object> argsMap = new HashMap<>(taskArgs);

                // runSync signature: runSync(TaskDefinition, Map<String, Object>)
                taskResult = taskManager.runSync(taskDef, argsMap);

                if (taskResult != null) {
                    // Save the result
                    context.saveObject(taskResult);
                    context.commitTransaction();

                    logger.info("✓ Task executed successfully!");
                    logger.info("  Result ID: {}", taskResult.getId());
                    logger.info("  Result Name: {}", taskResult.getName());
                    logger.info("  Completed: {}", taskResult.getCompleted());
                    logger.info("  Completion Status: {}", taskResult.getCompletionStatus());

                    // Log any messages or errors from task attributes
                    Object messagesObj = taskResult.getAttribute("messages");
                    if (messagesObj != null) {
                        logger.info("Task Messages: {}", messagesObj);
                    }

                    // Check for errors
                    if (TaskResult.CompletionStatus.Error.equals(taskResult.getCompletionStatus())) {
                        logger.error("✗ Task completed with errors!");
                        if (taskResult.getAttribute("exception") != null) {
                            logger.error("Exception: {}", taskResult.getAttribute("exception"));
                        }
                    } else if (TaskResult.CompletionStatus.Warning.equals(taskResult.getCompletionStatus())) {
                        logger.warn("⚠ Task completed with warnings");
                    } else {
                        logger.info("✓ Task completed successfully without errors");
                    }

                    // Log attributes
                    Map<String, Object> attributes = taskResult.getAttributes();
                    if (attributes != null && !attributes.isEmpty()) {
                        logger.info("Task Result Attributes:");
                        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                            logger.info("  {}: {}", entry.getKey(), entry.getValue());
                        }
                    }

                } else {
                    logger.error("✗ Task execution returned null result");
                    context.rollbackTransaction();
                }

            } catch (Exception e) {
                logger.error("✗ Error during task execution", e);
                context.rollbackTransaction();
                throw e;
            }

            logger.info("========================================");
            logger.info("Task Execution Completed");
            logger.info("========================================");

            return taskResult;

        } catch (GeneralException e) {
            logger.error("✗ Error executing task: {}", taskName, e);
            return null;
        }
    }

    /**
     * Execute task asynchronously and return immediately.
     * The task will run in the background on the IIQ server.
     *
     * @param taskName the name of the IIQ TaskDefinition
     * @param taskArguments optional map of arguments to pass to the task
     * @return TaskSchedule object if scheduled successfully, null otherwise
     */
    public TaskSchedule scheduleTaskForExecution(String taskName, Map<String, Object> taskArguments) {
        logger.info("Scheduling task for ASYNC execution: {}", taskName);

        try {
            SailPointContext context = remoteContext.getContext();

            // Get the task definition
            TaskDefinition taskDef = context.getObjectByName(TaskDefinition.class, taskName);
            if (taskDef == null) {
                logger.error("TaskDefinition not found: {}", taskName);
                return null;
            }

            logger.info("TaskDefinition found: {}", taskDef.getName());

            // Create task schedule
            TaskSchedule schedule = new TaskSchedule();
            schedule.setName(taskName + "_Schedule_" + System.currentTimeMillis());
            schedule.setTaskDefinition(taskDef);

            // Note: TaskSchedule doesn't directly support set arguments in IIQ 8.5
            // Arguments are typically set through the UI or task execution methods
            logger.info("Created task schedule: {}", schedule.getName());

            // Save schedule
            context.startTransaction();
            context.saveObject(schedule);
            context.commitTransaction();

            logger.info("✓ Task scheduled successfully: {}", schedule.getName());
            logger.info("  Schedule ID: {}", schedule.getId());
            logger.info("  Note: Task will be executed by IIQ's scheduler on the server");

            return schedule;

        } catch (GeneralException e) {
            logger.error("Error scheduling task: {}", taskName, e);
            return null;
        }
    }

    /**
     * Poll for a task result by name.
     * Useful when you've scheduled a task and want to wait for its completion.
     *
     * @param resultName the name of the TaskResult to poll for
     * @param maxWaitSeconds maximum time to wait in seconds
     * @return TaskResult if found and completed, null otherwise
     */
    public TaskResult pollForTaskResult(String resultName, int maxWaitSeconds) {
        logger.info("Polling for TaskResult: {} (max wait: {}s)", resultName, maxWaitSeconds);

        int maxAttempts = maxWaitSeconds / 5;
        int attempt = 0;

        try {
            SailPointContext context = remoteContext.getContext();

            while (attempt < maxAttempts) {
                Thread.sleep(5000); // 5 seconds
                context.decache(); // Refresh context

                TaskResult result = context.getObjectByName(TaskResult.class, resultName);

                if (result != null && result.getCompleted() != null) {
                    logger.info("✓ TaskResult found and completed at attempt {}", attempt + 1);
                    return result;
                }

                if (result != null) {
                    logger.debug("TaskResult found but not completed yet (attempt {})", attempt + 1);
                } else {
                    logger.debug("TaskResult not found yet (attempt {})", attempt + 1);
                }

                attempt++;
            }

            logger.warn("⚠ Polling timed out after {} attempts", maxAttempts);
            return null;

        } catch (GeneralException | InterruptedException e) {
            logger.error("Error polling for task result", e);
            return null;
        }
    }
}