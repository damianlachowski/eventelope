package com.eventelope.parser;

import com.eventelope.core.TestCase;
import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;
import com.eventelope.model.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parses YAML test case files into TestCase objects.
 * Updated to support the new structured test format with sections for preconditions, setup, execution, and cleanup.
 */
public class YamlParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlParser.class);
    private final Yaml yaml = new Yaml();

    /**
     * Parse a single YAML test file into a TestCase object with the new structure.
     *
     * @param file The YAML test file
     * @return The parsed TestCase
     */
    public TestCase parseTestCase(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Map<String, Object> yamlContent = yaml.load(fileInputStream);
            
            // The new format has a root "test" object
            Map<String, Object> testMap = (Map<String, Object>) yamlContent.get("test");
            if (testMap == null) {
                LOGGER.error("Invalid test file format. Missing 'test' root element: {}", file.getAbsolutePath());
                throw new RuntimeException("Invalid test file format. Missing 'test' root element.");
            }
            
            TestCase testCase = new TestCase();
            testCase.setFilePath(file.getAbsolutePath());
            
            // Parse basic test information
            testCase.setName((String) testMap.get("name"));
            testCase.setDescription((String) testMap.get("description"));
            
            // Parse preconditions section if it exists
            if (testMap.containsKey("preconditions")) {
                List<Map<String, Object>> preconditionsList = (List<Map<String, Object>>) testMap.get("preconditions");
                if (preconditionsList != null) {
                    List<Step> steps = parseSteps(preconditionsList, file);
                    testCase.setPreconditions(steps);
                }
            }
            
            // Parse setup section if it exists
            if (testMap.containsKey("setup")) {
                List<Map<String, Object>> setupList = (List<Map<String, Object>>) testMap.get("setup");
                if (setupList != null) {
                    List<Step> steps = parseSteps(setupList, file);
                    testCase.setSetup(steps);
                }
            }
            
            // Parse execution section (mandatory)
            List<Step> executionSteps = new ArrayList<>();
            if (testMap.containsKey("execution")) {
                List<Map<String, Object>> executionList = (List<Map<String, Object>>) testMap.get("execution");
                if (executionList != null) {
                    executionSteps = parseSteps(executionList, file);
                }
            } else {
                LOGGER.error("Invalid test file format. Missing 'execution' section: {}", file.getAbsolutePath());
                throw new RuntimeException("Invalid test file format. Missing mandatory 'execution' section.");
            }
            testCase.setExecution(executionSteps);
            
            // Parse cleanup section if it exists
            if (testMap.containsKey("cleanup")) {
                List<Map<String, Object>> cleanupList = (List<Map<String, Object>>) testMap.get("cleanup");
                if (cleanupList != null) {
                    List<Step> steps = parseSteps(cleanupList, file);
                    testCase.setCleanup(steps);
                }
            }
            
            LOGGER.info("Successfully parsed test case: {}", testCase.getName());
            return testCase;
            
        } catch (FileNotFoundException e) {
            LOGGER.error("Test file not found: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed to parse test file: " + file.getAbsolutePath(), e);
        } catch (Exception e) {
            LOGGER.error("Error parsing test file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed to parse test file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Parse steps from a section (preconditions, setup, execution, cleanup)
     * 
     * @param stepsList List of step objects from YAML
     * @param testFile The test file (for resolving relative paths)
     * @return Parsed Step objects
     */
    private List<Step> parseSteps(List<Map<String, Object>> stepsList, File testFile) {
        List<Step> steps = new ArrayList<>();
        
        for (Map<String, Object> stepEntry : stepsList) {
            Map<String, Object> stepMap = (Map<String, Object>) stepEntry.get("step");
            if (stepMap == null) {
                LOGGER.warn("Skipping invalid step entry (missing 'step' key)");
                continue;
            }
            
            Step step = new Step();
            step.setName((String) stepMap.get("name"));
            
            // Parse condition if present
            if (stepMap.containsKey("condition")) {
                String condition = (String) stepMap.get("condition");
                step.setCondition(condition);
                LOGGER.debug("Added condition '{}' to step '{}'", condition, step.getName());
            }
            
            // Parse retry configuration if present
            if (stepMap.containsKey("retries")) {
                Integer retries = ((Number) stepMap.get("retries")).intValue();
                step.setRetries(retries);
                LOGGER.debug("Added {} retries to step '{}'", retries, step.getName());
            }
            
            // Parse retry interval if present
            if (stepMap.containsKey("retryInterval")) {
                Long retryInterval = ((Number) stepMap.get("retryInterval")).longValue();
                step.setRetryInterval(retryInterval);
                LOGGER.debug("Added retry interval of {}ms to step '{}'", retryInterval, step.getName());
            }
            
            // Parse service if present
            if (stepMap.containsKey("service")) {
                String service = (String) stepMap.get("service");
                step.setService(service);
                LOGGER.debug("Added service '{}' to step '{}'", service, step.getName());
            }
            
            // Parse request
            if (stepMap.containsKey("request")) {
                Map<String, Object> requestMap = (Map<String, Object>) stepMap.get("request");
                ApiRequest request = parseRequest(requestMap, testFile);
                step.setRequest(request);
            }
            
            // Parse verification criteria (status, assertions, etc.)
            if (stepMap.containsKey("verify")) {
                Map<String, Object> verifyMap = (Map<String, Object>) stepMap.get("verify");
                ResponseVerifier verifier = parseVerifier(verifyMap);
                step.setVerify(verifier);
            }
            
            steps.add(step);
        }
        
        return steps;
    }

    /**
     * Parse an API request section from a step
     * 
     * @param requestMap Request YAML map
     * @param testFile The test file (for resolving relative paths)
     * @return Parsed ApiRequest
     */
    private ApiRequest parseRequest(Map<String, Object> requestMap, File testFile) {
        ApiRequest request = new ApiRequest();
        
        // Method is mandatory
        request.setMethod((String) requestMap.get("method"));
        
        // Support both endpoint and url fields for flexibility
        if (requestMap.containsKey("endpoint")) {
            request.setEndpoint((String) requestMap.get("endpoint"));
        } else if (requestMap.containsKey("url")) {
            request.setEndpoint((String) requestMap.get("url"));
        }
        
        // Parse headers if they exist
        if (requestMap.containsKey("headers")) {
            Map<String, String> headers = (Map<String, String>) requestMap.get("headers");
            request.setHeaders(headers);
        }
        
        // Parse payload if it exists
        if (requestMap.containsKey("payload")) {
            String payloadValue = (String) requestMap.get("payload");
            
            // Check if payload should be loaded from file
            if (payloadValue.trim().startsWith("file:")) {
                LOGGER.debug("Loading payload from file");
                String filePath = payloadValue.substring(5).trim(); // Remove "file:" prefix
                
                // Resolve the file path (handle relative paths)
                String resolvedPath = resolveFilePath(filePath, testFile);
                LOGGER.debug("Resolved payload file path: {}", resolvedPath);
                
                try {
                    String fileContent = readPayloadFromFile(resolvedPath);
                    request.setPayload(fileContent);
                    LOGGER.debug("Successfully loaded payload from file: {}", resolvedPath);
                } catch (IOException e) {
                    LOGGER.error("Failed to load payload from file: {}", resolvedPath, e);
                    throw new RuntimeException("Failed to load payload from file: " + resolvedPath + 
                                             " - Error: " + e.getMessage(), e);
                }
            } else {
                // Inline payload
                request.setPayload(payloadValue);
            }
        }
        
        // Parse authentication/user if it exists
        if (requestMap.containsKey("user")) {
            request.setUser((String) requestMap.get("user"));
        }
        
        // Parse timeout settings if they exist
        if (requestMap.containsKey("timeout")) {
            Object timeoutValue = requestMap.get("timeout");
            if (timeoutValue instanceof Integer) {
                request.setTimeout((Integer) timeoutValue);
                LOGGER.debug("Set request timeout to {}ms", request.getTimeout());
            } else if (timeoutValue instanceof String) {
                try {
                    request.setTimeout(Integer.parseInt((String) timeoutValue));
                    LOGGER.debug("Set request timeout to {}ms", request.getTimeout());
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid timeout value: {}, using default", timeoutValue);
                }
            }
        }
        
        // Parse connection timeout settings if they exist
        if (requestMap.containsKey("connectionTimeout")) {
            Object timeoutValue = requestMap.get("connectionTimeout");
            if (timeoutValue instanceof Integer) {
                request.setConnectionTimeout((Integer) timeoutValue);
                LOGGER.debug("Set connection timeout to {}ms", request.getConnectionTimeout());
            } else if (timeoutValue instanceof String) {
                try {
                    request.setConnectionTimeout(Integer.parseInt((String) timeoutValue));
                    LOGGER.debug("Set connection timeout to {}ms", request.getConnectionTimeout());
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid connection timeout value: {}, using default", timeoutValue);
                }
            }
        }
        
        // Parse template variables if they exist
        if (requestMap.containsKey("templateVariables")) {
            List<Map<String, String>> templateVarsList = (List<Map<String, String>>) requestMap.get("templateVariables");
            if (templateVarsList != null) {
                for (Map<String, String> varMap : templateVarsList) {
                    if (varMap.containsKey("name") && varMap.containsKey("value")) {
                        String name = varMap.get("name");
                        String value = varMap.get("value");
                        com.eventelope.template.TemplateVariable templateVar = 
                            new com.eventelope.template.TemplateVariable(name, value);
                        request.addTemplateVariable(templateVar);
                        LOGGER.debug("Added template variable '{}' with value '{}'", name, value);
                    } else {
                        LOGGER.warn("Invalid template variable definition (missing name or value): {}", varMap);
                    }
                }
            }
        }
        
        return request;
    }

    /**
     * Parse a response verifier section from a step
     * 
     * @param verifyMap Verify YAML map
     * @return Parsed ResponseVerifier
     */
    private ResponseVerifier parseVerifier(Map<String, Object> verifyMap) {
        ResponseVerifier verifier = new ResponseVerifier();
        
        // Parse expected status code (might be 'status' or 'statusCode' for flexibility)
        if (verifyMap.containsKey("statusCode")) {
            verifier.setStatusCode((Integer) verifyMap.get("statusCode"));
        } else if (verifyMap.containsKey("status")) {
            verifier.setStatusCode((Integer) verifyMap.get("status"));
        }
        
        // Parse expected headers if they exist
        if (verifyMap.containsKey("headers")) {
            verifier.setHeaders((Map<String, String>) verifyMap.get("headers"));
        }
        
        // Parse JSONPath assertions if they exist
        if (verifyMap.containsKey("jsonPathAssertions")) {
            List<Map<String, Object>> assertions = (List<Map<String, Object>>) verifyMap.get("jsonPathAssertions");
            verifier.setJsonPathAssertions(assertions);
        }
        
        // Parse extractions if they exist
        if (verifyMap.containsKey("extractions")) {
            List<Map<String, String>> extractionMaps = (List<Map<String, String>>) verifyMap.get("extractions");
            List<com.eventelope.extraction.ExtractionDefinition> extractions = new ArrayList<>();
            
            for (Map<String, String> extractionMap : extractionMaps) {
                com.eventelope.extraction.ExtractionDefinition extraction = new com.eventelope.extraction.ExtractionDefinition();
                extraction.setFrom(extractionMap.get("from"));
                extraction.setStoreTo(extractionMap.get("storeTo"));
                extractions.add(extraction);
                
                LOGGER.debug("Added extraction from '{}' to variable '{}'", 
                    extraction.getFrom(), extraction.getStoreTo());
            }
            
            verifier.setExtractions(extractions);
        }
        
        return verifier;
    }

    /**
     * Parse all YAML test files in a directory.
     *
     * @param directory The directory containing YAML test files
     * @return List of parsed TestCase objects
     */
    public List<TestCase> parseTestCases(String directory) {
        List<TestCase> testCases = new ArrayList<>();
        Path dir = Paths.get(directory);
        
        try {
            List<Path> yamlFiles = Files.walk(dir)
                    .filter(path -> path.toString().endsWith(".yaml") || path.toString().endsWith(".yml"))
                    .collect(Collectors.toList());
            
            for (Path path : yamlFiles) {
                File file = path.toFile();
                try {
                    TestCase testCase = parseTestCase(file);
                    testCases.add(testCase);
                    LOGGER.info("Parsed test case: {}", testCase.getName());
                } catch (Exception e) {
                    LOGGER.error("Error parsing test file: {}", file.getAbsolutePath(), e);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error accessing test directory: {}", directory, e);
            throw new RuntimeException("Failed to access test directory: " + directory, e);
        }
        
        return testCases;
    }

    /**
     * Read payload content from a JSON file.
     * Performs basic validation to ensure the file contains valid JSON.
     *
     * @param filePath Path to the JSON file
     * @return The file content as a string
     * @throws IOException If file cannot be read or doesn't contain valid JSON
     */
    private String readPayloadFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Payload file not found: " + filePath);
        }
        
        String content = new String(Files.readAllBytes(path));
        
        // Basic JSON validation - this will throw an exception if the content is not valid JSON
        try {
            // Use Jackson or another JSON library if available in the classpath
            // For now, using a simple check that might not catch all invalid JSON cases
            if (content.trim().isEmpty()) {
                throw new IOException("Payload file is empty: " + filePath);
            }
            
            char firstChar = content.trim().charAt(0);
            char lastChar = content.trim().charAt(content.trim().length() - 1);
            
            // Check if the JSON is an object or array
            if (!((firstChar == '{' && lastChar == '}') || (firstChar == '[' && lastChar == ']'))) {
                throw new IOException("Payload file does not contain valid JSON: " + filePath);
            }
        } catch (Exception e) {
            throw new IOException("Failed to validate JSON payload from file: " + filePath, e);
        }
        
        return content;
    }
    
    /**
     * Resolves the file path, handling both absolute and relative paths.
     * Supports multiple path resolution strategies:
     * 1. Absolute paths (starting with /)
     * 2. Project-relative paths (starting with src/)
     * 3. Test-directory-relative paths (starting with ./ or ../)
     * 4. Simple filenames (resolved relative to test file directory)
     * 
     * @param filePath The original file path (absolute or relative)
     * @param testFile The test file from which the path is referenced
     * @return The resolved absolute file path
     */
    private String resolveFilePath(String filePath, File testFile) {
        // Handle null or empty paths
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        // Normalize the file path by replacing backslashes with forward slashes
        filePath = filePath.replace('\\', '/');
        
        // If already absolute, return as is
        if (new File(filePath).isAbsolute()) {
            LOGGER.debug("Using absolute path: {}", filePath);
            return filePath;
        }
        
        // If starting with "src/", treat as relative to project root
        if (filePath.startsWith("src/")) {
            LOGGER.debug("Using project-relative path: {}", filePath);
            return filePath;
        }
        
        // If starting with "~/", resolve relative to user home directory
        if (filePath.startsWith("~/")) {
            String userHome = System.getProperty("user.home");
            String resolvedPath = userHome + filePath.substring(1);
            LOGGER.debug("Resolved home-relative path '{}' to '{}'", filePath, resolvedPath);
            return resolvedPath;
        }
        
        // Otherwise, resolve relative to the test file's directory
        File testDir = testFile.getParentFile();
        String resolvedPath = new File(testDir, filePath).getAbsolutePath();
        LOGGER.debug("Resolved test-relative path '{}' to '{}'", filePath, resolvedPath);
        return resolvedPath;
    }
}
