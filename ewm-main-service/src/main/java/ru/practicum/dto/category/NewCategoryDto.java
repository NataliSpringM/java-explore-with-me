package ru.practicum.dto.category;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * NEW CATEGORY DTO
 * String category name, must not be blank, min length = 1, max length = 50
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {
    @Size(min = 1, max = 50)
    @NotBlank
    private String name;
}
