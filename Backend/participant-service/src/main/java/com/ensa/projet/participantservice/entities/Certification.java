package com.ensa.projet.participantservice.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "certifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

    private Integer trainingId;
    private String certificateNumber;
    private LocalDateTime issueDate;
    private LocalDateTime expiryDate;
    private float finalScore;

    @Column(name = "skills_acquired")
    @ElementCollection
    private List<String> skillsAcquired = new ArrayList<>();

    private String certificateUrl;
    private boolean active;
}
