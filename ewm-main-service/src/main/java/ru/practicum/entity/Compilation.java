package ru.practicum.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

/**
 * COMPILATION entity
 * id: Integer compilation ID
 * events: List<Event> events. List of event identifiers included in the collection
 * title: String, nullable = false, max length = 50
 * pinned: Boolean flag. Equals true if the collection pinned to the main page of the site
 */
@Entity
@Table(name = "compilations")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id", nullable = false)
    Integer compilationId;
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "compilations_events",
            joinColumns = {@JoinColumn(name = "compilation_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")}
    )
    private List<Event> events;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "pinned")
    private Boolean pinned;
}
