package com.eventelope.parser;

import com.eventelope.core.TestCase;
import com.eventelope.model.ApiRequest;
import com.eventelope.model.ResponseVerifier;
import com.eventelope.model.TestMetadata;
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
 * 
 * Supported payload formats:
 * 1. Block scalar format for inline JSON:
 *    payload: |
 *      {
 *        "key": "value",
 *        "nested": {
 *          "key": "value"
 *        }
 *      }
 * 
 * 2. File reference format:
 *    payload: file:path/to/payload.json
 */
public class YamlParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(YamlParser.class);
    private final Yaml yaml = new Yaml();

    /**
     * Parse a single YAML test file into a TestCase object.
     *
     * @param file The YAML test file
     * @return The parsed TestCase
     */
    public TestCase parseTestCase(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Map<String, Object> testYaml = yaml.load(fileInputStream);
            
            TestCase testCase = new TestCase();
            testCase.setFilePath(file.getAbsolutePath());
            
            // Parse test name and description directly from root level
            testCase.setTestName((String) testYaml.get("testName"));
            testCase.setDescription((String) testYaml.get("description"));
            
            // Parse API request
            Map<String, Object> requestMap = (Map<String, Object>) testYaml.get("request");
            ApiRequest request = new ApiRequest();
            request.setMethod((String) requestMap.get("method"));
            request.setEndpoint((String) requestMap.get("endpoint"));
            
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
                    LOGGER.info("Loading payload from file for test: {}", testCase.getTestName());
                    String filePath = payloadValue.substring(5).trim(); // Remove "file:" prefix
                    
                    // Resolve the file path (handle relative paths)
                    String resolvedPath = resolveFilePath(filePath, file);
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
                    // Use the payload directly from YAML (should be using block scalar format)
                    request.setPayload(payloadValue);
                }
            }
            
            // Parse user if it exists
            if (requestMap.containsKey("user")) {
                request.setUser((String) requestMap.get("user"));
            }
            
            testCase.setRequest(request);
            
            // Parse response verifier
            Map<String, Object> verifierMap = (Map<String, Object>) testYaml.get("verifier");
            ResponseVerifier verifier = new ResponseVerifier();
            
            // Parse expected status code
            if (verifierMap.containsKey("statusCode")) {
                verifier.setStatusCode((Integer) verifierMap.get("statusCode"));
            }
            
            // Parse expected headers if they exist
            if (verifierMap.containsKey("headers")) {
                verifier.setHeaders((Map<String, String>) verifierMap.get("headers"));
            }
            
            // Parse JSONPath assertions if they exist
            if (verifierMap.containsKey("jsonPathAssertions")) {
                List<Map<String, Object>> assertions = (List<Map<String, Object>>) verifierMap.get("jsonPathAssertions");
                verifier.setJsonPathAssertions(assertions);
            }
            
            testCase.setVerifier(verifier);
            
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
                    LOGGER.info("Parsed test case: {}", testCase.getTestName());
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
