package com.ensa.projet.trainingservice.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParagraphDTO {
    private Integer id;
    private String title;
    private String description;
    private String image;
}