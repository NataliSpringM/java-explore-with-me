package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

/**
 * COMPILATION DTO
 * Integer compilation ID
 * List<EventShortDto> events. List of events in short details included in the collection
 * String title. Title of the compilation
 * Boolean pinned. Equals true if the collection pinned to the main page of the site
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    Integer id;
    List<EventShortDto> events;
    String title;
    Boolean pinned;
}


