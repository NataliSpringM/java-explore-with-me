package ru.practicum.service;

import org.springframework.stereotype.Component;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * STATISTICS SERVICE
 * save and receive viewing statistics data
 */
@Component
public interface StatsService {
    /**
     * Save info about users request to the uri of a specific service.
     *
     * @param requestData contains service name, uri and user ip address
     * @return user request data with assigned id and information about saving time
     */
    EndpointHit saveRequestData(EndpointHit requestData);

    /**
     * Get info about users requests to the uri of a specific service.
     *
     * @param start   : date and time of the start of the range for which statistics need to be downloaded
     *                (format "yyyy-MM-dd HH:mm:ss")
     * @param end:    date and time of the start of the range for which statistics need to be downloaded
     *                (format "yyyy-MM-dd HH:mm:ss")
     * @param uris:   list of uri for which statistics need to be downloaded
     * @param unique: should only unique visits be taken into account (only with a unique ip), default value: false
     * @return list of users requests, met specified criteria
     */
    List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
