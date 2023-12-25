package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotFoundException;
import ru.practicum.utils.logger.ListLogger;
import ru.practicum.utils.mapper.UserMapper;
import ru.practicum.utils.paging.Paging;

import java.util.List;

import static ru.practicum.utils.errors.ErrorConstants.USER_NAME_UNIQUE_VIOLATION;

/**
 * USER SERVICE implementation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Add new user
     * throw Exception if userName is not unique
     *
     * @param userData user's data
     * @return new user
     */
    @Override
    @Transactional
    public UserDto addUser(NewUserRequest userData) {
        try {
            User newUser = userRepository.save(UserMapper.toUserEntity(userData));
            UserDto newUserDto = UserMapper.toUserDto(newUser);
            log.info("New user: {} added.", newUserDto);
            return newUserDto;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictConstraintUniqueException(USER_NAME_UNIQUE_VIOLATION);
        }
    }

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
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {

        List<User> users;
        if (ids == null) {
            users = userRepository.findAll(Paging.getPageable(from, size)).getContent();
        } else {
            users = userRepository.findAllByIdIn(ids, Paging.getPageable(from, size));
        }
        ListLogger.logResultList(users);
        return UserMapper.toUserDtoList(users);
    }

    /**
     * Delete user
     * throw Exception if user does not exist by id
     *
     * @param userId id of the user to be deleted
     */
    @Override
    @Transactional
    public UserDto deleteUser(Long userId) {
        UserDto deletedUser = UserMapper.toUserDto(getUserOrThrowException(userId));
        userRepository.deleteById(userId);
        log.info("Delete user with id: {}, user: {}", userId, deletedUser);
        return deletedUser;
    }

    /**
     * Get users, sorted by rating with paging options
     *
     * @param from number of elements that need to be skipped to form the current page, default value = 10
     * @param size number of elements per page, default value = 10
     * @return list of users
     */
    @Override
    public List<UserDto> getInitiatorsByRating(Integer from, Integer size) {
        List<User> users = userRepository.findAllByOrderByRatingDesc(Paging.getPageable(from, size));
        ListLogger.logResultList(users);
        return UserMapper.toUserDtoList(users);
    }

    /**
     * get User from repository by id or throw NotFoundException
     *
     * @param userId user ID
     * @return User
     */
    private User getUserOrThrowException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorConstants.getNotFoundMessage("User", userId)));
    }
}
