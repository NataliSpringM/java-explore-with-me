package ru.practicum.dto.user;

import lombok.*;

/**
 * USER short DTO details
 * Long id. User ID
 * String name. User name
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {
    Long id;
    String name;
}