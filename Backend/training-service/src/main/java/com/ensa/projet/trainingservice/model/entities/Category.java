package com.ensa.projet.trainingservice.model.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    private String iconPath;

    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
    @Builder.Default
    private List<Training> trainings = new ArrayList<>();
}