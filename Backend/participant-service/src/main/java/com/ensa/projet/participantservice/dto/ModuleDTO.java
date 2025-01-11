package com.ensa.projet.participantservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class ModuleDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer orderIndex;
}
