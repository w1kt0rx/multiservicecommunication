package com.example.test.model;

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
}
