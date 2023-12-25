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
import ru.practicum.controllers.priv.RatingPrivateController;
import ru.practicum.controllers.priv.RequestPrivateController;
import ru.practicum.controllers.pub.RatingPublicController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Location;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * RATING INTEGRATION TESTS
 **/
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RatingTest {

    @Autowired
    UserAdminController userAdminController;
    @Autowired
    CategoryAdminController categoryAdminController;
    @Autowired
    EventPrivateController eventPrivateController;
    @Autowired
    EventAdminController eventAdminController;
    @Autowired
    RequestPrivateController requestPrivateController;
    @Autowired
    RatingPrivateController ratingPrivateController;
    @Autowired
    RatingPublicController ratingPublicController;

    NewUserRequest userAlex;
    NewUserRequest userAnna;
    NewUserRequest userEgor;
    NewUserRequest userOlga;
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
    ParticipationRequestDto requestDto;


    @BeforeEach
    public void create() {

        //create new Users DTO
        String nameAlex = "Alex";
        String nameAnna = "Anna";
        String nameEgor = "Egor";
        String nameOlga = "Olga";
        String emailAlex = "Alex@yandex.ru";
        String emailAnna = "Anna@yandex.ru";
        String emailEgor = "Egor@yandex.ru";
        String emailOlga = "Olga@yandex.ru";


        userAlex = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
        userAnna = NewUserRequest.builder()
                .name(nameAnna)
                .email(emailAnna)
                .build();
        userEgor = NewUserRequest.builder()
                .name(nameEgor)
                .email(emailEgor)
                .build();
        userOlga = NewUserRequest.builder()
                .name(nameOlga)
                .email(emailOlga)
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

        //create ParticipationRequestsDto
        requestDto = ParticipationRequestDto.builder()
                .build();

    }

    /**
     * should add and get rating for event
     */
    @Test
    @Transactional
    public void shouldAddAndGetEventRating() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";
        RatingDto ratingDto = ratingPrivateController.addEventRating(requesterId, eventId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", null)
                .hasFieldOrPropertyWithValue("eventId", eventId)
                .hasFieldOrPropertyWithValue("initiatorLike", null)
                .hasFieldOrPropertyWithValue("eventLike", true);

        // check event
        Long rate = 1L;
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(event).hasFieldOrPropertyWithValue("rating", rate);
    }

    /**
     * should add and delete positive rating for event
     */
    @Test
    @Transactional
    public void shouldAddAndDeletePositiveEventRating() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";
        RatingDto ratingDto = ratingPrivateController.addEventRating(requesterId, eventId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", null)
                .hasFieldOrPropertyWithValue("eventId", eventId)
                .hasFieldOrPropertyWithValue("initiatorLike", null)
                .hasFieldOrPropertyWithValue("eventLike", true);

        // check event
        Long rate = 1L;
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(event).hasFieldOrPropertyWithValue("rating", rate);

        // delete rating
        RatingDto deleted = ratingPrivateController.deleteEventRating(requesterId, eventId);

        // check rating
        assertThat(deleted).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", null)
                .hasFieldOrPropertyWithValue("eventId", eventId)
                .hasFieldOrPropertyWithValue("initiatorLike", null)
                .hasFieldOrPropertyWithValue("eventLike", true);

        // check event
        Long zeroRating = 0L;
        EventFullDto updated = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(updated).hasFieldOrPropertyWithValue("rating", zeroRating);
    }

    /**
     * should add and delete negative rating for event
     */
    @Test
    @Transactional
    public void shouldAddAndDeleteNegativeEventRating() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "DISLIKE";
        RatingDto ratingDto = ratingPrivateController.addEventRating(requesterId, eventId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", null)
                .hasFieldOrPropertyWithValue("eventId", eventId)
                .hasFieldOrPropertyWithValue("initiatorLike", null)
                .hasFieldOrPropertyWithValue("eventLike", false);

        // check event
        Long rate = -1L;
        EventFullDto event = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(event).hasFieldOrPropertyWithValue("rating", rate);

        // delete rating
        RatingDto deleted = ratingPrivateController.deleteEventRating(requesterId, eventId);

        // check deleted rating
        assertThat(deleted).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", null)
                .hasFieldOrPropertyWithValue("eventId", eventId)
                .hasFieldOrPropertyWithValue("initiatorLike", null)
                .hasFieldOrPropertyWithValue("eventLike", false);

        // check event
        Long zeroRating = 0L;
        EventFullDto updated = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(updated).hasFieldOrPropertyWithValue("rating", zeroRating);
    }


    /**
     * should add and delete positive rating for initiator
     */
    @Test
    @Transactional
    public void shouldAddAndDeletePositiveInitiatorRating() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";
        RatingDto ratingDto = ratingPrivateController.addInitiatorRating(requesterId, initiatorId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", initiatorId)
                .hasFieldOrPropertyWithValue("eventId", null)
                .hasFieldOrPropertyWithValue("initiatorLike", true)
                .hasFieldOrPropertyWithValue("eventLike", null);

        // check event
        Long rate = 1L;
        List<UserDto> users = userAdminController.getUsers(List.of(initiatorId), 0, 10);
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("rating", rate);

        // delete rating
        RatingDto deleted = ratingPrivateController.deleteInitiatorRating(requesterId, initiatorId);

        // check deleted rating
        assertThat(deleted).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", initiatorId)
                .hasFieldOrPropertyWithValue("eventId", null)
                .hasFieldOrPropertyWithValue("initiatorLike", true)
                .hasFieldOrPropertyWithValue("eventLike", null);

        // check event
        Long zeroRating = 0L;
        EventFullDto updated = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(updated).hasFieldOrPropertyWithValue("rating", zeroRating);
    }

    /**
     * should add and delete negative rating for initiator
     */
    @Test
    @Transactional
    public void shouldAddAndDeleteNegativeInitiatorRating() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "DISLIKE";
        RatingDto ratingDto = ratingPrivateController.addInitiatorRating(requesterId, initiatorId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", initiatorId)
                .hasFieldOrPropertyWithValue("eventId", null)
                .hasFieldOrPropertyWithValue("initiatorLike", false)
                .hasFieldOrPropertyWithValue("eventLike", null);

        // check event
        Long rate = -1L;
        List<UserDto> users = userAdminController.getUsers(List.of(initiatorId), 0, 10);
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("rating", rate);

        // delete rating
        RatingDto deleted = ratingPrivateController.deleteInitiatorRating(requesterId, initiatorId);

        // check deleted rating
        assertThat(deleted).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", initiatorId)
                .hasFieldOrPropertyWithValue("eventId", null)
                .hasFieldOrPropertyWithValue("initiatorLike", false)
                .hasFieldOrPropertyWithValue("eventLike", null);

        // check event
        Long zeroRating = 0L;
        EventFullDto updated = eventPrivateController.getEventByUser(initiatorId, eventId);
        assertThat(updated).hasFieldOrPropertyWithValue("rating", zeroRating);
    }


    /**
     * should add and get rating for initiator
     */
    @Test
    @Transactional
    public void shouldAddAndGetInitiatorRating() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "DISLIKE";
        RatingDto ratingDto = ratingPrivateController.addInitiatorRating(requesterId, initiatorId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", initiatorId)
                .hasFieldOrPropertyWithValue("eventId", null)
                .hasFieldOrPropertyWithValue("initiatorLike", false)
                .hasFieldOrPropertyWithValue("eventLike", null);

        // check event
        Long rate = -1L;
        List<UserDto> users = userAdminController.getUsers(List.of(initiatorId), 0, 10);
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("rating", rate);
    }


    /**
     * should fail add rating for event if user is not future participant
     */
    @Test
    @Transactional
    public void shouldFailAddEventRatingIfUserIsNotParticipant() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";

        // check throws
        Exception e = assertThrows(NotAllowedException.class,
                () -> ratingPrivateController.addEventRating(initiatorId, eventId, rating),
                "NotAllowedException was not thrown");

        assertEquals(e.getMessage(), ONLY_FOR_PARTICIPANT);

    }

    /**
     * should fail add rating for initiator if user is not found
     */
    @Test
    @Transactional
    public void shouldFailAddEventRatingIfUserNotFound() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";

        // check throws
        Long nonExistedId = -1L;
        Exception e = assertThrows(NotFoundException.class,
                () -> ratingPrivateController.addEventRating(nonExistedId, eventId, rating),
                "NotFoundException was not thrown");

        assertEquals(e.getMessage(), getNotFoundMessage("User", nonExistedId));

    }

    /**
     * should fail add rating for event if user is not found
     */
    @Test
    @Transactional
    public void shouldFailAddEventRatingIfEventNotFound() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";

        // check throws
        Long nonExistedId = -1L;
        Exception e = assertThrows(NotFoundException.class,
                () -> ratingPrivateController.addEventRating(requesterId, nonExistedId, rating),
                "NotFoundException was not thrown");

        assertEquals(e.getMessage(), getNotFoundMessage("Event", nonExistedId));

    }

    /**
     * should fail add rating for event if event is not published
     */
    @Test
    @Transactional
    public void shouldFailAddEventRatingIfEventNotPublished() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();


        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add rating
        String rating = "LIKE";

        // check throws
        Exception e = assertThrows(NotAllowedException.class,
                () -> ratingPrivateController.addEventRating(requesterId, eventId, rating),
                "NotAllowedException was not thrown");

        assertEquals(e.getMessage(), EVENT_IS_NOT_PUBLISHED_YET);

    }

    /**
     * should fail add rating for initiator if user is not future participant
     */
    @Test
    @Transactional
    public void shouldFailAddInitiatorRatingIfUserIsNotParticipant() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";

        // check throws
        Exception e = assertThrows(NotAllowedException.class,
                () -> ratingPrivateController.addInitiatorRating(initiatorId, initiatorId, rating),
                "NotAllowedException was not thrown");

        assertEquals(e.getMessage(), NOT_FOR_HIMSELF);

    }

    /**
     * should fail add rating for initiator if user is not found
     */
    @Test
    @Transactional
    public void shouldFailAddInitiatorRatingIfUserNotFound() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";

        // check throws
        Long nonExistedId = -1L;
        Exception e = assertThrows(NotFoundException.class,
                () -> ratingPrivateController.addInitiatorRating(nonExistedId, initiatorId, rating),
                "NotFoundException was not thrown");

        assertEquals(e.getMessage(), getNotFoundMessage("User", nonExistedId));

    }

    /**
     * should fail duplicate rating for event if initiator is not found
     */
    @Test
    @Transactional
    public void shouldFailAddEventRatingIfDuplicate() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";
        RatingDto ratingDto = ratingPrivateController.addEventRating(requesterId, eventId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", null)
                .hasFieldOrPropertyWithValue("eventId", eventId)
                .hasFieldOrPropertyWithValue("initiatorLike", null)
                .hasFieldOrPropertyWithValue("eventLike", true);


        // check throws

        Exception e = assertThrows(ConflictConstraintUniqueException.class,
                () -> ratingPrivateController.addEventRating(requesterId, eventId, rating),
                "ConflictConstraintUniqueException was not thrown");

        assertEquals(e.getMessage(), RATING_UNIQUE_VIOLATION);

    }


    /**
     * should fail duplicate rating for event if initiator is not found
     */
    @Test
    @Transactional
    public void shouldFailAddInitiatorRatingIfDuplicate() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";
        RatingDto ratingDto = ratingPrivateController.addInitiatorRating(requesterId, initiatorId, rating);
        Long ratingId = ratingDto.getId();

        // check rating
        assertThat(ratingDto).hasFieldOrPropertyWithValue("id", ratingId)
                .hasFieldOrPropertyWithValue("userId", requesterId)
                .hasFieldOrPropertyWithValue("initiatorId", initiatorId)
                .hasFieldOrPropertyWithValue("eventId", null)
                .hasFieldOrPropertyWithValue("initiatorLike", true)
                .hasFieldOrPropertyWithValue("eventLike", null);


        // check throws

        Exception e = assertThrows(ConflictConstraintUniqueException.class,
                () -> ratingPrivateController.addInitiatorRating(requesterId, initiatorId, rating),
                "ConflictConstraintUniqueException was not thrown");

        assertEquals(e.getMessage(), RATING_UNIQUE_VIOLATION);

    }

    /**
     * should fail add rating for event if initiator is not found
     */
    @Test
    @Transactional
    public void shouldFailAddInitiatorRatingIfInitiatorNotFound() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
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

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);

        //create requester
        UserDto requester = userAdminController.addUser(userAnna);
        Long requesterId = requester.getId();

        // add and confirm request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add rating
        String rating = "LIKE";

        // check throws
        Long nonExistedId = -1L;
        Exception e = assertThrows(NotFoundException.class,
                () -> ratingPrivateController.addInitiatorRating(requesterId, nonExistedId, rating),
                "NotFoundException was not thrown");

        assertEquals(e.getMessage(), getNotFoundMessage("User", nonExistedId));

    }

    /**
     * should get sorted by rating events
     */
    @Test
    @Transactional
    public void shouldGetSortedEventsByRatingDesc() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add events
        EventFullDto eventFullDto1 = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId1 = eventFullDto1.getId();
        EventFullDto eventFullDto2 = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId2 = eventFullDto2.getId();
        EventFullDto eventFullDto3 = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId3 = eventFullDto3.getId();
        EventFullDto eventFullDto4 = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId4 = eventFullDto4.getId();


        // publish 3 events

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId1, requestToPublish);
        eventAdminController.updateEventByAdmin(eventId2, requestToPublish);
        eventAdminController.updateEventByAdmin(eventId3, requestToPublish);


        //create requesters
        UserDto requester1 = userAdminController.addUser(userAnna);
        Long requesterId1 = requester1.getId();
        UserDto requester2 = userAdminController.addUser(userOlga);
        Long requesterId2 = requester2.getId();

        // add and confirm 3 requests
        ParticipationRequestDto request1 = requestPrivateController.addParticipationRequest(requesterId1, eventId1);
        ParticipationRequestDto request2 = requestPrivateController.addParticipationRequest(requesterId1, eventId2);
        ParticipationRequestDto request3 = requestPrivateController.addParticipationRequest(requesterId1, eventId3);
        Long requestId1 = request1.getId();
        Long requestId2 = request2.getId();
        Long requestId3 = request3.getId();
        ParticipationRequestDto request4 = requestPrivateController.addParticipationRequest(requesterId2, eventId1);
        ParticipationRequestDto request5 = requestPrivateController.addParticipationRequest(requesterId2, eventId3);
        Long requestId4 = request4.getId();
        Long requestId5 = request5.getId();

        // check requests
        assertThat(request1).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId1);
        assertThat(request2).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId2);
        assertThat(request3).hasFieldOrPropertyWithValue("id", requestId3)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId3);
        assertThat(request4).hasFieldOrPropertyWithValue("id", requestId4)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId1);
        assertThat(request5).hasFieldOrPropertyWithValue("id", requestId5)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId3);

        // add 2 likes to event 1
        String like = "LIKE";
        ratingPrivateController.addEventRating(requesterId1, eventId1, like);
        ratingPrivateController.addEventRating(requesterId2, eventId1, like);

        // check event 1 has rating 2
        Long rateEvent1 = 2L;
        EventFullDto event1 = eventPrivateController.getEventByUser(initiatorId, eventId1);
        assertThat(event1).hasFieldOrPropertyWithValue("rating", rateEvent1);

        // check event 2 has rating 0
        Long rateEvent2 = 0L;
        EventFullDto event2 = eventPrivateController.getEventByUser(initiatorId, eventId2);
        assertThat(event2).hasFieldOrPropertyWithValue("rating", rateEvent2);

        // add 1 dislike to event 3
        String dislike = "DISLIKE";
        ratingPrivateController.addEventRating(requesterId2, eventId3, dislike);
        Long rateEvent3 = -1L;
        EventFullDto event3 = eventPrivateController.getEventByUser(initiatorId, eventId3);
        assertThat(event3).hasFieldOrPropertyWithValue("rating", rateEvent3);

        // check event 4 has rating 0 but not published
        Long rateEvent4 = 0L;
        EventFullDto event4 = eventPrivateController.getEventByUser(initiatorId, eventId4);
        assertThat(event2).hasFieldOrPropertyWithValue("rating", rateEvent4);

        List<EventFullDto> sortedEvents = ratingPublicController.getEventsByRating(0, 10);
        assertThat(sortedEvents).asList().hasSize(3)
                .hasOnlyElementsOfType(EventFullDto.class)
                .doesNotContain(event4)
                .startsWith(event1)
                .contains(event2)
                .endsWith(event3);


    }

    /**
     * should get sorted by rating users
     */
    @Test
    @Transactional
    public void shouldGetSortedUsersByRatingDesc() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();

        // add event
        EventFullDto eventFullDto = eventPrivateController.addEvent(initiatorId, newEvent);
        Long eventId = eventFullDto.getId();

        // publish  event

        UpdateEventAdminRequest requestToPublish = UpdateEventAdminRequest.builder()
                .stateAction(StateAction.PUBLISH_EVENT)
                .build();
        eventAdminController.updateEventByAdmin(eventId, requestToPublish);


        //create requesters
        UserDto requester1 = userAdminController.addUser(userAnna);
        Long requesterId1 = requester1.getId();
        UserDto requester2 = userAdminController.addUser(userOlga);
        Long requesterId2 = requester2.getId();

        // add and confirm 3 requests
        ParticipationRequestDto request1 = requestPrivateController.addParticipationRequest(requesterId1, eventId);
        ParticipationRequestDto request2 = requestPrivateController.addParticipationRequest(requesterId2, eventId);
        Long requestId1 = request1.getId();
        Long requestId2 = request2.getId();

        // check requests
        assertThat(request1).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        assertThat(request2).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);

        // add 2 likes to initiator 1
        String like = "LIKE";
        ratingPrivateController.addEventRating(requesterId1, initiatorId, like);
        ratingPrivateController.addEventRating(requesterId2, initiatorId, like);

        List<UserDto> sortedUsers = ratingPublicController.getInitiatorByRating(0, 10);
        assertThat(sortedUsers).asList().hasSize(3)
                .hasOnlyElementsOfType(UserDto.class)
                .startsWith(initiator)
                .contains(requester1)
                .contains(requester2);


    }


}
