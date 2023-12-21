package ru.practicum.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.enums.RequestStatus;


import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * REQUEST entity
 * Request for participation in the event
 * Long requestId, request ID
 * LocalDateTime created, date and time the application was created
 * Event event.
 * User requester. User who submitted the request
 * status : RequestStatus enumeration [ PENDING, CONFIRMED, REJECTED, CANCELED ]
 */
@Entity
@Table(name = "participation_requests")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
