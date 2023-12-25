package ru.practicum.service.event;


import org.springframework.stereotype.Component;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * EVENT SERVICE interface
 */
@Component
public interface EventService {

    /**
     * Get events for public sorted by rating with paging options.
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return List of events met filtering criteria.
     * If no events are found by the specified filters, returns an empty list
     */
    List<EventFullDto> getEventsByRating(Integer from, Integer size);


    /**
     * Get events for public with filtering options.
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
    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                        String sort, Integer from, Integer size, HttpServletRequest request);

    /**
     * Get full event information by ID for public access
     * The event must be published
     * Event information should include the number of views and the number of confirmed requests
     * information that a request was made and processed for this endpoint must be saved in the statistics service
     *
     * @param id      event ID
     * @param request http request information
     * @return detailed event information
     */
    EventFullDto getPublicEventById(Long id, HttpServletRequest request);

    /**
     * Get events added by user
     * If no events are found by the specified filters, returns an empty list
     *
     * @param userId user id
     * @param from   number of elements that need to be skipped to form the current page
     * @param size   number of elements per page
     * @return list of events
     */
    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    /**
     * Add new event
     *
     * @param userId user id
     * @param event  event information
     * @return new event
     */
    EventFullDto addEvent(Long userId, @Valid NewEventDto event);

    /**
     * Get full information about event added by user
     * if no event with the given id is found, status code 404 is returned.
     *
     * @param userId user id
     * @return full event information
     */
    EventFullDto getEventByUser(Long userId, Long eventId);

    /**
     * Update information about event added by user
     * only canceled events or events pending moderation can be changed:
     * (Error code 409 expected)
     *
     * @param userId  user id
     * @param eventId event id
     * @param event   event information to update
     * @return full event information, if no event with the given id is found, status code 404 is returned.
     */
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest event);

    /**
     * Get requests to participate in a specific event added by the current user
     *
     * @param userId  user id
     * @param eventId event id
     * @return list of requests, if no application is found based on the specified filters, it returns an empty list
     */
    List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId);

    /**
     * Get events details by admin using specified criteria
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

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    /**
     * Edit event data and its status (reject, publish) by admin
     * <p>
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

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    /**
     * Process PATCH-request to the endpoint "users/{userId}/events/{eventId}/requests"
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
    EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId,
                                                        EventRequestStatusUpdateRequest request);
}
