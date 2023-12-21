package ru.practicum.controllers.priv;

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
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.service.event.EventService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;
import ru.practicum.utils.mapper.EventMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.*;
import static ru.practicum.utils.errors.ErrorConstants.*;
import static ru.practicum.utils.formatter.DateTimeFormatter.DATE_TIME_FORMATTER;

/**
 * EventPrivateController WebMvcTest
 */

@WebMvcTest(EventPrivateController.class)
@AutoConfigureMockMvc
public class EventPrivateControllerTest {

    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    EventService service;
    Long eventId;
    Long userId;
    String userPath;
    String eventPath;
    ParticipationRequestDto requestDto1;
    ParticipationRequestDto requestDto2;

    @BeforeEach
    public void create() {
        userId = 1L;
        eventId = 1L;
        userPath = "/1";
        eventPath = "/1";

    }

    /**
     * test addEvent method
     * POST-request "/users/{userId}/events"
     * when event data are valid
     * should return status CREATED 201
     * should invoke service addEvent method
     * should return EventFullDto
     */
    @Test
    @SneakyThrows
    public void addEvent_WhenEventIsValid_InvokeService_StatusIsCreated_ReturnEventDto() {

        // create valid data
        String annotation = "This is valid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        Long categoryId = 1L;

        // create NewEventDto
        NewEventDto eventIn = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .build();


        // create expected out EventFullDto:
        String userName = "Egor";
        String userEmail = "Egor@yandex.ru";
        Long userId = 1L;
        User initiator = User.builder()
                .name(userName)
                .email(userEmail)
                .build();
        String categoryName = "concert";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        EventFullDto eventOut = EventMapper
                .toEventFullDto(EventMapper
                        .toEventEntity(eventIn, location, initiator, category))
                .toBuilder()
                .id(eventId)
                .build();

        // map input and out objects into strings
        String eventInString = mapper.writeValueAsString(eventIn);
        String expectedEventString = mapper.writeValueAsString(eventOut);

        //mock service answer
        when(service.addEvent(userId, eventIn)).thenReturn(eventOut);

        //perform tested request and check status and content
        String result = mock.perform(post(USERS_PATH + userPath + EVENTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventInString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(eventOut.getId()), Long.class))
                .andExpect(jsonPath("$.annotation", is(eventOut.getAnnotation()), String.class))
                .andExpect(jsonPath("$.category", is(eventOut.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.confirmedRequests", is(0L), Long.class))
                .andExpect(jsonPath("$.createdOn").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.description", is(eventOut.getDescription()), String.class))
                .andExpect(jsonPath("$.eventDate", is(eventOut.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.location", is(eventOut.getLocation()), Location.class))
                .andExpect(jsonPath("$.paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.participantLimit", is(0L), Long.class))
                .andExpect(jsonPath("$.publishedOn", is(eventOut.getPublishedOn()), LocalDateTime.class))
                .andExpect(jsonPath("$.requestModeration", is(true), Boolean.class))
                .andExpect(jsonPath("$.state", is(EventState.PENDING.name()), String.class))
                .andExpect(jsonPath("$.title", is(eventOut.getTitle()), String.class))
                .andExpect(jsonPath("$.views", is(eventOut.getViews()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).addEvent(userId, eventIn);

        //check result
        assertEquals(expectedEventString, result);
    }

    /**
     * test addEvent method
     * POST-request "/user/{userId}/events"
     * when event data is not valid, has invalid annotation
     * should return status BAD REQUEST 400
     * should not invoke service addEvent method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addEvent_WhenEventHasNoTitle_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {
        // create invalid data : annotation length is less than 20 characters
        String annotation = "invalid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        Long categoryId = 1L;

        // create NewEventDto
        NewEventDto eventIn = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .build();


        // create expected out Object
        String fieldName = "annotation";
        String defaultMessage = "size must be between 20 and 2000";
        String message = "Field: " + fieldName + ". Error: " + defaultMessage + ". Value: " + annotation;

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(eventIn);

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userPath + EVENTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addEvent(any(), (any()));
    }

    /**
     * test addEvent method
     * POST-request "/users/{userId}/events"
     * when event data is not valid, has already existing title
     * should return status CONFLICT 409
     * should return ApiError
     * should invoke service addEvent method
     */
    @Test
    @SneakyThrows
    public void addEvent_WhenEventHasSameName_InvokeService_StatusIsConflict_ReturnApiError() {

        // create valid data
        String annotation = "This is valid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        Long categoryId = 1L;
        Long userId = 1L;

        // create NewEventDto
        NewEventDto eventIn = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .build();

        String message = "could not execute statement; "
                + "SQL [n/a]; constraint [uq_event_annotation]; "
                + "nested exception is org.hibernate.exception.ConstraintViolationException: "
                + "could not execute statement";

        //mock service response
        when(service.addEvent(userId, eventIn)).thenThrow(new DataIntegrityViolationException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String categoryInString = mapper.writeValueAsString(eventIn);

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userPath + EVENTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryInString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()),
                        String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).addEvent(userId, eventIn);
    }

    /**
     * test getEventByUser method
     * GET-request to the endpoint "users/{userId}/events/{eventId}"
     * when request are valid
     * should return status OK 200
     * should invoke service getEventByUser method
     * should return EventFullDto event
     */
    @Test
    @SneakyThrows
    public void getEventByUser_WhenRequestIsValid_InvokeService_StatusIsOk_ReturnEventFullDto() {

        // create valid data
        String annotation = "This is valid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        Long categoryId = 1L;

        // create NewEventDto
        NewEventDto eventIn = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .build();


        // create expected out EventFullDto:
        String userName = "Egor";
        String userEmail = "Egor@yandex.ru";
        Long userId = 1L;
        User initiator = User.builder()
                .name(userName)
                .email(userEmail)
                .build();
        String categoryName = "concert";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        EventFullDto eventOut = EventMapper
                .toEventFullDto(EventMapper
                        .toEventEntity(eventIn, location, initiator, category))
                .toBuilder()
                .id(eventId)
                .build();

        // map out objects into string
        String expectedEventString = mapper.writeValueAsString(eventOut);

        //mock service answer
        when(service.getEventByUser(userId, eventId)).thenReturn(eventOut);

        //perform tested request and check status and content
        String result = mock.perform(get(USERS_PATH + userPath + EVENTS_PATH + eventPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventOut.getId()), Long.class))
                .andExpect(jsonPath("$.annotation", is(eventOut.getAnnotation()), String.class))
                .andExpect(jsonPath("$.category", is(eventOut.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.confirmedRequests", is(0L), Long.class))
                .andExpect(jsonPath("$.createdOn").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.description", is(eventOut.getDescription()), String.class))
                .andExpect(jsonPath("$.eventDate", is(eventOut.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.location", is(eventOut.getLocation()), Location.class))
                .andExpect(jsonPath("$.paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.participantLimit", is(0L), Long.class))
                .andExpect(jsonPath("$.publishedOn", is(eventOut.getPublishedOn()), LocalDateTime.class))
                .andExpect(jsonPath("$.requestModeration", is(true), Boolean.class))
                .andExpect(jsonPath("$.state", is(EventState.PENDING.name()), String.class))
                .andExpect(jsonPath("$.title", is(eventOut.getTitle()), String.class))
                .andExpect(jsonPath("$.views", is(eventOut.getViews()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getEventByUser(userId, eventId);

        //check result
        assertEquals(expectedEventString, result);
    }

    /**
     * test getEventByUser method
     * GET-request to the endpoint "users/{userId}/events/{eventId}"
     * when request is invalid : invalid pathVariable
     * should return status BAD REQUEST 400
     * should not invoke service getEventByUser method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void getEventByUser_WhenPathVariableInvalid_NotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create data
        String invalidEventId = "/-1L";

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; "
                + "nested exception is java.lang.NumberFormatException: For input string: \"-1L\"";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + userPath + EVENTS_PATH + invalidEventId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getEventByUser(any(), any());
    }

    /**
     * test getEventByUser method
     * GET-request to the endpoint "users/{userId}/events/{eventId}"
     * when request is invalid at the repository level: event is not found
     * should return status NOT FOUND 404
     * should invoke service getEventByUser method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void getEventByUser_WhenEventNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create expected out Object
        String message = getNotFoundMessage("Event", eventId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .build();

        // mock service answer
        when(service.getEventByUser(userId, eventId)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + userPath + EVENTS_PATH + eventPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).getEventByUser(userId, eventId);
    }


    /**
     * test getEventsByUser method
     * GET-request to the endpoint "users/{userId}/events"
     * when request is valid
     * should return status OK 200
     * should invoke service getEventByUsers method
     * should return List<EventShortDto> list of events
     */
    @Test
    @SneakyThrows
    public void getEventsByUser_WhenRequestIsValid_InvokeService_StatusIsOk_ReturnListOfEvents() {

        // create valid data
        String annotation = "This is valid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2025, 2, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        Long categoryId = 1L;

        // create NewEventDto objects
        NewEventDto eventIn1 = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate1)
                .location(location)
                .title(title)
                .build();

        NewEventDto eventIn2 = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate2)
                .location(location)
                .title(title)
                .build();

        // create expected out object:
        String userName = "Egor";
        String userEmail = "Egor@yandex.ru";
        Long userId = 1L;
        User initiator = User.builder()
                .name(userName)
                .email(userEmail)
                .build();
        String categoryName = "concert";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        EventShortDto eventOut1 = EventMapper
                .toEventShortDto(EventMapper
                        .toEventEntity(eventIn1, location, initiator, category))
                .toBuilder()
                .id(eventId1)
                .build();
        EventShortDto eventOut2 = EventMapper
                .toEventShortDto(EventMapper
                        .toEventEntity(eventIn2, location, initiator, category))
                .toBuilder()
                .id(eventId2)
                .build();
        List<EventShortDto> events = List.of(eventOut1, eventOut2);

        // map out object into string
        String expectedEventString = mapper.writeValueAsString(events);

        //mock service answer
        Integer from = 0;
        Integer size = 2;
        when(service.getEventsByUser(userId, from, size)).thenReturn(events);

        //perform tested request and check status and content
        String result = mock.perform(get(USERS_PATH + userPath + EVENTS_PATH)
                        .param(FROM_PARAMETER_NAME, from.toString())
                        .param(SIZE_PARAMETER_NAME, size.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventOut1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventOut1.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].category", is(eventOut1.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventOut1.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.[0].paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.[0].title", is(eventOut1.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].views", is(eventOut1.getViews()), Long.class))
                .andExpect(jsonPath("$.[1].id", is(eventOut2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].annotation", is(eventOut2.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[1].category", is(eventOut2.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[1].eventDate", is(eventOut2.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[1].initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.[1].initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.[1].paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.[1].title", is(eventOut2.getTitle()), String.class))
                .andExpect(jsonPath("$.[1].views", is(eventOut2.getViews()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getEventsByUser(userId, from, size);

        //check result
        assertEquals(expectedEventString, result);
    }


    /**
     * test getEventsByUser method
     * GET-request to the endpoint "users/{userId}/events"
     * when request is valid and parameters are not set
     * should use default values
     * should return status OK 200
     * should invoke service getEventByUsers method
     * should return List<EventShortDto> list of events
     */
    @Test
    @SneakyThrows
    public void getEventsByUser_WhenRequestIsValid_ParametersAreNotSet_InvokeService_StatusIsOk_ReturnListOfEvents() {

        // create valid data
        String annotation = "This is valid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate1 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        LocalDateTime eventDate2 = LocalDateTime.of(2025, 2, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        Long categoryId = 1L;

        // create NewEventDto objects
        NewEventDto eventIn1 = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate1)
                .location(location)
                .title(title)
                .build();

        NewEventDto eventIn2 = NewEventDto.builder()
                .annotation(annotation)
                .category(categoryId)
                .description(description)
                .eventDate(eventDate2)
                .location(location)
                .title(title)
                .build();

        // create expected out object:
        String userName = "Egor";
        String userEmail = "Egor@yandex.ru";
        Long userId = 1L;
        User initiator = User.builder()
                .name(userName)
                .email(userEmail)
                .build();
        String categoryName = "concert";
        Category category = Category.builder()
                .name(categoryName)
                .build();
        Long eventId1 = 1L;
        Long eventId2 = 2L;
        EventShortDto eventOut1 = EventMapper
                .toEventShortDto(EventMapper
                        .toEventEntity(eventIn1, location, initiator, category))
                .toBuilder()
                .id(eventId1)
                .build();
        EventShortDto eventOut2 = EventMapper
                .toEventShortDto(EventMapper
                        .toEventEntity(eventIn2, location, initiator, category))
                .toBuilder()
                .id(eventId2)
                .build();
        List<EventShortDto> events = List.of(eventOut1, eventOut2);

        // map out object into string
        String expectedEventString = mapper.writeValueAsString(events);

        //mock service answer
        Integer from = 0;
        Integer size = 10;
        when(service.getEventsByUser(userId, from, size)).thenReturn(events);

        //perform tested request and check status and content
        String result = mock.perform(get(USERS_PATH + userPath + EVENTS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventOut1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventOut1.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].category", is(eventOut1.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventOut1.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.[0].paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.[0].title", is(eventOut1.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].views", is(eventOut1.getViews()), Long.class))
                .andExpect(jsonPath("$.[1].id", is(eventOut2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].annotation", is(eventOut2.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[1].category", is(eventOut2.getCategory()), CategoryDto.class))
                .andExpect(jsonPath("$.[1].eventDate", is(eventOut2.getEventDate()
                        .format(DATE_TIME_FORMATTER)), LocalDateTime.class))
                .andExpect(jsonPath("$.[1].initiator.id", is(initiator.getId()), Long.class))
                .andExpect(jsonPath("$.[1].initiator.name", is(initiator.getName()), String.class))
                .andExpect(jsonPath("$.[1].paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.[1].title", is(eventOut2.getTitle()), String.class))
                .andExpect(jsonPath("$.[1].views", is(eventOut2.getViews()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getEventsByUser(userId, from, size);

        //check result
        assertEquals(expectedEventString, result);
    }

    /**
     * test getEventsByUser method
     * GET-request to the endpoint "users/{userId}/events"
     * when request is invalid, parameter FROM is negative
     * should return status BAD REQUEST 400
     * should not invoke service getEventByUsers method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void getEventsByUser_WhenParameterFromIsInvalid_NotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create data
        String from = "-1";

        // create expected out Object
        String message = "Field: getEventsByUser.from. Error: must be greater than or equal to 0. Value: -1";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + userPath + EVENTS_PATH)
                        .param(FROM_PARAMETER_NAME, from)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getEventsByUser(any(), any(), any());
    }

    /**
     * test getParticipationRequests method
     * GET-request "users/{userId}/events/{eventId}/requests"
     * when request is invalid : invalid pathVariable userId
     * should return status OK 200
     * should invoke service getParticipationRequests method
     * should return requests made by user
     */
    @Test
    @SneakyThrows
    public void getParticipationRequests_whenInvalidPathVariable_InvokeService_StatusIsOk_ReturnListOfRequests() {

        // create data
        String invalidPathUserId = "/-1L";

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; "
                + "nested exception is java.lang.NumberFormatException: For input string: \"-1L\"";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + invalidPathUserId + EVENTS_PATH + eventPath + REQUESTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getParticipationRequests(any(), any());

    }

    /**
     * test getParticipationRequests method
     * GET-request "users/{userId}/events/{eventId}/requests"
     * when request is valid
     * should return status OK 200
     * should invoke service getParticipationRequests method
     * should return requests made by user
     */
    @Test
    @SneakyThrows
    public void getParticipationRequests_InvokeService_StatusIsOk_ReturnListOfRequests() {

        // create valid data
        Long userId = 1L;

        Long requesterId1 = 1L;
        Long requesterId2 = 2L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;

        //create ParticipationRequestsDto
        requestDto1 = ParticipationRequestDto.builder()
                .id(requestId1)
                .event(eventId)
                .created(LocalDateTime.now())
                .requester(requesterId1)
                .status(RequestStatus.PENDING)
                .build();
        requestDto2 = ParticipationRequestDto.builder()
                .id(requestId2)
                .event(eventId)
                .created(LocalDateTime.now())
                .requester(requesterId2)
                .status(RequestStatus.PENDING)
                .build();


        List<ParticipationRequestDto> requests = List.of(requestDto1, requestDto2);

        //mock service response
        when(service.getParticipationRequests(userId, eventId)).thenReturn(requests);

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + userPath + EVENTS_PATH + eventPath + REQUESTS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(requestDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].event", is(requestDto1.getEvent()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(requestDto1.getStatus().name()), RequestStatus.class))
                .andExpect(jsonPath("$.[0].created").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.[0].requester", is(requestDto1.getRequester()), Long.class))
                .andExpect(jsonPath("$.[1].id", is(requestDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].event", is(requestDto2.getEvent()), Long.class))
                .andExpect(jsonPath("$.[1].status", is(requestDto2.getStatus().name()), RequestStatus.class))
                .andExpect(jsonPath("$.[1].created").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.[1].requester", is(requestDto2.getRequester()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getParticipationRequests(userId, eventId);
    }

    /**
     * test updateEventByUser
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}"
     * should return status OK 200
     * should invoke service updateEventByUser method
     * should return updated Event
     */
    @Test
    @SneakyThrows
    public void updateEventByUser_whenRequestIsValid_StatusOk_InvokeService_ReturnEvent() {

        // create valid data
        Long userId = 1L;
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
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation(annotation)
                .description(description1)
                .eventDate(eventDate)
                .location(location)
                .title(title)
                .stateAction(StateAction.SEND_TO_REVIEW)
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
        when(service.updateEventByUser(userId, eventId, request)).thenReturn(eventOut);

        //perform tested request and check status and content.
        String userPathId = "/1";
        String eventPathId = "/1";
        mock.perform(patch(USERS_PATH + userPathId + EVENTS_PATH + eventPathId)
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
        verify(service).updateEventByUser(userId, eventId, request);
    }

    /**
     * test updateEventByUser
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}"
     * when event is not found by id
     * should return status NOT FOUND 404
     * should invoke service updateEventByUser method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateEventByUser_whenEventNotFound_StatusBadRequest_InvokeService_ReturnApiError() {

        // create data
        Long userId = 1L;
        Long eventId = -1L;
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
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
        when(service.updateEventByUser(userId, eventId, request)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content.
        String eventPathId = "/-1";
        String userPathId = "/1";
        mock.perform(patch(USERS_PATH + userPathId + EVENTS_PATH + eventPathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateEventByUser(userId, eventId, request);

    }

    /**
     * test updateEventByUser
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}"
     * when parameters are not valid
     * should return status BAD REQUEST 400
     * should not invoke service updateEventByUser method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateEventByUser_whenAnnotationIsNotValid_StatusBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create data
        Long userId = 1L;
        String invalid = "invalid";
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .annotation(invalid)
                .build();

        // create expected out Object
        String message = "Field: annotation. Error: size must be between 20 and 2000. Value: invalid";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //mock service answer
        when(service.updateEventByUser(userId, eventId, request)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content.
        String eventPathId = "/1";
        String userPathId = "/1";
        mock.perform(patch(USERS_PATH + userPathId + EVENTS_PATH + eventPathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).updateEventByUser(userId, eventId, request);

    }

    /**
     * test updateEventByUser
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}"
     * when event is already published
     * should return status CONFLICT 400
     * should invoke service updateEventByUser method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateEventByUser_whenEventIsPublished_StatusConflict_InvokeService_ReturnApiError() {

        // create data
        Long userId = 1L;
        Long eventId = 1L;
        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(StateAction.SEND_TO_REVIEW)
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
        when(service.updateEventByUser(userId, eventId, request)).thenThrow(new NotAllowedException(message));

        //perform tested request and check status and content.
        String eventPathId = "/1";
        String userPathId = "/1";
        mock.perform(patch(USERS_PATH + userPathId + EVENTS_PATH + eventPathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateEventByUser(userId, eventId, request);
    }

    /**
     * test updateRequestStatus
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}/requests"
     * should return status OK 200
     * should invoke service updateRequestStatus method
     * should return EventRequestStatusUpdateResult (lists of confirmed and rejected requests)
     */
    @Test
    @SneakyThrows
    public void updateRequestStatus_whenRequestIsValid_StatusOk_InvokeService_ReturnEventRequestStatusUpdateResult() {

        // create valid data
        Long userId = 1L;
        Long eventId = 1L;
        Long requesterId1 = 1L;
        Long requesterId2 = 2L;
        Long requestId1 = 1L;
        Long requestId2 = 2L;

        //create ParticipationRequestsDto
        requestDto1 = ParticipationRequestDto.builder()
                .id(requestId1)
                .event(eventId)
                .created(LocalDateTime.now())
                .requester(requesterId1)
                .status(RequestStatus.CONFIRMED)
                .build();
        requestDto2 = ParticipationRequestDto.builder()
                .id(requestId2)
                .event(eventId)
                .created(LocalDateTime.now())
                .requester(requesterId2)
                .status(RequestStatus.REJECTED)
                .build();

        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();

        // create expected out object
        EventRequestStatusUpdateResult resultObj = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(requestDto1))
                .rejectedRequests(List.of(requestDto2))
                .build();

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);
        String resultString = mapper.writeValueAsString(resultObj);

        //mock service answer
        when(service.updateRequestsStatus(userId, eventId, request)).thenReturn(resultObj);

        //perform tested request and check status and content.
        String userPathId = "/1";
        String eventPathId = "/1";
        String result = mock.perform(patch(USERS_PATH + userPathId + EVENTS_PATH + eventPathId + REQUESTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.[0].id",
                        is(requestDto1.getId()), Long.class))
                .andExpect(jsonPath("$.confirmedRequests.[0].event",
                        is(requestDto1.getEvent()), Long.class))
                .andExpect(jsonPath("$.confirmedRequests.[0].created")
                        .value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.confirmedRequests.[0].status",
                        is(requestDto1.getStatus().name()), RequestStatus.class))
                .andExpect(jsonPath("$.confirmedRequests.[0].requester",
                        is(requestDto1.getRequester()), Long.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].id",
                        is(requestDto2.getId()), Long.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].event",
                        is(requestDto2.getEvent()), Long.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].status",
                        is(requestDto2.getStatus().name()), RequestStatus.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].created")
                        .value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.rejectedRequests.[0].requester",
                        is(requestDto2.getRequester()), Long.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).updateRequestsStatus(userId, eventId, request);

        // check result
        assertEquals(result, resultString);
    }


    /**
     * test updateRequestStatus
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}/requests"
     * when parameters are not valid : eventId pathVariable is invalid
     * should return status BAD REQUEST 400
     * should not invoke service updateRequestStatus method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateRequestStatus_whenRequestIsNotValid_StatusBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create data
        String invalid = "/-1";
        Long requestId1 = 1L;
        Long requestId2 = 1L;
        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();

        // create expected out Object
        String message = "Field: updateRequestsStatus.eventId. Error: must be greater than 0. Value: -1";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //perform tested request and check status and content.
        mock.perform(patch(USERS_PATH + userPath + EVENTS_PATH + invalid + REQUESTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).updateRequestsStatus(any(), any(), any());
    }

    /**
     * test updateRequestStatus
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}/requests"
     * when request is not valid at the repository level : eventId not found
     * should return status NOT FOUND 404
     * should not invoke service updateRequestStatus method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateRequestStatus_whenEventNotFound_StatusNotFound_InvokeService_ReturnApiError() {

        // create data
        Long userId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 1L;
        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();

        // create expected out Object
        String message = getNotFoundMessage("Event", eventId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //mock service answer
        when(service.updateRequestsStatus(userId, eventId, request)).thenThrow(new NotFoundException(message));

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //perform tested request and check status and content.
        mock.perform(patch(USERS_PATH + userPath + EVENTS_PATH + eventPath + REQUESTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateRequestsStatus(any(), any(), any());
    }

    /**
     * test updateRequestStatus
     * PATCH-request to the endpoint "users/{userId}/events/{eventId}/requests"
     * when request is not valid at the service level : limit has been reached for all requests
     * should return status CONFLICT 409
     * should not invoke service updateRequestStatus method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void updateRequestStatus_whenLimitHasBeenReached_StatusConflict_InvokeService_ReturnApiError() {

        // create data
        Long userId = 1L;
        Long requestId1 = 1L;
        Long requestId2 = 1L;
        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatusUpdateRequest request = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();

        // create expected out Object
        String message = LIMIT;
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(ACTION_IS_NOT_ALLOWED)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //mock service answer
        when(service.updateRequestsStatus(userId, eventId, request)).thenThrow(new NotAllowedException(message));

        // map input and out objects into strings
        String requestString = mapper.writeValueAsString(request);

        //perform tested request and check status and content.
        mock.perform(patch(USERS_PATH + userPath + EVENTS_PATH + eventPath + REQUESTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).updateRequestsStatus(any(), any(), any());
    }

}
