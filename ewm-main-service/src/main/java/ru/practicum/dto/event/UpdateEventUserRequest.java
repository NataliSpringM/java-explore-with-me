package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.lang.Nullable;
import ru.practicum.entity.Location;
import ru.practicum.enums.StateAction;
import ru.practicum.utils.validation.TwoHoursLater;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;

/**
 * USER request to UPDATE EVENT DTO:
 * String annotation. Event annotation.
 * Integer category. Event category ID.
 * LocalDateTime eventDate. Date and time for which the event is scheduled ("yyyy-MM-dd HH:mm:ss")
 * String description. Full event description.
 * Location LocationDto. Latitude and longitude of the event LocationDto.
 * Boolean paid. Equals true if participation is paid
 * Integer participantLimit. Limitation on the number of participants. Value 0 means no restriction
 * Boolean requestModeration. Equals true if pre-moderation of applications for participation is required
 * Enumeration of initiator actions on event states. [SEND_TO_REVIEW, CANCEL_REVIEW]
 * String title. Event title.
 **/
@Data
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventUserRequest extends UpdateEventRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000)
    private String description;
    @JsonProperty("eventDate")
    @JsonFormat(pattern = DATE_TIME_FORMAT, shape = JsonFormat.Shape.STRING)
    @Nullable
    @TwoHoursLater
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
    @Size(min = 3, max = 120)
    private String title;
}
