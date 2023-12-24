package ru.practicum.controllers.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.category.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;


/**
 * CATEGORY CONTROLLER
 * processing HTTP-requests to "/categories" end-point to get categories of events.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(CATEGORIES_PATH)
public class CategoryPublicController {

    private final CategoryService categoryService;

    /**
     * Processing a GET-request to the endpoint "/categories"
     * get categories of events
     * with paging option: the size and the number of the page is defined by from/size parameters of request
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return categories of events
     */

    @GetMapping
    public List<CategoryDto> getCategories(
            @PositiveOrZero @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE) Integer from,
            @Positive @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-request to the endpoint \"/categories\".\n"
                + "CATEGORIES. PUBLIC ACCESS.\n"
                + "Get categories, starting from: {}, number of categories: {}", from, size);
        return categoryService.getCategories(from, size);
    }

    /**
     * Processing a GET-request to the endpoint "/categories/{catId}"
     * get a category by id
     *
     * @param catId category id
     * @return category
     */
    @GetMapping(CATEGORY_ID_PATH_VARIABLE)
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("GET-request to the endpoint \"categories/{}\".\n"
                + "CATEGORIES. PUBLIC ACCESS.\n"
                + "Get category by id {}", catId, catId);
        return categoryService.getCategoryById(catId);
    }
}
