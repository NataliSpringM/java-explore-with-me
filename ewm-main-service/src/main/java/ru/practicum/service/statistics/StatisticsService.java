package ru.practicum.service.statistics;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public interface StatisticsService {

    /**
     * save request for statistics
     *
     * @param request request data
     */
    void saveStats(HttpServletRequest request);

    /**
     * get statistics
     *
     * @param start start of required time period
     * @param end   end of required time period
     * @param uris  list of uris to get data
     * @return map with of unique views for event
     */
    Map<Long, Long> getStats(LocalDateTime start, LocalDateTime end, List<String> uris);

}
