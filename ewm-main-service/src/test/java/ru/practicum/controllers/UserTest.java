package ru.practicum.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.controllers.admin.UserAdminController;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.utils.errors.ErrorConstants;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.utils.errors.ErrorConstants.USER_NAME_UNIQUE_VIOLATION;

/**
 * USER INTEGRATION TESTS
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserTest {
    @Autowired
    UserAdminController userAdminController;
    String nameAlex;
    String nameAnna;
    String emailAlex;
    String emailAnna;
    NewUserRequest userAlex;
    NewUserRequest userAnna;
    NewUserRequest noNameUser;
    NewUserRequest noEmailUser;
    NewUserRequest sameEmailUser;


    @BeforeEach
    public void create() {
        nameAlex = "nameAlex";
        nameAnna = "Anna";
        emailAlex = "Alex@yandex.ru";
        emailAnna = "Anna@yandex.ru";

        userAlex = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
        userAnna = NewUserRequest.builder()
                .name(nameAnna)
                .email(emailAnna)
                .build();
        noNameUser = NewUserRequest.builder()
                .email(emailAlex)
                .build();
        noEmailUser = NewUserRequest.builder()
                .name(nameAlex)
                .build();
        sameEmailUser = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
    }

    /**
     * should add and get two users
     */
    @Test
    public void shouldAddAndGetUsers() {
        UserDto user1 = userAdminController.addUser(userAlex);
        UserDto user2 = userAdminController.addUser(userAnna);
        Long id1 = user1.getId();
        Long id2 = user2.getId();
        List<Long> ids = Arrays.asList(id1, id2);

        List<UserDto> users = userAdminController.getUsers(ids, 0, 2);

        assertThat(users).asList().hasSize(2);
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("id", id1)
                .hasFieldOrPropertyWithValue("name", nameAlex);
        assertThat((users.get(1))).hasFieldOrPropertyWithValue("id", id2)
                .hasFieldOrPropertyWithValue("name", nameAnna);
    }

    /**
     * should add two users and get one of them according to size parameter
     */
    @Test
    public void shouldGetUsersWithLimitedSize() {
        UserDto user1 = userAdminController.addUser(userAlex);
        UserDto user2 = userAdminController.addUser(userAnna);
        Long id1 = user1.getId();
        Long id2 = user2.getId();
        List<Long> ids = Arrays.asList(id1, id2);

        List<UserDto> users = userAdminController.getUsers(ids, 0, 1);

        assertThat(users).asList().hasSize(1);
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("id", id1)
                .hasFieldOrPropertyWithValue("name", nameAlex);

    }

    /**
     * should delete user
     */

    @Test
    public void shouldDeleteUser() {
        UserDto user1 = userAdminController.addUser(userAlex);
        UserDto user2 = userAdminController.addUser(userAnna);
        Long id1 = user1.getId();
        Long id2 = user2.getId();
        List<Long> ids = Arrays.asList(id1, id2);

        List<UserDto> users = userAdminController.getUsers(ids, 0, 2);

        assertThat(users).asList().hasSize(2);
        assertThat(users.get(0)).hasFieldOrPropertyWithValue("id", id1)
                .hasFieldOrPropertyWithValue("name", nameAlex)
                .hasFieldOrPropertyWithValue("email", emailAlex);
        assertThat((users.get(1))).hasFieldOrPropertyWithValue("id", id2)
                .hasFieldOrPropertyWithValue("name", nameAnna)
                .hasFieldOrPropertyWithValue("email", emailAnna);

        userAdminController.deleteUser(id1);

        List<UserDto> usersAfterDeleting = userAdminController.getUsers(ids, 0, 2);

        assertThat(usersAfterDeleting).asList().hasSize(1);
        assertThat((usersAfterDeleting.get(0))).hasFieldOrPropertyWithValue("id", id2)
                .hasFieldOrPropertyWithValue("name", nameAnna)
                .hasFieldOrPropertyWithValue("email", emailAnna);

    }

    /**
     * should throw exception if user by id to delete does not exist
     */

    @Test
    public void shouldFailDeleteUserIfDoesNotExist() {

        Long nonExistedId = -1L;
        String message = ErrorConstants.getNotFoundMessage("User", nonExistedId);

        Exception e = assertThrows(NotFoundException.class,
                () -> userAdminController.deleteUser(nonExistedId),
                "NotFoundException was not thrown");
        assertEquals(e.getMessage(), message);

    }


    /**
     * should fail add user without name
     */
    @Test
    public void shouldFailAddUserWithoutName() {

        assertThrows(ConstraintViolationException.class,
                () -> userAdminController.addUser(noNameUser),
                "ConstraintViolationException was not thrown");
    }

    /**
     * should fail add user without email
     */
    @Test
    public void shouldFailAddUserWithoutEmail() {

        assertThrows(ConstraintViolationException.class,
                () -> userAdminController.addUser(noEmailUser),
                "ConstraintViolationException was not thrown");

    }

    /**
     * should fail add user with doubling email
     */
    @Test
    public void shouldFailAddUserWithSameEmail() {

        userAdminController.addUser(userAlex);

        Exception e = assertThrows(ConflictConstraintUniqueException.class,
                () -> userAdminController.addUser(userAlex),
                "ConflictConstraintUniqueException was not thrown");
        assertEquals(e.getMessage(), USER_NAME_UNIQUE_VIOLATION);

    }
}
