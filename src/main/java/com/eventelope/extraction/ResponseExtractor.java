package com.eventelope.extraction;

import com.eventelope.context.TestContext;
import com.eventelope.context.TestStepVariable;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Extracts data from HTTP responses and stores it in the test context.
 */
public class ResponseExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseExtractor.class);

    /**
     * Extracts values from the response body using the provided extraction definitions
     * and stores them in the test context.
     *
     * @param responseBody    The response body as a string
     * @param extractions     The list of extraction definitions
     * @param testContext     The test context to store extracted values
     * @param currentStepName The name of the current step that's extracting these values
     */
    public void extractAndStoreValues(String responseBody, List<ExtractionDefinition> extractions, 
                                      TestContext testContext, String currentStepName) {
        if (extractions == null || extractions.isEmpty()) {
            return;
        }

        LOGGER.debug("Extracting values from response: {}", responseBody);
        
        for (ExtractionDefinition extraction : extractions) {
            try {
                LOGGER.debug("Extracting from path: {} to variable: {}", extraction.getFrom(), extraction.getStoreTo());
                
                Object extractedValue = JsonPath.read(responseBody, extraction.getFrom());
                
                // Store as a test step variable with tracking information
                TestStepVariable stepVar = new TestStepVariable(
                    extraction.getStoreTo(), 
                    extractedValue, 
                    currentStepName, 
                    extraction.getFrom()
                );
                
                // Set the variable in the test context
                testContext.setVariable(extraction.getStoreTo(), stepVar);
                
                LOGGER.debug("Extracted value: {} and stored to: {} from step: {}", 
                             extractedValue, extraction.getStoreTo(), currentStepName);
            } catch (Exception e) {
                LOGGER.error("Failed to extract value for path: {}", extraction.getFrom(), e);
            }
        }
    }
    
    /**
     * Overloaded method for backward compatibility
     */
    public void extractAndStoreValues(String responseBody, List<ExtractionDefinition> extractions, TestContext testContext) {
        extractAndStoreValues(responseBody, extractions, testContext, "Unknown Step");
    }
}