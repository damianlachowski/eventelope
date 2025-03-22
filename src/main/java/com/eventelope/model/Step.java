package com.eventelope.model;

/**
 * Represents a single step in a test case, which includes a name, request details, verification criteria,
 * and optional condition for conditional execution.
 * Also includes retry configuration for handling eventual consistency between services.
 */
public class Step {
    private String name;
    private ApiRequest request;
    private ResponseVerifier verify;
    private String condition;  // Added for conditional execution
    private Integer retries;   // Number of times to retry the step if it fails
    private Long retryInterval; // Time to wait between retries in milliseconds
    private String service;    // Optional service identifier for the step

    public Step() {
        // Default values
        this.retries = 0;
        this.retryInterval = 0L;
    }

    public Step(String name, ApiRequest request, ResponseVerifier verify) {
        this();
        this.name = name;
        this.request = request;
        this.verify = verify;
    }

    public Step(String name, ApiRequest request, ResponseVerifier verify, String condition) {
        this(name, request, verify);
        this.condition = condition;
    }
    
    public Step(String name, ApiRequest request, ResponseVerifier verify, String condition, 
                Integer retries, Long retryInterval, String service) {
        this(name, request, verify, condition);
        if (retries != null) this.retries = retries;
        if (retryInterval != null) this.retryInterval = retryInterval;
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public void setRequest(ApiRequest request) {
        this.request = request;
    }

    public ResponseVerifier getVerify() {
        return verify;
    }

    public void setVerify(ResponseVerifier verify) {
        this.verify = verify;
    }

    /**
     * Get the condition expression that determines if this step should be executed.
     * If null or empty, the step will always execute.
     * 
     * @return The condition expression
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Set the condition expression that determines if this step should be executed.
     * 
     * @param condition The condition expression (e.g., "${status} == 'success'")
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Check if this step has a condition.
     * 
     * @return true if the step has a non-empty condition, false otherwise
     */
    public boolean hasCondition() {
        return condition != null && !condition.trim().isEmpty();
    }

    /**
     * Get the number of times to retry the step if it fails.
     * 
     * @return The number of retries
     */
    public Integer getRetries() {
        return retries;
    }

    /**
     * Set the number of times to retry the step if it fails.
     * 
     * @param retries The number of retries
     */
    public void setRetries(Integer retries) {
        this.retries = retries != null ? retries : 0;
    }

    /**
     * Get the time to wait between retries in milliseconds.
     * 
     * @return The retry interval in milliseconds
     */
    public Long getRetryInterval() {
        return retryInterval;
    }

    /**
     * Set the time to wait between retries in milliseconds.
     * 
     * @param retryInterval The retry interval in milliseconds
     */
    public void setRetryInterval(Long retryInterval) {
        this.retryInterval = retryInterval != null ? retryInterval : 0L;
    }

    /**
     * Get the service identifier for the step.
     * 
     * @return The service identifier
     */
    public String getService() {
        return service;
    }

    /**
     * Set the service identifier for the step.
     * 
     * @param service The service identifier
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Check if this step has retry configuration.
     * 
     * @return true if the step has a non-zero retry count, false otherwise
     */
    public boolean hasRetryConfig() {
        return retries != null && retries > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Step{");
        sb.append("name='").append(name).append('\'');
        
        if (hasCondition()) {
            sb.append(", condition='").append(condition).append('\'');
        }
        
        if (hasRetryConfig()) {
            sb.append(", retries=").append(retries);
            sb.append(", retryInterval=").append(retryInterval).append("ms");
        }
        
        if (service != null && !service.isEmpty()) {
            sb.append(", service='").append(service).append('\'');
        }
        
        sb.append('}');
        return sb.toString();
    }
}