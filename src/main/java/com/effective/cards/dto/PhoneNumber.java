package com.effective.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Объект с номером телефона")
public record PhoneNumber(
        @Schema(description = "Номер телефона, содержащий от 10 до 15 цифр ('+' опционален в начале)",
        examples = {
                "+79092341239",
                "89003120412"
        })
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        String phoneNumber
) {

}
