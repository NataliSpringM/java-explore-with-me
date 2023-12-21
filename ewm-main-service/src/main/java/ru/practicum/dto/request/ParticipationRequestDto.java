package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.enums.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;

/**
 * PARTICIPATION REQUEST DTO
 * Request for participation in the event
 * Long requestId: Long, request ID
 * LocalDateTime created. LocalDateTime, date and time the request was created
 * Long event. Event ID
 * Long requester. ID of the user who submitted the request
 * RequestStatus status. RequestStatus /PENDING, CONFIRMED, REJECTED, CANCELED/
 **/
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    Long id;
    @JsonProperty("created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    LocalDateTime created;
    Long event;
    Long requester;
    RequestStatus status;

}
