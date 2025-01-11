package com.ensa.projet.trainingservice.repository;


import com.ensa.projet.trainingservice.model.entities.Content;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContentRepository extends JpaRepository<Content, Integer> {

}