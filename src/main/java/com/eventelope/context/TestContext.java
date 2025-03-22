package com.eventelope.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores and manages test execution context data.
 */
public class TestContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestContext.class);
    
    // Match ${variableName} pattern for variable interpolation
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    
    // Store variable values
    private final Map<String, Object> variables = new HashMap<>();
    
    /**
     * Set a variable value in the context.
     *
     * @param name The variable name
     * @param value The variable value
     */
    public void setVariable(String name, String value) {
        if (name != null && value != null) {
            variables.put(name, value);
            LOGGER.debug("Set variable '{}' to value '{}'", name, value);
        }
    }
    
    /**
     * Set a variable value in the context with any object type.
     *
     * @param name The variable name
     * @param value The variable value as any object
     */
    public void setVariableObject(String name, Object value) {
        if (name != null && value != null) {
            variables.put(name, value);
            LOGGER.debug("Set variable '{}' to object value", name);
        }
    }
    
    /**
     * Get a variable value from the context.
     *
     * @param name The variable name
     * @return The variable value, or null if not found
     */
    public String getVariable(String name) {
        Object value = variables.get(name);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Get a variable value as an object from the context.
     *
     * @param name The variable name
     * @return The variable value as Object, or null if not found
     */
    public Object getVariableObject(String name) {
        return variables.get(name);
    }
    
    /**
     * Check if a variable exists in the context.
     *
     * @param name The variable name
     * @return true if the variable exists, false otherwise
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }
    
    /**
     * Get all variables in the context.
     *
     * @return Map of variable names to values
     */
    public Map<String, Object> getAllVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * Interpolate variables in a string.
     * Replaces occurrences of ${variableName} with the corresponding variable value.
     *
     * @param input The input string
     * @return The interpolated string
     */
    public String interpolate(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        if (!input.contains("${")) {
            return input;
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object varObj = variables.get(varName);
            
            if (varObj != null) {
                // Convert object to string and escape special regex characters
                String varValue = Matcher.quoteReplacement(varObj.toString());
                matcher.appendReplacement(result, varValue);
                LOGGER.debug("Replaced variable '{}' with value '{}'", varName, varValue);
            } else {
                // Keep the original placeholder if variable not found
                LOGGER.warn("Variable '{}' not found in context, keeping original placeholder", varName);
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Clear all variables from the context.
     */
    public void clear() {
        variables.clear();
        LOGGER.debug("Cleared all variables from context");
    }
}