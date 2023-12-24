package ru.practicum.controllers.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.request.RequestService;

import javax.validation.constraints.Positive;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;


/**
 * REQUEST PRIVATE CONTROLLER
 * Closed API for working with current user requests to participate in events,
 * processing HTTP-requests to the endpoint "/users/{userId}/requests"
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(USERS_PATH + USER_ID_PATH_VARIABLE + REQUESTS_PATH)
@Validated
public class RequestPrivateController {
    private final RequestService requestService;

    /**
     * Processing GET-request to the endpoint "/users/{userId}/requests"
     * Get information about user requests to participate in other people's events
     *
     * @param userId user id
     * @return list of requests, if no request is found based on the specified filters, it returns an empty list
     */
    @GetMapping
    public List<ParticipationRequestDto> getUserParticipationRequests(@Positive @PathVariable Long userId) {
        log.info("GET-request to the endpoint \"/users/{}/requests\".\n"
                + "PARTICIPATION REQUESTS. PRIVATE ACCESS.\n"
                + "Get participation requests for user {}", userId, userId);
        return requestService.getParticipationRequests(userId);
    }


    /**
     * Processing POST-request to the endpoint "/users/{userId}/requests"
     * Add a request from a user to participate in an event
     * cannot add a repeat request (Expecting error code 409)
     * the event initiator cannot add a request to participate in his event (Error code 409 expected)
     * cannot participate in an unpublished event (Error code 409 expected)
     * if the event has reached the limit of requests for participation, an error must be returned
     * (Error code 409 is expected)
     * if pre-moderation of participation requests is disabled for an event,
     * then the request should automatically switch to the confirmed state
     *
     * @param userId  user id
     * @param eventId event id
     * @return new participation request
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@Positive @PathVariable Long userId,
                                                           @Positive @RequestParam Long eventId) {
        log.info("POST-request to the endpoint \"/users/{}/requests\".\n"
                + "PARTICIPATION REQUESTS. PRIVATE ACCESS.\n"
                + "Add new request to participate in the event: {} from user: {}", userId, eventId, userId);
        return requestService.addParticipationRequest(userId, eventId);
    }


    /**
     * Processing PATCH-request to the endpoint "/users/{userId}/requests"
     * Cancel own request to participate in an event
     *
     * @param userId    user id
     * @param requestId request id
     * @return canceled participation request
     */
    @PatchMapping(REQUEST_ID_PATH_VARIABLE + CANCEL_PATH)
    public ParticipationRequestDto cancelParticipationRequest(@Positive @PathVariable Long userId,
                                                              @Positive @PathVariable Long requestId) {
        log.info("PATCH-request to the endpoint \"/users/{}/requests\".\n"
                + "PARTICIPATION REQUESTS. PRIVATE ACCESS.\n"
                + "Cancel participation request with id: {} from user: {}", userId, requestId, userId);
        return requestService.cancelParticipationRequest(userId, requestId);
    }

}
