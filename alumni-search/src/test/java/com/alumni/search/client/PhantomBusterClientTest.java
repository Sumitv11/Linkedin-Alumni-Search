package com.alumni.search.client;

import com.alumni.search.dto.AlumniSearchRequest;
import com.alumni.search.dto.PhantomBusterResponse;
import com.alumni.search.exception.PhantomBusterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhantomBusterClient Unit Tests")
class PhantomBusterClientTest {

    @Mock
    private RestTemplate restTemplate;

    private PhantomBusterClient client;

    private AlumniSearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        client = new PhantomBusterClient(
                restTemplate,
                "test-api-key",
                "https://api.phantombuster.com/api/v2",
                "test-agent-id"
        );

        searchRequest = AlumniSearchRequest.builder()
                .university("University of XYZ")
                .designation("Software Engineer")
                .passoutYear(2020)
                .build();
    }

    @Test
    @DisplayName("searchAlumni - returns profiles on successful agent completion")
    void searchAlumni_Success_ReturnsProfiles() {
        PhantomBusterResponse launchResponse = new PhantomBusterResponse();
        launchResponse.setContainerId("container-123");

        PhantomBusterResponse.LinkedInProfile profile = new PhantomBusterResponse.LinkedInProfile();
        profile.setFullName("John Doe");
        profile.setTitle("Software Engineer");

        PhantomBusterResponse.Output output = new PhantomBusterResponse.Output();
        output.setResultObject(List.of(profile));

        PhantomBusterResponse pollResponse = new PhantomBusterResponse();
        pollResponse.setStatus("finished");
        pollResponse.setOutput(output);

        given(restTemplate.postForEntity(anyString(), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(launchResponse));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(pollResponse));

        List<PhantomBusterResponse.LinkedInProfile> result = client.searchAlumni(searchRequest);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("searchAlumni - throws PhantomBusterException on 401 Unauthorized")
    void searchAlumni_Unauthorized_ThrowsException() {
        given(restTemplate.postForEntity(anyString(), any(), eq(PhantomBusterResponse.class)))
                .willThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.searchAlumni(searchRequest))
                .isInstanceOf(PhantomBusterException.class)
                .hasMessageContaining("Invalid PhantomBuster API key");
    }

    @Test
    @DisplayName("searchAlumni - throws EMPTY_RESPONSE when output is null")
    void searchAlumni_NullOutput_ThrowsEmptyResponseException() {
        PhantomBusterResponse launchResponse = new PhantomBusterResponse();
        launchResponse.setContainerId("container-456");

        PhantomBusterResponse pollResponse = new PhantomBusterResponse();
        pollResponse.setStatus("finished");
        pollResponse.setOutput(null);

        given(restTemplate.postForEntity(anyString(), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(launchResponse));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(pollResponse));

        assertThatThrownBy(() -> client.searchAlumni(searchRequest))
                .isInstanceOf(PhantomBusterException.class)
                .hasMessageContaining("empty output");
    }

    @Test
    @DisplayName("searchAlumni - throws AGENT_ERROR when output contains error")
    void searchAlumni_AgentError_ThrowsAgentErrorException() {
        PhantomBusterResponse launchResponse = new PhantomBusterResponse();
        launchResponse.setContainerId("container-789");

        PhantomBusterResponse.Output output = new PhantomBusterResponse.Output();
        output.setError("LinkedIn rate limit exceeded");

        PhantomBusterResponse pollResponse = new PhantomBusterResponse();
        pollResponse.setStatus("error");
        pollResponse.setOutput(output);

        given(restTemplate.postForEntity(anyString(), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(launchResponse));
        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(pollResponse));

        assertThatThrownBy(() -> client.searchAlumni(searchRequest))
                .isInstanceOf(PhantomBusterException.class)
                .hasMessageContaining("LinkedIn rate limit exceeded");
    }

    @Test
    @DisplayName("searchAlumni - throws LAUNCH_FAILED when containerId is null")
    void searchAlumni_NullContainerId_ThrowsLaunchFailedException() {
        PhantomBusterResponse launchResponse = new PhantomBusterResponse();
        launchResponse.setContainerId(null);

        given(restTemplate.postForEntity(anyString(), any(), eq(PhantomBusterResponse.class)))
                .willReturn(ResponseEntity.ok(launchResponse));

        assertThatThrownBy(() -> client.searchAlumni(searchRequest))
                .isInstanceOf(PhantomBusterException.class)
                .hasMessageContaining("no container ID");
    }
}
