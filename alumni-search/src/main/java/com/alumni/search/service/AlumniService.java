package com.alumni.search.service;

import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.AlumniSearchRequest;

import java.util.List;

public interface AlumniService {

    /**
     * Searches for alumni profiles via PhantomBuster and persists new results.
     *
     * @param request the search criteria (university, designation, optional passout year)
     * @return list of matched alumni profiles
     */
    List<AlumniProfileDto> searchAndSaveAlumni(AlumniSearchRequest request);

    /**
     * Retrieves all saved alumni profiles from the database.
     *
     * @return list of all stored alumni profiles
     */
    List<AlumniProfileDto> getAllSavedAlumni();
}
