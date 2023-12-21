package ru.practicum.controllers.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.entity.Event;
import ru.practicum.service.compilation.CompilationService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;
import ru.practicum.utils.mapper.CompilationMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.ADMIN_PATH;
import static ru.practicum.utils.constants.Constants.COMPILATIONS_PATH;
import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * CompilationAdminController WebMvcTest
 */

@WebMvcTest(CompilationAdminController.class)
@AutoConfigureMockMvc
public class CompilationAdminControllerTest {
    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    CompilationService service;
    Integer compId;

    @BeforeEach
    public void create() {
        compId = 1;
    }

    /**
     * test addCompilation method
     * POST-request "/admin/compilations"
     * when compilation data are valid
     * should return status CREATED 200
     * should invoke service addCompilation method
     * should return CompilationDto
     */
    @Test
    @SneakyThrows
    public void addCompilation_WhenCompilationIsValid_InvokeService_StatusIsCreated_ReturnCompilationDto() {

        // create valid title
        String title = "Concerts";

        // create NewCompilationDto
        NewCompilationDto compilationIn = NewCompilationDto.builder()
                .title(title)
                .build();
        List<Event> events = new ArrayList<>();

        // create expected out CompilationDto:
        CompilationDto compilationOut = CompilationMapper
                .toCompilationDto(CompilationMapper
                        .toCompilationEntity(compilationIn, events)).toBuilder()
                .id(compId)
                .build();

        // map input and out objects into strings
        String compilationInString = mapper.writeValueAsString(compilationIn);
        String expectedCompilationString = mapper.writeValueAsString(compilationOut);

        //mock service answer
        when(service.addCompilation(compilationIn)).thenReturn(compilationOut);

        //perform tested request and check status and content
        String result = mock.perform(post(ADMIN_PATH + COMPILATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compilationInString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(compilationOut.getId()), Integer.class))
                .andExpect(jsonPath("$.title", is(compilationOut.getTitle()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).addCompilation(compilationIn);

        //check result
        assertEquals(expectedCompilationString, result);
    }

    /**
     * test addCompilation method
     * POST-request "/admin/compilations"
     * when compilation data is not valid, has no title
     * should return status BAD REQUEST 400
     * should not invoke service addCompilation method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addCompilation_WhenCompilationHasNoTitle_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create invalid input Object
        NewCompilationDto compilationIn = NewCompilationDto.builder()
                .build();

        // create expected out Object
        String fieldName = "title";
        String defaultMessage = "must not be blank";
        String message = "Field: " + fieldName + ". Error: " + defaultMessage + ". Value: " + null;

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        // map input and out objects into strings
        String compilationInString = mapper.writeValueAsString(compilationIn);

        //perform tested request and check status and content
        mock.perform(post(ADMIN_PATH + COMPILATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compilationInString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addCompilation(any());

    }

    /**
     * test addCompilation method
     * POST-request "/admin/compilations"
     * when compilation data is not valid, has already existing title
     * should invoke service addCompilation method
     * should return status CONFLICT 409
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addCompilation_WhenCompilationHasSameName_InvokeService_StatusIsConflict_ReturnApiError() {
        // create valid title
        String title = "Concerts";

        // create NewCompilationDto
        NewCompilationDto compilationIn = NewCompilationDto.builder()
                .title(title)
                .build();

        String message = COMPILATION_TITLE_UNIQUE_VIOLATION;

        //mock service response
        when(service.addCompilation(compilationIn)).thenThrow(new DataIntegrityViolationException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
                .message(message)
                .build();

        // map input object into strings
        String categoryInString = mapper.writeValueAsString(compilationIn);

        //perform tested request and check status and content
        mock.perform(post(ADMIN_PATH + COMPILATIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).addCompilation(compilationIn);
    }

    /**
     * test updateCompilation method
     * PATCH-request "/admin/compilations/{compId}"
     * when compilation data are valid
     * should return status OK 200
     * should invoke service updateCompilation method
     * should return CompilationDto
     */
    @Test
    @SneakyThrows
    public void updateCompilation_WhenDataIsValid_InvokeService_StatusIsOk_ReturnCompilationDto() {
        // create valid title
        String title = "Concerts";

        // create UpdateCompilationRequest
        UpdateCompilationRequest compilationIn = UpdateCompilationRequest.builder()
                .title(title)
                .build();
        List<EventShortDto> events = new ArrayList<>();

        // create expected out CompilationDto:
        CompilationDto compilationOut = CompilationDto.builder()
                .title(title)
                .id(compId)
                .events(events)
                .build();

        // map input and out objects into strings
        String compilationInString = mapper.writeValueAsString(compilationIn);
        String expectedString = mapper.writeValueAsString(compilationOut);

        //mock service answer
        when(service.updateCompilation(compId, compilationIn)).thenReturn(compilationOut);

        //perform tested request and check status and content
        String compIdPath = "/1";
        String result = mock.perform(patch(ADMIN_PATH + COMPILATIONS_PATH + compIdPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compilationInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationOut.getId()), Integer.class))
                .andExpect(jsonPath("$.title", is(compilationOut.getTitle()), String.class))
                .andExpect(jsonPath("$.events", is(compilationOut.getEvents()), List.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).updateCompilation(compId, compilationIn);

        //check result
        assertEquals(expectedString, result);
    }

    /**
     * test updateCategory method
     * PATCH-request "/admin/categories/{catId}"
     * when request is invalid : title is invalid
     * should return status BAD REQUEST 404
     * should invoke service updateCategory method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateCompilation_WhenTitleIsInvalid_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create CategoryDto
        UpdateCompilationRequest invalid = UpdateCompilationRequest.builder()
                .title("")
                .build();
        Integer compId = 1;


        // create expected out Object
        String fieldName = "title";
        String defaultMessage = "size must be between 1 and 50";
        String value = "";
        String message = "Field: " + fieldName + ". Error: " + defaultMessage + ". Value: " + value;

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String invalidString = mapper.writeValueAsString(invalid);


        //perform tested request and check status and content
        String compIdPath = "/1";
        mock.perform(patch(ADMIN_PATH + COMPILATIONS_PATH + compIdPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).updateCompilation(compId, invalid);
    }

    /**
     * test updateCompilation method
     * PATCH-request "/admin/compilations/{compId}"
     * when compilation not found
     * should return status NOT FOUND 404
     * should invoke service updateCategory method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateCompilation_WhenCompilationNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create request to update
        String title = "Concerts";
        UpdateCompilationRequest compilationIn = UpdateCompilationRequest.builder()
                .title(title)
                .build();
        Integer compId = -1;

        //mock service response
        String message = getNotFoundMessage("Compilation", compId);
        when(service.updateCompilation(compId, compilationIn)).thenThrow(new NotFoundException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String compilationInString = mapper.writeValueAsString(compilationIn);

        //perform tested request and check status and content
        String compIdPath = "/-1";
        mock.perform(patch(ADMIN_PATH + COMPILATIONS_PATH + compIdPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compilationInString))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateCompilation(compId, compilationIn);
    }

    /**
     * test deleteCompilation method
     * DELETE-request "/admin/compilations/{compId}"
     * should invoke deleteCompilation method in service
     * should return status NO_CONTENT 409
     * should return CompilationDto
     */
    @Test
    @SneakyThrows
    public void deleteCompilation_InvokeService_StatusIsNoContent() {

        // create expected out CompilationDto:
        String name = "Rock festivals";
        CompilationDto compilationDto = CompilationDto.builder()
                .title(name)
                .build();

        //mock service answer
        when(service.deleteCompilation(compId)).thenReturn(compilationDto);

        //perform tested request and check status and content
        String compIdPath = "/1";
        mock.perform(delete(ADMIN_PATH + COMPILATIONS_PATH + compIdPath))
                .andExpect(status().isNoContent());

        // verify invokes
        verify(service).deleteCompilation(compId);

    }


    /**
     * test deleteCompilation method
     * DELETE-request "/admin/compilations/{compId}"
     * should invoke deleteCompilation method in service
     * should return status NOT FOUND 404
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void deleteCompilation_WhenCompilationNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create data
        Integer compId = -1;
        String message = getNotFoundMessage("Compilation", compId);

        //mock service answer
        when(service.deleteCompilation(compId)).thenThrow(new NotFoundException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String compIdPath = "/-1";
        mock.perform(delete(ADMIN_PATH + COMPILATIONS_PATH + compIdPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).deleteCompilation(compId);
    }
}



