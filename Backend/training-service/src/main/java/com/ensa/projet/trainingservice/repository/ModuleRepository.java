package com.ensa.projet.trainingservice.repository;
import com.ensa.projet.trainingservice.model.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ModuleRepository extends JpaRepository<Module, Integer> {

}
