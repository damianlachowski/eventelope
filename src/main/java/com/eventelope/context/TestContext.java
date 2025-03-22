package com.eventelope.context;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages context for a test execution, allowing variables to be stored and
 * retrieved between steps in a test case.
 */
public class TestContext {
    private static final Logger LOGGER = Logger.getLogger(TestContext.class.getName());
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, TestStepVariable> variableTracking = new LinkedHashMap<>();

    /**
     * Stores a variable in the context.
     * 
     * @param name  The name of the variable
     * @param value The value to store
     */
    public void setVariable(String name, Object value) {
        // If it's a TestStepVariable, handle specially for tracking
        if (value instanceof TestStepVariable) {
            TestStepVariable stepVar = (TestStepVariable) value;
            variables.put(name, stepVar.getValue());
            variableTracking.put(name, stepVar);
            LOGGER.fine("Variable set: " + name + " = " + stepVar.getValue() + 
                       " (from step: " + stepVar.getSourceStep() + ")");
        } else {
            variables.put(name, value);
            LOGGER.fine("Variable set: " + name + " = " + value);
        }
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
     * Retrieves the variable with its tracking information if available.
     * 
     * @param name The name of the variable to retrieve
     * @return The TestStepVariable object if it exists, or null
     */
    public TestStepVariable getVariableWithTracking(String name) {
        return variableTracking.get(name);
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
     * Gets all variables with their tracking information.
     * 
     * @return A map of variable names to TestStepVariable objects
     */
    public Map<String, TestStepVariable> getAllVariablesWithTracking() {
        return new LinkedHashMap<>(variableTracking);
    }

    /**
     * Clears all variables from the context.
     */
    public void clear() {
        variables.clear();
        variableTracking.clear();
    }
}