package com.alumni.search.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alumni_profiles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"linkedin_url"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "current_role")
    private String currentRole;

    @Column(nullable = false)
    private String university;

    private String location;

    @Column(name = "linkedin_headline", length = 500)
    private String linkedinHeadline;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "passout_year")
    private Integer passoutYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
