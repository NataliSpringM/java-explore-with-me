package ru.practicum.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * EVENT entity
 * Long id. Event ID
 * String annotation. Event annotation. Nullable = false, min length = 20, max length = 7000
 * Category category. Event category. Nullable = false.
 * Integer confirmedRequests, default value = 0. Number of approved requests for participation in this event
 * LocalDateTime createdOn. Nullable = false. Date and time the event was created ("yyyy-MM-dd HH:mm:ss")
 * LocalDateTime eventDate. Nullable = false. Date and time for which the event is scheduled ("yyyy-MM-dd HH:mm:ss")
 * String description. Full event description. Nullable = false, min length = 20, max length = 7000
 * User initiator. Event initiator. Nullable = false.
 * Location LocationDto. Latitude and longitude of the event LocationDto. Nullable = false.
 * Boolean paid. Nullable = false. Equals true if participation is paid
 * Integer participantLimit. Limitation on the number of participants. Value 0 means no restriction
 * LocalDateTime publishedOn. Date and time the event was published ("yyyy-MM-dd HH:mm:ss")
 * Boolean requestModeration. Equals true if pre-moderation of applications for participation is required
 * String actionState. Enumeration of event lifecycle states. [ PENDING, PUBLISHED, CANCELED ]
 * String title. Event title, nullable = false, min length = 3, max length = 120
 * Long views. Number of event views
 */
@Entity
@Table(name = "events")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    private Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    @Column(name = "confirmed_requests")
    @ColumnDefault("0")
    private Integer confirmedRequests;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Size(min = 20, max = 7000)
    @Column(name = "description", length = 7000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", referencedColumnName = "user_id")
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;

    @Column(name = "paid")
    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    @ColumnDefault("true")
    private Boolean requestModeration;

    @Column(name = "state", length = 30)
    private String state;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "views")
    private Long views;

}
