package com.iamdk.directory.dto.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SCIM 2.0 Group Resource
 * https://datatracker.ietf.org/doc/html/rfc7643#section-8.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScimGroup {

    @JsonProperty("schemas")
    @Builder.Default
    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:Group");

    @JsonProperty("id")
    private String id;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("members")
    private List<Member> members;

    @JsonProperty("meta")
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Member {
        @JsonProperty("value")
        private String value;
        @JsonProperty("display")
        private String display;
        @JsonProperty("$ref")
        private String ref;
        @JsonProperty("type")
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @JsonProperty("resourceType")
        private String resourceType;
        @JsonProperty("created")
        private String created;
        @JsonProperty("lastModified")
        private String lastModified;
        @JsonProperty("location")
        private String location;
    }
}
