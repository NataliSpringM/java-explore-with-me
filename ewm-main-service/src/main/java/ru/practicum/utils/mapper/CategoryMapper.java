package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.Category;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Map CATEGORY entity and DTOs into each other
 */
@UtilityClass
public class CategoryMapper {
    /**
     * map NewCategoryDto into Category entity
     */
    public static Category toCategoryEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    /**
     * map Category entity into CategoryDto
     */

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }


    /**
     * map list of Category entities into CategoryDto list
     */

    public static List<CategoryDto> toCategoryDtoList(List<Category> categories) {
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }
}
