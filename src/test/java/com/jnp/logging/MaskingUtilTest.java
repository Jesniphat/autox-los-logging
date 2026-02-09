package com.jnp.logging;

import com.jnp.logging.util.MaskingUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MaskingUtil.
 */
class MaskingUtilTest {

    @Test
    void masksJsonFields() {
        String json = """
            {
                "username": "john",
                "password": "secret123",
                "email": "john@example.com"
            }
            """;
        
        String masked = MaskingUtil.maskJsonFields(json, List.of("password"), "***");
        
        assertThat(masked).contains("\"password\":\"***\"");
        assertThat(masked).contains("\"username\":\"john\"");
    }

    @Test
    void masksNestedJsonFields() {
        String json = """
            {
                "user": {
                    "name": "john",
                    "credentials": {
                        "password": "secret123"
                    }
                }
            }
            """;
        
        String masked = MaskingUtil.maskJsonFields(json, List.of("password"), "***");
        
        assertThat(masked).contains("\"password\":\"***\"");
        assertThat(masked).contains("\"name\":\"john\"");
    }

    @Test
    void handlesNullInput() {
        String result = MaskingUtil.maskJsonFields(null, List.of("password"), "***");
        assertThat(result).isNull();
    }

    @Test
    void handlesEmptyInput() {
        String result = MaskingUtil.maskJsonFields("", List.of("password"), "***");
        assertThat(result).isEmpty();
    }

    @Test
    void handlesInvalidJson() {
        String result = MaskingUtil.maskJsonFields("not json", List.of("password"), "***");
        assertThat(result).isEqualTo("not json");
    }

    @Test
    void masksCreditCard() {
        String input = "Card: 1234-5678-9012-3456";
        String masked = MaskingUtil.maskCreditCard(input);
        
        assertThat(masked).isEqualTo("Card: 1234-****-****-3456");
    }

    @Test
    void masksEmail() {
        String input = "Contact: john.doe@example.com";
        String masked = MaskingUtil.maskEmail(input);
        
        assertThat(masked).isEqualTo("Contact: ***@example.com");
    }

    @Test
    void isCaseInsensitive() {
        String json = """
            {
                "PASSWORD": "secret1",
                "Password": "secret2",
                "password": "secret3"
            }
            """;
        
        String masked = MaskingUtil.maskJsonFields(json, List.of("password"), "***");
        
        assertThat(masked).contains("\"PASSWORD\":\"***\"");
        assertThat(masked).contains("\"Password\":\"***\"");
        assertThat(masked).contains("\"password\":\"***\"");
    }
}
