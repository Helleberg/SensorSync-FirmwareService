package dk.sdu.firmwareservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public String getLatestRelease(String repo) throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity("https://api.github.com/repos/" + repo + "/releases/latest", String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            // Deserialize JSON response into ReleaseInfo POJO
            ReleaseInfo releaseInfo = objectMapper.readValue(response.getBody(), ReleaseInfo.class);
            // Extract and return the tag name
            return releaseInfo.getTagName();
        } else {
            //TODO: Handle error
            return null;
        }
    }

    // Define POJO representing the JSON response
    private static class ReleaseInfo {
        @JsonProperty("tag_name")
        private String tagName;

        public String getTagName() {
            return tagName;
        }
    }
}
