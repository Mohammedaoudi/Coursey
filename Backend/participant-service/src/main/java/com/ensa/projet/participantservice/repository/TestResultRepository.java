package com.ensa.projet.participantservice.repository;

import com.ensa.projet.participantservice.entities.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Integer> {
    List<TestResult> findByParticipantId(Integer participantId);
}
