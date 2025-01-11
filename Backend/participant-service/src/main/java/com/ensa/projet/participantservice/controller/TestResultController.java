package com.ensa.projet.participantservice.controller;
import com.ensa.projet.participantservice.dto.TestResultSubmissionDTO;
import com.ensa.projet.participantservice.entities.TestResult;

import com.ensa.projet.participantservice.service.interfaces.TestResultService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-results")

public class TestResultController {

    private final TestResultService testResultService;

    public TestResultController(TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    // Endpoint pour obtenir les résultats de tests par ID de participant
    @GetMapping("/participant/{participantId}")
    public ResponseEntity<List<TestResult>> getTestResultsByParticipantId(@PathVariable Integer participantId) {
        List<TestResult> testResults = testResultService.getTestResultsByParticipantId(participantId);
        if (!testResults.isEmpty()) {
            return ResponseEntity.ok(testResults);
        } else {
            return ResponseEntity.noContent().build(); // 204 No Content si aucun résultat trouvé
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<TestResult> submitTestResult(@RequestBody TestResultSubmissionDTO submission) {
        TestResult result = testResultService.submitTestResult(submission);
        return ResponseEntity.ok(result);
    }
}
