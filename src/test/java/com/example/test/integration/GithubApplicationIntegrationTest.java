package com.example.test.integration;

import com.example.test.dto.OwnerDto;
import com.example.test.dto.response.GithubResponse;
import com.example.test.mapper.RepositoryEntityMapper;
import com.example.test.model.RepositoryEntity;
import com.example.test.repository.RepositoryDetailRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableWireMock
@AutoConfigureMockMvc
public class GithubApplicationIntegrationTest {
    @Autowired
    RepositoryEntityMapper mapper;
    @Autowired
    private RepositoryDetailRepository repositoryDetailRepository;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        repositoryDetailRepository.deleteAll();
        WireMock.reset();
    }

    @Test
    public void get_githubApiIsAvailable_returnsGithubResponse() throws Exception {
        // given
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get(urlEqualTo("/repos/" + owner + "/" + repo))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("github-repository-response.json")));

        // when & then
        mockMvc.perform(get("/owner/{owner}/repository/{repositoryName}", owner, repo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hello-World"))
                .andExpect(jsonPath("$.owner.login").value("octocat"))
                .andExpect(jsonPath("$.full_name").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("This is your first repo!"))
                .andExpect(jsonPath("$.clone_url").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stargazers_count").value(1337))
                .andExpect(jsonPath("$.created_at").value("2011-01-26T19:01:12Z"));
    }

    @Test
    public void get_githubApiReturns503_triggersRetryerAndEventuallyReturnsFallbackResponse() throws Exception {
        // given
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get("/repos/" + owner + "/" + repo)
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));

        // when & then
        mockMvc.perform(get("/owner/{owner}/repository/{repositoryName}", owner, repo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.login").value(""))
                .andExpect(jsonPath("$.name").value("Hello-World"))
                .andExpect(jsonPath("$.full_name").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("Fallback response, service unavailable"))
                .andExpect(jsonPath("$.clone_url").value(""))
                .andExpect(jsonPath("$.stargazers_count").value(0))
                .andExpect(jsonPath("$.created_at").value(""));
    }

    @Test
    public void saveRepository_githubApiIsAvailable_savesEntityToDatabaseAndReturnsCreated() throws Exception {
        // given
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get("/repos/" + owner + "/" + repo)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("github-repository-response.json")));

        // when
        mockMvc.perform(post("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.repositoryName").value("Hello-World"))
                .andExpect(jsonPath("$.owner").value("octocat"))
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("This is your first repo!"))
                .andExpect(jsonPath("$.cloneUrl").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stars").value(1337))
                .andExpect(jsonPath("$.createdAt").value("2011-01-26T19:01:12Z"));

        // then
        List<RepositoryEntity> entities = repositoryDetailRepository.findAll();

        Assertions.assertAll(() -> {
            Assertions.assertEquals(1, entities.size());
            assertThat(entities.getFirst().getId()).isNotNull().isPositive();
            Assertions.assertEquals("Hello-World", entities.getFirst().getRepositoryName());
            Assertions.assertEquals("octocat", entities.getFirst().getOwner());
            Assertions.assertEquals("octocat/Hello-World", entities.getFirst().getFullName());
            Assertions.assertEquals("This is your first repo!", entities.getFirst().getDescription());
            Assertions.assertEquals("https://github.com/octocat/Hello-World.git", entities.getFirst().getCloneUrl());
            Assertions.assertEquals(1337, entities.getFirst().getStars());
            Assertions.assertEquals("2011-01-26T19:01:12Z", entities.getFirst().getCreatedAt());
        });
    }

    @Test
    public void saveRepository_githubApiIsUnavailable_savesFallbackEntityToDatabase() throws Exception {
        // given
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get("/repos/" + owner + "/" + repo)
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));

        // when
        mockMvc.perform(post("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.repositoryName").value("Hello-World"))
                .andExpect(jsonPath("$.owner").value(""))
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("Fallback response, service unavailable"))
                .andExpect(jsonPath("$.cloneUrl").value(""))
                .andExpect(jsonPath("$.stars").value(0))
                .andExpect(jsonPath("$.createdAt").value(""));

        // then
        List<RepositoryEntity> entities = repositoryDetailRepository.findAll();

        Assertions.assertAll(() -> {
            Assertions.assertEquals(1, entities.size());
            assertThat(entities.getFirst().getId()).isNotNull().isPositive();
            Assertions.assertEquals("Hello-World", entities.getFirst().getRepositoryName());
            Assertions.assertEquals("", entities.getFirst().getOwner());
            Assertions.assertEquals("octocat/Hello-World", entities.getFirst().getFullName());
            Assertions.assertEquals("Fallback response, service unavailable", entities.getFirst().getDescription());
            Assertions.assertEquals("", entities.getFirst().getCloneUrl());
            Assertions.assertEquals(0, entities.getFirst().getStars());
            Assertions.assertEquals("", entities.getFirst().getCreatedAt());
        });
    }

    @Test
    public void getLocalRepository_entityExistsInDatabase_returnsRepositoryResponse() throws Exception {
        // given
        GithubResponse response = new GithubResponse(new OwnerDto("octocat"), "Hello-World", "octocat/Hello-World", "This is your first repo!", "https://github.com/octocat/Hello-World.git", 1337 , "2011-01-26T19:01:12Z");
        repositoryDetailRepository.save(mapper.toEntity(response));
        String owner = "octocat";
        String repo = "Hello-World";

        // when & then
        mockMvc.perform(get("/local/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.repositoryName").value("Hello-World"))
                .andExpect(jsonPath("$.owner").value("octocat"))
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("This is your first repo!"))
                .andExpect(jsonPath("$.cloneUrl").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stars").value(1337))
                .andExpect(jsonPath("$.createdAt").value("2011-01-26T19:01:12Z"));
    }

    @Test
    public void getLocalRepository_entityDoesNotExist_throwsRepositoryEntityNotFoundException() throws Exception {
        // given
        String owner = "nonExistingOwner";
        String repo = "nonExistingRepo";

        // when & then
        mockMvc.perform(get("/local/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Repository entity nonExistingRepo doesn't exists")));
    }

    @Test
    public void updateRepository_entityExistsAndGithubApiAvailable_updatesEntityInDatabase() throws Exception {
        // given
        GithubResponse response = new GithubResponse(new OwnerDto("octocat"), "Hello-World", "octocat/Hello-World", "cos innego", "https://github.blablagit", 1337 , "2011-01-26T19:01:12Z");
        repositoryDetailRepository.save(mapper.toEntity(response));
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get("/repos/" + owner + "/" + repo)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("github-repository-response.json")));

        // when
        mockMvc.perform(put("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.repositoryName").value("Hello-World"))
                .andExpect(jsonPath("$.owner").value("octocat"))
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("This is your first repo!"))
                .andExpect(jsonPath("$.cloneUrl").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stars").value(1337))
                .andExpect(jsonPath("$.createdAt").value("2011-01-26T19:01:12Z"));

        // then
        List<RepositoryEntity> entities = repositoryDetailRepository.findAll();

        Assertions.assertAll(() -> {
            Assertions.assertEquals(1, entities.size());
            assertThat(entities.getFirst().getId()).isNotNull().isPositive();
            Assertions.assertEquals("Hello-World", entities.getFirst().getRepositoryName());
            Assertions.assertEquals("octocat", entities.getFirst().getOwner());
            Assertions.assertEquals("octocat/Hello-World", entities.getFirst().getFullName());
            Assertions.assertEquals("This is your first repo!", entities.getFirst().getDescription());
            Assertions.assertEquals("https://github.com/octocat/Hello-World.git", entities.getFirst().getCloneUrl());
            Assertions.assertEquals(1337, entities.getFirst().getStars());
            Assertions.assertEquals("2011-01-26T19:01:12Z", entities.getFirst().getCreatedAt());
        });
    }

    @Test
    public void updateRepository_entityExistsButGithubApiUnavailable_updatesDatabaseWithFallbackData() throws Exception {
        // given
        GithubResponse response = new GithubResponse(new OwnerDto("octocat"), "Hello-World", "octocat/Hello-World", "cos innego", "https://github.blablagit", 1337 , "2011-01-26T19:01:12Z");
        repositoryDetailRepository.save(mapper.toEntity(response));
        String owner = "octocat";
        String repo = "Hello-World";

        stubFor(get("/repos/" + owner + "/" + repo)
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")));

        // when
        mockMvc.perform(put("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.repositoryName").value("Hello-World"))
                .andExpect(jsonPath("$.owner").value(""))
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("Fallback response, service unavailable"))
                .andExpect(jsonPath("$.cloneUrl").value(""))
                .andExpect(jsonPath("$.stars").value(0))
                .andExpect(jsonPath("$.createdAt").value(""));

        // then
        List<RepositoryEntity> entities = repositoryDetailRepository.findAll();

        Assertions.assertAll(() -> {
            Assertions.assertEquals(1, entities.size());
            assertThat(entities.getFirst().getId()).isNotNull().isPositive();
            Assertions.assertEquals("Hello-World", entities.getFirst().getRepositoryName());
            Assertions.assertEquals("", entities.getFirst().getOwner());
            Assertions.assertEquals("octocat/Hello-World", entities.getFirst().getFullName());
            Assertions.assertEquals("Fallback response, service unavailable", entities.getFirst().getDescription());
            Assertions.assertEquals("", entities.getFirst().getCloneUrl());
            Assertions.assertEquals(0, entities.getFirst().getStars());
            Assertions.assertEquals("", entities.getFirst().getCreatedAt());
        });
    }

    @Test
    public void updateRepository_entityDoesNotExist_throwsRepositoryEntityNotFoundException() throws Exception {
        // given
        String owner = "nonExistingOwner";
        String repo = "nonExistingRepo";

        // when & then
        mockMvc.perform(put("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Repository entity nonExistingRepo doesn't exists")));
    }

    @Test
    public void deleteRepository_entityExistsInDatabase_removesEntityAndReturnsNoContent() throws Exception {
        // given
        GithubResponse response = new GithubResponse(new OwnerDto("octocat"), "Hello-World", "octocat/Hello-World", "cos innego", "https://github.blablagit", 1337 , "2011-01-26T19:01:12Z");
        repositoryDetailRepository.save(mapper.toEntity(response));
        String owner = "octocat";
        String repo = "Hello-World";

        // when
        mockMvc.perform(delete("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isNoContent());

        // then
        List<RepositoryEntity> entities = repositoryDetailRepository.findAll();
        Assertions.assertEquals(0, entities.size());
    }

    @Test
    public void deleteRepository_entityDoesNotExist_throwsRepositoryEntityNotFoundException() throws Exception {
        // given
        String owner = "nonExistingOwner";
        String repo = "nonExistingRepo";

        // when & then
        mockMvc.perform(delete("/repositories/{owner}/{repositoryName}", owner, repo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("Repository entity nonExistingRepo doesn't exists")));
    }
}