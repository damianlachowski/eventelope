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
            
            // Parse test metadata
            Map<String, Object> metadataMap = (Map<String, Object>) testYaml.get("metadata");
            TestMetadata metadata = new TestMetadata();
            metadata.setTestName((String) metadataMap.get("testName"));
            metadata.setDescription((String) metadataMap.get("description"));
            testCase.setMetadata(metadata);
            
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
            
            // Parse body if it exists
            if (requestMap.containsKey("body")) {
                Object body = requestMap.get("body");
                // Handle file reference for body
                if (body instanceof String && ((String) body).startsWith("file:")) {
                    String filePath = ((String) body).substring(5);
                    String fileContent = readBodyFromFile(filePath);
                    request.setBody(fileContent);
                } else {
                    request.setBody(body);
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
                    LOGGER.info("Parsed test case: {}", testCase.getMetadata().getTestName());
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
     * Read body content from a JSON file.
     *
     * @param filePath Path to the JSON file
     * @return The file content as a string
     */
    private String readBodyFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }
}
