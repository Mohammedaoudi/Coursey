package com.ensa.projet.participantservice.entities;

import com.ensa.projet.participantservice.dto.TrainingDTO;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_results")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "participant_id")
    private Integer participantId;

    private Integer trainingId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "test_result_id")
    @Builder.Default
    private List<ParticipantAnswer> userAnswers = new ArrayList<>();

    private float score;
    private LocalDateTime submissionDate;
    private boolean passed;

    @Transient
    private TrainingDTO trainingDTO;
    public void setTrainingDTO(TrainingDTO trainingDTO) {
        this.trainingDTO = trainingDTO;
    }

    public Integer getParticipantId() {
        return participantId;
    }
}