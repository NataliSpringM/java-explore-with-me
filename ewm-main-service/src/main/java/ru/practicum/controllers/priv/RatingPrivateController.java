package ru.practicum.controllers.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.service.rating.RatingService;

import javax.validation.Valid;

import static ru.practicum.utils.constants.Constants.*;

/**
 * RATING CONTROLLER
 * processing HTTP-requests to "/ratings/{userId}" end-point to add or delete likes and dislikes ratings.
 */

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(RATINGS_PATH + USER_ID_PATH_VARIABLE)
public class RatingPrivateController {
    private final RatingService ratingService;

    /**
     * Processing POST-request to the endpoint "/ratings/{userId}/events/{eventId}"
     * Add like or dislike to the event
     * user must have the confirmed  participant request in an event
     * duplicate rating is not allowed. any rating must be deleted before change mind
     * event must be published
     * initiator not allowed to like or dislike
     *
     * @param rating LIKE or DISLIKE rating
     * @return rating
     */
    @PostMapping(EVENTS_PATH + EVENT_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public RatingDto addEventRating(
            @Valid @PathVariable Long userId,
            @Valid @PathVariable Long eventId,
            @Valid @RequestParam(name = "rating") String rating) {
        log.info("POST-request to the endpoint \"/rating/{}/events/{}\"\n"
                        + "RATING. PRIVATE ACCESS.\n"
                        + "User {} add new rating to the event {}, rating: {}",
                userId, eventId, userId, eventId, rating);
        return ratingService.addEventRating(userId, eventId, rating);
    }

    /**
     * Processing POST-request to the endpoint "/ratings/{userId}/users/{userId}"
     * Add like or dislike to the initiator of the event
     * user must not rate himself
     * user must not rate user who is not an initiator of the events with confirmed participant requests
     *
     * @param rating LIKE or DISLIKE
     * @return rating
     */
    @PostMapping(USERS_PATH + INITIATOR_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public RatingDto addInitiatorRating(
            @Valid @PathVariable Long userId,
            @Valid @PathVariable Long initiatorId,
            @Valid @RequestParam(name = "rating") String rating) {
        log.info("POST-request to the endpoint \"/ratings/{}/users/{}\"\n"
                        + "RATING. PRIVATE ACCESS.\n"
                        + "User {} add new rating to the initiator of the events {}, rating: {}",
                userId, initiatorId, userId, initiatorId, rating);
        return ratingService.addInitiatorRating(userId, initiatorId, rating);
    }

    /**
     * Processing DELETE-request to the endpoint "/ratings/{userId}/users/{userId}
     * delete rating from initiator
     * user must be rater
     *
     * @return rating
     */
    @DeleteMapping(USERS_PATH + INITIATOR_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Validated
    public RatingDto deleteInitiatorRating(
            @Valid @PathVariable Long userId,
            @Valid @PathVariable Long initiatorId) {
        log.info("DELETE-request to the endpoint \"/ratings/{}/users/{}\"\n"
                        + "RATING. PRIVATE ACCESS.\n"
                        + "User: {} delete rating for the initiator: {}.",
                userId, initiatorId, userId, initiatorId);
        return ratingService.deleteInitiatorRating(userId, initiatorId);
    }

    /**
     * Processing DELETE-request to the endpoint "/ratings/{userId}/events/{eventId}
     * delete rating from event
     * user must be rater
     *
     * @return rating
     */
    @DeleteMapping(EVENTS_PATH + EVENT_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Validated
    public RatingDto deleteEventRating(
            @Valid @PathVariable Long userId,
            @Valid @PathVariable Long eventId) {
        log.info("DELETE-request to the endpoint \"/ratings/{}/events/{}\".\n."
                        + "RATING. PRIVATE ACCESS.\n"
                        + "User: {} delete rating for the event: {}.",
                userId, eventId, userId, eventId);
        return ratingService.deleteEventRating(userId, eventId);
    }
}
