package ru.practicum.controllers.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.user.UserService;
import ru.practicum.utils.errors.ApiError;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.formatter.HttpStatusFormatter;
import ru.practicum.utils.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.utils.constants.Constants.*;
import static ru.practicum.utils.errors.ErrorConstants.*;


/**
 * UserAdminController WebMvcTest
 */

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc
public class UserAdminControllerTest {

    @Autowired
    MockMvc mock;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService service;

    /**
     * test addUser method
     * POST-request "/admin/users"
     * when user data are valid
     * should return status created
     * should invoke service addUser method and return result
     */
    @Test
    @SneakyThrows
    public void addUser_WhenUserIsValid_StatusIsCreated_AndInvokeService() {

        // create valid name and email, id
        String name = "Alex";
        String email = "Alex@yandex.ru";
        Long userId = 1L;

        // create NewUserRequest
        NewUserRequest userIn = NewUserRequest.builder()
                .name(name)
                .email(email)
                .build();

        // create expected out UserDto:
        UserDto userOut = UserMapper.toUserDto(
                UserMapper.toUserEntity(userIn)).toBuilder().id(userId).build();

        // map input and out objects into strings
        String userInString = mapper.writeValueAsString(userIn);
        String expectedString = mapper.writeValueAsString(userOut);

        //mock service answer
        when(service.addUser(userIn)).thenReturn(userOut);

        //perform tested request and check status and content
        String result = mock.perform(post(ADMIN_PATH + USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userInString))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userOut.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userIn.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userIn.getEmail()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).addUser(userIn);

