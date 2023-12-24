package ru.practicum.controllers.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.user.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * USER ADMIN CONTROLLER, processing HTTP-requests to the endpoint "/admin/users"
 * Private ADMIN access API for working with users
 */

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ADMIN_PATH + USERS_PATH)
public class UserAdminController {

    private final UserService userService;

    /**
     * Processing GET-request to the endpoint "/admin/users"
     * Get users details by admin
     *
     * @param ids  list of user ids whose events need to be found
     * @param from number of elements that need to be skipped to form the current page, default value = 0
     * @param size number of elements per page, default value = 10
     * @return information about all users (sampling limitation parameters are taken into account),
     * or about specific users (specified identifiers are taken into account)
     * If no user is found by the specified filters, returns an empty list
     */

    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(name = IDS_PARAMETER_NAME,
                    required = false) List<Long> ids,
            @PositiveOrZero @RequestParam(
                    name = FROM_PARAMETER_NAME,
                    defaultValue = ZERO_DEFAULT_VALUE) Integer from,
            @Positive @RequestParam(
                    name = SIZE_PARAMETER_NAME,
                    defaultValue = TEN_DEFAULT_VALUE) Integer size) {
        log.info("GET-request to the endpoint \"/admin/users\".\n"
                + "USERS. ADMIN ACCESS.\n"
                + "Get users by ids: {}, starting from: {}, number of events: {}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    /**
     * Processing POST-request to the endpoint "/admin/users"
     * Add new user
     *
     * @param user data of the user to be added
     * @return new user
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated
    public UserDto addUser(@Valid @RequestBody NewUserRequest user) {
        log.info("POST-request to the endpoint \"/admin/users\".\n"
                + "USERS. ADMIN ACCESS.\n"
                + "Add new user: {}", user);
        return userService.addUser(user);
    }

    /**
     * Processing DELETE-request to the endpoint "/admin/users/{userId}"
     * Delete user
     *
     * @param userId id of the user to be deleted
     */
    @DeleteMapping(USER_ID_PATH_VARIABLE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserDto deleteUser(@PathVariable Long userId) {
        log.info("DELETE-request to the endpoint \"/admin/users/{}\".\n"
                + "USERS. ADMIN ACCESS.\n"
                + "Delete user by id: {}", userId, userId);
        return userService.deleteUser(userId);
    }
}
