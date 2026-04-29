package com.alumni.search.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfileDto {

    private String name;
    private String currentRole;
    private String university;
    private String location;
    private String linkedinHeadline;
    private String linkedinUrl;
    private Integer passoutYear;
}
