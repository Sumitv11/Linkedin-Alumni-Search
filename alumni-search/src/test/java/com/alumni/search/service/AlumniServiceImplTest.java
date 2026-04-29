package com.alumni.search.service;

import com.alumni.search.client.PhantomBusterClient;
import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.AlumniSearchRequest;
import com.alumni.search.dto.PhantomBusterResponse;
import com.alumni.search.exception.PhantomBusterException;
import com.alumni.search.model.AlumniProfile;
import com.alumni.search.repository.AlumniProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlumniServiceImpl Unit Tests")
class AlumniServiceImplTest {

    @Mock
    private PhantomBusterClient phantomBusterClient;

    @Mock
    private AlumniProfileRepository repository;

    @Mock
    private AlumniMapper mapper;

    @InjectMocks
    private AlumniServiceImpl alumniService;

    private AlumniSearchRequest searchRequest;
    private PhantomBusterResponse.LinkedInProfile rawProfile;
    private AlumniProfile entity;
    private AlumniProfileDto dto;

    @BeforeEach
    void setUp() {
        searchRequest = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .designation("Software Engineer")
                .passoutYear(2020)
                .build();

        rawProfile = new PhantomBusterResponse.LinkedInProfile();
        rawProfile.setFullName("John Doe");
        rawProfile.setTitle("Software Engineer");
        rawProfile.setCompany("XYZ Corp");
        rawProfile.setLocation("New York, NY");
        rawProfile.setProfileUrl("https://linkedin.com/in/johndoe");
        rawProfile.setGraduationYear(2020);

        entity = AlumniProfile.builder()
                .id(1L)
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinUrl("https://linkedin.com/in/johndoe")
                .passoutYear(2020)
                .build();

        dto = AlumniProfileDto.builder()
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .passoutYear(2020)
                .build();
    }

    @Test
    @DisplayName("searchAndSaveAlumni - should return mapped DTOs for new profiles")
    void searchAndSaveAlumni_NewProfile_ReturnsDtos() {
        given(phantomBusterClient.searchAlumni(searchRequest)).willReturn(List.of(rawProfile));
        given(repository.existsByLinkedinUrl(rawProfile.getProfileUrl())).willReturn(false);
        given(mapper.toEntity(rawProfile, searchRequest.getUniversity())).willReturn(entity);
        given(repository.save(entity)).willReturn(entity);
        given(mapper.toDto(entity)).willReturn(dto);

        List<AlumniProfileDto> result = alumniService.searchAndSaveAlumni(searchRequest);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        then(repository).should(times(1)).save(entity);
    }

    @Test
    @DisplayName("searchAndSaveAlumni - should update existing profile matched by LinkedIn URL")
    void searchAndSaveAlumni_ExistingProfile_UpdatesAndReturns() {
        given(phantomBusterClient.searchAlumni(searchRequest)).willReturn(List.of(rawProfile));
        given(repository.existsByLinkedinUrl(rawProfile.getProfileUrl())).willReturn(true);
        given(repository.findByLinkedinUrl(rawProfile.getProfileUrl())).willReturn(Optional.of(entity));
        given(mapper.toEntity(rawProfile, searchRequest.getUniversity())).willReturn(entity);
        given(repository.save(entity)).willReturn(entity);
        given(mapper.toDto(entity)).willReturn(dto);

        List<AlumniProfileDto> result = alumniService.searchAndSaveAlumni(searchRequest);

        assertThat(result).hasSize(1);
        then(repository).should(times(1)).save(entity);
    }

    @Test
    @DisplayName("searchAndSaveAlumni - should filter out profiles with blank names")
    void searchAndSaveAlumni_BlankNameProfile_IsFiltered() {
        PhantomBusterResponse.LinkedInProfile blankNameProfile = new PhantomBusterResponse.LinkedInProfile();
        blankNameProfile.setFullName("");
        blankNameProfile.setProfileUrl("https://linkedin.com/in/unknown");

        given(phantomBusterClient.searchAlumni(searchRequest)).willReturn(List.of(blankNameProfile));

        List<AlumniProfileDto> result = alumniService.searchAndSaveAlumni(searchRequest);

        assertThat(result).isEmpty();
        then(repository).should(never()).save(any());
    }

    @Test
    @DisplayName("searchAndSaveAlumni - should return empty list when PhantomBuster returns no results")
    void searchAndSaveAlumni_EmptyPhantomBusterResponse_ReturnsEmptyList() {
        given(phantomBusterClient.searchAlumni(searchRequest)).willReturn(List.of());

        List<AlumniProfileDto> result = alumniService.searchAndSaveAlumni(searchRequest);

        assertThat(result).isEmpty();
        then(repository).should(never()).save(any());
    }

    @Test
    @DisplayName("searchAndSaveAlumni - should propagate PhantomBusterException")
    void searchAndSaveAlumni_PhantomBusterThrows_PropagatesException() {
        given(phantomBusterClient.searchAlumni(searchRequest))
                .willThrow(new PhantomBusterException("API error", "TIMEOUT"));

        assertThatThrownBy(() -> alumniService.searchAndSaveAlumni(searchRequest))
                .isInstanceOf(PhantomBusterException.class)
                .hasMessageContaining("API error");
    }

    @Test
    @DisplayName("getAllSavedAlumni - should return all profiles from database")
    void getAllSavedAlumni_ReturnsAllProfiles() {
        given(repository.findAll()).willReturn(List.of(entity));
        given(mapper.toDto(entity)).willReturn(dto);

        List<AlumniProfileDto> result = alumniService.getAllSavedAlumni();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("getAllSavedAlumni - should return empty list when no profiles are stored")
    void getAllSavedAlumni_NoProfiles_ReturnsEmptyList() {
        given(repository.findAll()).willReturn(List.of());

        List<AlumniProfileDto> result = alumniService.getAllSavedAlumni();

        assertThat(result).isEmpty();
    }
}
