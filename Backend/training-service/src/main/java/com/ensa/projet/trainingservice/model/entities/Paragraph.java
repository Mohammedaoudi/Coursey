package com.ensa.projet.trainingservice.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "paragraphs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paragraph {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String image;

    @ManyToOne
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
}
