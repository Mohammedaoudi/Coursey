package com.ensa.projet.trainingservice.service.interfaces;

import com.ensa.projet.trainingservice.model.dao.QuizDTO;


import java.util.List;
public interface QuizService {
    List<QuizDTO> getQuizzesByTrainingId(Integer trainingId);
    List<QuizDTO> getQuizzesByModuleId(Integer moduleId);
    QuizDTO createQuiz(QuizDTO quizDTO);
    QuizDTO updateQuiz(Integer id, QuizDTO quizDTO);
    void deleteQuiz(Integer id);
    List<QuizDTO> getFinalQuizzesByTrainingId(Integer trainingId);
}