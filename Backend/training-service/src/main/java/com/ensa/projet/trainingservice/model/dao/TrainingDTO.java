package com.ensa.projet.trainingservice.model.dao;

import com.ensa.projet.trainingservice.model.entities.DifficultyLevel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {
    private Integer id;
    private String title;
    private String description;
    private String iconPath;

    private DifficultyLevel difficultyLevel;
    private Integer estimatedDurationMinutes;
    private String goals;
    private String prerequisites;
    private boolean supportAR;
    private boolean supportAI;

    private String urlYtb;
    private Integer categoryId;
    private String categoryName;
    private List<String> instructions;
    private List<ModuleDTO> modules;
    private List<QuizDTO> quizzes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean published;
}