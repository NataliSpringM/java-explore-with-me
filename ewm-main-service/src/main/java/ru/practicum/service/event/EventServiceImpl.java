package ru.practicum.service.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.*;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.enums.SortType;
import ru.practicum.enums.StateAction;
import ru.practicum.repository.*;
import ru.practicum.service.statistics.StatisticsService;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.logger.ListLogger;
import ru.practicum.utils.mapper.EnumMapper;
import ru.practicum.utils.mapper.EventMapper;
import ru.practicum.utils.mapper.RequestMapper;
import ru.practicum.utils.paging.Paging;
import ru.practicum.utils.validation.EnumTypeValidation;
import ru.practicum.utils.validation.EventTimeValidator;
import ru.practicum.utils.validation.TwoHoursLater;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.utils.constants.Constants.SLASH_PATH;
import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * EVENT SERVICE IMPLEMENTATION
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    private final LocationRepository locationRepository;
    private final StatisticsService statisticsService;


    /**
     * Add new event
     * the date and time for which the event is scheduled cannot be earlier than two hours from the current moment
     * (Error code 409 is expected)
     *
     * @param userId user id
     * @param dto    event details
     * @return new event
     */
    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto dto) {

        User initiator = getUserOrThrowException(userId);
        Category category = getCategoryOrThrowException(dto.getCategory());
        Location location = locationRepository.save(dto.getLocation());

        Event event = EventMapper.toEventEntity(dto, location, initiator, category);
        Event newEvent = eventRepository.save(event);
        log.info("Event with id {} added: {}", newEvent.getId(), newEvent);
        return EventMapper.toEventFullDto(newEvent);
    }


    /**
     * Get full event information by ID for public access
     * The event must be published
     * Event information should include the number of views and the number of confirmed requests
     * information that a request was made and processed for this endpoint must be saved in the statistics service
     *
     * @param eventId event ID
     * @param request HttpServletRequest details
     * @return detailed event information
     */
    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository
                .findByIdAndState(eventId, EventState.PUBLISHED.name())
                .orElseThrow(() -> new NotFoundException(
                        ErrorConstants.getNotFoundMessage("Event", eventId)));
        statisticsService.saveStats(request);
        Long views = statisticsService.getStats(
                event.getPublishedOn(), LocalDateTime.now(), List.of(request.getRequestURI())).get(eventId);

        Event eventWithStat = EventMapper.toEventWithStat(event, views);
        Event savedEvent = eventRepository.save(eventWithStat);
        EventFullDto result = EventMapper.toEventFullDto(savedEvent);
        log.info("Event with id {} was found: {}", eventId, result);
        return result;
    }

    /**
     * Get events sorted by rating
     * Only published events should appear in the results
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return List of events
     */
    @Override
    public List<EventFullDto> getEventsByRating(Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByStateOrderByRatingDesc(
                EventState.PUBLISHED.name(), Paging.getPageable(from, size));
        ListLogger.logResultList(events);
        return EventMapper.toEventFullDtoList(events);
    }

    /**
     * Get events with filtering options for public access.
     * Only published events should appear in the results
     * Text search (by annotation and detailed description) should be case-insensitive
     * If the date range [rangeStart-rangeEnd] is not specified in the request,
     * then you need to upload events that will occur later than the current date and time
     * Information about each event must include the number of views
     * and the number of applications already approved for participation
     * information that a request was made and processed for this endpoint
     * must be saved in the statistics service
     *
     * @param text          text to search in the content of the annotation and detailed description of the event
     * @param categories    list of category identifiers in which the search will be conducted
     * @param paid          search only for paid/free events
     * @param rangeStart    date and time no earlier than which the event should occur
     * @param rangeEnd      date and time no later than which the event must occur
     * @param onlyAvailable only events that have not reached the limit of participation requests, default value: false
     * @param sort          sorting option: by event date or by number of views (EVENT_DATE, VIEWS)
     * @param from          number of elements that need to be skipped to form the current page, default value = 10
     * @param size          number of elements per page, default value = 10
     * @return List of events met filtering criteria.
     * If no events are found by the specified filters, returns an empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Integer from, Integer size,
                                               HttpServletRequest request) {

        SortType sortType = (sort == null) ? null : EnumTypeValidation.getValidSortType(sort);
        LocalDateTime start = (rangeStart == null) ? LocalDateTime.now() : rangeStart;
        LocalDateTime end = (rangeEnd == null) ? LocalDateTime.now().plusYears(100) : rangeEnd;

        EventTimeValidator.checkStartTimeIsAfterEnd(start, end);

        List<Event> events;
        if (onlyAvailable) {
            events = eventRepository.findAvailableForPublic(
                    text, categories, paid, start, end,
                    String.valueOf(EventState.PUBLISHED), Paging.getPageable(from, size, sortType));

        } else {
            events = eventRepository.findAllForPublic(
                    text, categories, paid, start, end,
                    String.valueOf(EventState.PUBLISHED), Paging.getPageable(from, size, sortType));
        }

        statisticsService.saveStats(request);

        List<Event> eventsWithViews = Collections.emptyList();
        if (!events.isEmpty()) {
            LocalDateTime oldestEventPublishedOn = events.stream()
                    .min(Comparator.comparing(Event::getPublishedOn)).map(Event::getPublishedOn).stream()
                    .findFirst().orElseThrow();
            List<String> uris = getListOfUri(events, request.getRequestURI());

            Map<Long, Long> views = statisticsService.getStats(oldestEventPublishedOn, LocalDateTime.now(), uris);
            eventsWithViews = events
                    .stream()
                    .map(event -> EventMapper.toEventWithStat(event, views.get(event.getId())))
                    .collect(Collectors.toList());
        }
        List<Event> savedEvents = eventRepository.saveAll(eventsWithViews);
        List<EventShortDto> resultList = EventMapper.toEventShortDtoList(savedEvents);
        ListLogger.logResultList(resultList);
        return resultList;
    }


    /**
     * Get events added by user
     *
     * @param userId user id
     * @param from   number of elements that need to be skipped to form the current page
     * @param size   number of elements per page
     * @return list of events, if no events are found by the specified filters, returns an empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findAllByInitiator_Id(userId, Paging.getPageable(from, size));
        List<EventShortDto> resultList = EventMapper.toEventShortDtoList(events);
        ListLogger.logResultList(resultList);
        return resultList;
    }


    /**
     * Process GET-request to the endpoint "users/{userId}/events/{eventId}"
     * Get full information about event added by user
     *
     * @param userId  user id
     * @param eventId event id
     * @return full event information, if no event with the given id is found, status code 404 is returned.
     */
    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = getEventOrThrowException(eventId);
        EventFullDto result = EventMapper.toEventFullDto(event);
        log.info("Event with id {} was found: {}", eventId, result);
        return result;
    }


    /**
     * Process PATCH-request to the endpoint "users/{userId}/events/{eventId}"
     * Update information about event added by user
     * the date and time for which the event is scheduled cannot be earlier than two hours from the current moment:
     * (Error code 409 is expected)
     * only canceled events or events pending moderation can be changed:
     * (Error code 409 expected)
     *
     * @param userId  user id
     * @param eventId event id
     * @param request Data for changing event information. If the field is not specified in the request (equal to null),
     *                then changing this data is not required.
     *                (annotation, category ID, description, eventDate, LocationDto, paid, participantLimit,
     *                requestModeration, stateAction, title)
     * @return full event information, if no event with the given id is found, status code 404 is returned.
     */
    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {

        StateAction action = (request.getStateAction() == null) ? null :
                EnumTypeValidation.getValidUserAction(request.getStateAction());
        Event event = getEventOrThrowException(eventId);
        EventTimeValidator.checkStartTimeIsValid(event.getEventDate());
        checkEventStateIsCanceledOrPending(event.getState());
        checkIsInitiator(userId, event.getInitiator().getId());

        Event updatedEvent = eventRepository.save(updateNonNullFields(event, request, action));
        EventFullDto result = EventMapper.toEventFullDto(updatedEvent);
        log.info("Event with id {} was updated: {}", eventId, result);
        return result;
    }

    /**
     * obtain information about requests to participate in a specific event added by the current user
     *
     * @param userId  user id
     * @param eventId event id
     * @return list of requests, if no application is found based on the specified filters, it returns an empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {

        Event event = getEventOrThrowException(eventId);
        checkIsInitiator(userId, event.getInitiator().getId());

        List<Request> requests = requestRepository.findAllByEvent_Id(eventId);
        List<ParticipationRequestDto> resultList = RequestMapper.toParticipationRequestDtoList(requests);
        ListLogger.logResultList(resultList);
        return resultList;
    }

    /**
     * Search events
     *
     * @param users      list of user ids whose events need to be found
     * @param states     list of states in which the desired events are located
     * @param categories list of id categories in which the search will be conducted
     * @param rangeStart date and time no earlier than which the event should occur
     * @param rangeEnd   date and time no later than which the event must occur
     * @param from       number of elements that need to be skipped to form the current page, default value = 10
     * @param size       number of elements per page, default value = 10
     * @return complete information about all events that match the passed conditions,
     * if no events are found by the specified filters, returns an empty list
     */
    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Integer from, Integer size) {
        if (states != null) {
            EnumTypeValidation.checkValidEventStates(states);
        }
        List<Event> events = eventRepository.findForAdmin(
                users, states, categories, rangeStart, rangeEnd, Paging.getPageable(from, size));
        List<EventFullDto> resultList = EventMapper.toEventFullDtoList(events);
        ListLogger.logResultList(resultList);
        return resultList;
    }

    /**
     * Update event data and its status (reject, publish) by admin
     * The start date of modified events must be no earlier than an hour from the publication date
     * (Error code 409 expected)
     * An event can only be published if it is in a pending state, waiting for publish (Error code 409 expected)
     * An event can only be rejected if it has not yet been published (Error code 409 expected)
     *
     * @param eventId event id
     * @param request data for changing event information:
     *                (new annotation, category, description, eventDate("yyyy-MM-dd HH:mm:ss"),
     *                Location (latitude, longitude), paid flag, participation limit, request moderation flag,
     *                state (PUBLISH_EVENT, REJECT_EVENT), title).
     *                If the field is not specified in the request (equal to null),
     *                then changing this data is not required.
     */

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {

        StateAction action = (request.getStateAction() == null) ? null :
                EnumTypeValidation.getValidAdminAction(request.getStateAction());

        Event event = getEventOrThrowException(eventId);
        checkIsEventTimeIsNotTooLateToUpdateByAdmin(event.getEventDate());
        checkEventStateIsPending(event.getState());
        if (StateAction.PUBLISH_EVENT.equals(action)) {
            event = event.toBuilder().publishedOn(LocalDateTime.now()).build();
        }
        Event updatedEvent = eventRepository.save(updateNonNullFields(event, request, action));
        EventFullDto result = EventMapper.toEventFullDto(updatedEvent);
        log.info("Event with id {} was updated, {}", eventId, result);
        return result;
    }

    /**
     * update Status of requests (confirm, reject) for participation in an event added by the current user
     * if for an event the application limit is 0 or pre-moderation of applications is disabled,
     * then confirmation of applications is not required
     * it is impossible to confirm an application if the limit on applications for this event has already been reached
     * (Error code 409 is expected)
     * the status can only be changed for requests that are in a pending state (Error code 409 expected)
     * if, when confirming this application, the application limit for the event has been exhausted,
     * then all unconfirmed applications must be rejected
     *
     * @param userId  user id
     * @param eventId event id
     * @param request changing the status of the current user's event participation request
     *                (set IDs of current user's event participation requests,
     *                new status of the current user's event participation request)
     * @return result of confirmation/rejection of applications for participation in the event
     * (set of confirmed requests, set of rejected requests)
     */
    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                               EventRequestStatusUpdateRequest request) {

        Event event = eventRepository.getReferenceById(eventId);
        checkIsInitiator(userId, event.getInitiator().getId());

        List<Request> requests = requestRepository.findAllById(request.getRequestIds());
        checkAllRequestsStatusIsPending(requests);
        RequestStatus status = request.getStatus();

        int limit = event.getParticipantLimit();

        if (status.equals(RequestStatus.REJECTED)) {
            return rejectAllRequests(requests);
        }
        if (checkHasNoLimitAndModeration(limit, event.getRequestModeration())) {
            saveEventWithUpdatedNumberOfConfirmedRequests(event, requests.size());
            return confirmAllRequests(requests);
        }

        return resolveStatus(event, requests);

    }

    /**
     * get Event from repository by id or throw NotFoundException
     *
     * @param eventId event ID
     * @return Event
     */
    private Event getEventOrThrowException(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.getNotFoundMessage("Event", eventId)));
    }

    /**
     * get Category from repository by id or throw NotFoundException
     *
     * @param categoryId category ID
     * @return Category
     */
    private Category getCategoryOrThrowException(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorConstants.getNotFoundMessage("Category", categoryId)));
    }

    /**
     * get User from repository by id or throw NotFoundException
     *
     * @param userId user ID
     * @return User
     */
    private User getUserOrThrowException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.getNotFoundMessage("User", userId)));
    }

    /**
     * check whether limit of participation requests of the event has been reached and resolve status for requests
     *
     * @param event    event
     * @param requests list of participation requests
     * @return result with resolved statuses
     */

    private EventRequestStatusUpdateResult resolveStatus(Event event, List<Request> requests) {

        int limit = event.getParticipantLimit();
        int confirmed = event.getConfirmedRequests();

        checkIsEventAvailable(confirmed, limit);

        int breakPoint = limit - confirmed;
        int numberOfRequests = requests.size();
        log.info("Event is available. Number of requests to confirm is {}, available number is {}: ",
                numberOfRequests, breakPoint);
        List<Request> availableToConfirm;
        List<Request> mustBeRejected;
        List<Request> pendingRest;

        if (numberOfRequests < breakPoint) {
            saveEventWithUpdatedNumberOfConfirmedRequests(event, numberOfRequests);
            log.info("We can confirm all {} requests", numberOfRequests);
            return confirmAllRequests(requests);
        } else if (numberOfRequests == breakPoint) {
            availableToConfirm = requests;
            confirmAllRequests(availableToConfirm);
            mustBeRejected = getRestOfPendingRequests(event.getId());
            rejectAllRequests(mustBeRejected);
            saveEventWithUpdatedNumberOfConfirmedRequests(event, availableToConfirm.size());
            log.info("We can confirm all {} requests but have to reject all pending out of current session: {}." +
                            " Event {} is not more available",
                    availableToConfirm.size(), mustBeRejected.size(), event.getId());
            return constructResult(availableToConfirm, mustBeRejected);
        } else {
            availableToConfirm = requests.subList(0, breakPoint);
            confirmAllRequests(availableToConfirm);
            saveEventWithUpdatedNumberOfConfirmedRequests(event, availableToConfirm.size());
            mustBeRejected = requests.subList(breakPoint, numberOfRequests);
            rejectAllRequests(mustBeRejected);
            pendingRest = getRestOfPendingRequests(event.getId());
            rejectAllRequests(pendingRest);
            log.info("We can confirm only {} requests,  "
                            + "have to reject {} from current session "
                            + "and all pending out of current session: {}."
                            + " Event {} is not more available",
                    availableToConfirm.size(), requests.size() - availableToConfirm.size(),
                    pendingRest.size(), event.getId());
            return constructResult(availableToConfirm, mustBeRejected);
        }
    }

    /**
     * set Rejected status for all requests
     *
     * @param requests list of requests
     * @return list of requests with rejected status
     */

    private EventRequestStatusUpdateResult rejectAllRequests(List<Request> requests) {
        List<Request> savedRequests = saveRequestsWithNewStatus(requests, RequestStatus.REJECTED);
        return constructResult(Collections.emptyList(), savedRequests);
    }

    /**
     * set Rejected status for all requests
     *
     * @param requests list of requests
     * @return list of requests with confirmed status
     */

    private EventRequestStatusUpdateResult confirmAllRequests(List<Request> requests) {
        List<Request> savedRequests = saveRequestsWithNewStatus(requests, RequestStatus.CONFIRMED);
        return constructResult(savedRequests, Collections.emptyList());
    }

    /**
     * save Event with updated number of confirmedRequests
     *
     * @param event  event
     * @param number new number of confirmed requests for the event
     */

    private void saveEventWithUpdatedNumberOfConfirmedRequests(Event event, int number) {
        eventRepository.save(updateNumberOfConfirmedRequests(event, number));
        log.info("Number of confirmed requests to participate in the event: {} increased by {}:",
                event.getId(), number);
    }

    /**
     * save Requests with new status
     *
     * @param requests list of the requests
     * @param status   new request status
     * @return list of requests with new status, saved in repository
     */
    private List<Request> saveRequestsWithNewStatus(List<Request> requests, RequestStatus status) {
        List<Request> savedRequests = requestRepository.saveAll(updateStatusInList(requests, status));
        log.info("We set {} status for requests:", status);
        ListLogger.logResultList(savedRequests);
        return savedRequests;
    }

    /**
     * check limit of participation requests has been reached
     *
     * @param confirmed number of confirmed requests
     * @param limit     limit set for the event
     */
    private void checkIsEventAvailable(Integer confirmed, Integer limit) {

        log.info("We are checking that the limit of participants has not been reached yet. "
                + "Confirmed requests : {}. Participation limit is: {}", confirmed, limit);
        if (Objects.equals(confirmed, limit)) {
            throw new NotAllowedException(LIMIT);
        }
    }

    /**
     * check if time is valid to update event state by admin (more than hour before start)
     * throws exception if it is not
     *
     * @param time time to check
     */
    private void checkIsEventTimeIsNotTooLateToUpdateByAdmin(@TwoHoursLater LocalDateTime time) {
        if (time != null) {
            if (time.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new NotAllowedException(TIME_IS_LESS_THAN_ONE_HOUR_BEFORE_START);
            }
        }
    }

    /**
     * check if EventState is PENDING or CANCELED, throws exception if it is not
     *
     * @param state state to check
     */
    private void checkEventStateIsCanceledOrPending(String state) {
        log.info("We are checking that the state of the event: {}. It must be canceled or pending moderation", state);
        if (EventState.PUBLISHED.name().equals(state)) {
            throw new NotAllowedException(EVENT_IS_PUBLISHED);
        }
    }

    /**
     * check if EventState is PENDING, throws exception if it is not
     *
     * @param state state to check
     */
    private void checkEventStateIsPending(String state) {
        log.info("We are checking that the state of the event: {}. It must be pending for moderation", state);
        if (!EventState.PENDING.name().equals(state)) {
            throw new NotAllowedException(EVENT_IS_PUBLISHED);
        }
    }

    /**
     * check if user is initiator, throws exception if he is
     *
     * @param userId      user ID to check
     * @param initiatorId initiator ID
     */
    private void checkIsInitiator(Long userId, Long initiatorId) {
        log.info("We are checking is user with id {}:  is initiator of the event {}", userId, initiatorId);
        if (!initiatorId.equals(userId)) {
            throw new NotAllowedException(ONLY_FOR_INITIATOR);
        }
    }

    /**
     * check if event participation request can be automatically confirmed:
     * (limit is not set or request moderation is false)
     *
     * @param limit limit to check
     * @param mod   moderation status to check
     * @return true if event not required additional checks
     */
    private boolean checkHasNoLimitAndModeration(Integer limit, Boolean mod) {
        log.info("We are checking whether request could be confirmed automatically");
        if (limit == 0 || Boolean.FALSE.equals(mod)) {
            log.info("As participation limit is not set and moderation is not required," +
                    " your request is confirmed automatically");
            return true;
        }
        return false;
    }

    /**
     * check all requests are in pending status
     * throw exception if they are not
     *
     * @param requests list of requests to check
     */
    private void checkAllRequestsStatusIsPending(List<Request> requests) {
        log.info("We are checking all requests have pending status");
        boolean allPending = requests.stream().allMatch(r -> r.getStatus().equals(RequestStatus.PENDING));
        if (!allPending) {
            log.info("List of requests to update contains not pending requests");
            throw new NotAllowedException(NOT_PENDING);
        }
    }

    /**
     * update status in list of requests
     *
     * @param requests list of requests
     * @param status   new status to update
     * @return list of requests with updated status
     */

    private List<Request> updateStatusInList(List<Request> requests, RequestStatus status) {
        return requests.stream().map(request -> updateRequestStatus(request, status))
                .collect(Collectors.toList());
    }


    /**
     * update status of request
     *
     * @param request request
     * @param status  new status to update
     * @return updated request
     */
    private Request updateRequestStatus(Request request, RequestStatus status) {
        return request.toBuilder().status(status).build();
    }

    /**
     * update number of confirmed requests in the event
     *
     * @param event event
     * @param delta new confirmed requests quantity
     * @return updated event
     */
    private Event updateNumberOfConfirmedRequests(Event event, Integer delta) {
        return event.toBuilder()
                .confirmedRequests(event.getConfirmedRequests() + delta)
                .build();
    }

    /**
     * construct EventRequestStatusUpdateResultObject
     *
     * @param confirmed list of confirmed requests
     * @param rejected  list of rejected requested
     * @return EventRequestStatusUpdateResultObject
     */
    private EventRequestStatusUpdateResult constructResult(List<Request> confirmed, List<Request> rejected) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(RequestMapper.toParticipationRequestDtoList(confirmed))
                .rejectedRequests(RequestMapper.toParticipationRequestDtoList(rejected))
                .build();
    }

    /**
     * update and check nonNull field in the event
     *
     * @param event   event
     * @param request request to update
     * @param state   action to do
     * @return updated event
     */
    private Event updateNonNullFields(Event event, UpdateEventRequest request, StateAction state) {
        if (request.getAnnotation() != null) {
            event = event.toBuilder()
                    .annotation(request.getAnnotation())
                    .build();
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.getReferenceById(request.getCategory());
            event = event.toBuilder()
                    .category(category)
                    .build();
        }
        if (request.getDescription() != null) {
            event = event.toBuilder()
                    .description(request.getDescription())
                    .build();
        }
        if (request.getEventDate() != null) {
            LocalDateTime time = request.getEventDate();
            checkIsEventTimeIsNotTooLateToUpdateByAdmin(time);
            event = event.toBuilder()
                    .eventDate(request.getEventDate())
                    .build();
        }
        if (request.getLocation() != null) {
            Location location = locationRepository.save(request.getLocation());
            event = event.toBuilder()
                    .location(location)
                    .build();
        }
        if (request.getPaid() != null) {
            event = event.toBuilder()
                    .paid(request.getPaid())
                    .build();
        }
        if (request.getParticipantLimit() != null) {
            event = event.toBuilder()
                    .participantLimit(request.getParticipantLimit())
                    .build();
        }
        if (request.getParticipantLimit() != null) {
            event = event.toBuilder()
                    .requestModeration(request.getRequestModeration())
                    .build();
        }
        if (state != null) {
            event = event.toBuilder()
                    .state(String.valueOf(EnumMapper.mapToEventState(state)))
                    .build();
        }
        if (request.getTitle() != null) {
            event = event.toBuilder()
                    .title(request.getTitle())
                    .build();
        }
        return event;
    }

    /**
     * get list of uri
     *
     * @param events list of events
     * @param uri    string uri
     * @return list of uri
     */
    private List<String> getListOfUri(List<Event> events, String uri) {
        return events.stream().map(Event::getId).map(id -> getUriForEvent(uri, id))
                .collect(Collectors.toList());
    }

    /**
     * construct uri string for specified event
     *
     * @param uri     uri string
     * @param eventId event ID
     * @return uri string with eventId
     */
    private String getUriForEvent(String uri, Long eventId) {
        return uri + SLASH_PATH + eventId;
    }

    /**
     * get all pending requests from repository by event ID
     *
     * @param eventId event ID
     * @return list of pending requests
     */
    private List<Request> getRestOfPendingRequests(Long eventId) {
        return requestRepository
                .findALlByStatusAndEventId(RequestStatus.PENDING, eventId);
    }
}

