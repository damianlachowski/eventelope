package com.eventelope.model;

/**
 * Represents a single step in a test case, which includes a name, request details, and verification criteria.
 */
public class Step {
    private String name;
    private ApiRequest request;
    private ResponseVerifier verify;

    public Step() {
    }

    public Step(String name, ApiRequest request, ResponseVerifier verify) {
        this.name = name;
        this.request = request;
        this.verify = verify;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiRequest getRequest() {
        return request;
    }

    public void setRequest(ApiRequest request) {
        this.request = request;
    }

    public ResponseVerifier getVerify() {
        return verify;
    }

    public void setVerify(ResponseVerifier verify) {
        this.verify = verify;
    }

    @Override
    public String toString() {
        return "Step{" +
                "name='" + name + '\'' +
                '}';
    }
}