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
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.service.compilation.CompilationService;
import ru.practicum.utils.errors.ApiError;
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
import static ru.practicum.utils.formatter.DateTimeFormatter.DATE_TIME_FORMATTER;

/**
 * CompilationPublicController WebMvcTest
 */
@WebMvcTest(CompilationPublicController.class)
@AutoConfigureMockMvc
public class CompilationPublicControllerTest {
    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    CompilationService service;


    /**
     * test getCompilations method
     * GET-request "/compilations", no required parameters
     * when all parameters set and valid
     * should return status ok
     * should invoke service getCompilations method and return list of compilations dto objects
     */
    @Test
    @SneakyThrows
    public void getCompilations_WhenAllParametersAreSetAndValid_StatusIsOk_AndInvokeService_ReturnList() {

        // create UserShortDto initiator
        Long userId = 1L;
        String userName = "Egor";
        UserShortDto initiator = UserShortDto.builder()
                .id(userId)
                .name(userName)
                .build();

        // create CategoryDto category
        Long categoryId = 1L;
        String categoryName = "concert";
        CategoryDto categoryDto = CategoryDto.builder()
                .id(categoryId)
                .name(categoryName)
                .build();

        // create List of EventShorDto objects
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        String annotation1 = "This is valid annotation";
        String annotation2 = "This is valid annotation";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2026, 1, 1, 1, 1, 1);
        String title1 = "title";
        String title2 = "title";
        Integer confirmedRequests1 = 0;
        Integer confirmedRequests2 = 1;
        Long views1 = 0L;
        Long views2 = 1L;

        EventShortDto eventShortDto1 = EventShortDto.builder()
                .id(eventId1)
                .annotation(annotation1)
                .category(categoryDto)
                .confirmedRequests(confirmedRequests1)
                .eventDate(eventDate1)
                .initiator(initiator)
                .paid(false)
                .title(title1)
                .views(views1)
                .build();

        EventShortDto eventShortDto2 = EventShortDto.builder()
                .id(eventId2)
                .annotation(annotation2)
                .category(categoryDto)
                .confirmedRequests(confirmedRequests2)
                .eventDate(eventDate2)
                .initiator(initiator)
                .paid(true)
                .title(title2)
                .views(views2)
                .build();

        List<EventShortDto> events1 = List.of(eventShortDto1, eventShortDto2);
        List<EventShortDto> events2 = List.of(eventShortDto1);

        // create Compilations
        Integer compilationId1 = 1;
        Integer compilationId2 = 2;
        Integer compilationId3 = 3;
        String twoEventsCompilationTitle = "compilationTitle1";
        String oneEventCompilationTitle = "compilationTitle2";
        String emptyCompilationTitle = "compilationTitle3";
        CompilationDto compilationDto1 = CompilationDto.builder()
                .id(compilationId1)
                .events(events1)
                .title(twoEventsCompilationTitle)
                .pinned(true)
                .build();
        CompilationDto compilationDto2 = CompilationDto.builder()
                .id(compilationId2)
                .events(events2)
                .title(oneEventCompilationTitle)
                .pinned(true)
                .build();
        CompilationDto compilationDto3 = CompilationDto.builder()
                .id(compilationId3)
                .events(events2)
                .title(emptyCompilationTitle)
                .pinned(true)
                .build();


        // create expected out List:
        List<CompilationDto> compilations = List.of(compilationDto1, compilationDto2, compilationDto3);

        // map out object into string
        String expectedString = mapper.writeValueAsString(compilations);

        //mock service answer
        Integer from = 0;
        Integer size = 5;
        Boolean pinned = true;
        when(service.getCompilations(from, size, pinned)).thenReturn(compilations);

