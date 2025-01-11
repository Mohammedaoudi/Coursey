package com.ensa.projet.participantservice.service.interfaces;

import com.ensa.projet.participantservice.dto.TestResultSubmissionDTO;
import com.ensa.projet.participantservice.entities.TestResult;


import java.util.List;

public interface TestResultService {

    TestResult submitTestResult(TestResultSubmissionDTO submission);

    // Récupérer les résultats de tests par participant ID
    List<TestResult> getTestResultsByParticipantId(Integer participantId) ;




}
