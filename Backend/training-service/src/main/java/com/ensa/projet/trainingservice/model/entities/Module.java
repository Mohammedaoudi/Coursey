package com.ensa.projet.trainingservice.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex")
    @Builder.Default
    private List<Content> contents = new ArrayList<>();

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quiz> moduleQuizzes = new ArrayList<>();

    private boolean isFinal;
}