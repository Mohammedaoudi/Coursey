    package com.ensa.projet.trainingservice.controller;

    import com.ensa.projet.trainingservice.model.dao.ContentDTO;
    import com.ensa.projet.trainingservice.model.dao.ModuleDTO;
    import com.ensa.projet.trainingservice.model.dao.ParagraphDTO;
    import com.ensa.projet.trainingservice.model.dao.QuizDTO;
    import com.ensa.projet.trainingservice.model.entities.Content;
    import com.ensa.projet.trainingservice.model.entities.Paragraph;
    import com.ensa.projet.trainingservice.model.entities.Quiz;
    import com.ensa.projet.trainingservice.service.interfaces.ModuleService;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/modules")

    public class ModuleController {

        private final ModuleService moduleService;

        public ModuleController(ModuleService moduleService) {
            this.moduleService = moduleService;
        }

        @PostMapping("/{moduleId}/contents")
        public ResponseEntity<ModuleDTO> addContentToModule(
                @PathVariable Integer moduleId,
                @RequestBody Content content) {
            ModuleDTO updatedModule = moduleService.addContentToModule(moduleId, content);
            return new ResponseEntity<>(updatedModule, HttpStatus.CREATED);
        }

        @PostMapping("/{moduleId}/quizzes")
        public ResponseEntity<ModuleDTO> addQuizToModule(
                @PathVariable Integer moduleId,
                @RequestBody Quiz quiz) {
            ModuleDTO updatedModule = moduleService.addQuizToModule(moduleId, quiz);
            return new ResponseEntity<>(updatedModule, HttpStatus.CREATED);
        }

        @PostMapping("/{contentId}/paragraphs")
        public ResponseEntity<ContentDTO> addParagraphToContent(
                @PathVariable Integer contentId,
                @RequestBody Paragraph paragraph) {
            ContentDTO updatedContent = moduleService.addParagraphToContent(contentId, paragraph);
            return new ResponseEntity<>(updatedContent, HttpStatus.CREATED);
        }

        @GetMapping("/{moduleId}")
        public ResponseEntity<ModuleDTO> getModuleById(@PathVariable Integer moduleId) {
            ModuleDTO module = moduleService.findByModuleId(moduleId);
            return new ResponseEntity<>(module, HttpStatus.OK);
        }

        @GetMapping("/contents/{contentId}/paragraphs")
        public ResponseEntity<List<ParagraphDTO>> getParagraphsByContentId(@PathVariable Integer contentId) {
            List<ParagraphDTO> paragraphs = moduleService.getParagraphsByContentId(contentId);
            return new ResponseEntity<>(paragraphs, HttpStatus.OK);
        }
        @GetMapping("/{moduleId}/quizzes")
        public ResponseEntity<List<QuizDTO>> getQuizzesByModuleId(@PathVariable Integer moduleId) {
            List<QuizDTO> quizzes = moduleService.getQuizzesByModuleId(moduleId);
            return new ResponseEntity<>(quizzes, HttpStatus.OK);
        }




    }
