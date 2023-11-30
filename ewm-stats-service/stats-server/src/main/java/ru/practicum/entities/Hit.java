package ru.practicum.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * User request information model
 * Long id : identifier of the request
 * String app : identifier(name) of the service for which information is recorded
 * String uri : URI for which the request was made
 * String ip : IP address of the user who made the request
 * LocalDateTime timestamp : date and time when the request to the endpoint was made (format "yyyy-MM-dd HH:mm:ss")
 */
@Entity
@Table(name = "hits")
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "app", length = 64, nullable = false)
    private String app;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "ip", length = 32, nullable = false)
    private String ip;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
