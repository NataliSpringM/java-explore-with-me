package ru.practicum.utils.errors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMAT;


/**
 * Error details
 * String status: HTTP response status code
 * String reason: General description of the cause of the error
 * List<String> errors: List of stack traces or error descriptions
 * String message: Error message
 * LocalDateTime timestamp: Date and time when the error occurred (in the format "yyyy-MM-dd HH:mm:ss")
 */

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    @JsonProperty("errors")
    private List<Error> errors;
    @JsonProperty("status")
    private String status;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("message")
    String message;
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT)
    private LocalDateTime timestamp;

}

