package com.effective.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Объект, содержащий данные для создания пользователя")
public record UserCreationForm(
        @Schema(description = "Имя пользователя", examples = {"Анна-Мария", "Артём"})
        @NotBlank(message = "Name is required")
        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё\\-]+$", message = "Incorrect name format")
        String name,

        @Schema(description = "Фамилия пользователя", examples = { "Шишкина", "Петрова-Игнатова"})
        @NotBlank(message = "Surname is required")
        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё]+$", message = "Incorrect surname format")
        String surname,

        @Schema(description = "Отчество пользователя", example = "Иванов")
        @NotBlank(message = "Patronymic is required")
        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё\\-]+$", message = "Incorrect patronymic format")
        String patronymic,

        @Schema(description = "Номер телефона, содержащий от 10 до 15 цифр ('+' опционален в начале)",
                examples = {
                        "+79092341239",
                        "89003120412"
                })
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        String phone,

        @SuppressWarnings("SpellCheckingInspection")
        @Schema(description = "Пароль", example = "qwertyui")
        @NotBlank(message = "Password is required")
        String password
) {
}
