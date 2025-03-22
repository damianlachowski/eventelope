package com.eventelope.extraction;

/**
 * Defines a value extraction from an API response.
 */
public class ExtractionDefinition {
    private String variableName;
    private String jsonPath;
    private String defaultValue;
    private String from;     // Source of extraction (e.g., "body", "header")
    private String storeTo;  // Alias for variableName - backward compatibility

    /**
     * Default constructor.
     */
    public ExtractionDefinition() {
        // Default constructor for deserialization
    }

    /**
     * Constructor with variable name and JSONPath.
     *
     * @param variableName The name of the variable to store the extracted value
     * @param jsonPath The JSONPath expression to extract the value
     */
    public ExtractionDefinition(String variableName, String jsonPath) {
        this.variableName = variableName;
        this.jsonPath = jsonPath;
    }

    /**
     * Constructor with variable name, JSONPath, and default value.
     *
     * @param variableName The name of the variable to store the extracted value
     * @param jsonPath The JSONPath expression to extract the value
     * @param defaultValue The default value to use if extraction returns null
     */
    public ExtractionDefinition(String variableName, String jsonPath, String defaultValue) {
        this.variableName = variableName;
        this.jsonPath = jsonPath;
        this.defaultValue = defaultValue;
    }

    /**
     * Get the variable name.
     *
     * @return The variable name
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Set the variable name.
     *
     * @param variableName The variable name
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * Get the JSONPath expression.
     *
     * @return The JSONPath expression
     */
    public String getJsonPath() {
        return jsonPath;
    }

    /**
     * Set the JSONPath expression.
     *
     * @param jsonPath The JSONPath expression
     */
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    /**
     * Get the default value.
     *
     * @return The default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set the default value.
     *
     * @param defaultValue The default value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Get the extraction source.
     *
     * @return The extraction source (e.g., "body", "header")
     */
    public String getFrom() {
        return from;
    }

    /**
     * Set the extraction source.
     *
     * @param from The extraction source (e.g., "body", "header")
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Get the storage variable name (alias for variableName).
     *
     * @return The storage variable name
     */
    public String getStoreTo() {
        return storeTo != null ? storeTo : variableName;
    }

    /**
     * Set the storage variable name (alias for variableName).
     *
     * @param storeTo The storage variable name
     */
    public void setStoreTo(String storeTo) {
        this.storeTo = storeTo;
        if (this.variableName == null) {
            this.variableName = storeTo;
        }
    }

    @Override
    public String toString() {
        return "ExtractionDefinition{" +
                "variableName='" + variableName + '\'' +
                ", jsonPath='" + jsonPath + '\'' +
                (defaultValue != null ? ", defaultValue='" + defaultValue + '\'' : "") +
                (from != null ? ", from='" + from + '\'' : "") +
                (storeTo != null ? ", storeTo='" + storeTo + '\'' : "") +
                '}';
    }
}