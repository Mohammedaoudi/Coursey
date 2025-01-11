package com.ensa.projet.participantservice.dto;

import com.ensa.projet.participantservice.entities.EnrollmentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class EnrollmentDto {

    private Integer id;
    private Integer participantId;
    private Integer trainingId;
    private Date enrollmentDate;
    private EnrollmentStatus status;
    private List<ModuleProgressDto> moduleProgresses = new ArrayList<>();
}
