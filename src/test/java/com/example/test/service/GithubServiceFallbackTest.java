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

@SpringBootTest()
class GithubServiceFallbackTest {

    @Autowired
    private GithubService githubService;

    @MockitoBean
    private Client feignClient;

    @Test
    void getRepository_serviceAlwaysReturns503_fallbackTriggeredAndDefaultRepositoryReturned() throws IOException {
        //given
        String owner = "octocat";
        String repo = "Hello-World";

        Response response503 = buildResponse(503, "Service Unavailable");

        when(feignClient.execute(any(Request.class), any(Request.Options.class)))
                .thenReturn(response503);

        //when
        GithubResponse result = githubService.getRepository(owner, repo);

        //then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.description()).isEqualTo("Fallback response, service unavailable"),
                () -> assertThat(result.stargazers_count()).isEqualTo(0),
                () -> assertThat(result.ownerDto().login()).isEqualTo(""),
                () -> verify(feignClient, times(5)).execute(any(Request.class), any(Request.Options.class))
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