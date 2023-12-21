package ru.practicum.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.controllers.admin.CategoryAdminController;
import ru.practicum.controllers.admin.UserAdminController;
import ru.practicum.controllers.priv.EventPrivateController;
import ru.practicum.controllers.pub.CategoryPublicController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.entity.Location;
import ru.practicum.utils.errors.exceptions.ConflictConstraintUniqueException;
import ru.practicum.utils.errors.exceptions.NotAllowedException;
import ru.practicum.utils.errors.exceptions.NotFoundException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.utils.constants.Constants.TEN_DEFAULT_VALUE;
import static ru.practicum.utils.constants.Constants.ZERO_DEFAULT_VALUE;
import static ru.practicum.utils.errors.ErrorConstants.*;

/**
 * CATEGORY INTEGRATION TESTS
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CategoryTest {

    @Autowired
    CategoryAdminController categoryAdminController;
    @Autowired
    CategoryPublicController categoryPublicController;
    @Autowired
    EventPrivateController eventPrivateController;
    @Autowired
    UserAdminController userAdminController;

    NewCategoryDto newConcertDto;
    NewCategoryDto newExhibitionDto;
    NewCategoryDto noNameCategoryDto;
    String nameConcert;
    String nameExhibition;

    @BeforeEach
    public void create() {
        nameConcert = "concert";
        nameExhibition = "exhibition";
        newConcertDto = NewCategoryDto.builder()
                .name(nameConcert)
                .build();
        newExhibitionDto = NewCategoryDto.builder()
                .name(nameExhibition)
                .build();
        noNameCategoryDto = NewCategoryDto.builder()
                .build();
    }

    /**
     * should add category and get category by id
     */

    @Test
    @Transactional
    public void shouldAddAndGetCategory() {
        //add object
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();

        //get object by id and check properties
        CategoryDto categoryDto = categoryPublicController.getCategoryById(catId);
        assertThat(categoryDto).hasFieldOrPropertyWithValue("id", catId)
                .hasFieldOrPropertyWithValue("name", nameConcert);
    }

    /**
     * should fail add category without name
     */
    @Test
    public void shouldFailAddCategoryWithoutName() {
        assertThrows(ConstraintViolationException.class,
                () -> categoryAdminController.addCategory(noNameCategoryDto),
                "ConstraintViolationException was not thrown");

    }

    /**
     * should fail add category with same name
     */
    @Test
    public void shouldFailAddCategoryWithSameName() {

        categoryAdminController.addCategory(newConcertDto);

        Exception e = assertThrows(ConflictConstraintUniqueException.class,
                () -> categoryAdminController.addCategory(newConcertDto),
                "ConflictConstraintUniqueException was not thrown");
        assertEquals(e.getMessage(), CATEGORY_NAME_UNIQUE_VIOLATION);
    }

    /**
     * should update category
     */
    @Test
    @Transactional
    public void shouldUpdateCategory() {

        //add object
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();

        //get object by id and check properties
        CategoryDto categoryDto = categoryPublicController.getCategoryById(catId);
        assertThat(categoryDto).hasFieldOrPropertyWithValue("id", catId)
                .hasFieldOrPropertyWithValue("name", nameConcert);

        //update object and check properties
        String newName = "newName";
        CategoryDto updated = category.toBuilder().name(newName).build();

        CategoryDto result = categoryAdminController.updateCategory(catId, updated);

        assertThat(result).hasFieldOrPropertyWithValue("id", catId)
                .hasFieldOrPropertyWithValue("name", newName);
    }

    /**
     * should fail update category with same name
     */
    @Test
    public void shouldFailUpdateCategoryWithSameName() {

        CategoryDto category1 = categoryAdminController.addCategory(newConcertDto);
        CategoryDto category2 = categoryAdminController.addCategory(newExhibitionDto);
        Long catId2 = category2.getId();
        CategoryDto sameNameDto = CategoryDto.builder().name(category1.getName()).build();


        Exception e = assertThrows(ConflictConstraintUniqueException.class,
                () -> categoryAdminController.updateCategory(catId2, sameNameDto),
                "ConflictConstraintUniqueException was not thrown");
        assertEquals(e.getMessage(), CATEGORY_NAME_UNIQUE_VIOLATION);
    }


    /**
     * should fail update category if not found by id
     */
    @Test
    public void shouldFailUpdateCategoryIfDoesNotExist() {

        String name = "Festival";
        CategoryDto categoryDto = CategoryDto.builder().name(name).build();
        Long nonExistedId = -1L;

        Exception e = assertThrows(NotFoundException.class,
                () -> categoryAdminController.updateCategory(nonExistedId, categoryDto),
                "NotFoundException was not thrown");
        assertEquals(e.getMessage(), getNotFoundMessage("Category", nonExistedId));
    }


    /**
     * should delete category
     */
    @Test
    @Transactional
    public void shouldDeleteCategory() {

        //add object
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();

        //get object by id and check properties
        CategoryDto categoryDto = categoryPublicController.getCategoryById(catId);
        assertThat(categoryDto).hasFieldOrPropertyWithValue("id", catId)
                .hasFieldOrPropertyWithValue("name", nameConcert);

        //delete object and check object does not exist
        categoryAdminController.deleteCategory(catId);
        assertThrows(NotFoundException.class,
                () -> categoryPublicController.getCategoryById(catId),
                "NotFoundException was not thrown");
    }

    /**
     * should fail delete category if events associated with exist
     */
    @Test
    @Transactional
    public void shouldFailDeleteNotEmptyCategory() {
        //add User
        String nameAlex = "nameAlex";
        String emailAlex = "Alex@yandex.ru";
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
        UserDto initiator = userAdminController.addUser(newUserRequest);
        //add object
        CategoryDto category = categoryAdminController.addCategory(newConcertDto);
        Long catId = category.getId();

        //add event
        String annotation = "This is valid annotation";
        String description = "This is valid description";
        LocalDateTime eventDate = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude = 54.55f;
        Float longitude = 55.677777f;
        Location location = Location.builder()
                .lat(latitude)
                .lon(longitude)
                .build();
        String title = "title";
        NewEventDto newEventDto = NewEventDto.builder()
                .category(catId)
                .annotation(annotation)
                .description(description)
                .eventDate(eventDate)
                .location(location)
                .paid(true)
                .participantLimit(0)
                .requestModeration(true)
                .title(title)
                .build();
        eventPrivateController.addEvent(initiator.getId(), newEventDto);

        //check throws
        Exception e = assertThrows(NotAllowedException.class,
                () -> categoryAdminController.deleteCategory(catId),
                "NotAllowedException was not thrown");
        assertEquals(e.getMessage(), CATEGORY_IS_NOT_EMPTY);
    }


    /**
     * should get categories
     */
    @Test
    @Transactional
    public void shouldGetCategories() {

        //add objects
        CategoryDto categoryConcert = categoryAdminController.addCategory(newConcertDto);
        CategoryDto categoryExhibition = categoryAdminController.addCategory(newExhibitionDto);
        Long catId1 = categoryConcert.getId();
        Long catId2 = categoryExhibition.getId();

        //get all objects and check properties

        List<CategoryDto> list = categoryPublicController.
                getCategories(Integer.valueOf(ZERO_DEFAULT_VALUE), Integer.valueOf(TEN_DEFAULT_VALUE));
        assertThat(list).asList().hasSize(2)
                .hasOnlyElementsOfType(CategoryDto.class)
                .startsWith(categoryConcert)
                .endsWith(categoryExhibition);
        assertThat(list.get(0)).hasFieldOrPropertyWithValue("id", catId1)
                .hasFieldOrPropertyWithValue("name", nameConcert);
        assertThat(list.get(1)).hasFieldOrPropertyWithValue("id", catId2)
                .hasFieldOrPropertyWithValue("name", nameExhibition);


    }

    /**
     * should get limited size and skipped parameter list
     */
    @Test
    @Transactional
    public void shouldGetCategoriesWithSetLimit() {

        //add objects
        CategoryDto categoryConcert = categoryAdminController.addCategory(newConcertDto);
        CategoryDto categoryExhibition = categoryAdminController.addCategory(newExhibitionDto);
        Long catId2 = categoryExhibition.getId();

        //get objects with limited size and skipped parameter and check properties
        Integer from = 1;
        Integer size = 1;

        List<CategoryDto> list = categoryPublicController.getCategories(from, size);
        assertThat(list).asList().hasSize(1)
                .hasOnlyElementsOfType(CategoryDto.class)
                .doesNotContain(categoryConcert)
                .contains(categoryExhibition);
        assertThat(list.get(0)).hasFieldOrPropertyWithValue("id", catId2)
                .hasFieldOrPropertyWithValue("name", nameExhibition);

    }

}
