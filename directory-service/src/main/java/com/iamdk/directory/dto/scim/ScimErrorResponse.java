package com.iamdk.directory.dto.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SCIM 2.0 Error Response
 * https://datatracker.ietf.org/doc/html/rfc7644#section-3.12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScimErrorResponse {

    @JsonProperty("schemas")
    @Builder.Default
    private List<String> schemas = List.of("urn:ietf:params:scim:api:messages:2.0:Error");

    @JsonProperty("status")
    private String status;

    @JsonProperty("scimType")
    private String scimType;

    @JsonProperty("detail")
    private String detail;
}
