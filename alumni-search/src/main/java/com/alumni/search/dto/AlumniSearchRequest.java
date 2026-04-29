package com.alumni.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniSearchRequest {

    @NotBlank(message = "University name is required")
    @Size(min = 2, max = 200, message = "University name must be between 2 and 200 characters")
    private String university;

    @NotBlank(message = "Designation is required")
    @Size(min = 2, max = 200, message = "Designation must be between 2 and 200 characters")
    private String designation;

    @Min(value = 1950, message = "Pass-out year must be 1950 or later")
    @Max(value = 2100, message = "Pass-out year must be 2100 or earlier")
    private Integer passoutYear;
}
