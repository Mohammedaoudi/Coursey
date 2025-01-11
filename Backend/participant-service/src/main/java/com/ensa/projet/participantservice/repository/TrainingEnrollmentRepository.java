package com.ensa.projet.participantservice.repository;

import com.ensa.projet.participantservice.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TrainingEnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    Enrollment findByParticipantIdAndTrainingId(Integer participantId, Integer trainingId);
}