package com.ensa.projet.participantservice.dto;

import lombok.Data;

@Data
public class QuizDTO {
    private Integer id;
    private String question;
    private String[] options;
    private Integer correctAnswerIndex;
    private Integer moduleId;
    private Integer trainingId;
    private boolean finalQuiz;
}