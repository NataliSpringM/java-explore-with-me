package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.Request;
import ru.practicum.enums.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * get requests by event id
     *
     * @param eventId event id
     * @return list of requests
     */
    List<Request> findAllByEvent_Id(Long eventId);


    /**
     * get requests by requester id
     *
     * @param userId requester ID
     * @return list of requests
     */
    List<Request> findAllByRequester_Id(Long userId);

    /**
     * get requests by requester id and event id
     *
     * @param userId  requester ID
     * @param eventId event ID
     * @return list of requests
     */
    List<Request> findAllByRequester_IdAndEvent_Id(Long userId, Long eventId);

    /**
     * get requests by status and event id
     *
     * @param status  requestStatus
     * @param eventId event ID
     * @return list of requests
     */
    List<Request> findALlByStatusAndEventId(RequestStatus status, Long eventId);
}
