package com.eventelope.condition;

import com.eventelope.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.*;

public class ConditionEvaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionEvaluator.class);

    public boolean evaluateCondition(String condition, TestContext context) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }

        try {
            String interpolatedCondition = interpolateVariables(condition, context);
            LOGGER.debug("Original condition: '{}', Interpolated: '{}'", 
                         condition, interpolatedCondition);

            String[] parts = parseCondition(interpolatedCondition);
            if (parts == null || parts.length != 2) {
                return Boolean.parseBoolean(interpolatedCondition.trim());
            }

            String left = removeQuotes(parts[0].trim());
            String right = removeQuotes(parts[1].trim());
            String operator = findOperator(interpolatedCondition);

            return evaluateWithAssertJ(left, operator, right);
        } catch (Exception e) {
            LOGGER.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }

    private String[] parseCondition(String condition) {
        if (condition.contains("==")) return condition.split("==");
        if (condition.contains("!=")) return condition.split("!=");
        if (condition.contains(">=")) return condition.split(">=");
        if (condition.contains("<=")) return condition.split("<=");
        if (condition.contains(">")) return condition.split(">");
        if (condition.contains("<")) return condition.split("<");
        if (condition.contains(" contains ")) return condition.split(" contains ");
        if (condition.contains(" startsWith ")) return condition.split(" startsWith ");
        if (condition.contains(" endsWith ")) return condition.split(" endsWith ");
        return null;
    }

    private String findOperator(String condition) {
        if (condition.contains("==")) return "==";
        if (condition.contains("!=")) return "!=";
        if (condition.contains(">=")) return ">=";
        if (condition.contains("<=")) return "<=";
        if (condition.contains(">")) return ">";
        if (condition.contains("<")) return "<";
        if (condition.contains(" contains ")) return "contains";
        if (condition.contains(" startsWith ")) return "startsWith";
        if (condition.contains(" endsWith ")) return "endsWith";
        return null;
    }

    private boolean evaluateWithAssertJ(String left, String operator, String right) {
        try {
            switch (operator) {
                case "==":
                    assertThat(left).isEqualTo(right);
                    return true;
                case "!=":
                    assertThat(left).isNotEqualTo(right);
                    return true;
                case ">":
                    assertThat(Double.parseDouble(left)).isGreaterThan(Double.parseDouble(right));
                    return true;
                case "<":
                    assertThat(Double.parseDouble(left)).isLessThan(Double.parseDouble(right));
                    return true;
                case ">=":
                    assertThat(Double.parseDouble(left)).isGreaterThanOrEqualTo(Double.parseDouble(right));
                    return true;
                case "<=":
                    assertThat(Double.parseDouble(left)).isLessThanOrEqualTo(Double.parseDouble(right));
                    return true;
                case "contains":
                    assertThat(left).contains(right);
                    return true;
                case "startsWith":
                    assertThat(left).startsWith(right);
                    return true;
                case "endsWith":
                    assertThat(left).endsWith(right);
                    return true;
                default:
                    return false;
            }
        } catch (AssertionError e) {
            return false;
        }
    }

    private String interpolateVariables(String condition, TestContext context) {
        String result = condition;
        int startIdx = result.indexOf("${");
        while (startIdx != -1) {
            int endIdx = result.indexOf("}", startIdx);
            if (endIdx == -1) break;

            String varName = result.substring(startIdx + 2, endIdx);
            Object varValueObj = context.getVariable(varName);
            String varValue = varValueObj != null ? String.valueOf(varValueObj) : "";

            if (varValueObj == null) {
                LOGGER.warn("Variable '{}' not found in context, using empty string", varName);
            }

            result = result.substring(0, startIdx) + varValue + result.substring(endIdx + 1);
            startIdx = result.indexOf("${", startIdx + varValue.length());
        }
        return result;
    }

    private String removeQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("'") && value.endsWith("'")) || 
                (value.startsWith("\"") && value.endsWith("\""))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}