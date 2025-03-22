package com.eventelope.extraction;

/**
 * Represents a definition for extracting a value from a response body using
 * JSONPath and storing it in the test context.
 */
public class ExtractionDefinition {
    private String from;
    private String storeTo;

    /**
     * Gets the JSONPath expression to extract from.
     *
     * @return The JSONPath expression
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the JSONPath expression to extract from.
     *
     * @param from The JSONPath expression
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Gets the variable name to store the extracted value to in the test context.
     *
     * @return The variable name
     */
    public String getStoreTo() {
        return storeTo;
    }

    /**
     * Sets the variable name to store the extracted value to in the test context.
     *
     * @param storeTo The variable name
     */
    public void setStoreTo(String storeTo) {
        this.storeTo = storeTo;
    }
}