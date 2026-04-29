package com.alumni.search.service;

import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.PhantomBusterResponse;
import com.alumni.search.model.AlumniProfile;
import org.springframework.stereotype.Component;

/**
 * Converts between AlumniProfile entities, DTOs, and PhantomBuster raw responses.
 */
@Component
public class AlumniMapper {

    public AlumniProfileDto toDto(AlumniProfile entity) {
        return AlumniProfileDto.builder()
                .name(entity.getName())
                .currentRole(entity.getCurrentRole())
                .university(entity.getUniversity())
                .location(entity.getLocation())
                .linkedinHeadline(entity.getLinkedinHeadline())
                .linkedinUrl(entity.getLinkedinUrl())
                .passoutYear(entity.getPassoutYear())
                .build();
    }

    public AlumniProfile toEntity(PhantomBusterResponse.LinkedInProfile profile, String university) {
        return AlumniProfile.builder()
                .name(profile.getFullName())
                .currentRole(profile.getTitle())
                .university(university)
                .location(profile.getLocation())
                .linkedinHeadline(buildHeadline(profile))
                .linkedinUrl(profile.getProfileUrl())
                .passoutYear(profile.getGraduationYear())
                .build();
    }

    private String buildHeadline(PhantomBusterResponse.LinkedInProfile profile) {
        if (profile.getTitle() != null && profile.getCompany() != null) {
            return profile.getTitle() + " at " + profile.getCompany();
        }
        return profile.getSummary();
    }
}
