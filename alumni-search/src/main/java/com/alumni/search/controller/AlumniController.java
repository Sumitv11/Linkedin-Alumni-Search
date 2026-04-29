package com.alumni.search.controller;

import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.AlumniSearchRequest;
import com.alumni.search.dto.ApiResponse;
import com.alumni.search.service.AlumniService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumni")
@RequiredArgsConstructor
@Slf4j
public class AlumniController {

    private final AlumniService alumniService;

    /**
     * POST /api/alumni/search
     * Searches LinkedIn alumni profiles via PhantomBuster and persists the results.
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<AlumniProfileDto>>> searchAlumni(
            @Valid @RequestBody AlumniSearchRequest request) {

        log.info("POST /api/alumni/search — university={}, designation={}, passoutYear={}",
                request.getUniversity(), request.getDesignation(), request.getPassoutYear());

        List<AlumniProfileDto> results = alumniService.searchAndSaveAlumni(request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * GET /api/alumni/all
     * Returns all alumni profiles stored in the database.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AlumniProfileDto>>> getAllAlumni() {
        log.info("GET /api/alumni/all");
        List<AlumniProfileDto> all = alumniService.getAllSavedAlumni();
        return ResponseEntity.ok(ApiResponse.success(all));
    }
}
