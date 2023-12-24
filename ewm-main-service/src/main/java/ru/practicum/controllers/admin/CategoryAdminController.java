package ru.practicum.controllers.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.category.CategoryService;


import javax.validation.Valid;

import static ru.practicum.utils.constants.Constants.*;

/**
 * CATEGORY ADMIN CONTROLLER
 * private API for working with categories, processing HTTP-requests to the endpoint "/admin/categories"
 * processing HTTP-requests to "admin/categories" end-point to get categories of events.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(ADMIN_PATH + CATEGORIES_PATH)
public class CategoryAdminController {

    private final CategoryService categoryService;

    /**
     * Processing POST-request to the endpoint "/admin/categories"
     * Add new category
     * category name must be unique
     *
     * @param category category
     * @return new category
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto category) {
        log.info("POST-request to the endpoint \"/admin/categories\".\n. "
                + "CATEGORIES. ADMIN ACCESS.\n"
                + "Add new category: {}", category);
        return categoryService.addCategory(category);
    }

    /**
     * Process DELETE-request to the endpoint "/admin/categories/{catId}"
     * Delete category by id
     * there should not be any events associated with the category
     *
     * @param catId category id
     */
    @DeleteMapping(CATEGORY_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CategoryDto deleteCategory(@PathVariable Long catId) {
        log.info("\"DELETE-request to the endpoint \"/admin/categories/{}\".\n"
                + "CATEGORIES. ADMIN ACCESS.\n"
                + "Delete the category by id: {}", catId, catId);
        return categoryService.deleteCategory(catId);
    }

    /**
     * Process PATCH-request to the endpoint "/admin/categories/{catId}"
     * Update category by id
     * category name must be unique
     *
     * @param catId    category id
     * @param category category information to update
     * @return updated category information
     */
    @PatchMapping(CATEGORY_ID_PATH_VARIABLE)
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @Valid @RequestBody CategoryDto category) {
        log.info("PATCH-request to the endpoint \"/admin/categories/{}\".\n"
                + "CATEGORIES. ADMIN ACCESS.\n"
                + "Update the category by id: {}, new data: {}", catId, catId, category);
        return categoryService.updateCategory(catId, category);
    }
}
