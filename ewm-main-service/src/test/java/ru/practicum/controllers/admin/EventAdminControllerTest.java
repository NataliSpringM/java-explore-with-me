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
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.entity.Category;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.StateAction;
import ru.practicum.service.event.EventService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;
import ru.practicum.utils.mapper.EventMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.*;
import static ru.practicum.utils.errors.ErrorConstants.*;
import static ru.practicum.utils.formatter.DateTimeFormatter.DATE_TIME_FORMATTER;

/**
 * EventAdminController WebMvcTest
 **/

@WebMvcTest(EventAdminController.class)
@AutoConfigureMockMvc
public class EventAdminControllerTest {
    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    EventService service;
    Long eventId;

    @BeforeEach
    public void create() {
        eventId = 1L;
    }

    /**
     * test getEventsByAdmin when parameters are not set
     * method GET-request "/admin/events"
     * should return status OK
     * should invoke service getEventsByAdmin
     * method and return result
     */
    @Test
    @SneakyThrows
    public void getEventsByAdmin_whenParametersAreNotSet_returnStatusOk_invokeService_andReturnEventsFullDtoList() {

        // create valid data
        String annotation1 = "This is valid annotation";
        String description1 = "This is valid description";
        String annotation2 = "This is valid annotation";
        String description2 = "This is valid description";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2025, 2, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title1 = "title1";
        String title2 = "title2";
        Long categoryId = 1L;

        // create NewEventDto
        NewEventDto eventIn1 = NewEventDto.builder()
                .annotation(annotation1)
                .category(categoryId)
                .description(description1)
                .eventDate(eventDate1)
                .location(location)
                .title(title1)
                .build();
        NewEventDto eventIn2 = NewEventDto.builder()
                .annotation(annotation2)
                .category(categoryId)
                .description(description2)
                .eventDate(eventDate2)
                .location(location)
                .title(title2)
                .build();


        // create expected out EventFullDto:
        String userName = "Egor";
        String userEmail = "Egor@yandex.ru";
        User initiator = User.builder()
                .name(userName)
                .email(userEmail)
                .build();
        String categoryName = "concert";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        EventFullDto eventOut1 = EventMapper
                .toEventFullDto(EventMapper
                        .toEventEntity(eventIn1, location, initiator, category))
                .toBuilder()
                .id(eventId)
                .build();
        EventFullDto eventOut2 = EventMapper
                .toEventFullDto(EventMapper
                        .toEventEntity(eventIn2, location, initiator, category))
                .toBuilder()
                .id(eventId)
                .build();
        List<EventFullDto> events = List.of(eventOut1, eventOut2);

        // map input and out objects into strings
        String expectedEventString = mapper.writeValueAsString(events);

        //mock service answer
        when(service
                .getEventsByAdmin(null, null, null, null, null, 0, 10)).thenReturn(events);

        //perform tested request and check status and content.
        String result = mock.perform(get(ADMIN_PATH + EVENTS_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        // verify invokes
        verify(service).getEventsByAdmin(null, null, null, null, null, 0, 10);

        //check result
        assertEquals(expectedEventString, result);
    }

    /**
     * test getEventsByAdmin
     * method GET-request "/admin/events"
     * should return status OK
     * should invoke service getEventsByAdmin
     * method and return result
     */
    @Test
    @SneakyThrows
    public void getEventsByAdmin_returnStatusOk_invokeService_andReturnEventsFullDtoList() {

        // create valid data
        String annotation1 = "This is valid annotation";
        String description1 = "This is valid description";
        String annotation2 = "This is valid annotation";
        String description2 = "This is valid description";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2025, 2, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title1 = "title1";
        String title2 = "title2";
        Long categoryId = 1L;

        // create NewEventDto
        NewEventDto eventIn1 = NewEventDto.builder()
                .annotation(annotation1)
                .category(categoryId)
                .description(description1)
                .eventDate(eventDate1)
                .location(location)
                .title(title1)
                .build();
        NewEventDto eventIn2 = NewEventDto.builder()
                .annotation(annotation2)
                .category(categoryId)
                .description(description2)
                .eventDate(eventDate2)
                .location(location)
                .title(title2)
                .build();


        // create expected out EventFullDto:
        String userName = "Egor";
        String userEmail = "Egor@yandex.ru";
        Long userId = 1L;
        User initiator = User.builder()
                .name(userName)
                .email(userEmail)
                .build();

        List<Long> users = List.of(userId);
        List<String> states = List.of(EventState.PENDING.name());
        List<Long> categories = List.of(categoryId);

        String categoryName = "concert";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        EventFullDto eventOut1 = EventMapper
                .toEventFullDto(EventMapper
                        .toEventEntity(eventIn1, location, initiator, category))
                .toBuilder()
                .id(eventId)
                .build();
        EventFullDto eventOut2 = EventMapper
                .toEventFullDto(EventMapper
                        .toEventEntity(eventIn2, location, initiator, category))
                .toBuilder()
                .id(eventId)
                .build();
        List<EventFullDto> events = List.of(eventOut1, eventOut2);


        // map input and out objects into strings
        String expectedEventString = mapper.writeValueAsString(events);

        //mock service answer
        when(service.getEventsByAdmin(users, states, categories, eventDate1, eventDate2, 0, 10)).thenReturn(events);

        //perform tested request and check status and content.
        String result = mock.perform(get(ADMIN_PATH + EVENTS_PATH)
                        .param(USERS_PARAM_NAME, userId.toString())
                        .param(STATES_PARAMETER_NAME, EventState.PENDING.name())
                        .param(CATEGORIES_PARAMETER_NAME, categoryId.toString())
                        .param(RANGE_START_PARAMETER_NAME, eventDate1.format(DATE_TIME_FORMATTER))
                        .param(RANGE_END_PARAMETER_NAME, eventDate2.format(DATE_TIME_FORMATTER))
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventOut1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventOut1.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].category", is(eventOut1.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(0L), Long.class))
                .andExpect(jsonPath("$.[0].createdOn").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.[0].description", is(eventOut1.getDescription()), String.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventOut1.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.[0].location", is(eventOut1.getLocation()), Location.class))
                .andExpect(jsonPath("$.[0].paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.[0].participantLimit", is(0L), Long.class))
                .andExpect(jsonPath("$.[0].publishedOn", is(eventOut1.getPublishedOn()), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].requestModeration", is(true), Boolean.class))
                .andExpect(jsonPath("$.[0].state", is(EventState.PENDING.name()), String.class))
                .andExpect(jsonPath("$.[0].title", is(eventOut1.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].views", is(eventOut1.getViews()), Long.class))
                .andExpect(jsonPath("$.[1].id", is(eventOut2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].annotation", is(eventOut2.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[1].category", is(eventOut2.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[1].confirmedRequests", is(0L), Long.class))
                .andExpect(jsonPath("$.[1].createdOn").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.[1].description", is(eventOut2.getDescription()), String.class))
                .andExpect(jsonPath("$.[1].eventDate", is(eventOut2.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.[1].location", is(eventOut2.getLocation()), Location.class))
                .andExpect(jsonPath("$.[1].paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.[1].participantLimit", is(0L), Long.class))
                .andExpect(jsonPath("$.[1].publishedOn", is(eventOut2.getPublishedOn()), LocalDateTime.class))
                .andExpect(jsonPath("$.[1].requestModeration", is(true), Boolean.class))
                .andExpect(jsonPath("$.[1].state", is(EventState.PENDING.name()), String.class))
                .andExpect(jsonPath("$.[1].title", is(eventOut2.getTitle()), String.class))
                .andExpect(jsonPath("$.[1].views", is(eventOut2.getViews()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getEventsByAdmin(users, states, categories, eventDate1, eventDate2, 0, 10);

        //check result
        assertEquals(expectedEventString, result);
    }


    /**
     * test getEventsByAdmin with invalid parameters
     * method GET-request "/admin/events"
     * should return status bad request
     * should not invoke service getEventsByAdmin method and return ApiError
     */

    @Test
    @SneakyThrows

    public void getEventsByAdminWithInvalidParamUsers_returnStatusBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create  parameters
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusMonths(1);
        List<EventFullDto> events = Collections.emptyList();
        Long categoryId = 1L;
        Long userId = 1L;

        List<Long> users = List.of(userId);
        List<String> states = List.of(EventState.PENDING.name());
        List<Long> categories = List.of(categoryId);

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.util.List';"
                + " nested exception is java.lang.NumberFormatException: For input string: \"[1]\"";

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //mock service answer
        when(service.getEventsByAdmin(users, states, categories, start, end, 0, 10)).thenReturn(events);

        //perform tested request and check status and content.
        mock.perform(get(ADMIN_PATH + EVENTS_PATH)
                        .param(USERS_PARAM_NAME, users.toString())
                        .param(STATES_PARAMETER_NAME, EventState.PENDING.name())
                        .param(CATEGORIES_PARAMETER_NAME, categoryId.toString())
                        .param(RANGE_START_PARAMETER_NAME, start.format(DATE_TIME_FORMATTER))
                        .param(RANGE_END_PARAMETER_NAME, end.format(DATE_TIME_FORMATTER))
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service, never()).getEventsByAdmin(users, states, categories, start, end, 0, 10);

    }

    /**
     * test getEventsByAdmin when parameter FROM is not valid
     * method GET-request "/admin/events"
     * should return status bad request
     * should not invoke service getEventsByAdmin method and return ApiError
     */
    @Test
    @SneakyThrows
    public void getEventsByAdmin_whenParameterFromNotValid_StatusBadRequest_DoesNotInvokeService_ReturnApiError() {

        //create parameters
        String from = "FROM";
        String size = "10";

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

        //perform tested request and check status and content.
        mock.perform(get(ADMIN_PATH + EVENTS_PATH)
                        .param(FROM_PARAMETER_NAME, from)
                        .param(SIZE_PARAMETER_NAME, size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());

    }

    /**
     * test getEventsByAdmin when parameter SIZE is not valid
     * method GET-request "/admin/events"
     * should return status bad request
     * should not invoke service getEventsByAdmin method and return ApiError
     */
    @Test
    @SneakyThrows
    public void getEventsByAdmin_whenParameterSizeNotValid_StatusBadRequest_DoesNotInvokeService_ReturnApiError() {

        String from = "0";
        String size = "SIZE";

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

        //perform tested request and check status and content.
        mock.perform(get(ADMIN_PATH + EVENTS_PATH)
                        .param(FROM_PARAMETER_NAME, from)
                        .param(SIZE_PARAMETER_NAME, size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getEventsByAdmin(any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * test updateEventByAdmin
     * PATCH-request to the endpoint "/admin/events/{eventId}"
     * should return status ok
     * should invoke service updateEventByAdmin method and return updated Event
     */
    @Test
    @SneakyThrows
    public void updateEventsByAdmin_whenRequestIsValid_StatusOk_InvokeService_ReturnEvent() {

        // create valid data
        String annotation = "This is valid annotation";
        String description1 = "This is valid description";
        LocalDateTime eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";

        // create request to update
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .annotation(annotation)
                .description(description1)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        // create expected out Object
        EventFullDto eventOut = EventFullDto.builder()
                .annotation(annotation)
                .description(description1)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .state(EventState.PUBLISHED)
                .build();

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //mock service answer
        when(service.updateEventByAdmin(eventId, request)).thenReturn(eventOut);

        //perform tested request and check status and content.
        String eventPathId = "/1";
        mock.perform(patch(ADMIN_PATH + EVENTS_PATH + eventPathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventOut.getId()), Long.class))
                .andExpect(jsonPath("$.annotation", is(eventOut.getAnnotation()), String.class))
                .andExpect(jsonPath("$.category", is(eventOut.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.description", is(eventOut.getDescription()), String.class))
                .andExpect(jsonPath("$.eventDate", is(eventOut.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.location", is(eventOut.getLocation()), Location.class))
                .andExpect(jsonPath("$.state", is(EventState.PUBLISHED.name()), String.class))
                .andExpect(jsonPath("$.title", is(eventOut.getTitle()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).updateEventByAdmin(eventId, request);
    }

    /**
     * test updateEventByAdmin
     * PATCH-request to the endpoint "/admin/events/{eventId}"
     * when event is not found by id
     * should return status not found
     * should invoke service updateEventByAdmin method and return ApiError
     */
    @Test
    @SneakyThrows
    public void updateEventsByAdmin_whenEventNotFound_StatusBadRequest_InvokeService_ReturnApiError() {

        // create data
        Long eventId = -1L;
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        // create expected out Object
        String message = ErrorConstants.getNotFoundMessage("Event", eventId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //mock service answer
        when(service.updateEventByAdmin(eventId, request)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content.
        String eventPathId = "/-1";
        mock.perform(patch(ADMIN_PATH + EVENTS_PATH + eventPathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateEventByAdmin(eventId, request);

    }

    /**
     * test updateEventByAdmin
     * PATCH-request to the endpoint "/admin/events/{eventId}"
     * when event is already published
     * should return status conflict
     * should invoke service updateEventByAdmin method and return ApiError
     */
    @Test
    @SneakyThrows
    public void updateEventsByAdmin_whenEventIsPublished_StatusConflict_InvokeService_ReturnApiError() {

        // create data
        Long eventId = 1L;
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();

        // create expected out Object
        String message = EVENT_IS_PUBLISHED;
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(ACTION_IS_NOT_ALLOWED)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //mock service answer
        when(service.updateEventByAdmin(eventId, request)).thenThrow(new NotAllowedException(message));

        //perform tested request and check status and content.
        String eventPathId = "/1";
        mock.perform(patch(ADMIN_PATH + EVENTS_PATH + eventPathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateEventByAdmin(eventId, request);
    }
}
