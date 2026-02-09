package com.jnp.logging.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for masking sensitive data in logs.
 */
public class MaskingUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MaskingUtil() {
        // Utility class
    }

    /**
     * Mask sensitive fields in a JSON string.
     */
    public static String maskJsonFields(String json, List<String> fieldsToMask, String maskValue) {
        if (json == null || json.isBlank()) {
            return json;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            Set<String> fieldsLowerCase = fieldsToMask.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            
            maskFields(rootNode, fieldsLowerCase, maskValue);
            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            // If not valid JSON, return as-is
            return json;
        }
    }

    /**
     * Mask sensitive fields in an object.
     */
    public static Object maskObjectFields(Object obj, List<String> fieldsToMask, String maskValue) {
        if (obj == null) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(obj);
            String maskedJson = maskJsonFields(json, fieldsToMask, maskValue);
            return objectMapper.readTree(maskedJson);
        } catch (JsonProcessingException e) {
            return obj;
        }
    }

    private static void maskFields(JsonNode node, Set<String> fieldsToMask, String maskValue) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                if (fieldsToMask.contains(fieldName.toLowerCase())) {
                    objectNode.put(fieldName, maskValue);
                } else {
                    maskFields(objectNode.get(fieldName), fieldsToMask, maskValue);
                }
            });
        } else if (node.isArray()) {
            node.forEach(element -> maskFields(element, fieldsToMask, maskValue));
        }
    }

    /**
     * Mask a specific pattern in a string (e.g., credit card numbers).
     */
    public static String maskPattern(String input, String pattern, String replacement) {
        if (input == null) {
            return null;
        }
        return input.replaceAll(pattern, replacement);
    }

    /**
     * Mask credit card numbers (16 digits, optionally with spaces or dashes).
     */
    public static String maskCreditCard(String input) {
        if (input == null) {
            return null;
        }
        // Match 16 digits with optional spaces or dashes
        return input.replaceAll("\\b(\\d{4})[- ]?(\\d{4})[- ]?(\\d{4})[- ]?(\\d{4})\\b", 
                "$1-****-****-$4");
    }

    /**
     * Mask email addresses.
     */
    public static String maskEmail(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", 
                "***@$2");
    }
}
