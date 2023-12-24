package ru.practicum.controllers.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.event.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * EVENT CONTROLLER
 * processing HTTP-requests to "/events" end-point to get events.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(EVENTS_PATH)
public class EventPublicController {
    private final EventService eventService;


    /**
     * processing a GET-request to the endpoint "events"
     * Get events with filtering options.
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

    @GetMapping
    public List<EventShortDto> getPublicEvents(
            @RequestParam(
                    name = TEXT_PARAMETER_NAME,
                    required = false) String text,
            @RequestParam(
                    name = CATEGORIES_PARAMETER_NAME,
                    required = false) List<Long> categories,
            @RequestParam(
                    name = PAID_PARAMETER_NAME,
                    required = false) Boolean paid,
            @RequestParam(
                    name = RANGE_START_PARAMETER_NAME,
                    required = false)
            @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeStart,
            @RequestParam(
                    name = RANGE_END_PARAMETER_NAME,
                    required = false)
            @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime rangeEnd,
            @RequestParam(
                    name = ONLY_AVAILABLE_PARAM_NAME,
                    defaultValue = FALSE_DEFAULT_VALUE) Boolean onlyAvailable,
            @RequestParam(
                    name = SORT_PARAMETER_NAME,
                    required = false) String sort,
            @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE) @PositiveOrZero Integer from,
            @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE) @Positive Integer size,
            HttpServletRequest request) {
        log.info("GET-request to the endpoint \"events\".\n"
                        + "EVENTS. PUBLIC ACCESS.\n"
                        + "Get events by text: {} from categories: {}, paid: {}, start: {}, end: {},\n"
                        + " only available: {}, sort by: {}, starting from: {}, number of events: {}, request by {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request.getRemoteAddr());

        return eventService
                .getPublicEvents(text, categories, paid, rangeStart, rangeEnd,
                        onlyAvailable, sort, from, size, request);
    }

    /**
     * Processing a GET-request to the endpoint "/events/{eventId}
     * Get full event information by ID for public access
     * The event must be published
     * Event information should include the number of views and the number of confirmed requests
     * information that a request was made and processed for this endpoint must be saved in the statistics service
     *
     * @param eventId event ID
     * @return detailed event information
     */
    @GetMapping(EVENT_ID_PATH_VARIABLE)
    public EventFullDto getPublicEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("GET-request to the endpoint \"/events/{}.\n"
                + "EVENTS. PUBLIC ACCESS.\n"
                + "Get event by id {}", eventId, eventId);
        return eventService.getPublicEventById(eventId, request);
    }
}
