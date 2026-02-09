package com.jnp.logging.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Model representing request information for logging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestInfo {

    @JsonProperty("headers")
    private Map<String, String> headers;

    @JsonProperty("query_params")
    private Map<String, String> queryParams;

    @JsonProperty("path_params")
    private Map<String, String> pathParams;

    @JsonProperty("body")
    private Object body;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("content_length")
    private Long contentLength;
}}
