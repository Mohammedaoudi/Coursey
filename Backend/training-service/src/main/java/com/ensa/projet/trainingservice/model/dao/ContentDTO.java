package com.ensa.projet.trainingservice.model.dao;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDTO {
    private Integer id;
    private String title;
    private String description;

    private String url;
    private Integer orderIndex;
}
