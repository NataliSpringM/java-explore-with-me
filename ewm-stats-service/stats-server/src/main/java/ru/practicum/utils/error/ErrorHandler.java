package ru.practicum.utils.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.CONTROLLERS_PATH;
import static ru.practicum.utils.constants.Constants.INCORRECTLY_MADE_REQUEST;

@RestControllerAdvice(CONTROLLERS_PATH)
@Slf4j
public class ErrorHandler {

    /**
     * Handle errors with validation violations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUniqueSqlViolations(ValidationException e) {
        return ApiError.builder()
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with validation violations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentValidViolations(MethodArgumentNotValidException e) {
        return ApiError.builder()
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with parameters type violations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeValidViolations(MethodArgumentTypeMismatchException e) {
        return ApiError.builder()
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with IllegalArgumentException
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException e) {
        return ApiError.builder()
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with unknown errors
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnknownErrors(RuntimeException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }


}
