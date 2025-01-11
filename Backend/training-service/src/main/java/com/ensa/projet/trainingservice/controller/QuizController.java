package com.ensa.projet.trainingservice.controller;

import com.ensa.projet.trainingservice.model.dao.QuizDTO;
import com.ensa.projet.trainingservice.service.interfaces.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody QuizDTO quizDTO) {
        return new ResponseEntity<>(quizService.createQuiz(quizDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable Integer id, @RequestBody QuizDTO quizDTO) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quizDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Integer id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesByModule(@PathVariable Integer moduleId) {
        return ResponseEntity.ok(quizService.getQuizzesByModuleId(moduleId));
    }

    @GetMapping("/training/{trainingId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesByTraining(@PathVariable Integer trainingId) {
        return ResponseEntity.ok(quizService.getQuizzesByTrainingId(trainingId));
    }

    @GetMapping("/training/{trainingId}/final")
    public ResponseEntity<List<QuizDTO>> getFinalQuizzesByTraining(@PathVariable Integer trainingId) {
        return ResponseEntity.ok(quizService.getFinalQuizzesByTrainingId(trainingId));
    }
}