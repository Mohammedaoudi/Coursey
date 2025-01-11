package com.ensa.projet.participantservice.controller;

import com.ensa.projet.participantservice.dto.EnrollmentDto;
import com.ensa.projet.participantservice.service.interfaces.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private static final String GENERIC_ERROR_MESSAGE = "An error occurred: ";

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentDto> enrollInTraining(
            @RequestParam Integer participantId,
            @RequestParam Integer trainingId) {
        try {
            EnrollmentDto enrollment = enrollmentService.enrollInTraining(participantId, trainingId);
            return ResponseEntity.ok(enrollment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<EnrollmentDto> getEnrollmentById(@PathVariable Integer enrollmentId) {
        try {
            EnrollmentDto enrollmentDto = enrollmentService.findEnrollemntById(enrollmentId);
            return ResponseEntity.ok(enrollmentDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{enrollmentId}/modules/{moduleId}/complete")
    public ResponseEntity<String> markModuleComplete(
            @PathVariable Integer enrollmentId,
            @PathVariable Integer moduleId) {
        try {
            enrollmentService.markModuleComplete(enrollmentId, moduleId);
            return ResponseEntity.ok().body("Module marked as complete");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GENERIC_ERROR_MESSAGE + e.getMessage());
        }
    }

    @GetMapping("/check")
    public ResponseEntity<EnrollmentDto> checkEnrollment(
            @RequestParam Integer participantId,
            @RequestParam Integer trainingId) {
        return ResponseEntity.ok(enrollmentService.getEnrollment(participantId, trainingId));
    }

    @GetMapping("/participant/{participantId}")
    public ResponseEntity<Object> getParticipantEnrollments(@PathVariable Integer participantId) {
        try {
            List<EnrollmentDto> enrollments = enrollmentService.getParticipantEnrollments(participantId);
            if (enrollments.isEmpty()) {
                return ResponseEntity.ok("No enrollments found for participant: " + participantId);
            }
            return ResponseEntity.ok(enrollments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GENERIC_ERROR_MESSAGE + e.getMessage());
        }
    }
}