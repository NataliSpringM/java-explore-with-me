package ru.practicum.controllers.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.event.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * EVENT ADMIN CONTROLLER
 * Private ADMIN access API for working with events, processing HTTP-requests to the endpoint "/admin/events"
 */

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ADMIN_PATH + EVENTS_PATH)
public class EventAdminController {
    private final EventService eventService;

    /**
     * Processing GET-request to the endpoint "/admin/events"
     * Event search
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

    @GetMapping
    public List<EventFullDto> getEventsByAdmin(
            @RequestParam(
                    name = USERS_PARAM_NAME,
                    required = false) List<Long> users,
            @RequestParam(
                    name = STATES_PARAMETER_NAME,
                    required = false) List<String> states,
            @RequestParam(
                    name = CATEGORIES_PARAMETER_NAME,
                    required = false) List<Long> categories,
            @RequestParam(
                    name = RANGE_START_PARAMETER_NAME,
                    required = false)
            @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(
                    name = RANGE_END_PARAMETER_NAME,
                    required = false)
            @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE) Integer from,
            @Positive @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-request to the endpoint \"/admin/events\".\n"
                        + "EVENTS. ADMIN ACCESS.\n"
                        + "Get events, added by users: {}, in states: {}, by categories: {}, \n"
                        + "in time interval between {} and {}, starting from: {}, number of events: {}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    /**
     * Processing PATCH-request to the endpoint "/admin/events/{eventId}"
     * Edit event data and its status (reject, publish)
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
    @PatchMapping(EVENT_ID_PATH_VARIABLE)
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody(required = false) UpdateEventAdminRequest request) {
        log.info("PATCH-request to the endpoint \"/admin/events/{}\".\n"
                + "EVENTS. ADMIN ACCESS.\n"
                + "Update event by id: {}. New data: {}", eventId, eventId, request);
        return eventService.updateEventByAdmin(eventId, request);
    }
}
