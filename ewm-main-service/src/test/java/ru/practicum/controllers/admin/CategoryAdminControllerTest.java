package ru.practicum.controllers.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.category.CategoryService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;
import ru.practicum.utils.mapper.CategoryMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.ADMIN_PATH;
import static ru.practicum.utils.constants.Constants.CATEGORIES_PATH;
import static ru.practicum.utils.errors.ErrorConstants.*;


/**
 * CategoryAdminController WebMvcTest
 */

@WebMvcTest(CategoryAdminController.class)
@AutoConfigureMockMvc
public class CategoryAdminControllerTest {
    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    CategoryService service;
    Long catId;

    @BeforeEach
    public void create() {
        catId = 1L;
    }

    /**
     * test addCategory method
     * POST-request "/admin/categories"
     * when category info is valid
     * should return status CREATED 200
     * should invoke service addCategory method
     * should return result CategoryDto
     */
    @Test
    @SneakyThrows
    public void addCategory_WhenCategoryIsValid_InvokeService_StatusIsCreated_ReturnCategoryDto() {

        // create valid name
        String name = "Concert";

        // create NewCategoryDto
        NewCategoryDto categoryIn = NewCategoryDto.builder()
                .name(name)
                .build();

        // create expected out CategoryDto:
        CategoryDto categoryOut = CategoryMapper.toCategoryDto(
                CategoryMapper.toCategoryEntity(categoryIn)).toBuilder().id(catId).build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(categoryIn);
        String expectedCategoryString = mapper.writeValueAsString(categoryOut);

        //mock service answer
        when(service.addCategory(categoryIn)).thenReturn(categoryOut);

        //perform tested request and check status and content
        String result = mock.perform(post(ADMIN_PATH + CATEGORIES_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoryOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(categoryIn.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).addCategory(categoryIn);

        //check result
        assertEquals(expectedCategoryString, result);
    }

    /**
     * test addCategory method
     * POST-request "/admin/categories"
     * when category info is invalid, has no name
     * should return status BAD REQUEST 400
     * should not invoke service addCategory method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addCategory_WhenCategoryHasNoName_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create invalid input Object
        NewCategoryDto categoryIn = NewCategoryDto.builder()
                .build();

        // create expected out Object
        String fieldName = "name";
        String defaultMessage = "must not be blank";
        String message = "Field: " + fieldName + ". Error: " + defaultMessage + ". Value: " + null;

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(categoryIn);

        //perform tested request and check status and content
        mock.perform(post(ADMIN_PATH + CATEGORIES_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addCategory(any());

    }

    /**
     * test addCategory method
     * POST-request "/admin/categories"
     * when category info is not valid, has already existing name
     * should return status CONFLICT 409
     * should return ApiError
     * should invoke service addCategory method
     */
    @Test
    @SneakyThrows
    public void addCategory_WhenCategoryHasSameName_InvokeService_StatusIsConflict_ReturnApiError() {

        // create valid name
        String name = "Concert";

        // create NewCategoryDto
        NewCategoryDto categoryIn = NewCategoryDto.builder()
                .name(name)
                .build();

        //mock service response
        when(service.addCategory(categoryIn)).thenThrow(
                new ConflictConstraintUniqueException(CATEGORY_NAME_UNIQUE_VIOLATION));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
                .message(CATEGORY_NAME_UNIQUE_VIOLATION)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(categoryIn);

        //perform tested request and check status and content
        mock.perform(post(ADMIN_PATH + CATEGORIES_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).addCategory(categoryIn);
    }

    /**
     * test updateCategory method
     * PATCH-request "/admin/categories/{catId}"
     * when category info is valid
     * should return status OK 200
     * should invoke service updateCategory method
     * should return CategoryDto
     */
    @Test
    @SneakyThrows
    public void updateCategory_WhenDataIsValid_InvokeService_StatusIsOk_ReturnCategoryDto() {

        // create CategoryDto

        String name = "Concert";
        CategoryDto categoryIn = CategoryDto.builder()
                .name(name)
                .build();

        // create expected CategoryDto
        Long catId = 1L;
        String newName = "Festival";
        CategoryDto categoryOut = categoryIn.toBuilder()
                .id(catId)
                .name(newName)
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(categoryIn);
        String expectedString = mapper.writeValueAsString(categoryOut);

        //mock service answer
        when(service.updateCategory(catId, categoryIn)).thenReturn(categoryOut);

        //perform tested request and check status and content
        String catIdPath = "/1";
        String result = mock.perform(patch(ADMIN_PATH + CATEGORIES_PATH + catIdPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(categoryOut.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).updateCategory(catId, categoryIn);

        //check result
        assertEquals(expectedString, result);
    }

    /**
     * test updateCategory method
     * PATCH-request "/admin/categories/{catId}"
     * when category not found by id
     * should return status NOT FOUND 404
     * should invoke service updateCategory method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateCategory_WhenCategoryNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create CategoryDto

        String name = "Concert";
        CategoryDto categoryIn = CategoryDto.builder()
                .name(name)
                .build();
        Long catId = 1L;

        //mock service response
        String message = getNotFoundMessage("Category", catId);
        when(service.updateCategory(catId, categoryIn)).thenThrow(new NotFoundException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(categoryIn);

        //perform tested request and check status and content
        String catIdPath = "/1";
        mock.perform(patch(ADMIN_PATH + CATEGORIES_PATH + catIdPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateCategory(catId, categoryIn);
    }

    /**
     * test updateCategory method
     * PATCH-request "/admin/categories/{catId}"
     * when category name is not unique
     * should return status CONFLICT 409
     * should invoke service updateCategory method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateCategory_WhenCategoryNameIsNotUnique_InvokeService_StatusIsConflict_ReturnApiError() {

        // create CategoryDto

        String name = "Concert";
        CategoryDto categoryIn = CategoryDto.builder()
                .name(name)
                .build();
        Long catId = 1L;

        //mock service response
        when(service.updateCategory(catId, categoryIn)).thenThrow(
                new ConflictConstraintUniqueException(CATEGORY_NAME_UNIQUE_VIOLATION));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
                .message(CATEGORY_NAME_UNIQUE_VIOLATION)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(categoryIn);

        //perform tested request and check status and content
        String catIdPath = "/1";
        mock.perform(patch(ADMIN_PATH + CATEGORIES_PATH + catIdPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateCategory(catId, categoryIn);
    }

    /**
     * test deleteCategory method
     * DELETE-request "/admin/categories/{catId}"
     * should invoke deleteCategory method in service
     * should return status NO_CONTENT 409
     * should return deleted CategoryDto
     */
    @Test
    @SneakyThrows
    public void deleteCategory_InvokeService_StatusIsNoContent() {

        // create expected out CategoryDto:
        String name = "Concert";
        CategoryDto categoryDto = CategoryDto.builder()
                .name(name)
                .build();

        //mock service answer
        when(service.deleteCategory(catId)).thenReturn(categoryDto);

        //perform tested request and check status and content
        String categoryIdPath = "/1";
        mock.perform(delete(ADMIN_PATH + CATEGORIES_PATH + categoryIdPath))
                .andExpect(status().isNoContent());

        // verify invokes
        verify(service).deleteCategory(catId);

    }

    /**
     * test deleteCategory method when category is not empty
     * DELETE-request "/admin/categories/{catId}"
     * should invoke deleteCategory method in service
     * should return status CONFLICT 409
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void deleteCategory_WhenCategoryIsNotEmpty_InvokeService_StatusIsConflict_ReturnApiError() {

        // create data
        Long catId = 1L;
        String message = CATEGORY_IS_NOT_EMPTY;

        //mock service answer
        when(service.deleteCategory(catId)).thenThrow(new NotAllowedException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(ACTION_IS_NOT_ALLOWED)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String catIdPath = "/1";
        mock.perform(delete(ADMIN_PATH + CATEGORIES_PATH + catIdPath))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).deleteCategory(catId);
    }

    /**
     * test deleteCategory method
     * DELETE-request "/admin/categories/{catId}"
     * should invoke deleteCategory method in service
     * should return status NOT FOUND 404
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void deleteCategory_WhenCategoryNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create data
        Long catId = -1L;
        String message = getNotFoundMessage("Category", catId);

        //mock service answer
        when(service.deleteCategory(catId)).thenThrow(new NotFoundException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String catIdPath = "/-1";
        mock.perform(delete(ADMIN_PATH + CATEGORIES_PATH + catIdPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).deleteCategory(catId);
    }
}
