package com.ensa.projet.trainingservice.repository;

import com.ensa.projet.trainingservice.model.entities.Paragraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParagraphRepository extends JpaRepository<Paragraph, Long> {
}
