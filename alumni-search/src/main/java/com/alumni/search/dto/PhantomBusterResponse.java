package com.alumni.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

/**
 * Maps the PhantomBuster agent launch/result API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhantomBusterResponse {

    private String id;
    private String status;

    @JsonProperty("containerId")
    private String containerId;

    private Output output;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        private List<LinkedInProfile> resultObject;
        private String error;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LinkedInProfile {
        private String fullName;
        private String title;
        private String company;
        private String location;
        private String summary;
        private String profileUrl;
        private String school;
        private Integer graduationYear;
    }
}
