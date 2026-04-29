package com.alumni.search.repository;

import com.alumni.search.model.AlumniProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumniProfileRepository extends JpaRepository<AlumniProfile, Long> {

    Optional<AlumniProfile> findByLinkedinUrl(String linkedinUrl);

    List<AlumniProfile> findByUniversityIgnoreCase(String university);

    @Query("""
            SELECT a FROM AlumniProfile a
            WHERE LOWER(a.university) LIKE LOWER(CONCAT('%', :university, '%'))
            AND (:passoutYear IS NULL OR a.passoutYear = :passoutYear)
            ORDER BY a.name ASC
            """)
    List<AlumniProfile> findByUniversityContainingAndPassoutYear(
            @Param("university") String university,
            @Param("passoutYear") Integer passoutYear
    );

    boolean existsByLinkedinUrl(String linkedinUrl);
}
