package com.eventelope.http;

import com.eventelope.auth.AuthenticationHandler;
import com.eventelope.model.ApiRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handles HTTP requests using RestAssured.
 */
public class RestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
    private final AuthenticationHandler authHandler = new AuthenticationHandler();

    // Default base URL for relative paths
    private static final String DEFAULT_BASE_URL = "https://jsonplaceholder.typicode.com";

    /**
     * Execute an HTTP request and return the response.
     *
     * @param request The API request to execute
     * @return The HTTP response
     */
    public Response executeRequest(ApiRequest request) {
        String endpoint = processEndpoint(request.getEndpoint());
        LOGGER.info("Executing {} request to {}", request.getMethod(), endpoint);
        
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
            
            // Add the payload as the request body
            requestSpec.body(request.getPayload());
        }
        
        // Execute the request based on method
        Response response;
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
}
