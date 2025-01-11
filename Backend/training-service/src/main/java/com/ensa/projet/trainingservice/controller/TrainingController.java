package com.ensa.projet.trainingservice.controller;


import com.ensa.projet.trainingservice.model.dao.ModuleDTO;

import com.ensa.projet.trainingservice.model.dao.TrainingDTO;
import com.ensa.projet.trainingservice.service.interfaces.TrainingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    public ResponseEntity<TrainingDTO> createTraining(@RequestBody TrainingDTO trainingDTO) {
        TrainingDTO created = trainingService.createTraining(trainingDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingDTO> updateTraining(@PathVariable Integer id, @RequestBody TrainingDTO trainingDTO) {
        TrainingDTO updated = trainingService.updateTraining(id, trainingDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Integer id) {
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingDTO> getTraining(@PathVariable Integer id) {
        TrainingDTO training = trainingService.getTrainingById(id);
        return ResponseEntity.ok(training);
    }

    @GetMapping
    public ResponseEntity<List<TrainingDTO>> getAllTrainings() {
        List<TrainingDTO> trainings = trainingService.getAllTrainings();
        return ResponseEntity.ok(trainings);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TrainingDTO>> getTrainingsByCategory(@PathVariable Integer categoryId) {
        List<TrainingDTO> trainings = trainingService.getTrainingsByCategory(categoryId);
        return ResponseEntity.ok(trainings);
    }

    @PostMapping("/{id}/modules")
    public ResponseEntity<ModuleDTO> addModule(@PathVariable Integer id, @RequestBody ModuleDTO moduleDTO) {
        ModuleDTO added = trainingService.addModule(id, moduleDTO);
        return new ResponseEntity<>(added, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishTraining(@PathVariable Integer id) {
        trainingService.publishTraining(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Void> unpublishTraining(@PathVariable Integer id) {
        trainingService.unpublishTraining(id);
        return ResponseEntity.ok().build();
    }
}