package ru.practicum.dto.category;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * CATEGORY DTO
 * Long category ID
 * String category name
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    @Size(min = 1, max = 50)
    @NotBlank
    private String name;
}

