package com.ensa.projet.participantservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingDTO {
    private Integer id;
    private String title;
    private String description;
    private String iconPath;
    private String difficultyLevel;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean published;
}