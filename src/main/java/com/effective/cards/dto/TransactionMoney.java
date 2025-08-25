package com.effective.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Объект, содержащий номера двух карт и сумму перевода")
public record TransactionMoney(
        @Schema(description = "Номер карты, с которой списываются средства, состоящий из 16 цифр." +
                " Каждые 4 цифры подряд допускают разделители в виде ' ' и '-'",
                examples = {
                        "3418451754125712",
                        "2414-1265-2462-1235",
                        "3151 5536 2435 1286"
                })
        @NotBlank(message = "Second card number is required")
        @Pattern(regexp = "^(\\d{4}[-\\s]?){3}\\d{4}$", message = "Invalid card number format")
        String firstCard,

        @Schema(description = "Номер карты, на которую зачисляются средства, состоящий из 16 цифр." +
                " Каждые 4 цифры подряд допускают разделители в виде ' ' и '-'",
                examples = {
                        "2414-1265-2462-1235",
                        "3151 5536 2435 1286",
                        "3418451754125712"
        })
        @NotBlank(message = "First card number is required")
        @Pattern(regexp = "^(\\d{4}[-\\s]?){3}\\d{4}$", message = "Invalid card number format")
        String secondCard,

        @Schema(description = "Сумма перевода. Она должна быть больше нуля", example = "130.05")
        @Positive(message = "Sum must greater than zero")
        BigDecimal sum
) {
}
