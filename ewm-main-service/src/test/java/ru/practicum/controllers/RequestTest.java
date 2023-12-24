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
import ru.practicum.controllers.priv.RequestPrivateController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Location;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.StateAction;
import ru.practicum.utils.errors.exceptions.NotAllowedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.utils.errors.ErrorConstants.ONLY_FOR_INITIATOR;

/**
 * REQUEST INTEGRATION TESTS
 **/
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RequestTest {
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
     * should add and get CONFIRMED request
     */
    @Test
    @Transactional
    public void shouldAddAndGetConfirmedRequest() {

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

        // add request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);


    }

    /**
     * should add and get PENDING request
     */
    @Test
    @Transactional
    public void shouldAddAndGetPendingRequest() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
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

        // add request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);


    }

    /**
     * should add and get request
     */
    @Test
    @Transactional
    public void shouldFailAddRequestIfUserIsInitiator() {

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
        userAdminController.addUser(userAnna);

        // add request
        assertThrows(NotAllowedException.class,
                () -> requestPrivateController.addParticipationRequest(initiatorId, eventId),
                "NotAllowedException was not thrown");

    }

    /**
     * should cancel request
     */
    @Test
    @Transactional
    public void shouldCancelRequest() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
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

        // add request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // cancel request
        ParticipationRequestDto canceled = requestPrivateController.cancelParticipationRequest(requesterId, eventId);


        // check request
        assertThat(canceled).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CANCELED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

    }

    /**
     * should confirm requests
     */
    @Test
    @Transactional
    public void shouldConfirmRequests() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
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

        //create requesters
        UserDto requester1 = userAdminController.addUser(userAnna);
        Long requesterId1 = requester1.getId();

        UserDto requester2 = userAdminController.addUser(userEgor);
        Long requesterId2 = requester2.getId();

        // add request
        ParticipationRequestDto request1 = requestPrivateController.addParticipationRequest(requesterId1, eventId);
        Long requestId1 = request1.getId();
        ParticipationRequestDto request2 = requestPrivateController.addParticipationRequest(requesterId2, eventId);
        Long requestId2 = request2.getId();

        // check request
        assertThat(request1).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        assertThat(request2).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);


        // confirm requests by Admin
        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatusUpdateRequest requestToConfirm = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();
        EventRequestStatusUpdateResult result = eventPrivateController
                .updateRequestsStatus(initiatorId, eventId, requestToConfirm);

        // check result
        assertThat(result).hasFieldOrProperty("confirmedRequests")
                .hasFieldOrPropertyWithValue("rejectedRequests", Collections.emptyList());
        assertThat(result.getConfirmedRequests().get(0))
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED);
        assertThat(result.getConfirmedRequests().get(1))
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED);

        // check requests
        List<ParticipationRequestDto> requestAnna = requestPrivateController.getUserParticipationRequests(requesterId1);
        assertThat(requestAnna.get(0)).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        List<ParticipationRequestDto> requestEgor = requestPrivateController.getUserParticipationRequests(requesterId2);
        assertThat(requestEgor.get(0)).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);

    }

    /**
     * should confirm part of requests
     */
    @Test
    @Transactional
    public void shouldConfirmPartOfRequests() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
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

        //create requesters
        UserDto requester1 = userAdminController.addUser(userAnna);
        Long requesterId1 = requester1.getId();

        UserDto requester2 = userAdminController.addUser(userEgor);
        Long requesterId2 = requester2.getId();

        UserDto requester3 = userAdminController.addUser(userOlga);
        Long requesterId3 = requester3.getId();

        // add request
        ParticipationRequestDto request1 = requestPrivateController.addParticipationRequest(requesterId1, eventId);
        Long requestId1 = request1.getId();
        ParticipationRequestDto request2 = requestPrivateController.addParticipationRequest(requesterId2, eventId);
        Long requestId2 = request2.getId();
        ParticipationRequestDto request3 = requestPrivateController.addParticipationRequest(requesterId3, eventId);
        Long requestId3 = request3.getId();

        // check request
        assertThat(request1).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        assertThat(request2).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);
        assertThat(request3).hasFieldOrPropertyWithValue("id", requestId3)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId3)
                .hasFieldOrPropertyWithValue("event", eventId);


        // confirm requests by Admin
        List<Long> requestIds = List.of(requestId1, requestId2, requestId3);
        EventRequestStatusUpdateRequest requestToConfirm = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();
        EventRequestStatusUpdateResult result = eventPrivateController
                .updateRequestsStatus(initiatorId, eventId, requestToConfirm);

        // check result
        assertThat(result.getConfirmedRequests()).asList().hasSize(2);

        assertThat(result.getConfirmedRequests().get(0))
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED);
        assertThat(result.getConfirmedRequests().get(1))
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED);


        assertThat(result.getRejectedRequests()).asList().hasSize(1);

        assertThat(result.getRejectedRequests().get(0))
                .hasFieldOrPropertyWithValue("status", RequestStatus.REJECTED);

        // check requests
        List<ParticipationRequestDto> requestAnna = requestPrivateController.getUserParticipationRequests(requesterId1);
        assertThat(requestAnna.get(0)).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        List<ParticipationRequestDto> requestEgor = requestPrivateController.getUserParticipationRequests(requesterId2);
        assertThat(requestEgor.get(0)).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.CONFIRMED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);
        List<ParticipationRequestDto> requestOlga = requestPrivateController.getUserParticipationRequests(requesterId3);
        assertThat(requestOlga.get(0)).hasFieldOrPropertyWithValue("id", requestId3)
                .hasFieldOrPropertyWithValue("status", RequestStatus.REJECTED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId3)
                .hasFieldOrPropertyWithValue("event", eventId);

    }

    /**
     * should reject requests
     */
    @Test
    @Transactional
    public void shouldRejectRequests() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
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

        //create requesters
        UserDto requester1 = userAdminController.addUser(userAnna);
        Long requesterId1 = requester1.getId();

        UserDto requester2 = userAdminController.addUser(userEgor);
        Long requesterId2 = requester2.getId();

        // add request
        ParticipationRequestDto request1 = requestPrivateController.addParticipationRequest(requesterId1, eventId);
        Long requestId1 = request1.getId();
        ParticipationRequestDto request2 = requestPrivateController.addParticipationRequest(requesterId2, eventId);
        Long requestId2 = request2.getId();

        // check request
        assertThat(request1).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        assertThat(request2).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);


        // reject request by Admin
        List<Long> requestIds = List.of(requestId1, requestId2);
        EventRequestStatusUpdateRequest requestsToConfirm = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.REJECTED)
                .build();
        EventRequestStatusUpdateResult result = eventPrivateController
                .updateRequestsStatus(initiatorId, eventId, requestsToConfirm);

        // check result
        assertThat(result).hasFieldOrPropertyWithValue("confirmedRequests", Collections.emptyList())
                .hasFieldOrProperty("rejectedRequests");
        assertThat(result.getRejectedRequests().get(0))
                .hasFieldOrPropertyWithValue("status", RequestStatus.REJECTED);
        assertThat(result.getRejectedRequests().get(1))
                .hasFieldOrPropertyWithValue("status", RequestStatus.REJECTED);


        // check requests
        List<ParticipationRequestDto> requestAnna = requestPrivateController.getUserParticipationRequests(requesterId1);
        assertThat(requestAnna.get(0)).hasFieldOrPropertyWithValue("id", requestId1)
                .hasFieldOrPropertyWithValue("status", RequestStatus.REJECTED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId1)
                .hasFieldOrPropertyWithValue("event", eventId);
        List<ParticipationRequestDto> requestEgor = requestPrivateController.getUserParticipationRequests(requesterId2);
        assertThat(requestEgor.get(0)).hasFieldOrPropertyWithValue("id", requestId2)
                .hasFieldOrPropertyWithValue("status", RequestStatus.REJECTED)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId2)
                .hasFieldOrPropertyWithValue("event", eventId);

    }

    /**
     * should fail confirm  request if user is not initiator of the event
     */
    @Test
    @Transactional
    public void shouldFailUpdateRequestWithoutAccess() {

        // create initiator, category and event
        UserDto initiator = userAdminController.addUser(userAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
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

        // add request
        ParticipationRequestDto request = requestPrivateController.addParticipationRequest(requesterId, eventId);
        Long requestId = request.getId();

        // check request
        assertThat(request).hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("status", RequestStatus.PENDING)
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("requester", requesterId)
                .hasFieldOrPropertyWithValue("event", eventId);

        // confirm request by Admin
        List<Long> requestIds = List.of(requestId);
        EventRequestStatusUpdateRequest requestToConfirm = EventRequestStatusUpdateRequest.builder()
                .requestIds(requestIds)
                .status(RequestStatus.CONFIRMED)
                .build();

        // check throws
        Exception e = assertThrows(NotAllowedException.class, () -> eventPrivateController
                        .updateRequestsStatus(requesterId, eventId, requestToConfirm),
                "NotAllowedException was not thrown");

        // check message
        assertEquals(e.getMessage(), ONLY_FOR_INITIATOR);

    }


}
