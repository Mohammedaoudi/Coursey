package com.ensa.projet.trainingservice.service.implemnt;

import com.ensa.projet.trainingservice.exception.ResourceNotFoundException;
import com.ensa.projet.trainingservice.model.dao.CategoryDTO;
import com.ensa.projet.trainingservice.model.dao.ModuleDTO;
import com.ensa.projet.trainingservice.model.dao.TrainingDTO;
import com.ensa.projet.trainingservice.model.entities.Module;

import com.ensa.projet.trainingservice.model.entities.Category;
import com.ensa.projet.trainingservice.model.entities.Training;
import com.ensa.projet.trainingservice.repository.CategoryRepository;
import com.ensa.projet.trainingservice.service.interfaces.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private static final String GENERIC_ERROR_MESSAGE = "Category not found";

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setIconPath(categoryDTO.getIconPath());

        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    @Override
    public CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setIconPath(categoryDTO.getIconPath());

        Category updated = categoryRepository.save(category);
        return convertToDTO(updated);
    }

    @Override
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(GENERIC_ERROR_MESSAGE);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDTO getCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GENERIC_ERROR_MESSAGE));
        return convertToDTO(category);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = new ArrayList<>();

        for (Category category : categories) {
            categoryDTOs.add(convertToDTO(category));
        }

        return categoryDTOs;
    }


    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIconPath(category.getIconPath());

        List<TrainingDTO> trainingDTOs = new ArrayList<>();
        if (category.getTrainings() != null) {
            for (Training training : category.getTrainings()) {
                TrainingDTO trainingDTO = TrainingDTO.builder()
                        .id(training.getId())
                        .title(training.getTitle())
                        .description(training.getDescription())
                        .iconPath(training.getIconPath())
                        .difficultyLevel(training.getDifficultyLevel())
                        .estimatedDurationMinutes(training.getEstimatedDurationMinutes())
                        .goals(training.getGoals())
                        .prerequisites(training.getPrerequisites())
                        .supportAR(training.isSupportAR())
                        .supportAI(training.isSupportAI())
                        .urlYtb(training.getUrlYtb())
                        .categoryId(training.getCategory().getId())
                        .categoryName(training.getCategory().getName())
                        .instructions(new ArrayList<>(training.getInstructions()))
                        .modules(convertModules(training.getModules()))
                        .createdAt(training.getCreatedAt())
                        .updatedAt(training.getUpdatedAt())
                        .published(training.isPublished())
                        .build();
                trainingDTOs.add(trainingDTO);
            }
        }
        dto.setTrainings(trainingDTOs);
        return dto;
    }

    private List<ModuleDTO> convertModules(List<Module> modules) {
        if (modules == null) return Collections.emptyList();
        List<ModuleDTO> moduleDTOs = new ArrayList<>();
        for (Module module : modules) {
            ModuleDTO moduleDTO = ModuleDTO.builder()
                    .id(module.getId())
                    .title(module.getTitle())
                    .description(module.getDescription())
                    .orderIndex(module.getOrderIndex())
                    .build();
            moduleDTOs.add(moduleDTO);
        }
        return moduleDTOs;
    }
}