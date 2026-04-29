package com.alumni.search.controller;

import com.alumni.search.dto.AlumniProfileDto;
import com.alumni.search.dto.AlumniSearchRequest;
import com.alumni.search.exception.PhantomBusterException;
import com.alumni.search.service.AlumniService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlumniController.class)
@DisplayName("AlumniController Integration Tests")
class AlumniControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlumniService alumniService;

    private AlumniProfileDto sampleDto() {
        return AlumniProfileDto.builder()
                .name("John Doe")
                .currentRole("Software Engineer")
                .university("University of XYZ")
                .location("New York, NY")
                .linkedinHeadline("Passionate Software Engineer at XYZ Corp")
                .passoutYear(2020)
                .build();
    }

    // ── POST /api/alumni/search ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /search - valid request returns 200 with alumni data")
    void search_ValidRequest_Returns200() throws Exception {
        AlumniSearchRequest request = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .designation("Software Engineer")
                .passoutYear(2020)
                .build();

        given(alumniService.searchAndSaveAlumni(any())).willReturn(List.of(sampleDto()));

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"))
                .andExpect(jsonPath("$.data[0].currentRole").value("Software Engineer"))
                .andExpect(jsonPath("$.data[0].passoutYear").value(2020));
    }

    @Test
    @DisplayName("POST /search - missing university returns 400")
    void search_MissingUniversity_Returns400() throws Exception {
        AlumniSearchRequest request = AlumniSearchRequest.builder()
                .designation("Software Engineer")
                .build();

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /search - missing designation returns 400")
    void search_MissingDesignation_Returns400() throws Exception {
        AlumniSearchRequest request = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .build();

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("POST /search - passoutYear is optional, request still valid")
    void search_WithoutPassoutYear_Returns200() throws Exception {
        AlumniSearchRequest request = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .designation("Data Scientist")
                .build();

        given(alumniService.searchAndSaveAlumni(any())).willReturn(List.of());

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /search - PhantomBuster error returns 502")
    void search_PhantomBusterError_Returns502() throws Exception {
        AlumniSearchRequest request = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .designation("Engineer")
                .build();

        given(alumniService.searchAndSaveAlumni(any()))
                .willThrow(new PhantomBusterException("Agent timed out", "TIMEOUT"));

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorCode").value("TIMEOUT"));
    }

    @Test
    @DisplayName("POST /search - invalid passout year returns 400")
    void search_InvalidPassoutYear_Returns400() throws Exception {
        String invalidRequest = """
                {
                  "university": "University of XYZ",
                  "designation": "Engineer",
                  "passoutYear": 1800
                }
                """;

        mockMvc.perform(post("/api/alumni/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    // ── GET /api/alumni/all ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /all - returns list of all saved alumni")
    void getAll_ReturnsAlumniList() throws Exception {
        given(alumniService.getAllSavedAlumni()).willReturn(List.of(sampleDto()));

        mockMvc.perform(get("/api/alumni/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /all - returns empty list when no alumni saved")
    void getAll_NoAlumni_ReturnsEmptyList() throws Exception {
        given(alumniService.getAllSavedAlumni()).willReturn(List.of());

        mockMvc.perform(get("/api/alumni/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
