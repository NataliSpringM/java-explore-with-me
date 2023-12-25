package ru.practicum.dto.request;

import lombok.*;
import ru.practicum.enums.RequestStatus;

import java.util.List;

/**
 * RESULT get by REQUESTER of the PARTICIPATION REQUEST STATUS after updating
 * List<Long> requestsIds. List of the IDs of current user's event participation requests
 * RequestStatus status. New status to update
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}
