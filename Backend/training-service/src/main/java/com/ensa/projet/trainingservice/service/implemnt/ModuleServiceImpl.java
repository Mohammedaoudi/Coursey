package com.ensa.projet.trainingservice.service.implemnt;

import com.ensa.projet.trainingservice.exception.ResourceNotFoundException;
import com.ensa.projet.trainingservice.model.dao.ContentDTO;
import com.ensa.projet.trainingservice.model.dao.ModuleDTO;
import com.ensa.projet.trainingservice.model.dao.ParagraphDTO;
import com.ensa.projet.trainingservice.model.dao.QuizDTO;
import com.ensa.projet.trainingservice.model.entities.Content;
import com.ensa.projet.trainingservice.model.entities.Paragraph;
import com.ensa.projet.trainingservice.model.entities.Quiz;
import com.ensa.projet.trainingservice.model.entities.Module;
import com.ensa.projet.trainingservice.repository.ContentRepository;
import com.ensa.projet.trainingservice.repository.ModuleRepository;
import com.ensa.projet.trainingservice.service.interfaces.ModuleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service

public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ContentRepository contentRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository,ContentRepository contentRepository) {
        this.moduleRepository = moduleRepository;
        this.contentRepository = contentRepository;
    }

    @Override
    public ModuleDTO addContentToModule(Integer moduleId, Content content) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        content.setModule(module);
        content.setOrderIndex(module.getContents().size() + 1);
        module.getContents().add(content);

        Module savedModule = moduleRepository.save(module);
        return convertToModuleDTO(savedModule);
    }

    @Override
    public ModuleDTO addQuizToModule(Integer moduleId, Quiz quiz) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));
        quiz.setModule(module);
        module.getModuleQuizzes().add(quiz);

        Module savedModule = moduleRepository.save(module);
        return convertToModuleDTO(savedModule);
    }
    @Override
    public ModuleDTO findByModuleId(Integer moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));
        return convertToModuleDTO(module);
    }

    @Override
    public List<ParagraphDTO> getParagraphsByContentId(Integer contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + contentId));

        return content.getParagraphs().stream()
                .map(this::convertToParagraphDTO)
                .toList();
    }

    private ParagraphDTO convertToParagraphDTO(Paragraph paragraph) {
        return ParagraphDTO.builder()
                .id(paragraph.getId())
                .title(paragraph.getTitle())
                .description(paragraph.getDescription())
                .image(paragraph.getImage())
                .build();
    }


    @Override
    public ContentDTO addParagraphToContent(Integer contentId, Paragraph paragraph) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        // Set the content reference in paragraph
        paragraph.setContent(content);

        // Add paragraph to content's list
        content.getParagraphs().add(paragraph);

        Content savedContent = contentRepository.save(content);
        return convertToContentDTO(savedContent);
    }

    @Override
    public List<QuizDTO> getQuizzesByModuleId(Integer moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        return module.getModuleQuizzes().stream()
                .map(this::convertToQuizDTO)
                .toList();
    }
    private QuizDTO convertToQuizDTO(Quiz quiz) {
        return QuizDTO.builder()
                .id(quiz.getId())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .correctAnswerIndex(quiz.getCorrectAnswerIndex())
                .isFinalQuiz(quiz.isFinalQuiz())
                .moduleId(quiz.getModule().getId())
                .trainingId(quiz.getTraining() != null ? quiz.getTraining().getId() : null)
                .build();
    }

    private ModuleDTO convertToModuleDTO(Module module) {
        return ModuleDTO.builder()
                .id(module.getId())
                .title(module.getTitle())
                .finished(module.isFinal())
                .trainingId(module.getTraining() != null ? module.getTraining().getId() : null)
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .contents(module.getContents().stream()
                        .map(this::convertToContentDTO)
                        .toList())
                .build();
    }

    private ContentDTO convertToContentDTO(Content content) {
        return ContentDTO.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .url(content.getUrl())
                .orderIndex(content.getOrderIndex())
                .build();
    }


}
