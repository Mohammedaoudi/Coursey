package com.ensa.projet.trainingservice.service.implemnt;


import com.ensa.projet.trainingservice.exception.ResourceNotFoundException;
import com.ensa.projet.trainingservice.model.dao.QuizDTO;
import com.ensa.projet.trainingservice.model.entities.Quiz;
import com.ensa.projet.trainingservice.model.entities.Module;
import com.ensa.projet.trainingservice.model.entities.Training;
import com.ensa.projet.trainingservice.repository.ModuleRepository;
import com.ensa.projet.trainingservice.repository.QuizRepository;
import com.ensa.projet.trainingservice.repository.TrainingRepository;
import com.ensa.projet.trainingservice.service.interfaces.QuizService;

import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final ModuleRepository moduleRepository;
    private final TrainingRepository trainingRepository;

    public QuizServiceImpl(QuizRepository quizRepository,
                           ModuleRepository moduleRepository,
                           TrainingRepository trainingRepository) {
        this.quizRepository = quizRepository;
        this.moduleRepository = moduleRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    public List<QuizDTO> getQuizzesByTrainingId(Integer trainingId) {
        return quizRepository.findByTrainingIdAndIsFinalQuizTrue(trainingId)
                .stream()
                .map(this::convertToQuizDTO)
                .toList(); // Produces an unmodifiable list
    }


    @Override
    public List<QuizDTO> getQuizzesByModuleId(Integer moduleId) {
        return quizRepository.findByModuleId(moduleId)
                .stream()
                .map(this::convertToQuizDTO)
                .toList();
    }

    @Override
    public List<QuizDTO> getFinalQuizzesByTrainingId(Integer trainingId) {
        return quizRepository.findByTrainingIdAndIsFinalQuizTrue(trainingId)
                .stream()
                .map(this::convertToQuizDTO)
                .toList();
    }

    @Override
    public QuizDTO createQuiz(QuizDTO quizDTO) {
        Quiz quiz = new Quiz();
        quiz.setQuestion(quizDTO.getQuestion());
        quiz.setOptions(quizDTO.getOptions());
        quiz.setCorrectAnswerIndex(quizDTO.getCorrectAnswerIndex());
        quiz.setFinalQuiz(quizDTO.isFinalQuiz());

        if (quizDTO.getModuleId() != null) {
            Module module = moduleRepository.findById(quizDTO.getModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
            quiz.setModule(module);
        }

        if (quizDTO.getTrainingId() != null) {
            Training training = trainingRepository.findById(quizDTO.getTrainingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Training not found"));
            quiz.setTraining(training);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToQuizDTO(savedQuiz);
    }

    @Override
    public QuizDTO updateQuiz(Integer id, QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        quiz.setQuestion(quizDTO.getQuestion());
        quiz.setOptions(quizDTO.getOptions());
        quiz.setCorrectAnswerIndex(quizDTO.getCorrectAnswerIndex());

        Quiz updatedQuiz = quizRepository.save(quiz);
        return convertToQuizDTO(updatedQuiz);
    }

    @Override
    public void deleteQuiz(Integer id) {
        if (!quizRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quiz not found");
        }
        quizRepository.deleteById(id);
    }

    private QuizDTO convertToQuizDTO(Quiz quiz) {
        return QuizDTO.builder()
                .id(quiz.getId())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .correctAnswerIndex(quiz.getCorrectAnswerIndex())
                .isFinalQuiz(quiz.isFinalQuiz())
                .moduleId(quiz.getModule() != null ? quiz.getModule().getId() : null)
                .trainingId(quiz.getTraining() != null ? quiz.getTraining().getId() : null)
                .build();
    }
}