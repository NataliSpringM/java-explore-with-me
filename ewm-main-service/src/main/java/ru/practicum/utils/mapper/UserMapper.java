package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Map User entity and DTOs into each other
 */
@UtilityClass
public class UserMapper {

    /**
     * map NewUserRequest into User entity
     */
    public static User toUserEntity(NewUserRequest user) {
        return User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .rating(0L)
                .build();
    }

    /**
     * map User entity into UserDto
     */

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .rating(user.getRating())
                .build();
    }

    /**
     * map User entity into UserDto
     */

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    /**
     * map UserDto into UserShortDto
     */

    public static UserShortDto toUserShortDto(UserDto userDto) {
        return UserShortDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .build();
    }

    /**
     * map list of User entities into UserDto list
     */

    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
