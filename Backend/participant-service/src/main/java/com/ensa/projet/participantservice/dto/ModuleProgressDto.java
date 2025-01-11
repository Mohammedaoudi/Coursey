package com.ensa.projet.participantservice.dto;


import com.ensa.projet.participantservice.entities.ModuleStatus;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class ModuleProgressDto {

    private Integer id;
    private ModuleStatus status;
    private String moduleName;
    private Integer moduleId;



}
