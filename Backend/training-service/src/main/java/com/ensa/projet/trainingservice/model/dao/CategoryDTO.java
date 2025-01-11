package com.ensa.projet.trainingservice.model.dao;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer id;
    private String name;
    private String description;
    private String iconPath;
    private List<TrainingDTO> trainings;
}
