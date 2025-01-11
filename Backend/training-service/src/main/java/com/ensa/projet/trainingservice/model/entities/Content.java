package com.ensa.projet.trainingservice.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;


    private String url;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Paragraph> paragraphs = new ArrayList<>();
}

