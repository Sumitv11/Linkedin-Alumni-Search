package com.alumni.search.service;

import com.alumni.search.client.PhantomBusterClient;
import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.AlumniSearchRequest;
import com.alumni.search.dto.PhantomBusterResponse;
import com.alumni.search.model.AlumniProfile;
import com.alumni.search.repository.AlumniProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlumniServiceImpl implements AlumniService {

    private final PhantomBusterClient phantomBusterClient;
    private final AlumniProfileRepository repository;
    private final AlumniMapper mapper;

    @Override
    @Transactional
    public List<AlumniProfileDto> searchAndSaveAlumni(AlumniSearchRequest request) {
        List<PhantomBusterResponse.LinkedInProfile> rawProfiles =
                phantomBusterClient.searchAlumni(request);

        List<AlumniProfile> savedProfiles = rawProfiles.stream()
                .filter(p -> p.getFullName() != null && !p.getFullName().isBlank())
                .map(p -> upsert(p, request.getUniversity()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Persisted {} alumni profiles for university={}",
                savedProfiles.size(), request.getUniversity());

        return savedProfiles.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumniProfileDto> getAllSavedAlumni() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Inserts a new alumni record or updates an existing one matched by LinkedIn URL.
     * Returns the persisted entity.
     */
    private AlumniProfile upsert(PhantomBusterResponse.LinkedInProfile rawProfile, String university) {
        try {
            String linkedinUrl = rawProfile.getProfileUrl();

            if (linkedinUrl != null && repository.existsByLinkedinUrl(linkedinUrl)) {
                AlumniProfile existing = repository.findByLinkedinUrl(linkedinUrl).orElseThrow();
                existing.setCurrentRole(rawProfile.getTitle());
                existing.setLinkedinHeadline(mapper.toEntity(rawProfile, university).getLinkedinHeadline());
                existing.setLocation(rawProfile.getLocation());
                return repository.save(existing);
            }

            return repository.save(mapper.toEntity(rawProfile, university));
        } catch (Exception e) {
            log.warn("Failed to persist profile '{}': {}", rawProfile.getFullName(), e.getMessage());
            return null;
        }
    }
}
