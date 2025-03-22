package com.eventelope.condition;

import com.eventelope.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates conditional expressions to determine if a step should be executed.
 * Supports simple condition syntax with variable interpolation.
 */
public class ConditionEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionEvaluator.class);
    
    /**
     * Evaluate a condition expression using variables from the test context.
     * Supports the following operators: ==, !=, >, <, >=, <=, contains, startsWith, endsWith
     * 
     * @param condition The condition expression (e.g., "${status} == 'success'")
     * @param context The test context containing variables to substitute
     * @return true if the condition evaluates to true, false otherwise
     */
    public boolean evaluateCondition(String condition, TestContext context) {
        if (condition == null || condition.isEmpty()) {
            return true; // Empty condition is always true
        }
        
        try {
            // First replace all variables
            String interpolatedCondition = interpolateVariables(condition, context);
            LOGGER.debug("Original condition: '{}', Interpolated: '{}'", 
                         condition, interpolatedCondition);
            
            // Try to identify parts of the expression
            if (interpolatedCondition.contains("==")) {
                return evaluateEquals(interpolatedCondition);
            } else if (interpolatedCondition.contains("!=")) {
                return evaluateNotEquals(interpolatedCondition);
            } else if (interpolatedCondition.contains(">=")) {
                return evaluateGreaterThanOrEqual(interpolatedCondition);
            } else if (interpolatedCondition.contains("<=")) {
                return evaluateLessThanOrEqual(interpolatedCondition);
            } else if (interpolatedCondition.contains(">")) {
                return evaluateGreaterThan(interpolatedCondition);
            } else if (interpolatedCondition.contains("<")) {
                return evaluateLessThan(interpolatedCondition);
            } else if (interpolatedCondition.contains(" contains ")) {
                return evaluateContains(interpolatedCondition);
            } else if (interpolatedCondition.contains(" startsWith ")) {
                return evaluateStartsWith(interpolatedCondition);
            } else if (interpolatedCondition.contains(" endsWith ")) {
                return evaluateEndsWith(interpolatedCondition);
            } else {
                // If no operator is found, treat the condition as a boolean value
                return Boolean.parseBoolean(interpolatedCondition.trim());
            }
        } catch (Exception e) {
            LOGGER.error("Error evaluating condition: {}", condition, e);
            return false; // Default to false if evaluation fails
        }
    }
    
    /**
     * Interpolate variables in the condition expression.
     * 
     * @param condition The condition expression
     * @param context The test context
     * @return The condition with variables replaced by their values
     */
    private String interpolateVariables(String condition, TestContext context) {
        String result = condition;
        
        // Find all variables in the format ${varName}
        int startIdx = result.indexOf("${");
        while (startIdx != -1) {
            int endIdx = result.indexOf("}", startIdx);
            if (endIdx == -1) {
                break; // No closing brace found
            }
            
            String varName = result.substring(startIdx + 2, endIdx);
            Object varValueObj = context.getVariable(varName);
            String varValue;
            
            if (varValueObj == null) {
                LOGGER.warn("Variable '{}' not found in context, using empty string", varName);
                varValue = ""; // Use empty string if variable not found
            } else {
                varValue = String.valueOf(varValueObj); // Convert Object to String
            }
            
            result = result.substring(0, startIdx) + varValue + result.substring(endIdx + 1);
            startIdx = result.indexOf("${", startIdx + varValue.length());
        }
        
        return result;
    }
    
    /**
     * Evaluate an equality condition (==).
     * 
     * @param condition The condition to evaluate
     * @return true if the left side equals the right side, false otherwise
     */
    private boolean evaluateEquals(String condition) {
        String[] parts = condition.split("==");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid equals condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // Remove quotes from string literals
        left = removeQuotes(left);
        right = removeQuotes(right);
        
        LOGGER.debug("Evaluating equality: '{}' == '{}'", left, right);
        return left.equals(right);
    }
    
    /**
     * Evaluate a not-equals condition (!=).
     * 
     * @param condition The condition to evaluate
     * @return true if the left side does not equal the right side, false otherwise
     */
    private boolean evaluateNotEquals(String condition) {
        String[] parts = condition.split("!=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid not-equals condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // Remove quotes from string literals
        left = removeQuotes(left);
        right = removeQuotes(right);
        
        LOGGER.debug("Evaluating inequality: '{}' != '{}'", left, right);
        return !left.equals(right);
    }
    
    /**
     * Evaluate a greater-than condition (>).
     * 
     * @param condition The condition to evaluate
     * @return true if the left side is greater than the right side, false otherwise
     */
    private boolean evaluateGreaterThan(String condition) {
        String[] parts = condition.split(">");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid greater-than condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        try {
            double leftValue = Double.parseDouble(left);
            double rightValue = Double.parseDouble(right);
            
            LOGGER.debug("Evaluating greater than: {} > {}", leftValue, rightValue);
            return leftValue > rightValue;
        } catch (NumberFormatException e) {
            LOGGER.error("Cannot compare non-numeric values with > operator: {} and {}", left, right);
            return false;
        }
    }
    
    /**
     * Evaluate a less-than condition (<).
     * 
     * @param condition The condition to evaluate
     * @return true if the left side is less than the right side, false otherwise
     */
    private boolean evaluateLessThan(String condition) {
        String[] parts = condition.split("<");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid less-than condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        try {
            double leftValue = Double.parseDouble(left);
            double rightValue = Double.parseDouble(right);
            
            LOGGER.debug("Evaluating less than: {} < {}", leftValue, rightValue);
            return leftValue < rightValue;
        } catch (NumberFormatException e) {
            LOGGER.error("Cannot compare non-numeric values with < operator: {} and {}", left, right);
            return false;
        }
    }
    
    /**
     * Evaluate a greater-than-or-equal condition (>=).
     * 
     * @param condition The condition to evaluate
     * @return true if the left side is greater than or equal to the right side, false otherwise
     */
    private boolean evaluateGreaterThanOrEqual(String condition) {
        String[] parts = condition.split(">=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid greater-than-or-equal condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        try {
            double leftValue = Double.parseDouble(left);
            double rightValue = Double.parseDouble(right);
            
            LOGGER.debug("Evaluating greater than or equal: {} >= {}", leftValue, rightValue);
            return leftValue >= rightValue;
        } catch (NumberFormatException e) {
            LOGGER.error("Cannot compare non-numeric values with >= operator: {} and {}", left, right);
            return false;
        }
    }
    
    /**
     * Evaluate a less-than-or-equal condition (<=).
     * 
     * @param condition The condition to evaluate
     * @return true if the left side is less than or equal to the right side, false otherwise
     */
    private boolean evaluateLessThanOrEqual(String condition) {
        String[] parts = condition.split("<=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid less-than-or-equal condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        try {
            double leftValue = Double.parseDouble(left);
            double rightValue = Double.parseDouble(right);
            
            LOGGER.debug("Evaluating less than or equal: {} <= {}", leftValue, rightValue);
            return leftValue <= rightValue;
        } catch (NumberFormatException e) {
            LOGGER.error("Cannot compare non-numeric values with <= operator: {} and {}", left, right);
            return false;
        }
    }
    
    /**
     * Evaluate a contains condition.
     * 
     * @param condition The condition to evaluate
     * @return true if the left side contains the right side, false otherwise
     */
    private boolean evaluateContains(String condition) {
        String[] parts = condition.split(" contains ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid contains condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // Remove quotes from string literals
        left = removeQuotes(left);
        right = removeQuotes(right);
        
        LOGGER.debug("Evaluating contains: '{}' contains '{}'", left, right);
        return left.contains(right);
    }
    
    /**
     * Evaluate a startsWith condition.
     * 
     * @param condition The condition to evaluate
     * @return true if the left side starts with the right side, false otherwise
     */
    private boolean evaluateStartsWith(String condition) {
        String[] parts = condition.split(" startsWith ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid startsWith condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // Remove quotes from string literals
        left = removeQuotes(left);
        right = removeQuotes(right);
        
        LOGGER.debug("Evaluating starts with: '{}' startsWith '{}'", left, right);
        return left.startsWith(right);
    }
    
    /**
     * Evaluate an endsWith condition.
     * 
     * @param condition The condition to evaluate
     * @return true if the left side ends with the right side, false otherwise
     */
    private boolean evaluateEndsWith(String condition) {
        String[] parts = condition.split(" endsWith ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid endsWith condition: " + condition);
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // Remove quotes from string literals
        left = removeQuotes(left);
        right = removeQuotes(right);
        
        LOGGER.debug("Evaluating ends with: '{}' endsWith '{}'", left, right);
        return left.endsWith(right);
    }
    
    /**
     * Remove quotes from a string literal.
     * 
     * @param value The string value, possibly with quotes
     * @return The string without surrounding quotes
     */
    private String removeQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("'") && value.endsWith("'")) || 
                (value.startsWith("\"") && value.endsWith("\""))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}