package com.ensa.projet.participantservice.controller;

import com.ensa.projet.participantservice.dto.KeycloakUserInfo;
import com.ensa.projet.participantservice.dto.ParticipantDTO;
import com.ensa.projet.participantservice.entities.Participant;
import com.ensa.projet.participantservice.service.interfaces.ParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/participants")
public class ParticipantController {


    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping
    public ResponseEntity<Participant> createParticipant(
            @RequestParam String userId,
            @RequestBody KeycloakUserInfo userInfo) {
        try {
            Participant participant = participantService.createParticipant(userId, userInfo);
            return ResponseEntity.ok(participant);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            // Consider creating a constant for error messages if not already defined
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ParticipantDTO> getParticipantByUserId(@PathVariable String userId) {
        ParticipantDTO participant = participantService.getParticipantByUserId(userId);
        if (participant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(participant);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDTO> getParticipantById(@PathVariable Integer id) {
        ParticipantDTO participant = participantService.getParticipantById(id);
        return ResponseEntity.ok(participant);
    }


}
