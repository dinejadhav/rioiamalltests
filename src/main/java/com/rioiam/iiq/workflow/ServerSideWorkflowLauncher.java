package com.rioiam.iiq.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sailpoint.api.RequestManager;
import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.Request;
import sailpoint.object.RequestDefinition;
import sailpoint.tools.GeneralException;
import com.rioiam.iiq.context.IIQRemoteContext;

import java.util.Date;
import java.util.Map;

/**
 * Launches workflows on the IIQ server side using RequestManager.
 * This ensures WorkItems are created properly within the server context.
 */
@Component
public class ServerSideWorkflowLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ServerSideWorkflowLauncher.class);

    @Autowired
    private IIQRemoteContext remoteContext;

    /**
     * Schedule a workflow to launch on the IIQ server.
     * This creates a Request that the IIQ server will process, ensuring proper WorkItem creation.
     *
     * @param workflowName Name of the workflow to launch
     * @param launcher Identity name who launches the workflow
     * @param variables Input variables for the workflow
     * @return Request ID if successful, null if failed
     */
    public String launchWorkflowOnServer(String workflowName, String launcher, Map<String, Object> variables) {
        logger.info("========================================");
        logger.info("Scheduling workflow launch on IIQ server");
        logger.info("Workflow: {}", workflowName);
        logger.info("Launcher: {}", launcher);
        logger.info("========================================");

        SailPointContext context = remoteContext.getContext();

        try {
            // Get or create the "Workflow Request" RequestDefinition
            RequestDefinition reqDef = context.getObjectByName(RequestDefinition.class, "Workflow Request");

            if (reqDef == null) {
                logger.warn("'Workflow Request' RequestDefinition not found, creating a new one");
                reqDef = new RequestDefinition();
                reqDef.setName("Workflow Request");
                reqDef.setExecutor("sailpoint.request.WorkflowRequestExecutor");
                context.saveObject(reqDef);
                context.commitTransaction();
            }

            // Set workflow-specific attributes
            Attributes<String, Object> reqArgs = new Attributes<>();
            reqArgs.put("workflow", workflowName);
            reqArgs.put("launcher", launcher);

            // Add all workflow variables
            if (variables != null && !variables.isEmpty()) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    reqArgs.put(entry.getKey(), entry.getValue());
                }
            }

            // Create the Request object
            Request request = new Request();
            request.setDefinition(reqDef);
            request.setAttributes(reqDef, reqArgs);
            request.setName("Launch-" + workflowName + "-" + System.currentTimeMillis());

            // Add the request using RequestManager
            RequestManager.addRequest(context, request);

            logger.info("✓ Workflow launch request queued successfully");
            logger.info("  Request ID: {}", request.getId());
            logger.info("  Request Name: {}", request.getName());
            logger.info("  The IIQ server will process this request and create WorkItems");

            return request.getId();

        } catch (GeneralException e) {
            logger.error("✗ Error queuing workflow launch request", e);
            return null;
        }
    }
}
