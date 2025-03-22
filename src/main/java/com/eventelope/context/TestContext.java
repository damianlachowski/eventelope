package com.eventelope.context;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages context for a test execution, allowing variables to be stored and
 * retrieved between steps in a test case.
 */
public class TestContext {
    private static final Logger LOGGER = Logger.getLogger(TestContext.class.getName());
    private final Map<String, Object> variables = new HashMap<>();

    /**
     * Stores a variable in the context.
     * 
     * @param name  The name of the variable
     * @param value The value to store
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
        LOGGER.fine("Variable set: " + name + " = " + value);
    }

    /**
     * Retrieves a variable from the context.
     * 
     * @param name The name of the variable to retrieve
     * @return The value of the variable, or null if it doesn't exist
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Checks if a variable exists in the context.
     * 
     * @param name The name of the variable to check
     * @return true if the variable exists, false otherwise
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * Gets all variables in the context.
     * 
     * @return A map of all variables
     */
    public Map<String, Object> getAllVariables() {
        return new HashMap<>(variables);
    }

    /**
     * Clears all variables from the context.
     */
    public void clear() {
        variables.clear();
    }
}