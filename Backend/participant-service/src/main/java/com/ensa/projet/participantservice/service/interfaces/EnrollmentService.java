package com.ensa.projet.participantservice.service.interfaces;

import com.ensa.projet.participantservice.dto.EnrollmentDto;


import java.util.List;

public interface EnrollmentService {
    EnrollmentDto enrollInTraining(Integer participantId, Integer trainingId);
    EnrollmentDto getEnrollment(Integer participantId, Integer trainingId );
    List<EnrollmentDto> getParticipantEnrollments(Integer participantId);
    void markModuleComplete(Integer enrollmentId, Integer moduleId);
    EnrollmentDto findEnrollemntById(Integer enrollmentId);

}