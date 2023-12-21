package ru.practicum.service.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.utils.constants.Constants.SLASH_PATH;
import static ru.practicum.utils.formatter.DateTimeFormatter.DATE_TIME_FORMATTER;

/**
 * STATISTICS SERVICE IMPLEMENTATION
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticServiceImpl implements StatisticsService {
    private final StatsClient statsClient;
    @Value(value = "${app.name}")
    private String appName;

    /**
     * save request for statistics
     *
     * @param request request data
     */
    @Override
    public void saveStats(HttpServletRequest request) {
        statsClient.saveRequestData(EndpointHit.builder()
                .app(appName)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .build());

    }

    /**
     * get statistics
     *
     * @param start start of required time period
     * @param end   end of required time period
     * @param uris  list of uris to get data
     * @return map with number of unique views for event
     */
    @Override
    public Map<Long, Long> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {

        ObjectMapper mapper = new ObjectMapper();
        List<ViewStats> stats;
        List<?> list;

        ResponseEntity<Object> response = statsClient.getStatistics(
                start.format(DATE_TIME_FORMATTER),
                end.format(DATE_TIME_FORMATTER),
                uris, true);

        if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
            throw new HttpServerErrorException(response.getStatusCode(),
                    String.format("An unexpected error occurred "
                            + "while trying to get browsing statistics by URI %s", uris));
        }

        if (response.getBody() instanceof List<?>) {
            list = (List<?>) response.getBody();
        } else {
            throw new ClassCastException("An unexpected result from statistic server could not resolved.");
        }
        if (list.isEmpty()) {
            return uris.stream().map(this::getEventIdFromUri)
                    .collect(Collectors.toMap(Function.identity(), s -> 0L));
        } else {
            log.info("WE HAVE STATISTIC RESULT:");
            stats = list.stream().map(e -> mapper.convertValue(e, ViewStats.class)).collect(Collectors.toList());
            return stats.stream()
                    .collect(Collectors.toMap(ViewStats -> getEventIdFromUri(ViewStats.getUri()),
                            ViewStats::getHits));
        }
    }

    /**
     * parse URI and get eventId
     *
     * @param uri endpoint name
     * @return eventId
     */
    private Long getEventIdFromUri(String uri) {
        String[] parts = uri.split(SLASH_PATH);
        return Long.parseLong(parts[parts.length - 1]);
    }
}
