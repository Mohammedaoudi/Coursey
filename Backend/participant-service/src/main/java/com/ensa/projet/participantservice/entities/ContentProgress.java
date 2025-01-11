package com.ensa.projet.participantservice.entities;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "content_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "moduleProgress_id")
    private ModuleProgress moduleProgress;

    private Integer contentId;

    private String contentType;


    private boolean completed;





}
