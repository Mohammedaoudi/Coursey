package com.ensa.projet.trainingservice.model.dao;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer orderIndex;
    private boolean finished;
    private Integer trainingId;

    private List<ContentDTO> contents;
}