package com.ensa.projet.participantservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class TestResultSubmissionDTO {
    private Integer participantId;
    private Integer trainingId;
    private Integer moduleId;
    private List<ParticipantAnswerDTO> answers;
}