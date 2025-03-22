package com.eventelope.http;

import com.eventelope.auth.AuthenticationHandler;
import com.eventelope.context.TestContext;
import com.eventelope.model.ApiRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles HTTP requests using RestAssured.
 */
public class RestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
    private final AuthenticationHandler authHandler = new AuthenticationHandler();

    // Default base URL for relative paths
    private static final String DEFAULT_BASE_URL = "https://jsonplaceholder.typicode.com";
    
    // Default timeout values in milliseconds (not currently used)
    private static final int DEFAULT_TIMEOUT = 30000;

    /**
     * Execute an HTTP request and return the response.
     *
     * @param request The API request to execute
     * @return The HTTP response
     */
    public Response executeRequest(ApiRequest request) {
        return executeRequest(request, null);
    }
    
    /**
     * Execute an HTTP request with variable substitution and return the response.
     *
     * @param request The API request to execute
     * @param context The test context containing variables to substitute
     * @return The HTTP response
     */
    public Response executeRequest(ApiRequest request, TestContext context) {
        // Process endpoint with variable substitution if context is provided
        String endpoint = request.getEndpoint();
        if (context != null && endpoint != null) {
            endpoint = replaceVariables(endpoint, context);
        }
        endpoint = processEndpoint(endpoint);
        
        // Log the timeout settings but don't apply them yet
        int connectionTimeout = request.getConnectionTimeout();
        int timeout = request.getTimeout();
        
        if (connectionTimeout > 0 || timeout > 0) {
            LOGGER.info("Executing {} request to {} with timeout settings: connection={}ms, socket={}ms", 
                request.getMethod(), endpoint, 
                connectionTimeout > 0 ? connectionTimeout : "default", 
                timeout > 0 ? timeout : "default");
        } else {
            LOGGER.info("Executing {} request to {}", request.getMethod(), endpoint);
        }
        
        RequestSpecification requestSpec = RestAssured.given()
                .log().all();  // Log all request details
        
        // Add headers
        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                requestSpec.header(header.getKey(), header.getValue());
            }
        }
        
        // Add authentication if user is specified
        if (request.getUser() != null) {
            authHandler.applyAuthentication(requestSpec, request.getUser());
        }
        
        // Add payload for POST, PUT, PATCH methods
        if (request.getPayload() != null && 
            (request.getMethod().equals("POST") || 
             request.getMethod().equals("PUT") || 
             request.getMethod().equals("PATCH"))) {
            
            // If Content-Type is not set, default to application/json
            if (!request.getHeaders().containsKey("Content-Type") && 
                !request.getHeaders().containsKey("content-type")) {
                requestSpec.header("Content-Type", "application/json");
            }
            
            // Replace variables in payload if context is provided
            String payload = request.getPayload();
            if (context != null) {
                payload = replaceVariables(payload, context);
                LOGGER.debug("Payload after variable replacement: {}", payload);
            }
            
            // Add the payload as the request body
            requestSpec.body(payload);
        }
        
        // Execute the request based on method
        Response response;
        try {
            switch (request.getMethod()) {
                case "GET":
                    response = requestSpec.get(endpoint);
                    break;
                case "POST":
                    response = requestSpec.post(endpoint);
                    break;
                case "PUT":
                    response = requestSpec.put(endpoint);
                    break;
                case "DELETE":
                    response = requestSpec.delete(endpoint);
                    break;
                case "PATCH":
                    response = requestSpec.patch(endpoint);
                    break;
                case "HEAD":
                    response = requestSpec.head(endpoint);
                    break;
                case "OPTIONS":
                    response = requestSpec.options(endpoint);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
            }
            
            // Log response details
            response.then().log().all();
            
            return response;
        } catch (Exception e) {
            LOGGER.error("Request failed: {}", e.getMessage());
            if (e.getMessage().contains("timeout") || e.getMessage().contains("timed out")) {
                LOGGER.error("Request timed out: {}", e.getMessage());
                throw new RuntimeException("Request timed out: " + e.getMessage(), e);
            }
            throw e;
        }
    }
    
    /**
     * Process the endpoint URL to handle both absolute and relative paths.
     * For relative paths (starting with /), prepend the default base URL.
     *
     * @param endpoint The endpoint URL from the request
     * @return The processed endpoint URL
     */
    private String processEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("Endpoint cannot be null or empty");
        }
        
        // If it's already a full URL (starts with http:// or https://), return as is
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        
        // If it's a relative path (starts with /), append to default base URL
        if (endpoint.startsWith("/")) {
            return DEFAULT_BASE_URL + endpoint;
        }
        
        // If it doesn't start with /, add a / and append to default base URL
        return DEFAULT_BASE_URL + "/" + endpoint;
    }
    
    /**
     * Replace variables in the format ${variableName} with their values from the context.
     * 
     * @param input String containing variables to replace
     * @param context TestContext containing variable values
     * @return String with variables replaced by their values
     */
    private String replaceVariables(String input, TestContext context) {
        if (input == null || context == null) {
            return input;
        }
        
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
}
