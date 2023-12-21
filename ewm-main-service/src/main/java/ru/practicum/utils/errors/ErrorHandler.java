package ru.practicum.utils.errors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.utils.constants.Constants.CONTROLLERS_PATH;
import static ru.practicum.utils.errors.ErrorConstants.*;

@RestControllerAdvice(CONTROLLERS_PATH)
@Slf4j
public class ErrorHandler {

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
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(constructMessage(e))
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
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with missing parameters
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameters(MissingServletRequestParameterException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(constructMessage(e))
                .timestamp(LocalDateTime.now())
                .build();
    }


    /**
     * Handle errors with not allowed operations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler(NotAllowedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRuntimeViolations(NotAllowedException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(ACTION_IS_NOT_ALLOWED)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with not found entities
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }


    /**
     * Handle errors with unique validation violations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler(ConflictConstraintUniqueException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRuntimeViolations(ConflictConstraintUniqueException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with unique validation violations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUniqueSqlViolations(ConstraintViolationException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(constructMessage(e))
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors with unique validation violations
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUniqueSqlViolations(ValidationException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Handle errors when trying to save duplicate values for fields with uniqueness restrictions
     *
     * @param e Exception class
     * @return ApiError
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictUniqueConstraintViolations(DataIntegrityViolationException e) {
        return ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
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
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
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
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR))
                .reason(e.getClass().getName())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * construct String message with details on exception
     *
     * @param e exception
     * @return message
     */
    private String constructMessage(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        assert error != null;
        return ErrorMessage.builder()
                .field(error.getField())
                .error(error.getDefaultMessage())
                .value(error.getRejectedValue())
                .build().toString();

    }

    /**
     * construct String message with details on exception
     *
     * @param e exception
     * @return message
     */
    private String constructMessage(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        return violations.stream()
                .map(violation -> new ErrorMessage(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .collect(Collectors.toList())
                .get(0)
                .toString();
    }


    private String constructMessage(MissingServletRequestParameterException e) {
        return ErrorMessage.builder()
                .field(e.getParameterType() + " " + e.getParameterName())
                .error(e.getMessage())
                .value(null)
                .build()
                .toString();
    }
}
