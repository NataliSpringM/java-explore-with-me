package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.logger.ListLogger;
import ru.practicum.utils.mapper.CategoryMapper;
import ru.practicum.utils.paging.Paging;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import static ru.practicum.utils.errors.ErrorConstants.CATEGORY_IS_NOT_EMPTY;
import static ru.practicum.utils.errors.ErrorConstants.CATEGORY_NAME_UNIQUE_VIOLATION;

/**
 * CATEGORY SERVICE IMPLEMENTATION
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    /**
     * get Categories list
     *
     * @param from starting page parameter
     * @param size size of page
     * @return list
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        List<Category> categories = categoryRepository.findAll(Paging.getPageable(from, size)).getContent();
        ListLogger.logResultList(categories);
        return CategoryMapper.toCategoryDtoList(categories);
    }

    /**
     * get Category by id
     *
     * @param catId category id
     * @return category
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.getNotFoundMessage("Category", catId)));
        log.info("Category {} was found by id {}", category, catId);
        return CategoryMapper.toCategoryDto(category);
    }

    /**
     * Add new category
     * category name must be unique
     *
     * @param categoryData category's data
     * @return new category
     */
    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto categoryData) {
        try {
            Category newCategory = categoryRepository.save(CategoryMapper.toCategoryEntity(categoryData));
            CategoryDto newCategoryDto = CategoryMapper.toCategoryDto(newCategory);
            log.info("Category {} added", newCategoryDto);
            return newCategoryDto;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictConstraintUniqueException(CATEGORY_NAME_UNIQUE_VIOLATION);
        }
    }

    /**
     * Delete category by id
     * there should not be any events associated with the category
     *
     * @param catId category id
     */
    @Override
    @Transactional
    public CategoryDto deleteCategory(Long catId) {
        CategoryDto deleted = CategoryMapper.toCategoryDto(getCategoryOrThrowException(catId));
        checkIfEventsExistByCategory(catId);
        categoryRepository.deleteById(catId);
        log.info("Delete category with id: {}, category: {}", catId, deleted);
        return deleted;
    }


    /**
     * Update category by id
     * category name must be unique
     *
     * @param catId category id
     * @param dto   category information to update
     * @return updated category information
     */
    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto dto) {
        checkIfCategoryNameIsUnique(catId, dto.getName());
        Category category = getCategoryOrThrowException(catId);
        Category updated = category.toBuilder()
                .name(dto.getName())
                .build();
        Category updatedCategory = categoryRepository.save(updated);
        CategoryDto updatedCategoryDto = CategoryMapper.toCategoryDto(updatedCategory);
        log.info("Category {} updated", updatedCategoryDto);
        return updatedCategoryDto;
    }


    /**
     * get Category from repository by id or throw NotFoundException
     *
     * @param categoryId category ID
     * @return Category
     */
    private Category getCategoryOrThrowException(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorConstants.getNotFoundMessage("Category", categoryId)));
    }

    /**
     * check if there are events associated with category
     * throw exception if category is not empty
     *
     * @param catId category ID
     */
    private void checkIfEventsExistByCategory(Long catId) {
        if (eventRepository.existsByCategory_Id(catId)) {
            throw new NotAllowedException(CATEGORY_IS_NOT_EMPTY);
        }
    }

    /**
     * check if category new name is unique
     * throw exception if it's not
     */
    private void checkIfCategoryNameIsUnique(Long catId, String name) {
        if (categoryRepository.existsByNameAndIdNot(name, catId))
            throw new ConflictConstraintUniqueException(CATEGORY_NAME_UNIQUE_VIOLATION);
    }

}
