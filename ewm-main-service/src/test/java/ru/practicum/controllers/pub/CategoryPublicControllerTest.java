package ru.practicum.controllers.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.category.CategoryService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.*;
import static ru.practicum.utils.errors.ErrorConstants.INCORRECTLY_MADE_REQUEST;
import static ru.practicum.utils.errors.ErrorConstants.OBJECT_NOT_FOUND;

/**
 * CategoryPublicController WebMvcTest
 */
@WebMvcTest(CategoryPublicController.class)
@AutoConfigureMockMvc
public class CategoryPublicControllerTest {

    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    CategoryService service;

    /**
     * test get categories method
     * GET-request "/categories", no required parameters
     * when all parameters set and valid
     * should return status ok
     * should invoke service getCategories method and return list of categories dto objects
     */
    @Test
    @SneakyThrows
    public void getCategories_WhenAllParametersAreSetAndValid_StatusIsOk_AndInvokeService_ReturnList() {

        // create CategoryDto categories
        Long categoryId1 = 1L;
        String categoryName1 = "concert";
        CategoryDto categoryDto1 = CategoryDto.builder()
                .id(categoryId1)
                .name(categoryName1)
                .build();
        Long categoryId2 = 2L;
        String categoryName2 = "exhibition";
        CategoryDto categoryDto2 = CategoryDto.builder()
                .id(categoryId2)
                .name(categoryName2)
                .build();

        // create expected out List:
        List<CategoryDto> categories = List.of(categoryDto1, categoryDto2);

        // map out object into string
        String expectedString = mapper.writeValueAsString(categories);

        //mock service answer
        Integer from = 0;
        Integer size = 5;
        when(service.getCategories(from, size)).thenReturn(categories);

        //perform tested request and check status and content
        String result = mock.perform(get(CATEGORIES_PATH)
                        .param(FROM_PARAMETER_NAME, from.toString())
                        .param(SIZE_PARAMETER_NAME, size.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(categoryDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(categoryDto1.getName()), String.class))
                .andExpect(jsonPath("$.[1].id", is(categoryDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].name", is(categoryDto2.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCategories(from, size);

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test get categories method
     * GET-request "/categories", no required parameters
     * when parameters are not set should return default values
     * should return status ok
     * should invoke service getCategories method and return list of category dto objects
     */
    @Test
    @SneakyThrows
    public void getCategories_WhenParametersAreNotSet_StatusIsOk_AndInvokeService_ReturnListByDefaultValues() {

        // create CategoryDto categories
        Long categoryId1 = 1L;
        String categoryName1 = "concert";
        CategoryDto categoryDto1 = CategoryDto.builder()
                .id(categoryId1)
                .name(categoryName1)
                .build();
        Long categoryId2 = 2L;
        String categoryName2 = "exhibition";
        CategoryDto categoryDto2 = CategoryDto.builder()
                .id(categoryId2)
                .name(categoryName2)
                .build();

        // create expected out List:
        List<CategoryDto> categories = List.of(categoryDto1, categoryDto2);

        // map out object into string
        String expectedString = mapper.writeValueAsString(categories);

        //mock service answer
        Integer from = 0;
        Integer size = 5;
        when(service.getCategories(from, size)).thenReturn(categories);

        //perform tested request and check status and content
        String result = mock.perform(get(CATEGORIES_PATH)
                        .param(FROM_PARAMETER_NAME, from.toString())
                        .param(SIZE_PARAMETER_NAME, size.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id",
                        is(categoryDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name",
                        is(categoryDto1.getName()), String.class))
                .andExpect(jsonPath("$.[1].id",
                        is(categoryDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].name",
                        is(categoryDto2.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCategories(from, size);

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test get categories method
     * GET-request "/categories", no required parameters
     * when parameter FROM is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getCategories method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getCategories_WhenParameterFromNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer';"
                + " nested exception is java.lang.NumberFormatException: For input string:"
                + " \"FROM\"";

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // create invalid parameters
        String from = "FROM";
        String size = "0";

        //perform tested request and check status and content
        mock.perform(get(CATEGORIES_PATH)
                        .param(SIZE_PARAMETER_NAME, size)
                        .param(FROM_PARAMETER_NAME, from))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service, never()).getCategories(any(), any());
    }

    /**
     * test get categories method
     * GET-request "/categories", no required parameters
     * when parameter SIZE is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getCategories method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getCategories_WhenParameterSizeNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer';"
                + " nested exception is java.lang.NumberFormatException: For input string:"
                + " \"SIZE\"";

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // create invalid parameters
        String from = "1";
        String size = "SIZE";

        //perform tested request and check status and content
        mock.perform(get(CATEGORIES_PATH)
                        .param(FROM_PARAMETER_NAME, from)
                        .param(SIZE_PARAMETER_NAME, size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getCategories(any(), any());
    }

    /**
     * test get category by ID method
     * GET-request "/categories/{catId}", no required parameters
     * should return status ok
     * should invoke service getCompilationById method and return category
     */
    @Test
    @SneakyThrows
    public void getCategoryById_StatusIsOk_AndInvokeService_ReturnCategory() {

        // create CategoryDto categories
        Long categoryId = 1L;
        String categoryName = "concert";
        CategoryDto categoryDto = CategoryDto.builder()
                .id(categoryId)
                .name(categoryName)
                .build();

        // map out object into string
        String expectedString = mapper.writeValueAsString(categoryDto);

        //mock service answer
        String pathVariable = "/1";
        when(service.getCategoryById(categoryId)).thenReturn(categoryDto);

        //perform tested request and check status and content
        String result = mock.perform(get(CATEGORIES_PATH + pathVariable))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(categoryDto.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCategoryById(categoryId);

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test get category by ID method
     * GET-request ""/compilations/{compId}", no required parameters
     * when path variable is not valid
     * should return status bad request
     * should not invoke service getCategoryById method and return apiError
     */
    @Test
    @SneakyThrows
    public void getCategoryById_whenPathVariableNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long';"
                + " nested exception is java.lang.NumberFormatException: For input string:"
                + " \"ONE\"";

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String invalidPathVariable = "/ONE";
        mock.perform(get(CATEGORIES_PATH + invalidPathVariable))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getCategoryById(any());

    }

    /**
     * test get category by ID method
     * GET-request ""/categories/{catId}", no required parameters
     * when category not found
     * should return status not found
     * should invoke service getCategoryById method and return apiError
     */
    @Test
    @SneakyThrows
    public void getCategoryById_whenObjectNotFound_StatusIsNotFound_InvokeService_ReturnApiError() {

        Long categoryId = -1L;

        // create expected out Object
        String message = ErrorConstants.getNotFoundMessage("Category", categoryId);

        //mock service answer
        when(service.getCategoryById(categoryId)).thenThrow(new NotFoundException(message));

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String pathVariable = "/-1";
        mock.perform(get(CATEGORIES_PATH + pathVariable))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).getCategoryById(categoryId);
    }
}
