package ru.practicum.service.request;


import org.springframework.stereotype.Component;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

/**
 * PARTICIPATION REQUESTS SERVICE interface
 */
@Component
public interface RequestService {

    /**
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
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    /**
     * Cancel request to participate in an event
     *
     * @param userId    user id
     * @param requestId request id
     * @return canceled participation request
     */
    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

    /**
     * Obtain information about user requests to participate in other people's events
     *
     * @param userId user id
     * @return list of requests, if no request is found based on the specified filters, it returns an empty list
     */
    List<ParticipationRequestDto> getParticipationRequests(Long userId);
}
