package com.ensa.projet.trainingservice.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Entity
@Table(name = "Quiz")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id")
    private Training training;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private Module module;

    @Column(nullable = false)
    private String question;

    @ElementCollection
    @CollectionTable(name = "quiz_options",
            joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "option_text")
    private List<String> options;

    @Column(nullable = false)
    private Integer correctAnswerIndex;


    @Column(name = "final_quiz")
    private boolean isFinalQuiz;
}