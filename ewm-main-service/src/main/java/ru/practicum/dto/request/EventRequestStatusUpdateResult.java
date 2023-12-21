package ru.practicum.dto.request;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

/**
 * RESULT get by ORGANISER : PARTICIPATION REQUEST STATUSES after updating
 * List<ParticipationRequestDto> confirmedRequests. List of the confirmed requests
 * (created, requestId, requester, status)
 * List<ParticipationRequestDto> rejectedRequests. List of the rejected requests
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class EventRequestStatusUpdateResult {
    List<ParticipationRequestDto> confirmedRequests;
    List<ParticipationRequestDto> rejectedRequests;

}
