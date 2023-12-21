package ru.practicum.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.controllers.admin.CategoryAdminController;
import ru.practicum.controllers.admin.EventAdminController;
import ru.practicum.controllers.admin.UserAdminController;
import ru.practicum.controllers.priv.EventPrivateController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.entity.Location;
import ru.practicum.enums.EventState;
import ru.practicum.enums.StateAction;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.mapper.EventMapper;
import ru.practicum.utils.mapper.UserMapper;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.utils.constants.Constants.TEN_DEFAULT_VALUE;
import static ru.practicum.utils.constants.Constants.ZERO_DEFAULT_VALUE;
import static ru.practicum.utils.errors.ErrorConstants.EVENT_IS_PUBLISHED;

/**
 * EVENT INTEGRATION TESTS
 **/
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EventTest {
    @Autowired
    UserAdminController userAdminController;
    @Autowired
    CategoryAdminController categoryAdminController;
    @Autowired
    EventPrivateController eventPrivateController;
    @Autowired
    EventAdminController eventAdminController;

    NewUserRequest requestUserAlex;
    NewUserRequest requestUserAnna;
    NewCategoryDto newConcertDto;
    NewCategoryDto newExhibitionDto;
    String annotation;
    String invalidAnnotation;
    String description;
    LocalDateTime eventDate;
    LocalDateTime invalidEventDate;
    Location location;
    Float latitude;
    Float longitude;
    String title;
    NewEventDto newEventDtoAllFieldsLimitIsSetEqualsTwo;
    NewEventDto newEventDtoAllFieldsLimitIsNotSet;


    @BeforeEach
    public void create() {

        //create new Users DTO
        String nameAlex = "nameAlex";
        String nameAnna = "Anna";
        String emailAlex = "Alex@yandex.ru";
        String emailAnna = "Anna@yandex.ru";

        requestUserAlex = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
        requestUserAnna = NewUserRequest.builder()
                .name(nameAnna)
                .email(emailAnna)
                .build();

        //create new Categories DTO
        String nameConcert = "concert";
        String nameExhibition = "exhibition";
        newConcertDto = NewCategoryDto.builder()
                .name(nameConcert)
                .build();
        newExhibitionDto = NewCategoryDto.builder()
                .name(nameExhibition)
                .build();


        //create new Events DTO
        annotation = "This is valid annotation";
        invalidAnnotation = "invalidAnnotation";
        description = "This is valid description";
        eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        invalidEventDate = LocalDateTime.now().plusMinutes(30);
        latitude = 54.55f;
        longitude = 55.677777f;
        location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        title = "title";
        newEventDtoAllFieldsLimitIsNotSet = NewEventDto.builder()
                .annotation(annotation)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .paid(true)
                .participantLimit(0)
                .requestModeration(true)
                .title(title)
                .build();
        newEventDtoAllFieldsLimitIsSetEqualsTwo = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .participantLimit(2)
                .build();
    }

    /**
     * should add and get event
     */
    @Test
    @Transactional
    public void shouldAddAndGetEventByInitiator() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();


        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // get event
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);

        // check event
        assertThat(event).hasFieldOrPropertyWithValue("id", eventId)
                .hasFieldOrPropertyWithValue("annotation", annotation)
                .hasFieldOrPropertyWithValue("category", category)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", UserMapper.toUserShortDto(initiator))
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PENDING)
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("views", 0L);

    }

    /**
     * should update event - send to review
     */
    @Test
    @Transactional
    public void shouldUpdateEventByInitiator() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // get event
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);

        // check event
        assertThat(event).hasFieldOrPropertyWithValue("id", eventId)
                .hasFieldOrPropertyWithValue("annotation", annotation)
                .hasFieldOrPropertyWithValue("category", category)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", UserMapper.toUserShortDto(initiator))
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PENDING)
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("views", 0L);

        // create event to update
        String newAnnotation = "We update event with the newAnnotation";
        String newTitle = "newTitle";
        UpdateEventUserRequest request = EventMapper
                .toUpdateEventUserRequest(event, StateAction.SEND_TO_REVIEW)
                .toBuilder()
                .annotation(newAnnotation)
                .paid(false)
                .participantLimit(555)
                .title(newTitle)
                .build();
        EventFullDto updatedEvent = eventPrivateController.updateEventByUser(initiatorId, eventId, request);

        // check only annotation, paid, participantLimit and title fields were updated

        assertThat(updatedEvent).hasFieldOrPropertyWithValue("id", eventId)
                .hasFieldOrPropertyWithValue("annotation", newAnnotation)
                .hasFieldOrPropertyWithValue("category", category)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", UserMapper.toUserShortDto(initiator))
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", false)
                .hasFieldOrPropertyWithValue("participantLimit", 555)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PENDING)
                .hasFieldOrPropertyWithValue("title", newTitle)
                .hasFieldOrPropertyWithValue("views", 0L);

    }

    /**
     * should update event by admin - publish event
     */
    @Test
    @Transactional
    public void shouldUpdateEventByAdmin() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // get event
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);

        // check event
        assertThat(event).hasFieldOrPropertyWithValue("id", eventId)
                .hasFieldOrPropertyWithValue("annotation", annotation)
                .hasFieldOrPropertyWithValue("category", category)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", UserMapper.toUserShortDto(initiator))
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PENDING)
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("views", 0L);

        // create event to update
        String newAnnotation = "We update event with the newAnnotation";
        String newTitle = "newTitle";
        UpdateEventAdminRequest request = EventMapper
                .toUpdateEventAdminRequest(event, StateAction.PUBLISH_EVENT)
                .toBuilder()
                .annotation(newAnnotation)
                .paid(false)
                .participantLimit(555)
                .title(newTitle)
                .build();
        EventFullDto updatedEvent = eventAdminController.updateEventByAdmin(eventId, request);

        // check only annotation, paid, participantLimit and title fields were updated

        assertThat(updatedEvent).hasFieldOrPropertyWithValue("id", eventId)
                .hasFieldOrPropertyWithValue("annotation", newAnnotation)
                .hasFieldOrPropertyWithValue("category", category)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", UserMapper.toUserShortDto(initiator))
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", false)
                .hasFieldOrPropertyWithValue("participantLimit", 555)
                .hasFieldOrProperty("publishedOn")
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PUBLISHED)
                .hasFieldOrPropertyWithValue("title", newTitle)
                .hasFieldOrPropertyWithValue("views", 0L);

    }

    /**
     * should fail add event with invalid time (start is earlier than two hours after the current moment)
     */
    @Test
    public void shouldFailAddEventByInitiatorWithInvalidTime() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .eventDate(invalidEventDate)
                .build();

        // check exception was thrown
        assertThrows(ConstraintViolationException.class,
                () -> eventPrivateController.addEvent(initiatorId, newEvent),
                "ConstraintViolationException was not thrown updating with invalid time");

    }

    /**
     * should fail update event with invalid request (annotation length is less than required)
     */
    @Test
    @Transactional
    public void shouldFailUpdateEventByInitiatorWithInvalidRequestAnnotation() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // get event
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);

        // check event
        assertThat(event).hasFieldOrPropertyWithValue("id", eventId)
                .hasFieldOrPropertyWithValue("annotation", annotation)
                .hasFieldOrPropertyWithValue("category", category)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", UserMapper.toUserShortDto(initiator))
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PENDING)
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("views", 0L);

        // create event to update
        String newAnnotation = invalidAnnotation;
        UpdateEventUserRequest request = EventMapper
                .toUpdateEventUserRequest(event, StateAction.SEND_TO_REVIEW)
                .toBuilder()
                .annotation(newAnnotation)
                .build();

        // check exception was thrown
        assertThrows(ConstraintViolationException.class,
                () -> eventPrivateController.updateEventByUser(initiatorId, eventId, request),
                "ConstraintViolationException was not thrown updating to short annotation");

    }

    /**
     * should fail update event with invalid request (annotation length is less than required)
     */
    @Test
    @Transactional
    public void shouldFailUpdateEventByInitiatorWithInvalidRequestTime() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // get event
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);

        // create event to update
        UpdateEventUserRequest request = EventMapper
                .toUpdateEventUserRequest(event, StateAction.SEND_TO_REVIEW)
                .toBuilder()
                .eventDate(invalidEventDate)
                .build();

        // check exception was thrown
        assertThrows(ConstraintViolationException.class,
                () -> eventPrivateController.updateEventByUser(initiatorId, eventId, request),
                "ConstraintViolationException was not thrown updating with invalid time");

    }

    /**
     * should fail update event with invalid request (StateAction type is not allowed for User)
     */
    @Test
    @Transactional
    public void shouldFailUpdateEventByInitiatorWithInvalidUserStateAction() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // get event
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);


        // create event to update
        StateAction invalidUserAction = StateAction.PUBLISH_EVENT;
        UpdateEventUserRequest request = EventMapper
                .toUpdateEventUserRequest(event, StateAction.SEND_TO_REVIEW)
                .toBuilder()
                .stateAction(invalidUserAction)
                .build();

        // check exception was thrown
        assertThrows(IllegalArgumentException.class,
                () -> eventPrivateController.updateEventByUser(initiatorId, eventId, request),
                "IllegalArgumentException was not thrown updating with invalid state action for user");

    }

    /**
     * should fail update event with invalid event state (eventState is PUBLISHED)
     */
    @Test
    @Transactional
    public void shouldFailUpdateEventByInitiatorWithInvalidEventState() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event

        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // publish event
        UpdateEventAdminRequest publishRequest = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        EventFullDto publishedEvent = eventAdminController.updateEventByAdmin(eventId, publishRequest);

        // create event to update

        UpdateEventUserRequest request = EventMapper
                .toUpdateEventUserRequest(publishedEvent, StateAction.CANCEL_REVIEW)
                .toBuilder()
                .build();

        // check exception was thrown
        Exception e = assertThrows(NotAllowedException.class,
                () -> eventPrivateController.updateEventByUser(initiatorId, eventId, request),
                "NotAllowedException was not thrown updating with invalid event state");
        assertEquals(e.getMessage(), EVENT_IS_PUBLISHED);

    }


    /**
     * should get events for admin
     */
    @Test
    @Transactional
    public void shouldGetEventsForAdmin() {

        // create initiators
        UserDto initiatorAlex = userAdminController.addUser(requestUserAlex);
        UserDto initiatorAnna = userAdminController.addUser(requestUserAnna);
        Long initiatorAlexId = initiatorAlex.getId();
        Long initiatorAnnaId = initiatorAnna.getId();
        UserShortDto shortInitiatorAlex = UserMapper.toUserShortDto(initiatorAlex);
        UserShortDto shortInitiatorAnna = UserMapper.toUserShortDto(initiatorAnna);

        // create categories
        CategoryDto concert = categoryAdminController.addCategory(newConcertDto);
        CategoryDto exhibition = categoryAdminController.addCategory(newExhibitionDto);
        Long catId1 = concert.getId();
        Long catId2 = exhibition.getId();

        //create events
        NewEventDto newEvent1 = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId1)
                .build();
        NewEventDto newEvent2 = newEvent1.toBuilder()
                .category(catId2)
                .title("title Event 2")
                .build();
        NewEventDto newEvent3 = newEvent1.toBuilder()
                .category(catId2)
                .title("title Event 3")
                .build();

        // add events
        EventFullDto eventFullDto1 = eventPrivateController.addEvent(initiatorAlexId, newEvent1);
        Long eventId1 = eventFullDto1.getId();
        EventFullDto eventFullDto2 = eventPrivateController.addEvent(initiatorAnnaId, newEvent2);
        Long eventId2 = eventFullDto2.getId();
        EventFullDto eventFullDto3 = eventPrivateController.addEvent(initiatorAlexId, newEvent3);
        Long eventId3 = eventFullDto3.getId();

        // get events (search by all users, all categories, pending state, existed time, skipped and size default value)

        List<EventFullDto> events = eventAdminController.getEventsByAdmin(
                null, null, null, null, null,
                Integer.valueOf(ZERO_DEFAULT_VALUE), Integer.valueOf(TEN_DEFAULT_VALUE));

        // check list
        assertThat(events).asList().hasSize(3)
                .hasOnlyElementsOfType(EventFullDto.class)
                .contains(eventFullDto1)
                .contains(eventFullDto2)
                .contains(eventFullDto3);
        assertThat(events.get(0)).hasFieldOrPropertyWithValue("id", eventId1)
                .hasFieldOrPropertyWithValue("annotation", annotation)
                .hasFieldOrPropertyWithValue("category", concert)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description)
                .hasFieldOrPropertyWithValue("initiator", shortInitiatorAlex)
                .hasFieldOrPropertyWithValue("eventDate", eventDate)
                .hasFieldOrPropertyWithValue("location", location)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PENDING)
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("views", 0L);
        assertThat(events.get(1)).hasFieldOrPropertyWithValue("id", eventId2)
                .hasFieldOrPropertyWithValue("category", exhibition)
                .hasFieldOrPropertyWithValue("initiator", shortInitiatorAnna);
        assertThat(events.get(2)).hasFieldOrPropertyWithValue("id", eventId3)
                .hasFieldOrPropertyWithValue("category", exhibition)
                .hasFieldOrPropertyWithValue("initiator", shortInitiatorAlex);

    }

}