        //check result
        assertEquals(expectedString, result);
    }

    /**
     * test addUser method
     * POST-request "/admin/users"
     * when user data is not valid, has no email
     * should return status bad request
     * should not invoke service addUser method and return ApiError
     */
    @Test
    @SneakyThrows
    public void addUser_WhenUserHasNoEmail_StatusIsBadRequest_DoesNotInvokeService() {

        // create valid name
        String name = "Alex";

        // create input Object
        NewUserRequest userIn = NewUserRequest.builder()
                .name(name)
                .build();

        // create expected out Object
        String fieldName = "email";
        String defaultMessage = "must not be blank";
        String message = "Field: " + fieldName + ". Error: " + defaultMessage + ". Value: " + null;

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String userInString = mapper.writeValueAsString(userIn);

        //perform tested request and check status and content
        mock.perform(post(ADMIN_PATH + USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userInString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).addUser(any());

    }

    /**
     * test addUser method
     * POST-request "/admin/users"
     * when User data is not valid, has already existing email
     * should return status conflict
     * should return ApiError
     */
    @Test
    @SneakyThrows
    public void addUser_WhenUserHasSameEmail_InvokeService_StatusIsConflict_ReturnApiError() {
        // create valid name and email, id
        String name = "Alex";
        String email = "alex@yandex.ru";


        // create NewUserRequest
        NewUserRequest userIn = NewUserRequest.builder()
                .name(name)
                .email(email)
                .build();

        String message = "could not execute statement; "
                + "SQL [n/a]; constraint [uq_user_name]; "
                + "nested exception is org.hibernate.exception.ConstraintViolationException: "
                + "could not execute statement";

        //mock service response
        when(service.addUser(userIn)).thenThrow(new DataIntegrityViolationException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.CONFLICT))
                .reason(DATA_INTEGRITY_VIOLATION)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // map input and out objects into strings
        String userInString = mapper.writeValueAsString(userIn);

        //perform tested request and check status and content
        mock.perform(post(ADMIN_PATH + USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userInString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).addUser(userIn);

    }

    /**
     * test getUsers method
     * GET-request "/admin/users", no required parameters
     * when parameters are valid and set
     * should return status ok
     * should invoke service getUsers method and return list of users
     */
    @Test
    @SneakyThrows
    public void getUsers_WhenParametersAreValid_StatusIsOk_InvokeService_ReturnListOfUsers() {

        // create valid names and emails, ids
        String name1 = "Alex";
        String email1 = "Alex@yandex.ru";
        Long userId1 = 1L;
        String name2 = "Egor";
        String email2 = "Egor@yandex.ru";
        Long userId2 = 2L;
        List<Long> ids = List.of(userId1, userId2);

        // create Users
        UserDto user1 = UserDto.builder()
                .id(userId1)
                .name(name1)
                .email(email1)
                .build();
        UserDto user2 = UserDto.builder()
                .id(userId2)
                .name(name2)
                .email(email2)
                .build();

        // create expected out UserDto:
        List<UserDto> users = List.of(user1, user2);

        // map input and out objects into strings
        String expectedString = mapper.writeValueAsString(users);

        //mock service answer
        Integer from = 0;
        Integer size = 10;
        when(service.getUsers(ids, from, size)).thenReturn(users);

        //perform tested request and check status and content
        String result = mock.perform(get(ADMIN_PATH + USERS_PATH)
                        .param(IDS_PARAMETER_NAME, userId1.toString())
                        .param(IDS_PARAMETER_NAME, userId2.toString())
                        .param(FROM_PARAMETER_NAME, ZERO_DEFAULT_VALUE)
                        .param(SIZE_PARAMETER_NAME, TEN_DEFAULT_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(user1.getName()), String.class))
                .andExpect(jsonPath("$.[0].email", is(user1.getEmail()), String.class))
                .andExpect(jsonPath("$.[1].id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].name", is(user2.getName()), String.class))
                .andExpect(jsonPath("$.[1].email", is(user2.getEmail()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getUsers(ids, from, size);

        //check result
        assertEquals(expectedString, result);
    }

    /**
     * test getUsers method
     * GET-request "/admin/users", no required parameters
     * when parameters are not set
     * should return status ok
     * should invoke service getUsers method and return list of users
     */
    @Test
    @SneakyThrows
    public void getUsers_WhenParametersAreNotSet_StatusIsOk_InvokeService_ReturnListOfUsersByDefaultValues() {

        // create valid names and emails, ids
        String name1 = "Alex";
        String email1 = "Alex@yandex.ru";
        Long userId1 = 1L;
        String name2 = "Egor";
        String email2 = "Egor@yandex.ru";
        Long userId2 = 2L;


        // create Users
        UserDto user1 = UserDto.builder()
                .id(userId1)
                .name(name1)
                .email(email1)
                .build();
        UserDto user2 = UserDto.builder()
                .id(userId2)
                .name(name2)
                .email(email2)
                .build();

        // create expected out UserDto:
        List<UserDto> users = List.of(user1, user2);

        // map input and out objects into strings
        String expectedString = mapper.writeValueAsString(users);

        //mock service answer
        Integer from = 0;
        Integer size = 10;
        when(service.getUsers(null, from, size)).thenReturn(users);

        //perform tested request and check status and content
        String result = mock.perform(get(ADMIN_PATH + USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(user1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(user1.getName()), String.class))
                .andExpect(jsonPath("$.[0].email", is(user1.getEmail()), String.class))
                .andExpect(jsonPath("$.[1].id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].name", is(user2.getName()), String.class))
                .andExpect(jsonPath("$.[1].email", is(user2.getEmail()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // verify invokes
        verify(service).getUsers(null, from, size);

        //check result
        assertEquals(expectedString, result);
    }

    /**
     * test getUsers method
     * GET-request "/admin/users", no required parameters
     * when parameter FROM is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getUsers method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getUsers_WhenParameterFromNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create parameters
        String invalidFrom = "FROM";
        String size = "0";
        String ids = "1";

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer';"
                + " nested exception is java.lang.NumberFormatException: For input string:"
                + " \"FROM\"";
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        mock.perform(get(ADMIN_PATH + USERS_PATH)
                        .param(IDS_PARAMETER_NAME, ids)
                        .param(FROM_PARAMETER_NAME, invalidFrom)
                        .param(SIZE_PARAMETER_NAME, size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));
        // verify invokes
        verify(service, never()).getUsers(any(), any(), any());
    }

    /**
     * test getUsers method
     * GET-request "/admin/users", no required parameters
     * when parameter SIZE is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getUsers method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getUsers_WhenParameterSizeNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create parameters
        String from = "0";
        String invalidSize = "SIZE";
        String ids = "1";

        // create expected out Object
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer';"
                + " nested exception is java.lang.NumberFormatException: For input string:"
                + " \"SIZE\"";

        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        mock.perform(get(ADMIN_PATH + USERS_PATH)
                        .param(IDS_PARAMETER_NAME, ids)
                        .param(FROM_PARAMETER_NAME, from)
                        .param(SIZE_PARAMETER_NAME, invalidSize))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getUsers(any(), any(), any());
    }

    /**
     * test getUsers method
     * GET-request "/admin/users", no required parameters
     * when parameter IDS is not valid, could not be converted into Integer value
     * should return status bad request
     * should not invoke service getUsers method and return ApiError object
     */
    @Test
    @SneakyThrows
    public void getUsers_WhenParameterIdsNotValid_StatusIsBadRequest_DoesNotInvokeService_ReturnApiError() {

        // create parameters
        String from = "0";
        String size = "10";
        Long userId = 1L;
        List<Long> list = List.of(userId);
        String invalidValue = list.toString();
        String message = "Failed to convert value of type 'java.lang.String' to required type 'java.util.List';"
                + " nested exception is java.lang.NumberFormatException: For input string:"
                + " \"" + invalidValue + "\"";

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.BAD_REQUEST))
                .reason(INCORRECTLY_MADE_REQUEST)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        mock.perform(get(ADMIN_PATH + USERS_PATH)
                        .param(IDS_PARAMETER_NAME, invalidValue)
                        .param(FROM_PARAMETER_NAME, from)
                        .param(SIZE_PARAMETER_NAME, size))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service, never()).getUsers(any(), any(), any());
    }

    /**
     * test deleteUser method
     * DELETE-request "/admin/users"
     * should invoke deleteUser method in service
     * should return status NO_CONTENT
     */
    @Test
    @SneakyThrows
    public void deleteUser_InvokeService_StatusIsNoContent() {

        // create UserDto
        String name = "Alex";
        String email = "Alex@yandex.ru";
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .id(userId)
                .name(name)
                .email(email)
                .build();

        // mock service answer
        when(service.deleteUser(userId)).thenReturn(userDto);

        //perform tested request and check status and content
        String userIdPath = "/1";
        mock.perform(delete(ADMIN_PATH + USERS_PATH + userIdPath))
                .andExpect(status().isNoContent());

        // verify invokes
        verify(service).deleteUser(userId);
    }

    /**
     * test deleteUser method
     * DELETE-request "/admin/users"
     * should invoke deleteUser method in service
     * should return status NotFound and ApiError
     */
    @Test
    @SneakyThrows
    public void deleteUser_WhenUserNotFound_InvokeService_StatusIsNotFound_ReturnApiError() {

        // create data
        Long userId = -1L;
        String message = getNotFoundMessage("User", userId);

        //mock service answer
        when(service.deleteUser(userId)).thenThrow(new NotFoundException(message));

        // create expected out Object
        ApiError apiError = ApiError.builder()
                .status(HttpStatusFormatter.formatHttpStatus(HttpStatus.NOT_FOUND))
                .reason(OBJECT_NOT_FOUND)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        //perform tested request and check status and content
        String userIdPath = "/-1";
        mock.perform(delete(ADMIN_PATH + USERS_PATH + userIdPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(apiError.getStatus()), String.class))
                .andExpect(jsonPath("$.reason", is(apiError.getReason()), String.class))
                .andExpect(jsonPath("$.message", is(apiError.getMessage()), String.class))
                .andExpect(jsonPath("$.timestamp").value(lessThanOrEqualTo(LocalDateTime.now().toString())));

        // verify invokes
        verify(service).deleteUser(userId);
    }
}
