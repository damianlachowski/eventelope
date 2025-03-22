package com.eventelope.context;

/**
 * Represents a variable in the test context with information about which step created or updated it.
 * Used for enhanced reporting of variable flow between test steps.
 */
public class TestStepVariable {
    private String name;
    private Object value;
    private String sourceStep;
    private String jsonPath;

    public TestStepVariable(String name, Object value, String sourceStep, String jsonPath) {
        this.name = name;
        this.value = value;
        this.sourceStep = sourceStep;
        this.jsonPath = jsonPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSourceStep() {
        return sourceStep;
    }

    public void setSourceStep(String sourceStep) {
        this.sourceStep = sourceStep;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    /**
     * Returns a formatted string representation of this variable
     * for use in reports and logs.
     */
    @Override
    public String toString() {
        return value + " (from step: " + sourceStep + ", path: " + jsonPath + ")";
    }
    
    /**
     * Returns a HTML-formatted representation of this variable
     * for use in the HTML reports.
     */
    public String toHtmlString() {
        String valueStr = (value != null) ? value.toString() : "null";
        
        // If the value is very long, truncate it for display
        if (valueStr.length() > 100) {
            valueStr = valueStr.substring(0, 97) + "...";
        }
        
        return "<strong>" + valueStr + "</strong><br><span style='font-size:0.9em;color:#6c757d;'>" +
               "From step: " + sourceStep + "<br>JSONPath: " + jsonPath + "</span>";
    }
}