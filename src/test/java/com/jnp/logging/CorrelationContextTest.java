package com.jnp.logging;

import com.jnp.logging.context.CorrelationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CorrelationContext.
 */
class CorrelationContextTest {

    @AfterEach
    void tearDown() {
        CorrelationContext.clear();
    }

    @Test
    void generatesCorrelationIdWhenNotSet() {
        String correlationId = CorrelationContext.getCorrelationId();
        assertThat(correlationId).isNotNull().isNotEmpty();
    }

    @Test
    void returnsSetCorrelationId() {
        String expected = "test-correlation-id";
        CorrelationContext.setCorrelationId(expected);
        
        assertThat(CorrelationContext.getCorrelationId()).isEqualTo(expected);
    }

    @Test
    void clearsCorrelationId() {
        CorrelationContext.setCorrelationId("test-id");
        CorrelationContext.clear();
        
        // After clear, getting should generate new ID
        String newId = CorrelationContext.getCorrelationId();
        assertThat(newId).isNotEqualTo("test-id");
    }

    @Test
    void hasCorrelationIdReturnsTrueWhenSet() {
        CorrelationContext.setCorrelationId("test-id");
        assertThat(CorrelationContext.hasCorrelationId()).isTrue();
    }

    @Test
    void hasCorrelationIdReturnsFalseWhenNotSet() {
        CorrelationContext.clear();
        assertThat(CorrelationContext.hasCorrelationId()).isFalse();
    }

    @Test
    void generatedIdsAreUnique() {
        String id1 = CorrelationContext.generateCorrelationId();
        String id2 = CorrelationContext.generateCorrelationId();
        
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void handlesBlankCorrelationId() {
        CorrelationContext.setCorrelationId("   ");
        
        // Should generate new ID for blank input
        String id = CorrelationContext.getCorrelationId();
        assertThat(id).isNotBlank();
    }
}
