package com.eventelope.core;

import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;
import com.eventelope.model.Step;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete test case with a structured format including preconditions, setup, execution, and cleanup steps.
 */
public class TestCase {
    private String name;
    private String description;
    private List<Step> preconditions;
    private List<Step> setup;
    private List<Step> execution;
    private List<Step> cleanup;
    private String filePath;

    public TestCase() {
        this.preconditions = new ArrayList<>();
        this.setup = new ArrayList<>();
        this.execution = new ArrayList<>();
        this.cleanup = new ArrayList<>();
    }

    // For backward compatibility during transition
    private ApiRequest request;
    private ResponseVerifier verifier;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Step> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<Step> preconditions) {
        this.preconditions = preconditions;
    }

    public List<Step> getSetup() {
        return setup;
    }

    public void setSetup(List<Step> setup) {
        this.setup = setup;
    }

    public List<Step> getExecution() {
        return execution;
    }

    public void setExecution(List<Step> execution) {
        this.execution = execution;
    }

    public List<Step> getCleanup() {
        return cleanup;
    }

    public void setCleanup(List<Step> cleanup) {
        this.cleanup = cleanup;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // For backward compatibility
    public String getTestName() {
        return name;
    }

    public void setTestName(String testName) {
        this.name = testName;
    }

    // Only used during the transition phase
    public ApiRequest getRequest() {
        return request;
    }

    public void setRequest(ApiRequest request) {
        this.request = request;
    }

    public ResponseVerifier getVerifier() {
        return verifier;
    }

    public void setVerifier(ResponseVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", preconditions=" + (preconditions != null ? preconditions.size() : 0) +
                ", setup=" + (setup != null ? setup.size() : 0) +
                ", execution=" + (execution != null ? execution.size() : 0) +
                ", cleanup=" + (cleanup != null ? cleanup.size() : 0) +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
