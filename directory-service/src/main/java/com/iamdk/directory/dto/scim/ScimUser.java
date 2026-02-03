package com.iamdk.directory.dto.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * SCIM 2.0 User Resource
 * https://datatracker.ietf.org/doc/html/rfc7643#section-8.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScimUser {

    @JsonProperty("schemas")
    @Builder.Default
    private List<String> schemas = List.of("urn:ietf:params:scim:schemas:core:2.0:User");

    @JsonProperty("id")
    private String id;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("name")
    private Name name;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("emails")
    private List<Email> emails;

    @JsonProperty("phoneNumbers")
    private List<PhoneNumber> phoneNumbers;

    @JsonProperty("active")
    private Boolean active;

    @JsonProperty("groups")
    private List<GroupRef> groups;

    @JsonProperty("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User")
    private EnterpriseExtension enterprise;

    @JsonProperty("meta")
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Name {
        @JsonProperty("givenName")
        private String givenName;
        @JsonProperty("familyName")
        private String familyName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Email {
        @JsonProperty("value")
        private String value;
        @JsonProperty("type")
        private String type;
        @JsonProperty("primary")
        private Boolean primary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneNumber {
        @JsonProperty("value")
        private String value;
        @JsonProperty("type")
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupRef {
        @JsonProperty("value")
        private String value;
        @JsonProperty("display")
        private String display;
        @JsonProperty("$ref")
        private String ref;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterpriseExtension {
        @JsonProperty("department")
        private String department;
        @JsonProperty("manager")
        private String manager;
        @JsonProperty("title")
        private String title;
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
