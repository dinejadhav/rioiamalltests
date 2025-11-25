package com.rioiam.iiq.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

/**
 * Provides a mock HTTP session for testing workflows that require session context.
 * This allows programmatic workflow execution that normally requires UI/browser session.
 */
public class MockHttpSessionProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockHttpSessionProvider.class);

    /**
     * Create a mock HTTP session with basic attributes needed for SailPoint workflows.
     *
     * @param sessionId Unique session ID
     * @param identityName Name of the identity associated with this session
     * @return Mock HttpSession object
     */
    public static HttpSession createMockSession(String sessionId, String identityName) {
        logger.debug("Creating mock HTTP session: {} for identity: {}", sessionId, identityName);
        return new MockHttpSession(sessionId, identityName);
    }

    /**
     * Create a mock HTTP session with default session ID.
     *
     * @param identityName Name of the identity associated with this session
     * @return Mock HttpSession object
     */
    public static HttpSession createMockSession(String identityName) {
        String sessionId = "MOCK-SESSION-" + UUID.randomUUID().toString();
        return createMockSession(sessionId, identityName);
    }

    /**
     * Inner class implementing HttpSession interface for mock sessions.
     */
    private static class MockHttpSession implements HttpSession {

        private final String sessionId;
        private final String identityName;
        private final long creationTime;
        private final Map<String, Object> attributes;
        private int maxInactiveInterval = 1800; // 30 minutes default
        private boolean valid = true;

        public MockHttpSession(String sessionId, String identityName) {
            this.sessionId = sessionId;
            this.identityName = identityName;
            this.creationTime = System.currentTimeMillis();
            this.attributes = new HashMap<>();

            // Set SailPoint-specific session attributes
            attributes.put("userIdentity", identityName);
            attributes.put("mockSession", true);

            // Create a workflow session map to store workflow-related data
            // This is what SailPoint's WorkItemService looks for
            Map<String, Object> workflowSession = new HashMap<>();
            workflowSession.put("identity", identityName);
            workflowSession.put("workflowSessionId", sessionId);
            attributes.put("workflowSession", workflowSession);
        }

        @Override
        public long getCreationTime() {
            return creationTime;
        }

        @Override
        public String getId() {
            return sessionId;
        }

        @Override
        public long getLastAccessedTime() {
            return System.currentTimeMillis();
        }

        @Override
        public ServletContext getServletContext() {
            return null; // Not needed for workflow execution
        }

        @Override
        public void setMaxInactiveInterval(int interval) {
            this.maxInactiveInterval = interval;
        }

        @Override
        public int getMaxInactiveInterval() {
            return maxInactiveInterval;
        }

        @Override
        public HttpSessionContext getSessionContext() {
            return null; // Deprecated
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public Object getValue(String name) {
            return getAttribute(name); // Deprecated method
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(attributes.keySet());
        }

        @Override
        public String[] getValueNames() {
            return attributes.keySet().toArray(new String[0]); // Deprecated method
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public void putValue(String name, Object value) {
            setAttribute(name, value); // Deprecated method
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public void removeValue(String name) {
            removeAttribute(name); // Deprecated method
        }

        @Override
        public void invalidate() {
            valid = false;
            attributes.clear();
        }

        @Override
        public boolean isNew() {
            return false;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public String toString() {
            return "MockHttpSession{" +
                    "id='" + sessionId + '\'' +
                    ", identity='" + identityName + '\'' +
                    ", valid=" + valid +
                    '}';
        }
    }
}
