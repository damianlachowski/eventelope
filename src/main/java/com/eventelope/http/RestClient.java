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

    /**
     * Execute an HTTP request and return the response.
     *
     * @param request The API request to execute
     * @return The HTTP response
     */
    public Response executeRequest(ApiRequest request) {
        LOGGER.info("Executing {} request to {}", request.getMethod(), request.getEndpoint());
        
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
        
        // Add body for POST, PUT, PATCH methods
        if (request.getBody() != null && 
            (request.getMethod().equals("POST") || 
             request.getMethod().equals("PUT") || 
             request.getMethod().equals("PATCH"))) {
            
            // If Content-Type is not set, default to application/json
            if (!request.getHeaders().containsKey("Content-Type") && 
                !request.getHeaders().containsKey("content-type")) {
                requestSpec.header("Content-Type", "application/json");
            }
            
            // Add the body
            requestSpec.body(request.getBody());
        }
        
        // Execute the request based on method
        Response response;
        switch (request.getMethod()) {
            case "GET":
                response = requestSpec.get(request.getEndpoint());
                break;
            case "POST":
                response = requestSpec.post(request.getEndpoint());
                break;
            case "PUT":
                response = requestSpec.put(request.getEndpoint());
                break;
            case "DELETE":
                response = requestSpec.delete(request.getEndpoint());
                break;
            case "PATCH":
                response = requestSpec.patch(request.getEndpoint());
                break;
            case "HEAD":
                response = requestSpec.head(request.getEndpoint());
                break;
            case "OPTIONS":
                response = requestSpec.options(request.getEndpoint());
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
        }
        
        // Log response details
        response.then().log().all();
        
        return response;
    }
}
