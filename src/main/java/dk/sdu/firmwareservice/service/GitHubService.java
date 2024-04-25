package dk.sdu.firmwareservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GitHubService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public GitHubService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getLatestToitRelease() {
        ResponseEntity<String> response = restTemplate.getForEntity("https://api.github.com/repos/toitlang/toit/releases/latest", String.class);
        return extractVersion(response);
    }

    public String getLatestAthenaRelease() {
        ResponseEntity<String> response = restTemplate.getForEntity("https://api.github.com/repos/Helleberg/SensorSync-AthenaContainer/releases/latest", String.class);
        return extractVersion(response);
    }

    private String extractVersion(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            // Deserialize JSON response into ReleaseInfo POJO
            try {
                ReleaseInfo releaseInfo = objectMapper.readValue(response.getBody(), ReleaseInfo.class);
                return releaseInfo.getTagName();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("Could not deserialize release response!");
            }
        } else {
            System.out.println("Request could not be processed!");
            return null;
        }
        return null;
    }

    // Define POJO representing the JSON response
    @Getter
    private static class ReleaseInfo {
        @JsonProperty("tag_name")
        private String tagName;
    }
}