        //perform tested request and check status and content
        String result = mock.perform(get(COMPILATIONS_PATH)
                        .param(PINNED_PARAMETER_NAME, pinned.toString())
                        .param(FROM_PARAMETER_NAME, from.toString())
                        .param(SIZE_PARAMETER_NAME, size.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id",
                        is(compilationDto1.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].events.[0].id",
                        is(events1.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.[0].events.[0].annotation",
                        is(events1.get(0).getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].category",
                        is(events1.get(0).getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[0].events.[0].category.id",
                        is(events1.get(0).getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].events.[0].category.name",
                        is(events1.get(0).getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].confirmedRequests",
                        is(events1.get(0).getConfirmedRequests()), Integer.class))
                .andExpect(jsonPath("$.[0].events.[0].eventDate",
                        is(events1.get(0).getEventDate()
                                .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].events.[0].initiator.id",
                        is(events1.get(0).getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].events.[0].initiator.name",
                        is(events1.get(0).getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].paid",
                        is(events1.get(0).getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].events.[0].title",
                        is(events1.get(0).getTitle()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].views",
                        is(events1.get(0).getViews()), Long.class))
                .andExpect(jsonPath("$.[0].title",
                        is(compilationDto1.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].pinned",
                        is(compilationDto1.getPinned()), Boolean.class))
                .andExpect(jsonPath("$.[1].id",
                        is(compilationDto2.getId()), Integer.class))
                .andExpect(jsonPath("$.[1].title",
                        is(compilationDto2.getTitle()), String.class))
                .andExpect(jsonPath("$.[1].pinned",
                        is(compilationDto2.getPinned()), Boolean.class))
                .andExpect(jsonPath("$.[2].id",
                        is(compilationDto3.getId()), Integer.class))
                .andExpect(jsonPath("$.[2].title",
                        is(compilationDto3.getTitle()), String.class))
                .andExpect(jsonPath("$.[2].pinned"
                        , is(compilationDto3.getPinned()), Boolean.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCompilations(from, size, pinned);

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test getCompilations method
     * GET-request "/compilations", no required parameters
     * when parameters are not set should return default values
     * should return status ok
     * should invoke service getCompilations method and return list of compilations dto objects
     */
    @Test
    @SneakyThrows
    public void getCompilations_WhenParametersAreNotSet_StatusIsOk_AndInvokeService_ReturnListByDefaultValues() {

        // create UserShortDto initiator
        Long userId = 1L;
        String userName = "Egor";
        UserShortDto initiator = UserShortDto.builder()
                .id(userId)
                .name(userName)
                .build();

        // create CategoryDto category
        Long categoryId = 1L;
        String categoryName = "concert";
        CategoryDto categoryDto = CategoryDto.builder()
                .id(categoryId)
                .name(categoryName)
                .build();

        // create List of EventShorDto objects
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        String annotation1 = "This is valid annotation";
        String annotation2 = "This is valid annotation";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2026, 1, 1, 1, 1, 1);
        String title1 = "title";
        String title2 = "title";
        Integer confirmedRequests1 = 0;
        Integer confirmedRequests2 = 1;
        Long views1 = 0L;
        Long views2 = 1L;

        EventShortDto eventShortDto1 = EventShortDto.builder()
                .id(eventId1)
                .annotation(annotation1)
                .category(categoryDto)
                .confirmedRequests(confirmedRequests1)
                .eventDate(eventDate1)
                .initiator(initiator)
                .paid(false)
                .title(title1)
                .views(views1)
                .build();

        EventShortDto eventShortDto2 = EventShortDto.builder()
                .id(eventId2)
                .annotation(annotation2)
                .category(categoryDto)
                .confirmedRequests(confirmedRequests2)
                .eventDate(eventDate2)
                .initiator(initiator)
                .paid(true)
                .title(title2)
                .views(views2)
                .build();

        List<EventShortDto> events1 = List.of(eventShortDto1, eventShortDto2);
        List<EventShortDto> events2 = List.of(eventShortDto1);

        // create Compilations
        Integer compilationId1 = 1;
        Integer compilationId2 = 2;
        Integer compilationId3 = 3;
        String twoEventsCompilationTitle = "compilationTitle1";
        String oneEventCompilationTitle = "compilationTitle2";
        String emptyCompilationTitle = "compilationTitle3";
        CompilationDto compilationDto1 = CompilationDto.builder()
                .id(compilationId1)
                .events(events1)
                .title(twoEventsCompilationTitle)
                .pinned(false)
                .build();
        CompilationDto compilationDto2 = CompilationDto.builder()
                .id(compilationId2)
                .events(events2)
                .title(oneEventCompilationTitle)
                .pinned(false)
                .build();
        CompilationDto compilationDto3 = CompilationDto.builder()
                .id(compilationId3)
                .events(events2)
                .title(emptyCompilationTitle)
                .pinned(false)
                .build();


        // create expected out List:
        List<CompilationDto> compilations = List.of(compilationDto1, compilationDto2, compilationDto3);

        // map out object into string
        String expectedString = mapper.writeValueAsString(compilations);

        //mock service answer
        Integer from = 0;
        Integer size = 10;
        when(service.getCompilations(from, size, null)).thenReturn(compilations);

        //perform tested request and check status and content
        String result = mock.perform(get(COMPILATIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id",
                        is(compilationDto1.getId()), Integer.class))
                .andExpect(jsonPath("$.[0].events.[0].id",
                        is(events1.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.[0].events.[0].annotation",
                        is(events1.get(0).getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].category",
                        is(events1.get(0).getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[0].events.[0].category.id",
                        is(events1.get(0).getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].events.[0].category.name",
                        is(events1.get(0).getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].confirmedRequests",
                        is(events1.get(0).getConfirmedRequests()), Integer.class))
                .andExpect(jsonPath("$.[0].events.[0].eventDate",
                        is(events1.get(0).getEventDate()
                                .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].events.[0].initiator.id",
                        is(events1.get(0).getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].events.[0].initiator.name",
                        is(events1.get(0).getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].paid",
                        is(events1.get(0).getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].events.[0].title",
                        is(events1.get(0).getTitle()), String.class))
                .andExpect(jsonPath("$.[0].events.[0].views",
                        is(events1.get(0).getViews()), Long.class))
                .andExpect(jsonPath("$.[0].title",
                        is(compilationDto1.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].pinned",
                        is(compilationDto1.getPinned()), Boolean.class))
                .andExpect(jsonPath("$.[1].id",
                        is(compilationDto2.getId()), Integer.class))
                .andExpect(jsonPath("$.[1].title",
                        is(compilationDto2.getTitle()), String.class))
                .andExpect(jsonPath("$.[1].pinned",
                        is(compilationDto2.getPinned()), Boolean.class))
                .andExpect(jsonPath("$.[2].id",
                        is(compilationDto3.getId()), Integer.class))
                .andExpect(jsonPath("$.[2].title",
                        is(compilationDto3.getTitle()), String.class))
                .andExpect(jsonPath("$.[2].pinned"
                        , is(compilationDto3.getPinned()), Boolean.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCompilations(from, size, null);

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test getCompilations method
     * GET-request "/compilations", no required parameters
     * when parameter FROM is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getCompilations method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getCompilations_WhenParameterFromNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create parameters
        String from = "FROM";
        String size = "0";
        String pinned = "true";

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

        // map out object into string
        String expectedString = mapper.writeValueAsString(apiError);

        //perform tested request and check status and content
        String result = mock.perform(get(COMPILATIONS_PATH)
                        .param(PINNED_PARAMETER_NAME, pinned)
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
        verify(service, never()).getCompilations(any(), any(), any());

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test getCompilations method
     * GET-request "/compilations", no required parameters
     * when parameter SIZE is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getCompilations method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getCompilations_WhenParameterSizeNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

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
        String pinned = "true";

        //perform tested request and check status and content
        mock.perform(get(COMPILATIONS_PATH)
                        .param(PINNED_PARAMETER_NAME, pinned)
                        .param(SIZE_PARAMETER_NAME, size)
                        .param(FROM_PARAMETER_NAME, from))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service, never()).getCompilations(any(), any(), any());

    }

    /**
     * test getCompilationById method
     * GET-request ""/compilations/{compId}", no required parameters
     * should return status ok
     * should invoke service getCompilationById method and return compilation
     */
    @Test
    @SneakyThrows
    public void getCompilationById_StatusIsOk_AndInvokeService_ReturnCompilation() {

        // create UserShortDto initiator
        Long userId = 1L;
        String userName = "Egor";
        UserShortDto initiator = UserShortDto.builder()
                .id(userId)
                .name(userName)
                .build();

        // create CategoryDto category
        Long categoryId = 1L;
        String categoryName = "concert";
        CategoryDto categoryDto = CategoryDto.builder()
                .id(categoryId)
                .name(categoryName)
                .build();

        // create List of EventShorDto objects
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        String annotation1 = "This is valid annotation";
        String annotation2 = "This is valid annotation";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2026, 1, 1, 1, 1, 1);
        String title1 = "title";
        String title2 = "title";
        Integer confirmedRequests1 = 0;
        Integer confirmedRequests2 = 1;
        Long views1 = 0L;
        Long views2 = 1L;

        EventShortDto eventShortDto1 = EventShortDto.builder()
                .id(eventId1)
                .annotation(annotation1)
                .category(categoryDto)
                .confirmedRequests(confirmedRequests1)
                .eventDate(eventDate1)
                .initiator(initiator)
                .paid(false)
                .title(title1)
                .views(views1)
                .build();

        EventShortDto eventShortDto2 = EventShortDto.builder()
                .id(eventId2)
                .annotation(annotation2)
                .category(categoryDto)
                .confirmedRequests(confirmedRequests2)
                .eventDate(eventDate2)
                .initiator(initiator)
                .paid(true)
                .title(title2)
                .views(views2)
                .build();

        List<EventShortDto> events1 = List.of(eventShortDto1, eventShortDto2);

        // create expected out Compilation
        Integer compilationId = 1;
        String twoEventsCompilationTitle = "compilationTitle";
        CompilationDto compilationDto = CompilationDto.builder()
                .id(compilationId)
                .events(events1)
                .title(twoEventsCompilationTitle)
                .pinned(true)
                .build();


        // map out object into string
        String expectedString = mapper.writeValueAsString(compilationDto);

        //mock service answer
        when(service.getCompilationById(compilationId)).thenReturn(compilationDto);

        //perform tested request and check status and content
        String compPathVariable = "/1";
        String result = mock.perform(get(COMPILATIONS_PATH + compPathVariable))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",
                        is(compilationDto.getId()), Integer.class))
                .andExpect(jsonPath("$.events.[0].id",
                        is(events1.get(0).getId()), Long.class))
                .andExpect(jsonPath("$.events.[0].annotation",
                        is(events1.get(0).getAnnotation()), String.class))
                .andExpect(jsonPath("$.events.[0].category",
                        is(events1.get(0).getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.events.[0].category.id",
                        is(events1.get(0).getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.events.[0].category.name",
                        is(events1.get(0).getCategory().getName()), String.class))
                .andExpect(jsonPath("$.events.[0].confirmedRequests",
                        is(events1.get(0).getConfirmedRequests()), Integer.class))
                .andExpect(jsonPath("$.events.[0].eventDate",
                        is(events1.get(0).getEventDate()
                                .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.events.[0].initiator.id",
                        is(events1.get(0).getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.events.[0].initiator.name",
                        is(events1.get(0).getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.events.[0].paid",
                        is(events1.get(0).getPaid()), Boolean.class))
                .andExpect(jsonPath("$.events.[0].title",
                        is(events1.get(0).getTitle()), String.class))
                .andExpect(jsonPath("$.events.[0].views",
                        is(events1.get(0).getViews()), Long.class))
                .andExpect(jsonPath("$.title",
                        is(compilationDto.getTitle()), String.class))
                .andExpect(jsonPath("$.pinned",
                        is(compilationDto.getPinned()), Boolean.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCompilationById(compilationId);

        //check result
        assertEquals(result, expectedString);
    }

    /**
     * test getCompilationById method
     * GET-request ""/compilations/{compId}", no required parameters
     * when path variable is not valid
     * should return status bad request
     * should not invoke service getCompilationById method and return apiError
     */
    @Test
    @SneakyThrows
    public void getCompilationById_whenPathVariableNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer';"
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
        mock.perform(get(COMPILATIONS_PATH + invalidPathVariable))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service, never()).getCompilationById(any());

    }

    /**
     * test getCompilationById method
     * GET-request "/compilations/{compId}", no required parameters
     * when compilation not found
     * should return status not found
     * should invoke service getCompilationById method and return apiError
     */
    @Test
    @SneakyThrows
    public void getCompilationById_whenObjectNotFound_StatusIsNotFound_InvokeService_ReturnApiError() {

        Integer compilationId = -1;

        // create expected out Object
        String message = "Compilation with id=" + compilationId + "was not found";

        //mock service answer
        when(service.getCompilationById(compilationId)).thenThrow(new NotFoundException(message));

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String pathVariable = "/-1";
        mock.perform(get(COMPILATIONS_PATH + pathVariable))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getCompilationById(compilationId);

    }

}
