package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * REQUEST TO UPDATE COMPILATION
 * Changing information about a compilation of events.
 * If the field is not specified in the request (equal to null), then changing this data is not required.
 * List<Long> events. List of selection event ids to completely replace the current list
 * String title. Title of the compilation
 * Boolean pinned. Equals true if the collection pinned to the main page of the site
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {
    private List<Long> events;
    @Size(min = 1, max = 50)
    private String title;
    private Boolean pinned;

}
