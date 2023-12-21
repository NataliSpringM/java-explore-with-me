package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.entity.Location;
import ru.practicum.utils.validation.TwoHoursLater;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;

/**
 * NEW EVENT DTO
 * String annotation. Event annotation, must not be blank, min length = 20, max length = 2000
 * Integer category. ID of the category to which the event belongs
 * String description. Full event description.
 * Integer confirmedRequests. Number of approved requests for participation in this event
 * LocalDateTime eventDate. Date and time for which the event is scheduled ("yyyy-MM-dd HH:mm:ss")
 * Location LocationDto. Latitude and longitude of the event LocationDto.
 * Boolean paid. Equals true if participation is paid
 * Integer participantLimit. Limitation on the number of participants. Value 0 means no restriction
 * Boolean requestModeration. Equals true if pre-moderation of applications for participation is required
 * String title. Event title.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @Size(min = 20, max = 2000)
    @NotBlank
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000)
    @NotBlank
    private String description;
    @NotNull
    @TwoHoursLater
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;
    @NotNull
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    @Size(min = 3, max = 120)
    String title;
}
