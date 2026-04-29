package com.alumni.search.service;

import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.PhantomBusterResponse;
import com.alumni.search.model.AlumniProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AlumniMapper Unit Tests")
class AlumniMapperTest {

    private AlumniMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AlumniMapper();
    }

    @Test
    @DisplayName("toDto - maps all entity fields correctly")
    void toDto_MapsAllFields() {
        AlumniProfile entity = AlumniProfile.builder()
                .name("Jane Smith")
                .currentRole("Data Scientist")
                .university("University of XYZ")
                .location("San Francisco, CA")
                .linkedinHeadline("Data Scientist | AI Enthusiast")
                .linkedinUrl("https://linkedin.com/in/janesmith")
                .passoutYear(2019)
                .build();

        AlumniProfileDto dto = mapper.toDto(entity);

        assertThat(dto.getName()).isEqualTo("Jane Smith");
        assertThat(dto.getCurrentRole()).isEqualTo("Data Scientist");
        assertThat(dto.getUniversity()).isEqualTo("University of XYZ");
        assertThat(dto.getLocation()).isEqualTo("San Francisco, CA");
        assertThat(dto.getLinkedinHeadline()).isEqualTo("Data Scientist | AI Enthusiast");
        assertThat(dto.getPassoutYear()).isEqualTo(2019);
    }

    @Test
    @DisplayName("toEntity - builds headline from title and company")
    void toEntity_BuildsHeadlineFromTitleAndCompany() {
        PhantomBusterResponse.LinkedInProfile raw = new PhantomBusterResponse.LinkedInProfile();
        raw.setFullName("John Doe");
        raw.setTitle("Software Engineer");
        raw.setCompany("TechCorp");
        raw.setLocation("New York, NY");
        raw.setProfileUrl("https://linkedin.com/in/johndoe");
        raw.setGraduationYear(2020);

        AlumniProfile entity = mapper.toEntity(raw, "University of XYZ");

        assertThat(entity.getName()).isEqualTo("John Doe");
        assertThat(entity.getCurrentRole()).isEqualTo("Software Engineer");
        assertThat(entity.getUniversity()).isEqualTo("University of XYZ");
        assertThat(entity.getLinkedinHeadline()).isEqualTo("Software Engineer at TechCorp");
        assertThat(entity.getPassoutYear()).isEqualTo(2020);
    }

    @Test
    @DisplayName("toEntity - falls back to summary when title or company is null")
    void toEntity_FallsBackToSummaryWhenTitleOrCompanyNull() {
        PhantomBusterResponse.LinkedInProfile raw = new PhantomBusterResponse.LinkedInProfile();
        raw.setFullName("Alice Brown");
        raw.setSummary("Aspiring data engineer");
        raw.setProfileUrl("https://linkedin.com/in/alicebrown");

        AlumniProfile entity = mapper.toEntity(raw, "MIT");

        assertThat(entity.getLinkedinHeadline()).isEqualTo("Aspiring data engineer");
    }
}
