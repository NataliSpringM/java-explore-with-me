package ru.practicum.controllers.priv;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.RequestStatus;
import ru.practicum.service.request.RequestService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.*;
import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * RequestPrivateController WebMvcTest
 */

@WebMvcTest(RequestPrivateController.class)
@AutoConfigureMockMvc
public class RequestPrivateControllerTest {

    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    RequestService service;
    Long eventId;
    Long requestId;
    Long requesterId;
    String requestIdPath;
    String userIdPath;

    @BeforeEach
    public void create() {
        requestId = 1L;
        eventId = 1L;
        requesterId = 1L;
        userIdPath = "/1";
        requestIdPath = "/1";
    }


    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is valid
     * should return status CREATED 201
     * should return ParticipationRequestDto
     * should invoke service addParticipationRequest method
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenRequestIsValid_InvokeService_StatusIsCreated_ReturnRequest() {

        // create expected
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(requesterId)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .event(eventId)
                .build();

        // map input and out objects into strings
        String expectedString = mapper.writeValueAsString(requestDto);

        //mock service answer
        when(service.addParticipationRequest(requesterId, eventId)).thenReturn(requestDto);

        //perform tested request and check status and content
        String result = mock.perform(post(USERS_PATH + userIdPath + REQUESTS_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, eventId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.created").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.event", is(requestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.status", is(requestDto.getStatus().name()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).addParticipationRequest(requesterId, eventId);

        // check result
        assertEquals(expectedString, result);
    }

    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is invalid : required parameter is missed
     * should not invoke service addParticipationRequest method
     * should return status BAD REQUEST 400
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenRequiredParameterMissed_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create expected out Object
        String message = "Field: Long eventId. "
                + "Error: Required request parameter 'eventId' for method parameter type Long is not present. "
                + "Value: null";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userIdPath + REQUESTS_PATH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }

    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is not valid : invalid eventId parameter (negative)
     * should return status BAD REQUEST 400
     * should not invoke service updateCompilation method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenRequestParamIsNegative_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        //create data
        long invalidEventId = -1L;

        // create expected out Object
        String message = "Field: addParticipationRequest.eventId. Error: must be greater than 0. Value: -1";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userIdPath + REQUESTS_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, String.valueOf(invalidEventId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }

    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is not valid : invalid pathVariable userId (negative)
     * should return status BAD REQUEST 400
     * should not invoke service updateCompilation method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenPathVariableIsNegative_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        //create data
        String invalidPathId = "/-1";

        // create expected out Object
        String message = "Field: addParticipationRequest.userId. Error: must be greater than 0. Value: -1";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + invalidPathId + REQUESTS_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, eventId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }


    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is not valid : invalid (cannot be to convert into number format)
     * should return status BAD REQUEST 400
     * should not invoke service updateCompilation method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenRequestParamIsNotNumber_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        //create data
        String invalidParamEventId = "EVENT_ID";

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; "
                + "nested exception is java.lang.NumberFormatException: For input string: \"EVENT_ID\"";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userIdPath + REQUESTS_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, invalidParamEventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }

    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is not valid at the repository level : object (event) is not found by id
     * should invoke service addParticipationRequest method
     * should return status NOT FOUND 404
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenEntityNotFound_InvokeService_StatusNotFound_ReturnApiError() {


        // create expected out Object
        String message = ErrorConstants.getNotFoundMessage("Event", eventId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //mock service answer
        when(service.addParticipationRequest(requesterId, eventId)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userIdPath + REQUESTS_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, eventId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).addParticipationRequest(requesterId, eventId);
    }

    /**
     * test addParticipationRequest method
     * POST-request to the endpoint "/users/{userId}/requests"
     * when request is invalid at the service level :  repeated request from the same user
     * should invoke service addParticipationRequest method
     * should return status CONFLICT 409
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addRequest_WhenRequestIsNotFirst_InvokeService_StatusIsConflict_ReturnApiError() {

        //mock service answer
        String message = REPEATED_REQUEST;
        when(service.addParticipationRequest(requesterId, eventId))
                .thenThrow(new NotAllowedException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(ACTION_IS_NOT_ALLOWED)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(post(USERS_PATH + userIdPath + REQUESTS_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, eventId.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).addParticipationRequest(requesterId, eventId);
    }

    /**
     * test cancelParticipationRequest method
     * PATCH-request to the endpoint "/users/{userId}/requests"
     * when request is valid
     * should invoke service cancelParticipationRequest method
     * should return status OK 200
     * should return ParticipationRequestDto
     */
    @Test
    @SneakyThrows
    public void cancelRequest_WhenRequestIsValid_InvokeService_StatusIsOk_ReturnRequest() {

        // create expected
        ParticipationRequestDto requestDto = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(requesterId)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .event(eventId)
                .build();

        // map input and out objects into strings
        String expectedString = mapper.writeValueAsString(requestDto);

        //mock service answer
        when(service.cancelParticipationRequest(requestId, eventId)).thenReturn(requestDto);

        //perform tested request and check status and content
        String result = mock
                .perform(patch(USERS_PATH + userIdPath + REQUESTS_PATH + requestIdPath + CANCEL_PATH)
                        .param(EVENT_ID_PARAMETER_NAME, eventId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.created").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.event", is(requestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.status", is(requestDto.getStatus().name()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).cancelParticipationRequest(requestId, eventId);

        // check result
        assertEquals(expectedString, result);
    }

    /**
     * test cancelParticipationRequest method
     * PATCH-request to the endpoint "/users/{userId}/requests/{requestId}"
     * when request is not valid : invalid negative pathVariable requestId
     * should return status BAD REQUEST 400
     * should not invoke service cancelParticipationRequest method
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void cancelRequest_WhenPathVariableIsNotValid_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create data
        String invalidPathId = "/-1";

        // create expected out Object
        String message = "Field: cancelParticipationRequest.requestId. Error: must be greater than 0. Value: -1";

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(patch(USERS_PATH + userIdPath + REQUESTS_PATH + invalidPathId + CANCEL_PATH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }

    /**
     * test cancelParticipationRequest method
     * PATCH-request to the endpoint "/users/{userId}/requests/{requestId}"
     * when request is not valid at the service level: request is not found
     * should invoke service cancelParticipationRequest method
     * should return status NOT FOUND 404
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void cancelRequest_WhenRequiredParameterMissed_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create expected out Object
        String message = getNotFoundMessage("Request", requestId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .build();

        // mock service answer
        when(service.cancelParticipationRequest(requesterId, requestId)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content
        mock.perform(patch(USERS_PATH + userIdPath + REQUESTS_PATH + requestIdPath + CANCEL_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }

    /**
     * test cancelParticipationRequest method
     * PATCH-request to the endpoint "/users/{userId}/requests/{requestId}"
     * when request is not valid at the service level: request is not found
     * should invoke service cancelParticipationRequest method
     * should return status NOT FOUND 404
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void cancelRequest_WhenRequestNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create expected out Object
        String message = getNotFoundMessage("Request", requestId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .build();

        // mock service answer
        when(service.cancelParticipationRequest(requesterId, requestId)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content
        mock.perform(patch(USERS_PATH + userIdPath + REQUESTS_PATH + requestIdPath + CANCEL_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addParticipationRequest(any(), any());
    }

    /**
     * test getUserParticipationRequests method
     * GET-request to the endpoint "/users/{userId}/requests"
     * when request is valid
     * should return status OK 200
     * should return list of ParticipationRequestDto
     * should invoke service getUserParticipationRequests method
     */
    @Test
    @SneakyThrows
    public void getRequests_WhenRequestIsValid_InvokeService_StatusIsOK_ReturnRequestsList() {

        // create expected
        ParticipationRequestDto requestDto1 = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(requesterId)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .event(eventId)
                .build();
        ParticipationRequestDto requestDto2 = ParticipationRequestDto.builder()
                .id(requestId)
                .requester(requesterId)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .event(eventId)
                .build();
        List<ParticipationRequestDto> requests = List.of(requestDto1, requestDto2);

        // map out object into strings
        String expectedString = mapper.writeValueAsString(requests);

        //mock service answer
        when(service.getParticipationRequests(requesterId)).thenReturn(requests);

        //perform tested request and check status and content
        String result = mock.perform(get(USERS_PATH + userIdPath + REQUESTS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(requestDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].requester", is(requestDto1.getRequester()), Long.class))
                .andExpect(jsonPath("$.[0].created").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.[0].event", is(requestDto1.getEvent()), Long.class))
                .andExpect(jsonPath("$.[0].status", is(requestDto1.getStatus().name()), String.class))
                .andExpect(jsonPath("$.[1].id", is(requestDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].requester", is(requestDto2.getRequester()), Long.class))
                .andExpect(jsonPath("$.[1].created").value(lessThanOrEqualTo(LocalDateTime.now().toString())))
                .andExpect(jsonPath("$.[1].event", is(requestDto2.getEvent()), Long.class))
                .andExpect(jsonPath("$.[1].status", is(requestDto2.getStatus().name()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getParticipationRequests(requesterId);

        // check result
        assertEquals(expectedString, result);
    }

    /**
     * test getUserParticipationRequests method
     * GET-request to the endpoint "/users/{userId}/requests"
     * when request is invalid : invalid pathVariable (negative value)
     * should return status NOT FOUND 404
     * should return ApiError
     * should not invoke service getUserParticipationRequests method
     */
    @Test
    @SneakyThrows
    public void getRequests_WhenPathVariableIsInvalid_DoesNotInvokeService_StatusIsBadRequest_ReturnApiError() {

        // create data
        String invalidPathVariable = "/-1L";

        // create expected
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long';"
                + " nested exception is java.lang.NumberFormatException: For input string: \"-1L\"";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .build();

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + invalidPathVariable + REQUESTS_PATH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getParticipationRequests(any());
    }

    /**
     * test getUserParticipationRequests method
     * GET-request to the endpoint "/users/{userId}/requests"
     * when request is invalid at the repository level : requester is not found
     * should invoke service getUserParticipationRequests method
     * should return status NOT FOUND 404
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void getRequests_WhenRequesterNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create expected
        String message = getNotFoundMessage("User", requesterId);
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .build();

        // mock service answer
        when(service.getParticipationRequests(requesterId)).thenThrow(new NotFoundException(message));

        //perform tested request and check status and content
        mock.perform(get(USERS_PATH + userIdPath + REQUESTS_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).getParticipationRequests(requesterId);

    }
}
