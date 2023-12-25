package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.entity.Location;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;

/**
 * FULL EVENT DTO:
 * Long id. Event ID
 * String annotation. Event annotation.
 * CategoryDto category. Event category.
 * Integer confirmedRequests. Number of approved requests for participation in this event
 * LocalDateTime createdOn. Date and time the event was created ("yyyy-MM-dd HH:mm:ss")
 * LocalDateTime eventDate. Date and time for which the event is scheduled ("yyyy-MM-dd HH:mm:ss")
 * String description. Full event description.
 * UserShortDto initiator. Event initiator (short info).
 * Location LocationDto. Latitude and longitude of the event LocationDto.
 * Boolean paid. Equals true if participation is paid
 * Integer participantLimit. Limitation on the number of participants. Value 0 means no restriction
 * LocalDateTime publishedOn. Date and time the event was published ("yyyy-MM-dd HH:mm:ss")
 * Boolean requestModeration. Equals true if pre-moderation of applications for participation is required
 * String actionState. Enumeration of event lifecycle states. [ PENDING, PUBLISHED, CANCELED ]
 * String title. Event title.
 * Long views. Number of event views
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdOn;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long rating;
    private Long views;
}
