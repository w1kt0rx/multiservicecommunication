package com.example.test.service;

import com.example.test.dto.response.GithubResponse;
import feign.Client;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class GithubServiceRetryTest {

    @Autowired
    private GithubService githubService;

    @MockitoBean
    private Client feignClient;


    @Test
    void getRepository_serviceReturns503OnFirstAttempt_retryExecutedAndRepositoryReturned() throws IOException {
        //given
        String owner = "octocat";
        String repo = "Hello-World";
        String jsonResponse = """
                {
                    "owner": {
                        "login": "octocat"
                    },
                    "name": "Hello-World",
                    "full_name": "octocat/Hello-World",
                    "description": "This is your first repo!",
                    "clone_url": "https://github.com/octocat/Hello-World.git",
                    "stargazers_count": 1337,
                    "created_at": "2011-01-26T19:01:12Z"
                }
                """;

        Response response503 = buildResponse(503, "Service Unavailable");
        Response response200 = buildResponse(200, jsonResponse);

        when(feignClient.execute(any(Request.class), any(Request.Options.class)))
                .thenReturn(response503)
                .thenReturn(response503)
                .thenReturn(response200);

        //when
        GithubResponse result = githubService.getRepository(owner, repo);

        //then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.name()).isEqualTo("Hello-World"),
                () -> assertThat(result.full_name()).isEqualTo("octocat/Hello-World"),
                () -> assertThat(result.description()).isEqualTo("This is your first repo!"),
                () -> assertThat(result.clone_url()).isEqualTo("https://github.com/octocat/Hello-World.git"),
                () -> assertThat(result.stargazers_count()).isEqualTo(1337),
                () -> assertThat(result.created_at()).isEqualTo("2011-01-26T19:01:12Z"),
                () -> assertThat(result.ownerDto()).isNotNull(),
                () -> verify(feignClient, times(3)).execute(any(Request.class), any(Request.Options.class))
        );
    }

    private Response buildResponse(int status, String body) {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("application/json"));

        return Response.builder()
                .status(status)
                .reason("Reason")
                .request(Request.create(Request.HttpMethod.GET, "/repos/octocat/Hello-World", Collections.emptyMap(), null, null, null))
                .headers(headers)
                .body(body, StandardCharsets.UTF_8)
                .build();
    }
}