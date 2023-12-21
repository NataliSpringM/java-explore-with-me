package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.utils.BaseClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.utils.PathConstructor;

import java.util.List;
import java.util.Map;


import static ru.practicum.utils.Constants.*;

/**
 * creation HTTP-requests to stats-server
 */
@Service
@Slf4j
public class StatsClient extends BaseClient {
    @Autowired
    public StatsClient(@Value("${ewm-stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .build()
        );
    }

    /**
     * create POST-request to the "/hit" endpoint
     */
    public ResponseEntity<Object> saveRequestData(EndpointHit hit) {
        ResponseEntity<Object> response = post(HIT_PATH, hit);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response.getBody());
        }
        return response;
    }

    /**
     * create GET-request to "/stats" endpoint
     */
    public ResponseEntity<Object> getStatistics(String start, String end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                START_PARAMETER_NAME, start,
                END_PARAMETER_NAME, end,
                UNIQUE_PARAMETER_NAME, unique
        );
        log.info("We send GET-Request to StatsController with {}, {}, {}, {}:", start, end, uris, unique);
        String parameterPATH = PathConstructor.getParameterPath(start, end, uris, unique);
        return get(STATS_PATH + parameterPATH, parameters);
    }
}




