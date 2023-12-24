package ru.practicum.service.category;


import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

/**
 * CATEGORY SERVICE
 */
@Component
public interface CategoryService {
    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long catId);

    /**
     * Add new category
     * category name must be unique
     *
     * @param category category
     * @return new category
     */
    CategoryDto addCategory(NewCategoryDto category);

    /**
     * Delete category by id
     * there should not be any events associated with the category
     *
     * @param categoryId category id
     */
    CategoryDto deleteCategory(Long categoryId);

    /**
     * Update category by id
     * category name must be unique
     *
     * @param catId    category id
     * @param category category information to update
     * @return updated category information
     */
    CategoryDto updateCategory(Long catId, CategoryDto category);
}
