package com.eventelope.template;

/**
 * Represents a template variable for use in request payloads and other templates.
 * Example: {{variableName}} in a payload template would be replaced with the value.
 */
public class TemplateVariable {
    private String name;
    private String value;
    private String defaultValue;

    /**
     * Default constructor.
     */
    public TemplateVariable() {
        // Default constructor for deserialization
    }

    /**
     * Constructor with name and value.
     *
     * @param name The template variable name
     * @param value The template variable value
     */
    public TemplateVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Constructor with name, value, and default value.
     *
     * @param name The template variable name
     * @param value The template variable value
     * @param defaultValue The default value to use if value is null
     */
    public TemplateVariable(String name, String value, String defaultValue) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    /**
     * Get the template variable name.
     *
     * @return The variable name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the template variable name.
     *
     * @param name The variable name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the template variable value.
     *
     * @return The variable value
     */
    public String getValue() {
        return value != null ? value : defaultValue;
    }

    /**
     * Set the template variable value.
     *
     * @param value The variable value
     */
    public void setValue(String value) {
        this.value = value;
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
     * Get the effective value (the value, or defaultValue if value is null).
     *
     * @return The effective value
     */
    public String getEffectiveValue() {
        return value != null ? value : defaultValue;
    }

    @Override
    public String toString() {
        return "TemplateVariable{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                (defaultValue != null ? ", defaultValue='" + defaultValue + '\'' : "") +
                '}';
    }
}