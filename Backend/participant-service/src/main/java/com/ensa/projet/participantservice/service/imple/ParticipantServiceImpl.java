package com.ensa.projet.participantservice.service.imple;

import com.ensa.projet.participantservice.dto.*;
import com.ensa.projet.participantservice.entities.Certification;

import com.ensa.projet.participantservice.entities.Participant;
import com.ensa.projet.participantservice.repository.*;
import com.ensa.projet.participantservice.service.interfaces.ParticipantService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
public class ParticipantServiceImpl implements ParticipantService {


    private final ParticipantRepository participantRepository;

    public ParticipantServiceImpl(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    @Override
    public Participant createParticipant(String userId, KeycloakUserInfo userInfo) {
        // Check if participant already exists with this userId
        if (participantRepository.findByUserId(userId) != null) {
            throw new IllegalStateException("Participant already exists with userId: " + userId);
        }

        // Create new participant
        Participant participant = Participant.builder()
                .userId(userId)
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .email(userInfo.getEmail())
                .enrollments(new ArrayList<>())
                .certifications(new ArrayList<>())
                .build();

        // Save and return the new participant
        return participantRepository.save(participant);
    }

    @Override
    public Participant updateParticipant(Integer id, Participant participant) {
        return null;
    }

    @Override
    public ParticipantDTO getParticipantByUserId(String userId) {
        Participant participant = participantRepository.findByUserId(userId);
        if (participant == null) {
            return null;
        }
        return convertToDTO(participant);
    }

    @Override
    public List<Certification> getParticipantCertifications(Integer participantId) {
        return List.of();
    }

    private ParticipantDTO convertToDTO(Participant participant) {
        return ParticipantDTO.builder()
                .id(participant.getId())
                .userId(participant.getUserId())
                .firstName(participant.getFirstName())
                .lastName(participant.getLastName())
                .email(participant.getEmail())
                .phone(participant.getPhone())
                .address(participant.getAddress())
                .build();
    }
    @Override
    public ParticipantDTO getParticipantById(Integer id) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        // Map Participant to ParticipantDTO
        return ParticipantDTO.builder()
                .id(participant.getId())
                .firstName(participant.getFirstName())
                .lastName(participant.getLastName())
                .email(participant.getEmail())
                .phone(participant.getPhone())
                .address(participant.getAddress())
                .build();
    }

}