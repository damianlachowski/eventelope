package com.eventelope.template;

import com.eventelope.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes templates with variable substitution.
 */
public class TemplateProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProcessor.class);
    
    // Match {{variableName}} pattern for template variable interpolation
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");
    
    /**
     * Process a template string, substituting template variables.
     *
     * @param template The template string
     * @param variables The template variables
     * @param context The test context for additional variable interpolation
     * @return The processed template
     */
    public String processTemplate(String template, List<TemplateVariable> variables, TestContext context) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        // First, interpolate test context variables (${varName})
        if (context != null) {
            template = context.interpolate(template);
        }
        
        // If no template variables to process, return early
        if (variables == null || variables.isEmpty() || !template.contains("{{")) {
            return template;
        }
        
        // Then process template variables ({{varName}})
        StringBuffer result = new StringBuffer();
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String varValue = findTemplateVariableValue(varName, variables);
            
            if (varValue != null) {
                // Escape special regex characters in replacement string
                varValue = Matcher.quoteReplacement(varValue);
                matcher.appendReplacement(result, varValue);
                LOGGER.debug("Replaced template variable '{}' with value '{}'", varName, varValue);
            } else {
                // Keep the original placeholder if variable not found
                LOGGER.warn("Template variable '{}' not found, keeping original placeholder", varName);
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Load a template from a file, with support for relative and absolute paths.
     *
     * @param templateFilePath The path to the template file
     * @return The template content
     * @throws IOException If the file cannot be read
     */
    public String loadTemplateFromFile(String templateFilePath) throws IOException {
        if (templateFilePath == null || templateFilePath.isEmpty()) {
            throw new IllegalArgumentException("Template file path cannot be null or empty");
        }
        
        // Handle file: prefix
        if (templateFilePath.startsWith("file:")) {
            templateFilePath = templateFilePath.substring(5);
        }
        
        // Resolve the file path
        File templateFile = new File(templateFilePath);
        
        if (!templateFile.exists()) {
            // Try treating as project-relative path
            templateFile = new File(System.getProperty("user.dir"), templateFilePath);
            
            if (!templateFile.exists()) {
                throw new IOException("Template file not found: " + templateFilePath);
            }
        }
        
        // Read the file content
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }
        
        LOGGER.debug("Loaded template from file: {}", templateFile.getAbsolutePath());
        return content.toString();
    }
    
    /**
     * Find a template variable value by name.
     *
     * @param name The variable name
     * @param variables The list of template variables
     * @return The variable value, or null if not found
     */
    private String findTemplateVariableValue(String name, List<TemplateVariable> variables) {
        if (name == null || variables == null) {
            return null;
        }
        
        for (TemplateVariable var : variables) {
            if (name.equals(var.getName())) {
                return var.getEffectiveValue();
            }
        }
        
        return null;
    }
}