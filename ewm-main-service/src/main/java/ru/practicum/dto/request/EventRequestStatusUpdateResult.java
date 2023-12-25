package ru.practicum.dto.request;

import lombok.*;

import java.util.List;

/**
 * RESULT get by ORGANISER : PARTICIPATION REQUEST STATUSES after updating
 * List<ParticipationRequestDto> confirmedRequests. List of the confirmed requests
 * (created, requestId, requester, status)
 * List<ParticipationRequestDto> rejectedRequests. List of the rejected requests
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;
    private List<ParticipationRequestDto> rejectedRequests;

}
