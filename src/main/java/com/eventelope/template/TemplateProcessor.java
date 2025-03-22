package com.eventelope.template;

import com.eventelope.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes templates by replacing template variables with their values.
 */
public class TemplateProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProcessor.class);
    
    /**
     * Process a template by replacing all template variables with their values.
     * Template variables can come from:
     * 1. The explicitly provided list of template variables
     * 2. The test context (for variables defined in previous steps)
     * 
     * @param template The template string to process
     * @param templateVariables The list of template variables
     * @param testContext The test context containing other variables
     * @return The processed template with all variables replaced
     */
    public String processTemplate(String template, List<TemplateVariable> templateVariables, TestContext testContext) {
        if (template == null) {
            return null;
        }
        
        String result = template;
        
        // First, replace any ${variable} references from the test context
        if (testContext != null) {
            result = replaceFromContext(result, testContext);
        }
        
        // Then, replace any {{variable}} template references from explicit template variables
        if (templateVariables != null && !templateVariables.isEmpty()) {
            result = replaceTemplateVariables(result, templateVariables, testContext);
        }
        
        return result;
    }
    
    /**
     * Replace variables in the format ${variableName} with their values from the test context.
     * 
     * @param input The input string containing variables
     * @param context The test context containing variable values
     * @return The string with variables replaced
     */
    private String replaceFromContext(String input, TestContext context) {
        // Pattern to match ${variable} syntax
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variableValue = context.getVariable(variableName);
            
            if (variableValue != null) {
                // Replace the variable with its value from context
                matcher.appendReplacement(result, variableValue.toString().replace("$", "\\$"));
                LOGGER.debug("Replaced variable '{}' with value '{}'", variableName, variableValue);
            } else {
                LOGGER.warn("Variable '{}' not found in context, leaving as is", variableName);
                // If variable not found, leave the placeholder as is
                matcher.appendReplacement(result, "\\${" + variableName + "}");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Replace template variables in the format {{variableName}} with their values.
     * Values are obtained from the template variables list or from the test context if not
     * found in the template variables.
     * 
     * @param input The input string containing template variables
     * @param templateVariables The list of template variables
     * @param context The test context containing fallback variable values
     * @return The string with template variables replaced
     */
    private String replaceTemplateVariables(String input, List<TemplateVariable> templateVariables, TestContext context) {
        // Pattern to match {{variable}} syntax for template variables
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(input);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacementValue = findVariableValue(variableName, templateVariables, context);
            
            if (replacementValue != null) {
                // Replace the template variable with its value
                matcher.appendReplacement(result, replacementValue.replace("$", "\\$"));
                LOGGER.debug("Replaced template variable '{}' with value '{}'", variableName, replacementValue);
            } else {
                LOGGER.warn("Template variable '{}' not found, leaving as is", variableName);
                // If variable not found, leave the placeholder as is
                matcher.appendReplacement(result, "\\{\\{" + variableName + "\\}\\}");
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Find the value for a template variable by name.
     * First checks the template variables list, then falls back to the test context.
     * 
     * @param variableName The name of the variable to find
     * @param templateVariables The list of template variables
     * @param context The test context for fallback values
     * @return The variable value or null if not found
     */
    private String findVariableValue(String variableName, List<TemplateVariable> templateVariables, TestContext context) {
        // First, try to find the variable in the template variables list
        for (TemplateVariable var : templateVariables) {
            if (variableName.equals(var.getName())) {
                if (var.getValue() != null) {
                    return var.getValue();
                }
                // If the template variable has no explicit value, try the test context
                break;
            }
        }
        
        // If not found or no value, check the test context
        if (context != null) {
            Object contextValue = context.getVariable(variableName);
            if (contextValue != null) {
                return contextValue.toString();
            }
        }
        
        // Not found in either place
        return null;
    }
}