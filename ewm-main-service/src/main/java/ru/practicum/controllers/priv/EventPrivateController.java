package ru.practicum.controllers.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.event.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * EVENT PRIVATE CONTROLLER
 * Private API for working with events, processing HTTP-requests to the endpoint "/users/{userId}/events"
 */

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(USERS_PATH + USER_ID_PATH_VARIABLE + EVENTS_PATH)
public class EventPrivateController {
    private final EventService eventService;

    /**
     * Processing GET-request to the endpoint "/users/{userId}/events"
     * Get events added by user
     *
     * @param userId user id
     * @param from   number of elements that need to be skipped to form the current page
     * @param size   number of elements per page
     * @return list of events, if no events are found by the specified filters, returns an empty list
     */
    @GetMapping
    public List<EventShortDto> getEventsByUser(@Positive @PathVariable Long userId,
                                               @PositiveOrZero @RequestParam(
                                                       name = FROM_PARAMETER_NAME,
                                                       defaultValue = ZERO_DEFAULT_VALUE) Integer from,
                                               @Positive @RequestParam(
                                                       name = SIZE_PARAMETER_NAME,
                                                       defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-request to the endpoint \"/users/{}/events\".\n"
                + "EVENTS. PRIVATE ACCESS.\n"
                + "Get events added by user:{}, starting with: {}, number of events: {}", userId, userId, from, size);
        return eventService.getEventsByUser(userId, from, size);
    }

    /**
     * Process POST-request to the endpoint "/users/{userId}/events"
     * Add new event
     * the date and time for which the event is scheduled cannot be earlier than two hours from the current moment
     * (Error code 409 is expected)
     *
     * @param userId user id
     * @param event  event information
     * @return new event
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    public EventFullDto addEvent(@Positive @PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto event) {
        log.info("POST-request to the endpoint \"/users/{}/events\".\n"
                + "EVENTS. PRIVATE ACCESS.\n"
                + "An attempt is made to add an event {} by user {}.", userId, event.getTitle(), userId);
        return eventService.addEvent(userId, event);
    }

    /**
     * Process GET-request to the endpoint "users/{userId}/events/{eventId}"
     * Get full information about event added by user
     *
     * @param userId  user id
     * @param eventId event id
     * @return full event information, if no event with the given id is found, status code 404 is returned.
     */
    @GetMapping(EVENT_ID_PATH_VARIABLE)
    public EventFullDto getEventByUser(@Positive @PathVariable Long userId,
                                       @Positive @PathVariable Long eventId) {
        log.info("GET-request to the endpoint \"users/{}/events/{}\".\n"
                + "EVENTS. PRIVATE ACCESS.\n"
                + "Get event by id: {}", userId, eventId, eventId);
        return eventService.getEventByUser(userId, eventId);

    }

    /**
     * Processing PATCH-request to the endpoint "users/{userId}/events/{eventId}"
     * Update information about event added by user
     * the date and time for which the event is scheduled cannot be earlier than two hours from the current moment:
     * (Error code 409 is expected)
     * only canceled events or events pending moderation can be changed:
     * (Error code 409 expected)
     *
     * @param userId  user id
     * @param eventId event id
     * @param event   Data for changing event information. If the field is not specified in the request (equal to null),
     *                then changing this data is not required.
     * @return full event information, if no event with the given id is found, status code 404 is returned.
     */
    @PatchMapping(EVENT_ID_PATH_VARIABLE)

    public EventFullDto updateEventByUser(@PathVariable Long userId,
                                          @PathVariable Long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest event) {
        log.info("PATCH-request to the endpoint \"users/{}/events/{}\".\n"
                + "EVENTS. PRIVATE ACCESS.\n"
                + "Update event {} by user {}. New data: {}", userId, eventId, eventId, userId, event);
        return eventService.updateEventByUser(userId, eventId, event);

    }

    /**
     * Processing GET-request to the endpoint "users/{userId}/events/{eventId}/requests"
     * obtain information about requests to participate in a specific event added by the current user
     *
     * @param userId  user id
     * @param eventId event id
     * @return list of requests, if no application is found based on the specified filters, it returns an empty list
     */
    @GetMapping(EVENT_ID_PATH_VARIABLE + REQUESTS_PATH)
    public List<ParticipationRequestDto> getParticipationRequests(@Positive @PathVariable Long userId,
                                                                  @Positive @PathVariable Long eventId) {
        log.info("GET-request to the endpoint \"users/{}/events/{}/requests\".\n"
                        + "EVENTS. PARTICIPATION REQUESTS. PRIVATE ACCESS.\n"
                        + "Get requests to participate in the event with id: {} made by the user with id:{}",
                userId, eventId, eventId, userId);
        return eventService.getParticipationRequests(userId, eventId);

    }

    /**
     * Processing PATCH-request to the endpoint "users/{userId}/events/{eventId}/requests"
     * update the status of requests (confirm, reject) for participation in an event added by the current user
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
    @PatchMapping(EVENT_ID_PATH_VARIABLE + REQUESTS_PATH)
    public EventRequestStatusUpdateResult updateRequestsStatus(@Positive @PathVariable(name = "userId") Long userId,
                                                               @Positive @PathVariable(name = "eventId") Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PRIVATE ACCESS. USER UPDATE OWN REQUEST STATUS. \n"
                        + "PATCH-request to the endpoint \"users/{}/events/{}/requests\".\n"
                        + "User: {} {} event {} participation requests: {}",
                userId, eventId, userId, request.getStatus(), eventId, request);
        return eventService.updateRequestsStatus(userId, eventId, request);
    }
}
