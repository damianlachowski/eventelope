package com.eventelope.model;

/**
 * Contains metadata about a test case such as name and description.
 */
public class TestMetadata {
    private String testName;
    private String description;

    public TestMetadata() {
    }

    public TestMetadata(String testName, String description) {
        this.testName = testName;
        this.description = description;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TestMetadata{" +
                "testName='" + testName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
