package ru.practicum.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * LOCATION entity
 * Long id. Entity ID
 * Float latitude. Latitude of the event LocationDto
 * Float longitude. Longitude of the event LocationDto
 */
@Entity
@Table(name = "locations")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id", nullable = false)
    private Long id;
    @JsonProperty
    @Column(name = "latitude", nullable = false)
    private Float lat;
    @JsonProperty
    @Column(name = "longitude", nullable = false)
    private Float lon;
}
