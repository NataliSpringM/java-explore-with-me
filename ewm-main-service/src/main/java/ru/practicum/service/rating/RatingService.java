package ru.practicum.service.rating;

import org.springframework.stereotype.Component;
import ru.practicum.dto.rating.RatingDto;

/**
 * RATING SERVICE interface
 */
@Component
public interface RatingService {

    /**
     * Add like or dislike to the event
     * user must not be an initiator of the event
     * user must be a participant in an event that has already taken place
     *
     * @param rating like or dislike
     * @return rating
     */
    RatingDto addEventRating(Long userId, Long eventId, String rating);

    /**
     * Add like or dislike to the initiator of the event
     * user must not rate himself
     * user must not rate user who is not an initiator of the events
     *
     * @param rating like or dislike
     * @return rating
     */
    RatingDto addInitiatorRating(Long userId, Long initiatorId, String rating);

    /**
     * Processing DELETE-request to the endpoint "/ratings/{userId}/events/{eventId}
     * delete rating from event
     * user must be rater
     *
     * @return rating
     */
    RatingDto deleteInitiatorRating(Long userId, Long initiatorId);

    /**
     * Processing DELETE-request to the endpoint "/ratings/{userId}/users/{userId}
     * delete rating from initiator
     * user must be rater
     *
     * @return rating
     */
    RatingDto deleteEventRating(Long userId, Long eventId);
}
