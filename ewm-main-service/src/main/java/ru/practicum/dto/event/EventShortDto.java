package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;

/**
 * SHORT EVENT DTO
 * Long eventId. Event ID
 * String annotation. Event annotation.
 * CategoryDto category. Event category.
 * Integer confirmedRequests. Number of approved requests for participation in this event
 * LocalDateTime eventDate. Date and time for which the event is scheduled ("yyyy-MM-dd HH:mm:ss")
 * UserShortDto initiator. Event initiator (short info).
 * Boolean paid. Equals true if participation is paid
 * String title. Event title.
 * Long views. Number of event views
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    Long id;
    String annotation;
    CategoryDto category;
    Integer confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    LocalDateTime eventDate;
    UserShortDto initiator;
    Boolean paid;
    String title;
    Long views;
}
