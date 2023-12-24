package ru.practicum.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.controllers.admin.CategoryAdminController;
import ru.practicum.controllers.admin.CompilationAdminController;
import ru.practicum.controllers.admin.UserAdminController;
import ru.practicum.controllers.priv.EventPrivateController;
import ru.practicum.controllers.pub.CompilationPublicController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Location;
import ru.practicum.utils.errors.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * COMPILATION INTEGRATION TESTS
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CompilationTest {
    @Autowired
    UserAdminController userAdminController;
    @Autowired
    CategoryAdminController categoryAdminController;
    @Autowired
    EventPrivateController eventPrivateController;
    @Autowired
    CompilationAdminController compilationAdminController;
    @Autowired
    CompilationPublicController compilationPublicController;

    NewUserRequest requestUserAlex;
    NewUserRequest requestUserAnna;
    NewCategoryDto newConcertDto;
    NewCategoryDto newExhibitionDto;
    String annotation;
    String invalidAnnotation;
    String description;
    LocalDateTime eventDate;
    LocalDateTime invalidEventDate;
    Location location;
    Float latitude;
    Float longitude;
    String title;
    String compilationTitle;
    NewEventDto newEventDtoAllFieldsLimitIsSetEqualsTwo;
    NewEventDto newEventDtoAllFieldsLimitIsNotSet;
    NewCompilationDto newCompilationDto1;
    NewCompilationDto newCompilationDto2;


    @BeforeEach
    public void create() {

        //create new Users DTO
        String nameAlex = "nameAlex";
        String nameAnna = "Anna";
        String emailAlex = "Alex@yandex.ru";
        String emailAnna = "Anna@yandex.ru";

        requestUserAlex = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
        requestUserAnna = NewUserRequest.builder()
                .name(nameAnna)
                .email(emailAnna)
                .build();

        //create new Categories DTO
        String nameConcert = "concert";
        String nameExhibition = "exhibition";
        newConcertDto = NewCategoryDto.builder()
                .name(nameConcert)
                .build();
        newExhibitionDto = NewCategoryDto.builder()
                .name(nameExhibition)
                .build();


        //create new Events DTO
        annotation = "This is valid annotation";
        invalidAnnotation = "invalidAnnotation";
        description = "This is valid description";
        eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        invalidEventDate = LocalDateTime.now().plusMinutes(30);
        latitude = 54.55f;
        longitude = 55.677777f;
        location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        title = "title";
        newEventDtoAllFieldsLimitIsNotSet = NewEventDto.builder()
                .annotation(annotation)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .paid(true)
                .participantLimit(0)
                .requestModeration(true)
                .title(title)
                .build();
        newEventDtoAllFieldsLimitIsSetEqualsTwo = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .participantLimit(2)
                .build();

        // create NewCompilationsDto
        compilationTitle = "compilationTitle";
        newCompilationDto1 = NewCompilationDto.builder()
                .title(title)
                .build();
        newCompilationDto2 = NewCompilationDto.builder()
                .title(compilationTitle)
                .build();

    }

    /**
     * should add and get compilation by id
     */
    @Test
    @Transactional
    public void shouldAddAndGetCompilation() {

        // create initiator, category and events
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent1 = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();
        NewEventDto newEvent2 = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
                .category(catId)
                .build();


        // add events
        eventPrivateController.addEvent(initiatorId, newEvent1);
        eventPrivateController.addEvent(initiatorId, newEvent2);

        // add compilation
        CompilationDto compilationDto = compilationAdminController.addCompilation(newCompilationDto1);
        Integer compilationId = compilationDto.getId();

        // get compilation
        CompilationDto compilation = compilationPublicController.getCompilationById(compilationId);

        // check compilation
        assertThat(compilation).hasFieldOrPropertyWithValue("id", compilationId)
                .hasFieldOrPropertyWithValue("events", Collections.emptyList())
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("pinned", false);

    }

    /**
     * should add and get compilations
     */
    @Test
    @Transactional
    public void shouldAddAndGetCompilations() {

        // create initiator, category and events
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent1 = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();
        NewEventDto newEvent2 = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
                .category(catId)
                .build();


        // add events
        EventFullDto eventFullDto1 = eventPrivateController.addEvent(initiatorId, newEvent1);
        eventPrivateController.addEvent(initiatorId, newEvent2);
        Long eventId1 = eventFullDto1.getId();
        Long eventId2 = eventFullDto1.getId();
        List<Long> eventsId = List.of(eventId1, eventId2);

        // add compilation
        CompilationDto compilationDto1 = compilationAdminController.addCompilation(newCompilationDto1);
        CompilationDto compilationDto2 = compilationAdminController.addCompilation(newCompilationDto2
                .toBuilder()
                .events(eventsId)
                .build());

        // get compilation
        List<CompilationDto> compilations = compilationPublicController
                .getCompilations(false, 0, 5);

        // check compilation
        assertThat(compilations).asList().hasSize(2)
                .hasOnlyElementsOfType(CompilationDto.class)
                .contains(compilationDto1)
                .contains(compilationDto2);

    }

    /**
     * should fail get compilations
     */
    @Test
    @Transactional
    public void shouldFaiGetCompilationById() {

        // create nonExisting ID
        Integer compilationId = -1;

        // check throws and exception message
        Exception e = assertThrows(NotFoundException.class,
                () -> compilationPublicController.getCompilationById(compilationId),
                "NotFoundException was not thrown");

        assertEquals(e.getMessage(), "Compilation with id=" + compilationId + " was not found");

    }


    /**
     * should update compilation
     */
    @Test
    @Transactional
    public void shouldUpdateCompilation() {

        // create initiator, category and events
        UserDto initiator = userAdminController.addUser(requestUserAlex);
        Long initiatorId = initiator.getId();
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();
        NewEventDto newEvent1 = newEventDtoAllFieldsLimitIsNotSet.toBuilder()
                .category(catId)
                .build();
        NewEventDto newEvent2 = newEventDtoAllFieldsLimitIsSetEqualsTwo.toBuilder()
                .category(catId)
                .build();

        // add events
        EventFullDto eventFullDto1 = eventPrivateController.addEvent(initiatorId, newEvent1);
        EventFullDto eventFullDto2 = eventPrivateController.addEvent(initiatorId, newEvent2);
        Long eventId1 = eventFullDto1.getId();
        Long eventId2 = eventFullDto2.getId();
        List<Long> eventsId = List.of(eventId1, eventId2);


        // add compilation
        CompilationDto compilationDto = compilationAdminController.addCompilation(newCompilationDto1);
        Integer compilationId = compilationDto.getId();

        // get compilation
        CompilationDto compilation = compilationPublicController.getCompilationById(compilationId);

        // check compilation
        assertThat(compilation).hasFieldOrPropertyWithValue("id", compilationId)
                .hasFieldOrPropertyWithValue("events", Collections.emptyList())
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("pinned", false);

        // update compilation
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .events(eventsId)
                .pinned(true)
                .build();
        CompilationDto updated = compilationAdminController.updateCompilation(compilationId, request);

        // check compilation

        assertThat(updated).hasFieldOrPropertyWithValue("id", compilationId)
                //.hasFieldOrPropertyWithValue("events", )
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("pinned", true);

    }

    /**
     * should delete compilation
     */
    @Test
    @Transactional
    public void shouldDeleteCompilation() {

        // create initiator, category and events
        userAdminController.addUser(requestUserAlex);
        categoryAdminController.addCategory(newConcertDto);

        // add compilation
        CompilationDto compilationDto = compilationAdminController.addCompilation(newCompilationDto1);
        Integer compilationId = compilationDto.getId();

        // get compilation
        CompilationDto compilation = compilationPublicController.getCompilationById(compilationId);

        // check compilation
        assertThat(compilation).hasFieldOrPropertyWithValue("id", compilationId)
                .hasFieldOrPropertyWithValue("events", Collections.emptyList())
                .hasFieldOrPropertyWithValue("title", title)
                .hasFieldOrPropertyWithValue("pinned", false);

        // delete compilation
        compilationAdminController.deleteCompilation(compilationId);

        // check throws
        Exception e = assertThrows(NotFoundException.class,
                () -> compilationPublicController.getCompilationById(compilationId),
                "NotFoundException was not thrown");

        assertEquals(e.getMessage(), "Compilation with id=" + compilationId + " was not found");

    }

}
