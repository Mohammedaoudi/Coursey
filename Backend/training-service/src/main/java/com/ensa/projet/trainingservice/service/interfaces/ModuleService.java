package com.ensa.projet.trainingservice.service.interfaces;


import com.ensa.projet.trainingservice.model.dao.ContentDTO;
import com.ensa.projet.trainingservice.model.dao.ModuleDTO;
import com.ensa.projet.trainingservice.model.dao.ParagraphDTO;
import com.ensa.projet.trainingservice.model.dao.QuizDTO;
import com.ensa.projet.trainingservice.model.entities.Content;

import com.ensa.projet.trainingservice.model.entities.Paragraph;
import com.ensa.projet.trainingservice.model.entities.Quiz;

import java.util.List;

public interface ModuleService {
    ModuleDTO addContentToModule(Integer moduleId, Content content);
    ModuleDTO addQuizToModule(Integer moduleId, Quiz quiz);
    ContentDTO addParagraphToContent(Integer contentId, Paragraph paragraph);
    ModuleDTO findByModuleId(Integer moduleId);
    List<ParagraphDTO> getParagraphsByContentId(Integer contentId);
    List<QuizDTO> getQuizzesByModuleId(Integer moduleId);

}
