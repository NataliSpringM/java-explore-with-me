package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.entities.Hit;
import ru.practicum.utils.mapper.HitMapper;
import ru.practicum.repository.StatsRepository;
import ru.practicum.utils.logger.ListLogger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMATTER;

/**
 * STATISTICS SERVICE IMPLEMENTATION
 * save and receive viewing statistics data
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;

    /**
     * Save info about users request to the uri of a specific service.
     *
     * @param requestData contains service name, uri and user ip address
     * @return user request data with assigned id and information about saving time
     */
    @Override
    public EndpointHit saveRequestData(EndpointHit requestData) {
        Hit hit = HitMapper.toHitEntity(requestData);
        EndpointHit savedHit = HitMapper.toEndpointHit(repository.save(hit));
        log.info("Hit {} has been saved.", savedHit);
        return savedHit;
    }

    /**
     * Get info about users requests to the uri of a specific service.
     *
     * @param startString : date and time of the start of the range for which statistics need to be downloaded
     * @param endString:  date and time of the start of the range for which statistics need to be downloaded
     * @param uris:       list of uri for which statistics need to be downloaded
     * @param unique:     should only unique visits be taken into account (only with a unique ip), default value: false
     * @return list of users requests, met specified criteria, containing number of views
     */
    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStatistics(String startString, String endString, List<String> uris, Boolean unique) {

        List<Hit> hits = findHits(startString, endString, uris);

        List<ViewStats> stats = new ArrayList<>();

        if (!hits.isEmpty()) {
            if (Boolean.TRUE.equals(unique)) {
                hits = retainUniqueIpHits(hits);
            }
            stats = mapAndSortList(hits, uris);
        }
        ListLogger.logResultList(stats);
        return stats;
    }

    /**
     * find user requests in repository
     *
     * @param startString : date and time of the start of the range for which statistics need to be downloaded
     * @param endString:  date and time of the start of the range for which statistics need to be downloaded
     * @param uris:       list of uri for which statistics need to be downloaded
     * @return list of users requests, met specified criteria
     */
    private List<Hit> findHits(String startString, String endString, List<String> uris) {

        LocalDateTime start = LocalDateTime.parse(startString, DATE_TIME_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endString, DATE_TIME_FORMATTER);

        if (uris == null) {
            return repository.findAllByTimestampBetween(start, end);
        } else {
            return repository.findAllByUriInAndTimestampBetween(uris, start, end);
        }
    }


    /**
     * retain only unique ip visits
     *
     * @param hits list of Hit objects
     * @return list of Hits objects with unique ip
     */
    private List<Hit> retainUniqueIpHits(List<Hit> hits) {
        return new ArrayList<>(hits.stream()
                .collect(Collectors.toMap(Hit::getIp, Function.identity(), (hit1, hit2) -> hit1))
                .values());
    }

    /**
     * map list of hits into viewStats list and sort it in descending views order
     *
     * @param hits list of Hit objects
     * @return list of ViewStats objects
     */
    private List<ViewStats> mapAndSortList(List<Hit> hits, List<String> uris) {

        Comparator<ViewStats> viewDesc = Comparator.comparing(ViewStats::getHits).reversed();

        if (uris != null) {
            Map<String, Integer> uriViews = countUrisViews(hits);
            return hits.stream()
                    .map(hit -> new ViewStats(hit.getApp(), hit.getUri(), uriViews.get(hit.getUri())))
                    .distinct()
                    .sorted(viewDesc)
                    .collect(Collectors.toList());
        }

        Integer views = hits.size();
        return hits.stream()
                .map(hit -> new ViewStats(hit.getApp(), hit.getUri(), views))
                .sorted(viewDesc)
                .collect(Collectors.toList());
    }

    /**
     * collect hits by Uri and count them
     *
     * @param hits list of Hit objects
     * @return map with number of the views by uri
     */
    private Map<String, Integer> countUrisViews(List<Hit> hits) {
        return hits.stream()
                .collect(Collectors.groupingBy((Hit::getUri), Collectors.summingInt(h -> 1)));
    }
}
