package com.ensa.projet.trainingservice.service.interfaces;

import com.ensa.projet.trainingservice.model.dao.CategoryDTO;
import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO);
    void deleteCategory(Integer id);
    CategoryDTO getCategory(Integer id);
    List<CategoryDTO> getAllCategories();
}
