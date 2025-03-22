package com.eventelope.extraction;

import com.eventelope.context.TestContext;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Extracts values from API responses and stores them in the test context.
 */
public class ResponseExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseExtractor.class);

    /**
     * Extract values from a response according to extraction definitions and store them in the context.
     *
     * @param response The API response
     * @param extractions The extraction definitions
     * @param context The test context to store values in
     */
    public void extractValues(Response response, List<ExtractionDefinition> extractions, TestContext context) {
        if (extractions == null || extractions.isEmpty()) {
            return;
        }

        String responseBody = response.getBody().asString();
        JsonPath jsonPath = JsonPath.from(responseBody);

        for (ExtractionDefinition extraction : extractions) {
            try {
                extractSingleValue(jsonPath, extraction, context);
            } catch (Exception e) {
                LOGGER.warn("Failed to extract value for variable '{}' with JSONPath '{}': {}",
                        extraction.getVariableName(), extraction.getJsonPath(), e.getMessage());
                
                // Use default value if provided and extraction failed
                if (extraction.getDefaultValue() != null) {
                    LOGGER.info("Using default value '{}' for variable '{}'",
                            extraction.getDefaultValue(), extraction.getVariableName());
                    context.setVariable(extraction.getVariableName(), extraction.getDefaultValue());
                }
            }
        }
    }

    /**
     * Extract values from a response body string and store them in the context.
     * This is an alternative to extractValues that takes a string response body directly.
     *
     * @param responseBody The response body as a string
     * @param extractions The extraction definitions
     * @param context The test context to store values in
     */
    public void extractAndStoreValues(String responseBody, List<ExtractionDefinition> extractions, TestContext context) {
        if (extractions == null || extractions.isEmpty() || responseBody == null || responseBody.isEmpty()) {
            return;
        }

        JsonPath jsonPath = JsonPath.from(responseBody);

        for (ExtractionDefinition extraction : extractions) {
            try {
                extractSingleValue(jsonPath, extraction, context);
            } catch (Exception e) {
                LOGGER.warn("Failed to extract value for variable '{}' with JSONPath '{}': {}",
                        extraction.getVariableName(), extraction.getJsonPath(), e.getMessage());
                
                // Use default value if provided and extraction failed
                if (extraction.getDefaultValue() != null) {
                    LOGGER.info("Using default value '{}' for variable '{}'",
                            extraction.getDefaultValue(), extraction.getVariableName());
                    context.setVariable(extraction.getVariableName(), extraction.getDefaultValue());
                }
            }
        }
    }

    /**
     * Extract a single value according to an extraction definition.
     *
     * @param jsonPath The JsonPath for the response
     * @param extraction The extraction definition
     * @param context The test context to store the value in
     */
    private void extractSingleValue(JsonPath jsonPath, ExtractionDefinition extraction, TestContext context) {
        if (extraction.getVariableName() == null || extraction.getJsonPath() == null) {
            LOGGER.warn("Skipping invalid extraction with missing variable name or JSONPath");
            return;
        }

        LOGGER.debug("Extracting value for variable '{}' with JSONPath '{}'",
                extraction.getVariableName(), extraction.getJsonPath());

        Object extractedValue = jsonPath.get(extraction.getJsonPath());
        
        if (extractedValue == null) {
            if (extraction.getDefaultValue() != null) {
                LOGGER.info("JSONPath '{}' returned null, using default value '{}' for variable '{}'",
                        extraction.getJsonPath(), extraction.getDefaultValue(), extraction.getVariableName());
                context.setVariable(extraction.getVariableName(), extraction.getDefaultValue());
            } else {
                LOGGER.warn("JSONPath '{}' returned null for variable '{}' and no default value provided",
                        extraction.getJsonPath(), extraction.getVariableName());
            }
            return;
        }

        // Store the extracted value in the context
        String stringValue = extractedValue.toString();
        context.setVariable(extraction.getVariableName(), stringValue);
        LOGGER.info("Extracted value '{}' for variable '{}'", stringValue, extraction.getVariableName());
    }
}