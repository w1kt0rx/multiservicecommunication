package com.example.test.client;

import com.example.test.dto.response.GithubResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@EnableWireMock
public class GithubClientTest {

    @Autowired
    private GithubClient githubClient;

    @Test
    void getRepoByOwnerAndRepoName_serviceReturns200_repositoryReturnedSuccessfully() {
        //given
        String owner = "octocat";
        String repo = "Hello-World";
        stubFor(get(urlEqualTo("/repos/" + owner + "/" + repo))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("github-repository-response.json")));

        //when
        GithubResponse response = githubClient.getRepoByOwnerAndRepoName(owner, repo);

        //then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("Hello-World", response.name()),
                () -> assertEquals("octocat/Hello-World", response.full_name()),
                () -> assertEquals("This is your first repo!", response.description()),
                () -> assertEquals("https://github.com/octocat/Hello-World.git", response.clone_url()),
                () -> assertEquals(1337, response.stargazers_count()),
                () -> assertEquals("2011-01-26T19:01:12Z", response.created_at()),
                () -> assertNotNull(response.ownerDto()),
                () -> assertEquals("octocat", response.ownerDto().login())
        );
        verify(1, getRequestedFor(urlEqualTo("/repos/" + owner + "/" + repo)));
    }

    @Test
    void getRepoByOwnerAndRepoName_serviceReturns503Consistently_retryExhaustedAndFallbackReturned() {
        //given
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get(urlEqualTo("/repos/" + owner + "/" + repo))
                .willReturn(aResponse().withStatus(503)));

        //when
        GithubResponse response = githubClient.getRepoByOwnerAndRepoName(owner, repo);

        //then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(repo, response.name()),
                () -> assertEquals(owner + "/" + repo, response.full_name()),
                () -> assertEquals("Fallback response, service unavailable", response.description()),
                () -> assertEquals("", response.clone_url()),
                () -> assertEquals(0, response.stargazers_count()),
                () -> assertEquals("", response.created_at()),
                () -> assertNotNull(response.ownerDto()),
                () -> assertEquals("", response.ownerDto().login())
        );
        verify(5, getRequestedFor(urlEqualTo("/repos/" + owner + "/" + repo)));
    }

    @Test
    void getRepoByOwnerAndRepoName_serviceReturns500InternalServerError_fallbackExecutedImmediately() {
        //given
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get(urlEqualTo("/repos/" + owner + "/" + repo))
                .willReturn(aResponse().withStatus(500)));

        //when
        GithubResponse response = githubClient.getRepoByOwnerAndRepoName(owner, repo);

        //then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(repo, response.name()),
                () -> assertEquals(owner + "/" + repo, response.full_name()),
                () -> assertEquals("Fallback response, service unavailable", response.description()),
                () -> assertEquals("", response.clone_url()),
                () -> assertEquals(0, response.stargazers_count()),
                () -> assertEquals("", response.created_at()),
                () -> assertNotNull(response.ownerDto()),
                () -> assertEquals("", response.ownerDto().login())
        );
        verify(1, getRequestedFor(urlEqualTo("/repos/" + owner + "/" + repo)));
    }

    @Test
    void getRepoByOwnerAndRepoName_serviceReturns404NotFound_fallbackExecutedImmediately() {
        //given
        String owner = "octocat";
        String repo = "Non-Existing-Repo";

        stubFor(get(urlEqualTo("/repos/" + owner + "/" + repo))
                .willReturn(aResponse().withStatus(404)));

        //when
        GithubResponse response = githubClient.getRepoByOwnerAndRepoName(owner, repo);

        //then
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(repo, response.name()),
                () -> assertEquals(owner + "/" + repo, response.full_name()),
                () -> assertEquals("Fallback response, service unavailable", response.description()),
                () -> assertEquals("", response.clone_url()),
                () -> assertEquals(0, response.stargazers_count()),
                () -> assertEquals("", response.created_at()),
                () -> assertNotNull(response.ownerDto()),
                () -> assertEquals("", response.ownerDto().login())
        );
        verify(1, getRequestedFor(urlEqualTo("/repos/" + owner + "/" + repo)));
    }
}
