package com.example.test.model;

import com.example.test.dto.response.GithubResponse;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner;
    private String repositoryName;
    private String fullName;
    private String description;
    private String cloneUrl;
    private int stars;
    private String createdAt;

    public RepositoryEntity update(GithubResponse response) {
        this.setFullName(response.full_name());
        this.setOwner(response.ownerDto().login());
        this.setRepositoryName(response.name());
        this.setDescription(response.description());
        this.setCloneUrl(response.clone_url());
        this.setStars(response.stargazers_count());
        this.setCreatedAt(response.created_at());
        return this;
    }
}
