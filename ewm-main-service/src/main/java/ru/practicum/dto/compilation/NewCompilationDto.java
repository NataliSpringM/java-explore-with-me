package ru.practicum.dto.compilation;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * NEW COMPILATION DTO
 * List<Long> events. List of event identifiers included in the collection
 * String title. Must not be blank, min length = 1, max length = 50. Title of the compilation
 * Boolean pinned. Equals true if the collection pinned to the main page of the site
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    List<Long> events;
    @Size(min = 1, max = 50)
    @NotBlank
    String title;
    Boolean pinned;
}
