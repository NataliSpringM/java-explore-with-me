package ru.practicum.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.entity.Location;
import ru.practicum.enums.StateAction;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    protected String annotation;
    protected Long category;
    protected String description;
    protected LocalDateTime eventDate;
    protected Location location;
    protected Boolean paid;
    protected Integer participantLimit;
    protected Boolean requestModeration;
    protected StateAction stateAction;
    protected String title;
}
