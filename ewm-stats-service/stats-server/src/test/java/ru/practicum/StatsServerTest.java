package ru.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.controller.StatsController;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMATTER;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class StatsServerTest {
    @Autowired
    private StatsController controller;
    LocalDateTime time2020;
    LocalDateTime time2021;
    LocalDateTime time2022;
    LocalDateTime time2023;
    LocalDateTime time2024;
    LocalDateTime time2025;
    LocalDateTime time2050;
    String uriEvents;
    String uriEventsId;
    String ip;
    String app;
    EndpointHit hit1;
    EndpointHit hit2;
    EndpointHit hit3;
    EndpointHit hit4;


    @BeforeEach
    public void create() {

        app = "ewm-main-service";

        uriEvents = "events";
        uriEventsId = "events/1";

        ip = "192.163.0.1";

        time2020 = LocalDateTime.of(2020, 1, 1, 1, 1, 1);
        time2021 = time2020.plusYears(1);
        time2022 = time2020.plusYears(2);
        time2023 = time2020.plusYears(3);
        time2024 = time2020.plusYears(4);
        time2025 = time2020.plusYears(5);
        time2050 = time2020.plusYears(30);

        hit1 = EndpointHit.builder()
                .app(app)
                .uri(uriEvents)
                .ip(ip)
                .timestamp(time2021.format(DATE_TIME_FORMATTER))
                .build();
        hit2 = EndpointHit.builder()
                .app(app)
                .uri(uriEventsId)
                .ip(ip)
                .timestamp(time2022.format(DATE_TIME_FORMATTER))
                .build();
        hit3 = EndpointHit.builder()
                .app(app)
                .uri(uriEvents)
                .ip(ip)
                .timestamp(time2023.format(DATE_TIME_FORMATTER))
                .build();
        hit4 = EndpointHit.builder()
                .app(app)
                .uri(uriEvents)
                .ip(ip)
                .timestamp(time2024.format(DATE_TIME_FORMATTER))
                .build();
    }

    /**
     * test save requests info
     */
    @Test
    public void shouldSaveRequestInfo() {

        EndpointHit requestInfo1 = controller.saveRequestData(hit1);
        EndpointHit requestInfo2 = controller.saveRequestData(hit2);
        EndpointHit requestInfo3 = controller.saveRequestData(hit3);
        EndpointHit requestInfo4 = controller.saveRequestData(hit4);

        assertThat(requestInfo1).hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("ip", ip)
                .hasFieldOrPropertyWithValue("timestamp", time2021.format(DATE_TIME_FORMATTER));
        assertThat(requestInfo2).hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEventsId)
                .hasFieldOrPropertyWithValue("ip", ip)
                .hasFieldOrPropertyWithValue("timestamp", time2022.format(DATE_TIME_FORMATTER));
        assertThat(requestInfo3).hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("ip", ip)
                .hasFieldOrPropertyWithValue("timestamp", time2023.format(DATE_TIME_FORMATTER));
        assertThat(requestInfo4).hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("ip", ip)
                .hasFieldOrPropertyWithValue("timestamp", time2024.format(DATE_TIME_FORMATTER));

    }

    /**
     * test get statistics only by start and end time parameters
     */
    @Test
    public void shouldGetStatisticsByTimeParametersOnly() {

        controller.saveRequestData(hit1);
        controller.saveRequestData(hit2);
        controller.saveRequestData(hit3);
        controller.saveRequestData(hit4);

        List<ViewStats> stats = controller.getStatistics(
                time2020, time2025, null, null);

        assertThat(stats).asList().hasSize(4);
        assertThat(stats.get(0))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("hits", 4L);
        assertThat(stats.get(1))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEventsId)
                .hasFieldOrPropertyWithValue("hits", 4L);
        assertThat(stats.get(2))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("hits", 4L);
        assertThat(stats.get(2))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("hits", 4L);
    }

    /**
     * test get empty statistics only by start and end time parameters
     */
    @Test
    public void shouldGetEmptyStatisticsByTimeParametersOnly() {

        controller.saveRequestData(hit1);
        controller.saveRequestData(hit2);
        controller.saveRequestData(hit3);
        controller.saveRequestData(hit4);

        List<ViewStats> stats = controller.getStatistics(
                time2050, time2050, null, null);

        assertThat(stats).asList().isEmpty();
    }

    /**
     * test get statistics by time parameters and uris list, sorted by views in descending order
     */
    @Test
    public void shouldGetStatisticsByTimeParametersAndUrisList() {

        controller.saveRequestData(hit1);
        controller.saveRequestData(hit2);
        controller.saveRequestData(hit3);
        controller.saveRequestData(hit4);

        List<String> uris = List.of(uriEvents, uriEventsId);

        List<ViewStats> stats = controller.getStatistics(
                time2020, time2025, uris, null);
        assertThat(stats).asList().hasSize(2);

        assertThat(stats.get(0))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEvents)
                .hasFieldOrPropertyWithValue("hits", 3L);
        assertThat(stats.get(1))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEventsId)
                .hasFieldOrPropertyWithValue("hits", 1L);

        List<String> uris2 = List.of(uriEventsId);

        List<ViewStats> stats2 = controller.getStatistics(
                time2020, time2025, uris2, null);

        assertThat(stats2).asList().hasSize(1);
        assertThat(stats2.get(0))
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uriEventsId)
                .hasFieldOrPropertyWithValue("hits", 1L);

    }

    /**
     * test get statistics by time parameters, uris list and unique parameter as true
     */
    @Test
    public void shouldGetStatisticsByTimeParametersUrisListAndUniqueTrue() {

        controller.saveRequestData(hit1);
        controller.saveRequestData(hit2);
        controller.saveRequestData(hit3);
        controller.saveRequestData(hit4);

        List<String> uris = Arrays.asList(uriEvents, uriEventsId);

        List<ViewStats> statsByTimeAndUniqueTrue = controller.getStatistics(
                time2020, time2025, uris, true);

        assertThat(statsByTimeAndUniqueTrue).asList().hasSize(1);
    }

    /**
     * test get statistics by time parameters, without uris list and unique parameter as true
     */
    @Test
    public void shouldGetStatisticsByTimeParametersAndUniqueTrue() {

        controller.saveRequestData(hit1);
        controller.saveRequestData(hit2);
        controller.saveRequestData(hit3);
        controller.saveRequestData(hit4);

        List<ViewStats> statsByTimeAndUniqueTrue = controller.getStatistics(
                time2020, time2025, null, true);
        assertThat(statsByTimeAndUniqueTrue).asList().hasSize(1);
    }
}
