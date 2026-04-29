package com.alumni.search.client;

import com.alumni.search.dto.AlumniSearchRequest;
import com.alumni.search.dto.PhantomBusterResponse;
import com.alumni.search.exception.PhantomBusterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Client for interacting with the PhantomBuster API.
 *
 * PhantomBuster uses an asynchronous agent model:
 *  1. Launch the agent (POST /agents/{id}/launch) with search arguments
 *  2. Poll the container result until the job is complete
 *  3. Parse the output JSON for LinkedIn profiles
 */
@Component
@Slf4j
public class PhantomBusterClient {

    private static final int MAX_POLL_ATTEMPTS = 20;
    private static final long POLL_INTERVAL_MS = 3000L;

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final String agentId;

    public PhantomBusterClient(
            RestTemplate restTemplate,
            @Value("${phantombuster.api.key}") String apiKey,
            @Value("${phantombuster.api.base-url}") String baseUrl,
            @Value("${phantombuster.agent.id}") String agentId) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.agentId = agentId;
    }

    /**
     * Searches LinkedIn alumni profiles by launching a PhantomBuster agent and
     * polling for results.
     *
     * @param request the search criteria
     * @return list of raw LinkedIn profiles from PhantomBuster
     */
    public List<PhantomBusterResponse.LinkedInProfile> searchAlumni(AlumniSearchRequest request) {
        log.info("Launching PhantomBuster agent for university={}, designation={}, year={}",
                request.getUniversity(), request.getDesignation(), request.getPassoutYear());

        String containerId = launchAgent(request);
        PhantomBusterResponse result = pollForResult(containerId);

        if (result.getOutput() == null) {
            throw new PhantomBusterException(
                    "PhantomBuster returned empty output for the search query.",
                    "EMPTY_RESPONSE");
        }

        if (result.getOutput().getError() != null) {
            throw new PhantomBusterException(
                    "PhantomBuster agent error: " + result.getOutput().getError(),
                    "AGENT_ERROR");
        }

        List<PhantomBusterResponse.LinkedInProfile> profiles = result.getOutput().getResultObject();
        log.info("PhantomBuster returned {} profiles", profiles != null ? profiles.size() : 0);
        return profiles != null ? profiles : List.of();
    }

    private String launchAgent(AlumniSearchRequest request) {
        String url = baseUrl + "/agents/" + agentId + "/launch";
        HttpHeaders headers = buildHeaders();

        String searchQuery = buildSearchQuery(request);
        Map<String, Object> body = Map.of(
                "argument", Map.of(
                        "searches", List.of(searchQuery),
                        "numberOfProfiles", 10,
                        "sessionCookie", ""
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<PhantomBusterResponse> response =
                    restTemplate.postForEntity(url, entity, PhantomBusterResponse.class);

            if (response.getBody() == null || response.getBody().getContainerId() == null) {
                throw new PhantomBusterException(
                        "PhantomBuster agent launch returned no container ID.", "LAUNCH_FAILED");
            }
            return response.getBody().getContainerId();

        } catch (HttpClientErrorException e) {
            log.error("PhantomBuster client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new PhantomBusterException("Invalid PhantomBuster API key.", "UNAUTHORIZED");
            }
            throw new PhantomBusterException(
                    "PhantomBuster API request failed: " + e.getMessage(), "CLIENT_ERROR");
        } catch (HttpServerErrorException e) {
            log.error("PhantomBuster server error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PhantomBusterException(
                    "PhantomBuster service is unavailable. Please try again later.", "SERVER_ERROR");
        }
    }

    private PhantomBusterResponse pollForResult(String containerId) {
        String url = baseUrl + "/containers/" + containerId + "/output";
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            log.debug("Polling PhantomBuster container {} — attempt {}/{}", containerId, attempt, MAX_POLL_ATTEMPTS);
            try {
                ResponseEntity<PhantomBusterResponse> response =
                        restTemplate.exchange(url, HttpMethod.GET, entity, PhantomBusterResponse.class);

                PhantomBusterResponse body = response.getBody();
                if (body != null && isCompleted(body.getStatus())) {
                    return body;
                }

                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PhantomBusterException("Polling interrupted.", "INTERRUPTED");
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                throw new PhantomBusterException(
                        "Error while polling PhantomBuster: " + e.getMessage(), "POLL_ERROR");
            }
        }

        throw new PhantomBusterException(
                "PhantomBuster agent timed out after " + MAX_POLL_ATTEMPTS + " attempts.",
                "TIMEOUT");
    }

    private boolean isCompleted(String status) {
        return "finished".equalsIgnoreCase(status)
                || "error".equalsIgnoreCase(status)
                || "stopped".equalsIgnoreCase(status);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Phantombuster-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * Builds a LinkedIn search query string from the request parameters.
     * Format: "designation site:linkedin.com/in university [year]"
     */
    private String buildSearchQuery(AlumniSearchRequest request) {
        StringBuilder query = new StringBuilder();
        query.append(request.getDesignation())
             .append(" site:linkedin.com/in ")
             .append(request.getUniversity());
        if (request.getPassoutYear() != null) {
            query.append(" ").append(request.getPassoutYear());
        }
        return query.toString();
    }
}
