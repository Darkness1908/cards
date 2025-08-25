package com.effective.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Объект, содержащий номер карты")
public record CardNumber(
        @Schema(description = "Номер карты из 16 цифр. Каждые 4 цифры подряд допускают разделители в виде ' ' и '-'",
        examples = {
                "3418451754125712",
                "2414-1265-2462-1235",
                "3151 5536 2435 1286"
        })
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^(\\d{4}[-\\s]?){3}\\d{4}$", message = "Invalid card number format")
        String cardNumber
) {
}
