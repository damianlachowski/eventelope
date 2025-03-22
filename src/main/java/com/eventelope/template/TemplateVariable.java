package com.eventelope.template;

/**
 * Represents a variable that can be used for template substitution in payload files.
 */
public class TemplateVariable {
    private String name;
    private String value;
    
    /**
     * Gets the name of the template variable.
     * 
     * @return The variable name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the template variable.
     * 
     * @param name The variable name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the default value of the template variable.
     * 
     * @return The default value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Sets the default value of the template variable.
     * 
     * @param value The default value
     */
    public void setValue(String value) {
        this.value = value;
    }
}