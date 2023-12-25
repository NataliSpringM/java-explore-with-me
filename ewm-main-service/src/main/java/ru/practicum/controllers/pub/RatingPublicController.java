package ru.practicum.controllers.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.event.EventService;
import ru.practicum.service.user.UserService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * RATING PRIVATE CONTROLLER
 * processing HTTP-requests to "/ratings" end-point to get sorted by ratings lists
 */

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(RATINGS_PATH)
public class RatingPublicController {

    private final EventService eventService;
    private final UserService userService;

    /**
     * Processing GET-request to the endpoint "/ratings/events"
     * Get events sorted by rating with paging options
     * Only published events should appear in the results
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return sorted by rating events
     */
    @GetMapping(EVENTS_PATH)
    @ResponseStatus(HttpStatus.OK)
    @Validated
    public List<EventFullDto> getEventsByRating(
            @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE)
            @PositiveOrZero Integer from,
            @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE)
            @Positive Integer size) {
        log.info("GET-request to the endpoint \"/ratings/events\".\n. "
                + "RATINGS. PUBLIC ACCESS.\n"
                + "Get events sorted by rating, starting with: {}, number of events: {}", from, size);
        return eventService.getEventsByRating(from, size);
    }

    /**
     * Processing GET-request to the endpoint "/ratings/users"
     * get users sorted by rating as initiators
     *
     * @return sorted by rating users
     */
    @GetMapping(USERS_PATH)
    @ResponseStatus(HttpStatus.OK)
    @Validated
    public List<UserDto> getInitiatorByRating(
            @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE)
            @PositiveOrZero Integer from,
            @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE)
            @Positive Integer size) {
        log.info("GET-request to the endpoint \"/ratings/events\".\n. "
                + "RATINGS. PUBLIC ACCESS.\n"
                + "Get initiators sorted by rating, starting with: {}, number of users: {}", from, size);
        return userService.getInitiatorsByRating(from, size);
    }

}
