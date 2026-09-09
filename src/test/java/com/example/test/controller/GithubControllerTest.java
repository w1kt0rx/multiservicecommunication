package com.example.test.controller;

import com.example.test.dto.OwnerDto;
import com.example.test.dto.response.GithubResponse;
import com.example.test.dto.response.RepositoryResponse;
import com.example.test.exception.RepositoryEntityNotFoundException;
import com.example.test.service.GithubService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GithubControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    GithubService githubService;

    @Test
    void get_dataCorrect_returnsGithubResponse() throws Exception {
        //given
        String owner = "octocat";
        String repoName = "Hello-World";
        GithubResponse githubResponse = new GithubResponse(
                new OwnerDto("octocat"),
                "Hello-World",
                "octocat/Hello-World",
                "My first repo",
                "https://github.com/octocat/Hello-World.git",
                42,
                "2011-01-26T19:01:12Z"
        );
        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(githubService.getRepository(anyString(), anyString())).thenReturn(githubResponse);

        //when + then
        mockMvc.perform(get("/owner/{owner}/repository/{repositoryName}", owner, repoName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hello-World"))
                .andExpect(jsonPath("$.full_name").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("My first repo"))
                .andExpect(jsonPath("$.clone_url").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stargazers_count").value(42))
                .andExpect(jsonPath("$.created_at").value("2011-01-26T19:01:12Z"));

        Mockito.verify(githubService).getRepository(ownerCaptor.capture(), repoNameCaptor.capture());
        Assertions.assertAll(
                () -> Assertions.assertEquals("octocat", ownerCaptor.getValue()),
                () -> Assertions.assertEquals("Hello-World", repoNameCaptor.getValue())
        );
    }

    @Test
    void saveRepository_dataCorrect_returnsCreatedRepositoryResponse() throws Exception {
        //given
        String owner = "octocat";
        String repoName = "Hello-World";
        RepositoryResponse repositoryResponse = new RepositoryResponse(
                1L,
                owner,
                repoName,
                "octocat/Hello-World",
                "My first repo",
                "https://github.com/octocat/Hello-World.git",
                42,
                "2011-01-26T19:01:12Z"
        );
        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(githubService.saveRepositoryDetails(anyString(), anyString())).thenReturn(repositoryResponse);

        //when + then
        mockMvc.perform(post("/repositories/{owner}/{repositoryName}", owner, repoName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("My first repo"))
                .andExpect(jsonPath("$.cloneUrl").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stars").value(42))
                .andExpect(jsonPath("$.createdAt").value("2011-01-26T19:01:12Z"));

        Mockito.verify(githubService).saveRepositoryDetails(ownerCaptor.capture(), repoNameCaptor.capture());
        Assertions.assertAll(
                () -> Assertions.assertEquals("octocat", ownerCaptor.getValue()),
                () -> Assertions.assertEquals("Hello-World", repoNameCaptor.getValue())
        );
    }

    @Test
    void getLocalRepository_repositoryExists_returnsRepositoryResponse() throws Exception {
        //given
        String owner = "octocat";
        String repoName = "Hello-World";
        RepositoryResponse repositoryResponse = new RepositoryResponse(
                1L,
                owner,
                repoName,
                "octocat/Hello-World",
                "My first repo",
                "https://github.com/octocat/Hello-World.git",
                42,
                "2011-01-26T19:01:12Z"
        );
        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(githubService.getLocalRepositoryDetails(anyString(), anyString())).thenReturn(repositoryResponse);

        //when + then
        mockMvc.perform(get("/local/repositories/{owner}/{repositoryName}", owner, repoName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("octocat/Hello-World"))
                .andExpect(jsonPath("$.description").value("My first repo"))
                .andExpect(jsonPath("$.cloneUrl").value("https://github.com/octocat/Hello-World.git"))
                .andExpect(jsonPath("$.stars").value(42))
                .andExpect(jsonPath("$.createdAt").value("2011-01-26T19:01:12Z"));

        Mockito.verify(githubService).getLocalRepositoryDetails(ownerCaptor.capture(), repoNameCaptor.capture());
        Assertions.assertAll(
                () -> Assertions.assertEquals("octocat", ownerCaptor.getValue()),
                () -> Assertions.assertEquals("Hello-World", repoNameCaptor.getValue())
        );
    }

    @Test
    void getLocalRepository_repositoryNotExists_returnsNotFoundWithMessage() throws Exception {
        //given
        String owner = "octocat";
        String repoName = "Non-Existing-Repo";
        ArgumentCaptor<String> ownerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> repoNameCaptor = ArgumentCaptor.forClass(String.class);

        when(githubService.getLocalRepositoryDetails(anyString(), anyString()))
                .thenThrow(new RepositoryEntityNotFoundException(repoName));

        //when + then
        mockMvc.perform(get("/local/repositories/{owner}/{repositoryName}", owner, repoName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", Matchers.containsString("Repository entity Non-Existing-Repo doesn't exists")));

        Mockito.verify(githubService).getLocalRepositoryDetails(ownerCaptor.capture(), repoNameCaptor.capture());
        Assertions.assertAll(
                () -> Assertions.assertEquals("octocat", ownerCaptor.getValue()),
                () -> Assertions.assertEquals("Non-Existing-Repo", repoNameCaptor.getValue())
        );
    }
}