package ru.practicum.service.user;

import org.springframework.stereotype.Component;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;

import java.util.List;

/**
 * USER SERVICE interface
 */
@Component
public interface UserService {
    /**
     * Get users details by admin using specified criteria
     *
     * @param ids  list of user ids whose events need to be found
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return information about all users (sampling limitation parameters are taken into account),
     * or about specific users (specified identifiers are taken into account)
     * If no user is found by the specified filters, returns an empty list
     */

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    /**
     * Add new user
     *
     * @param user data of the user to be added
     * @return new user
     */
    UserDto addUser(NewUserRequest user);

    /**
     * Delete user or throw exception if user does not exist
     *
     * @param userId id of the user to be deleted
     */
    UserDto deleteUser(Long userId);

    /**
     * Get users, sorted by rating with paging options
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return list of users
     */
    List<UserDto> getInitiatorsByRating(Integer from, Integer size);
}
