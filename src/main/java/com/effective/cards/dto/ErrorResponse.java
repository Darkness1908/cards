package com.effective.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Структура ошибки API")
public record ErrorResponse(
        @Schema(description = "Статус ошибки", example = "404")
        int status,

        @Schema(description = "Код ошибки", example = "NOT FOUND")
        String code,

        @Schema(description = "Сообщение ошибки", example = "Card not found")
        String message
) {

}
