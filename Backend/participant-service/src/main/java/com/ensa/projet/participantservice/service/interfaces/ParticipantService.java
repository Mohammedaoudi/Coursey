package com.ensa.projet.participantservice.service.interfaces;

import com.ensa.projet.participantservice.dto.KeycloakUserInfo;
import com.ensa.projet.participantservice.dto.ParticipantDTO;
import com.ensa.projet.participantservice.entities.Certification;
import com.ensa.projet.participantservice.entities.Participant;

import java.util.List;



public interface ParticipantService {
    Participant createParticipant(String userId, KeycloakUserInfo userInfo);
    Participant updateParticipant(Integer id, Participant participant);
    ParticipantDTO getParticipantByUserId(String userId);
    List<Certification> getParticipantCertifications(Integer participantId);
    ParticipantDTO getParticipantById(Integer id);



}