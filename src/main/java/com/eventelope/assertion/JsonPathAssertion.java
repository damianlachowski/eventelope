package com.eventelope.assertion;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Handles JSONPath assertions on response bodies with enhanced comparison operations.
 */
public class JsonPathAssertion {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPathAssertion.class);

    // Assertion types
    public static final String EQUALS = "equals";
    public static final String CONTAINS = "contains";
    public static final String STARTS_WITH = "startsWith";
    public static final String ENDS_WITH = "endsWith";
    public static final String MATCHES = "matches";
    public static final String GREATER_THAN = "greaterThan";
    public static final String LESS_THAN = "lessThan";
    public static final String EXISTS = "exists";
    public static final String NOT_EXISTS = "notExists";
    public static final String IS_NULL = "isNull";
    public static final String IS_NOT_NULL = "isNotNull";
    public static final String IS_EMPTY = "isEmpty";
    public static final String IS_NOT_EMPTY = "isNotEmpty";

    /**
     * Assert that a JSONPath expression in a response body matches an expected value.
     *
     * @param responseBody The JSON response body as a string
     * @param jsonPath The JSONPath expression
     * @param expectedValue The expected value
     * @return null if the assertion passes, otherwise an error message
     */
    public String assertJsonPath(String responseBody, String jsonPath, Object expectedValue) {
        return assertJsonPath(responseBody, jsonPath, expectedValue, EQUALS);
    }

    /**
     * Assert that a JSONPath expression in a response body satisfies a condition with the expected value.
     *
     * @param responseBody The JSON response body as a string
     * @param jsonPath The JSONPath expression
     * @param expectedValue The expected value
     * @param assertionType The type of assertion to perform (equals, contains, etc.)
     * @return null if the assertion passes, otherwise an error message
     */
    public String assertJsonPath(String responseBody, String jsonPath, Object expectedValue, String assertionType) {
        try {
            // Special handling for existence assertions
            if (NOT_EXISTS.equals(assertionType)) {
                try {
                    JsonPath.read(responseBody, jsonPath);
                    return String.format("JSONPath should not exist but was found: '%s'", jsonPath);
                } catch (PathNotFoundException e) {
                    return null; // Success - path doesn't exist
                }
            }
            
            Object actualValue;
            try {
                actualValue = JsonPath.read(responseBody, jsonPath);
            } catch (PathNotFoundException e) {
                if (EXISTS.equals(assertionType)) {
                    return String.format("JSONPath does not exist: '%s'", jsonPath);
                }
                throw e;
            }
            
            // Special case for null checks
            if (IS_NULL.equals(assertionType)) {
                return actualValue == null ? null : 
                    String.format("Expected null for path '%s' but got: '%s'", jsonPath, actualValue);
            }
            
            if (IS_NOT_NULL.equals(assertionType)) {
                return actualValue != null ? null : 
                    String.format("Expected non-null value for path '%s'", jsonPath);
            }
            
            // Empty checks
            if (IS_EMPTY.equals(assertionType)) {
                boolean isEmpty = isEmptyValue(actualValue);
                return isEmpty ? null : 
                    String.format("Expected empty value for path '%s' but got: '%s'", jsonPath, actualValue);
            }
            
            if (IS_NOT_EMPTY.equals(assertionType)) {
                boolean isEmpty = isEmptyValue(actualValue);
                return !isEmpty ? null : 
                    String.format("Expected non-empty value for path '%s'", jsonPath);
            }
            
            // Standard equality check for EXISTS
            if (EXISTS.equals(assertionType)) {
                return null; // We already confirmed it exists
            }
            
            // For other assertion types, perform the appropriate comparison
            if (EQUALS.equals(assertionType)) {
                if (!objectEquals(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else if (CONTAINS.equals(assertionType)) {
                if (!objectContains(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected to contain '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else if (STARTS_WITH.equals(assertionType)) {
                if (!objectStartsWith(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected to start with '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else if (ENDS_WITH.equals(assertionType)) {
                if (!objectEndsWith(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected to end with '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else if (MATCHES.equals(assertionType)) {
                if (!objectMatches(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected to match pattern '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else if (GREATER_THAN.equals(assertionType)) {
                if (!objectGreaterThan(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected greater than '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else if (LESS_THAN.equals(assertionType)) {
                if (!objectLessThan(actualValue, expectedValue)) {
                    return String.format("JSONPath assertion failed for '%s': expected less than '%s' but got '%s'",
                            jsonPath, expectedValue, actualValue);
                }
            } else {
                return String.format("Unknown assertion type: '%s'", assertionType);
            }
            
            return null; // Assertion passed
        } catch (PathNotFoundException e) {
            return String.format("JSONPath not found: '%s'", jsonPath);
        } catch (Exception e) {
            LOGGER.error("Error evaluating JSONPath: {}", jsonPath, e);
            return String.format("Error evaluating JSONPath '%s': %s", jsonPath, e.getMessage());
        }
    }

    /**
     * Check if expected and actual values are equal, handling different types.
     *
     * @param actual The actual value
     * @param expected The expected value
     * @return true if values are equal
     */
    private boolean objectEquals(Object actual, Object expected) {
        if (actual == null && expected == null) {
            return true;
        }
        
        if (actual == null || expected == null) {
            return false;
        }
        
        // Handle numeric comparisons
        if (actual instanceof Number && expected instanceof Number) {
            if (actual instanceof Double || actual instanceof Float || 
                expected instanceof Double || expected instanceof Float) {
                double actualDouble = ((Number) actual).doubleValue();
                double expectedDouble = ((Number) expected).doubleValue();
                return Math.abs(actualDouble - expectedDouble) < 0.0001;
            } else {
                long actualLong = ((Number) actual).longValue();
                long expectedLong = ((Number) expected).longValue();
                return actualLong == expectedLong;
            }
        }
        
        // Handle booleans
        if (actual instanceof Boolean && expected instanceof Boolean) {
            return Objects.equals(actual, expected);
        }
        
        // Handle strings and other objects
        return Objects.equals(actual.toString(), expected.toString());
    }
    
    /**
     * Check if actual value contains expected value.
     *
     * @param actual The actual value
     * @param expected The expected value to be contained
     * @return true if actual contains expected
     */
    private boolean objectContains(Object actual, Object expected) {
        if (actual == null) {
            return false;
        }
        
        String expectedStr = expected != null ? expected.toString() : "";
        
        // Check if actual is a collection
        if (actual instanceof Collection) {
            Collection<?> collection = (Collection<?>) actual;
            for (Object item : collection) {
                if (objectEquals(item, expected)) {
                    return true;
                }
            }
            return false;
        }
        
        // Check if actual is a map
        if (actual instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) actual;
            return map.containsValue(expected) || map.containsKey(expected);
        }
        
        // Default to string contains check
        String actualStr = actual.toString();
        return actualStr.contains(expectedStr);
    }
    
    /**
     * Check if actual value starts with expected value.
     *
     * @param actual The actual value
     * @param expected The expected prefix
     * @return true if actual starts with expected
     */
    private boolean objectStartsWith(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        return actualStr.startsWith(expectedStr);
    }
    
    /**
     * Check if actual value ends with expected value.
     *
     * @param actual The actual value
     * @param expected The expected suffix
     * @return true if actual ends with expected
     */
    private boolean objectEndsWith(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        return actualStr.endsWith(expectedStr);
    }
    
    /**
     * Check if actual value matches the expected regex pattern.
     *
     * @param actual The actual value
     * @param expected The expected regex pattern
     * @return true if actual matches the pattern
     */
    private boolean objectMatches(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        String actualStr = actual.toString();
        String patternStr = expected.toString();
        
        try {
            Pattern pattern = Pattern.compile(patternStr);
            return pattern.matcher(actualStr).matches();
        } catch (Exception e) {
            LOGGER.warn("Invalid regex pattern: {}", patternStr, e);
            return false;
        }
    }
    
    /**
     * Check if actual value is greater than expected value.
     *
     * @param actual The actual value
     * @param expected The expected value
     * @return true if actual is greater than expected
     */
    private boolean objectGreaterThan(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        // Compare numbers - handle negative numbers properly
        if (actual instanceof Number && expected instanceof Number) {
            double actualDouble = ((Number) actual).doubleValue();
            double expectedDouble = ((Number) expected).doubleValue();
            
            // Debug log to help diagnose comparison issues
            LOGGER.debug("Comparing {} > {}: {}", actualDouble, expectedDouble, actualDouble > expectedDouble);
            return actualDouble > expectedDouble;
        }
        
        // If it's a string that represents a number, convert and compare numerically
        try {
            if (actual.toString().matches("-?\\d+(\\.\\d+)?") && expected.toString().matches("-?\\d+(\\.\\d+)?")) {
                double actualDouble = Double.parseDouble(actual.toString());
                double expectedDouble = Double.parseDouble(expected.toString());
                LOGGER.debug("Comparing numeric strings {} > {}: {}", actualDouble, expectedDouble, actualDouble > expectedDouble);
                return actualDouble > expectedDouble;
            }
        } catch (NumberFormatException e) {
            // Not valid numbers, fallback to string comparison
        }
        
        // Compare strings lexicographically
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        LOGGER.debug("Comparing strings {} > {}: {}", actualStr, expectedStr, actualStr.compareTo(expectedStr) > 0);
        return actualStr.compareTo(expectedStr) > 0;
    }
    
    /**
     * Check if actual value is less than expected value.
     *
     * @param actual The actual value
     * @param expected The expected value
     * @return true if actual is less than expected
     */
    private boolean objectLessThan(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        
        // Compare numbers - handle negative numbers properly
        if (actual instanceof Number && expected instanceof Number) {
            double actualDouble = ((Number) actual).doubleValue();
            double expectedDouble = ((Number) expected).doubleValue();
            
            // Debug log to help diagnose comparison issues
            LOGGER.debug("Comparing {} < {}: {}", actualDouble, expectedDouble, actualDouble < expectedDouble);
            return actualDouble < expectedDouble;
        }
        
        // If it's a string that represents a number, convert and compare numerically
        try {
            if (actual.toString().matches("-?\\d+(\\.\\d+)?") && expected.toString().matches("-?\\d+(\\.\\d+)?")) {
                double actualDouble = Double.parseDouble(actual.toString());
                double expectedDouble = Double.parseDouble(expected.toString());
                LOGGER.debug("Comparing numeric strings {} < {}: {}", actualDouble, expectedDouble, actualDouble < expectedDouble);
                return actualDouble < expectedDouble;
            }
        } catch (NumberFormatException e) {
            // Not valid numbers, fallback to string comparison
        }
        
        // Compare strings lexicographically
        String actualStr = actual.toString();
        String expectedStr = expected.toString();
        
        LOGGER.debug("Comparing strings {} < {}: {}", actualStr, expectedStr, actualStr.compareTo(expectedStr) < 0);
        return actualStr.compareTo(expectedStr) < 0;
    }
    
    /**
     * Check if a value is empty (empty string, empty collection, empty map, etc.)
     *
     * @param value The value to check
     * @return true if the value is considered empty
     */
    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }
        
        if (value instanceof String) {
            return ((String) value).isEmpty();
        }
        
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        
        if (value.getClass().isArray()) {
            // This is a simplified check that might not work for all array types
            return java.lang.reflect.Array.getLength(value) == 0;
        }
        
        return false;
    }
}
