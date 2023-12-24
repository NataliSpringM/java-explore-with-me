package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.entity.Category;
import ru.practicum.entity.Event;
import ru.practicum.entity.Location;
import ru.practicum.entity.User;
import ru.practicum.enums.EventState;
import ru.practicum.enums.SortType;
import ru.practicum.utils.mapper.CategoryMapper;
import ru.practicum.utils.mapper.EventMapper;
import ru.practicum.utils.mapper.UserMapper;
import ru.practicum.utils.paging.Paging;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.practicum.utils.constants.Constants.TEN_DEFAULT_VALUE;
import static ru.practicum.utils.constants.Constants.ZERO_DEFAULT_VALUE;

/**
 * EVENT REPOSITORY TESTS
 */
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EventRepositoryTest {
    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    LocationRepository locationRepository;
    User initiator;
    User participantAnna;
    User participantEgor;
    Long initiatorId;
    User participantOlga;
    Long participantAnnaId;
    Long participantOlgaId;
    Long participantEgorId;
    UserShortDto shortUserAlex;
    UserShortDto shortUserAnna;
    UserShortDto shortUserEgor;
    UserShortDto shortUserOlga;
    Category concert;
    Category exhibition;

    CategoryDto concertDto;
    CategoryDto exhibitionDto;
    Long catId1;
    Long catId2;
    String annotation1;
    String annotation2;
    String description1;
    String description2;
    LocalDateTime eventDate1;
    LocalDateTime eventDate2;
    Location location1;
    Location location2;
    String title1;
    String title2;
    Event event1;
    Event event2;
    Long eventId1;
    Long eventId2;

    @BeforeEach
    public void createEvents() {

        // create Users
        String nameAlex = "nameAlex";
        String nameAnna = "Anna";
        String nameEgor = "nameEgor";
        String nameOlga = "nameOlga";
        String emailAlex = "Alex@yandex.ru";
        String emailAnna = "Anna@yandex.ru";
        String emailEgor = "Egor@yandex.ru";
        String emailOlga = "Olga@yandex.ru";

        NewUserRequest requestUserAlex = NewUserRequest.builder()
                .name(nameAlex)
                .email(emailAlex)
                .build();
        NewUserRequest requestUserAnna = NewUserRequest.builder()
                .name(nameAnna)
                .email(emailAnna)
                .build();
        NewUserRequest requestUserOlga = NewUserRequest.builder()
                .name(nameOlga)
                .email(emailOlga)
                .build();
        NewUserRequest requestUserEgor = NewUserRequest.builder()
                .name(nameEgor)
                .email(emailEgor)
                .build();
        initiator = userRepository.save(UserMapper.toUserEntity(requestUserAlex));
        participantAnna = userRepository.save(UserMapper.toUserEntity(requestUserAnna));
        participantEgor = userRepository.save(UserMapper.toUserEntity(requestUserEgor));
        participantOlga = userRepository.save(UserMapper.toUserEntity(requestUserOlga));
        initiatorId = initiator.getId();
        participantAnnaId = participantAnna.getId();
        participantOlgaId = participantOlga.getId();
        participantEgorId = participantEgor.getId();
        shortUserAlex = UserMapper.toUserShortDto(initiator);
        shortUserAnna = UserMapper.toUserShortDto(participantAnna);
        shortUserEgor = UserMapper.toUserShortDto(participantEgor);
        shortUserOlga = UserMapper.toUserShortDto(participantOlga);

        // create Categories
        String nameConcert = "concert";
        String nameExhibition = "exhibition";
        NewCategoryDto newConcertDto = NewCategoryDto.builder()
                .name(nameConcert)
                .build();
        NewCategoryDto newExhibitionDto = NewCategoryDto.builder()
                .name(nameExhibition)
                .build();
        concert = categoryRepository.save(CategoryMapper.toCategoryEntity(newConcertDto));
        exhibition = categoryRepository.save(CategoryMapper.toCategoryEntity(newExhibitionDto));
        concertDto = CategoryMapper.toCategoryDto(concert);
        exhibitionDto = CategoryMapper.toCategoryDto(exhibition);
        catId1 = concertDto.getId();
        catId2 = exhibitionDto.getId();

        // create Event
        annotation1 = "This is valid annotation 1";
        annotation2 = "This is valid annotation 2";
        description1 = "This is valid description 1";
        description2 = "This is valid description 2";
        eventDate1 = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        eventDate2 = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
        Float latitude1 = 55.67f;
        Float longitude1 = 37.677777f;
        Float latitude2 = 47.67f;
        Float longitude2 = 39.677777f;
        location1 = Location.builder()
                .lat(latitude1)
                .lon(longitude1)
                .build();
        location2 = Location.builder()
                .lat(latitude2)
                .lon(longitude2)
                .build();
        locationRepository.save(location1);
        locationRepository.save(location2);
        title1 = "title 1";
        title2 = "title 2";
        NewEventDto newEvent1 = NewEventDto.builder()
                .category(catId1)
                .annotation(annotation1)
                .description(description1)
                .eventDate(eventDate1)
                .location(location1)
                .paid(true)
                .participantLimit(0)
                .requestModeration(true)
                .title(title1)
                .build();
        NewEventDto newEvent2 = newEvent1.toBuilder()
                .category(catId2)
                .annotation(annotation2)
                .description(description2)
                .eventDate(eventDate2)
                .location(location2)
                .paid(true)
                .participantLimit(2)
                .requestModeration(false)
                .title(title2)
                .build();
        Event event1ToSave = EventMapper.toEventEntity(newEvent1, location1, initiator, concert).toBuilder()
                .state(EventState.PUBLISHED.name()).build();
        Event event2ToSave = EventMapper.toEventEntity(newEvent2, location2, initiator, exhibition).toBuilder()
                .state(EventState.PUBLISHED.name())
                .confirmedRequests(2)
                .build();
        event1 = eventRepository.save(event1ToSave);
        event2 = eventRepository.save(event2ToSave);
        eventId1 = event1.getId();
        eventId2 = event2.getId();
    }

    /**
     * should get published events
     */
    @Test
    public void shouldGetAllEventsForPublic() {

        //create parameters for search
        String text = "annotation";
        List<Long> categories = List.of(1L, 2L);
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusYears(3);
        SortType sort = SortType.EVENT_DATE;
        String state = String.valueOf(EventState.PUBLISHED);
        Pageable page = Paging.getPageable(
                Integer.valueOf(ZERO_DEFAULT_VALUE),
                Integer.valueOf(TEN_DEFAULT_VALUE),
                sort);
        // get events
        List<Event> events = eventRepository.findAllForPublic(
                text, categories, true, start, end, state, page);

        // check event
        assertThat(events).asList().hasSize(2)
                .hasOnlyElementsOfType(Event.class)
                .contains(event1)
                .contains(event2);

        assertThat(events.get(0)).hasFieldOrPropertyWithValue("id", eventId2)
                .hasFieldOrPropertyWithValue("annotation", annotation2)
                .hasFieldOrPropertyWithValue("category", exhibition)
                .hasFieldOrPropertyWithValue("confirmedRequests", 2)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description2)
                .hasFieldOrPropertyWithValue("initiator", initiator)
                .hasFieldOrPropertyWithValue("eventDate", eventDate2)
                .hasFieldOrPropertyWithValue("location", location2)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 2)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", false)
                .hasFieldOrPropertyWithValue("state", EventState.PUBLISHED.name())
                .hasFieldOrPropertyWithValue("title", title2);
        assertThat(events.get(1)).hasFieldOrPropertyWithValue("id", eventId1)
                .hasFieldOrPropertyWithValue("annotation", annotation1)
                .hasFieldOrPropertyWithValue("category", concert)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description1)
                .hasFieldOrPropertyWithValue("initiator", initiator)
                .hasFieldOrPropertyWithValue("eventDate", eventDate1)
                .hasFieldOrPropertyWithValue("location", location1)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PUBLISHED.name())
                .hasFieldOrPropertyWithValue("title", title1);


    }

    /**
     * should get published events
     */
    @Test
    public void shouldGetAvailableEventsForPublic() {

        //create parameters for search
        String text = "annotation";
        List<Long> categories = List.of(catId2, catId1);
        Boolean paid = true;
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusMonths(1);
        SortType sort = SortType.EVENT_DATE;
        String state = String.valueOf(EventState.PUBLISHED);
        Pageable page = Paging.getPageable(
                Integer.valueOf(ZERO_DEFAULT_VALUE),
                Integer.valueOf(TEN_DEFAULT_VALUE),
                sort);

        // get events
        List<Event> events = eventRepository.findAvailableForPublic(
                text, categories, paid, start, end, state, page);

        // check event
        assertThat(events).asList().hasSize(1)
                .hasOnlyElementsOfType(Event.class)
                .contains(event1)
                .doesNotContain(event2);

        assertThat(events.get(0)).hasFieldOrPropertyWithValue("id", eventId1)
                .hasFieldOrPropertyWithValue("annotation", annotation1)
                .hasFieldOrPropertyWithValue("category", concert)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description1)
                .hasFieldOrPropertyWithValue("initiator", initiator)
                .hasFieldOrPropertyWithValue("eventDate", eventDate1)
                .hasFieldOrPropertyWithValue("location", location1)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PUBLISHED.name())
                .hasFieldOrPropertyWithValue("title", title1);

    }


    /**
     * should get published events
     */
    @Test
    public void shouldGetEventsForAdmin() {

        //create parameters for search
        List<Long> users = List.of(initiatorId);
        List<String> states = List.of(EventState.PUBLISHED.name());
        List<Long> categories = List.of(catId1, catId2);

        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusYears(3);
        SortType sort = SortType.EVENT_DATE;

        Pageable page = Paging.getPageable(
                Integer.valueOf(ZERO_DEFAULT_VALUE),
                Integer.valueOf(TEN_DEFAULT_VALUE),
                sort);
        // get events
        List<Event> events = eventRepository.findForAdmin(
                users, states, categories, start, end, page);

        // check event
        assertThat(events).asList().hasSize(2)
                .hasOnlyElementsOfType(Event.class)
                .contains(event1)
                .contains(event2);

        assertThat(events.get(0)).hasFieldOrPropertyWithValue("id", eventId2)
                .hasFieldOrPropertyWithValue("annotation", annotation2)
                .hasFieldOrPropertyWithValue("category", exhibition)
                .hasFieldOrPropertyWithValue("confirmedRequests", 2)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description2)
                .hasFieldOrPropertyWithValue("initiator", initiator)
                .hasFieldOrPropertyWithValue("eventDate", eventDate2)
                .hasFieldOrPropertyWithValue("location", location2)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 2)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", false)
                .hasFieldOrPropertyWithValue("state", EventState.PUBLISHED.name())
                .hasFieldOrPropertyWithValue("title", title2);
        assertThat(events.get(1)).hasFieldOrPropertyWithValue("id", eventId1)
                .hasFieldOrPropertyWithValue("annotation", annotation1)
                .hasFieldOrPropertyWithValue("category", concert)
                .hasFieldOrPropertyWithValue("confirmedRequests", 0)
                .hasFieldOrProperty("createdOn")
                .hasFieldOrPropertyWithValue("description", description1)
                .hasFieldOrPropertyWithValue("initiator", initiator)
                .hasFieldOrPropertyWithValue("eventDate", eventDate1)
                .hasFieldOrPropertyWithValue("location", location1)
                .hasFieldOrPropertyWithValue("paid", true)
                .hasFieldOrPropertyWithValue("participantLimit", 0)
                .hasFieldOrPropertyWithValue("publishedOn", null)
                .hasFieldOrPropertyWithValue("requestModeration", true)
                .hasFieldOrPropertyWithValue("state", EventState.PUBLISHED.name())
                .hasFieldOrPropertyWithValue("title", title1);

    }
}
