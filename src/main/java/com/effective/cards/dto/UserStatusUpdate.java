package com.effective.cards.dto;

import com.effective.cards.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Объект, содержащий данные для обновления статуса пользователя")
public record UserStatusUpdate(
        @Schema(description = "Номер телефона, содержащий от 10 до 15 цифр ('+' опционален в начале)",
                examples = {
                        "+79092341239",
                        "89003120412"
                })
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        String phone,

        @Schema(description = "Статус, устанавливаемый пользователю", allowableValues = {"BLOCKED", "ACTIVE"})
        @NotNull(message = "Status is required")
        UserStatus status
) {
}
