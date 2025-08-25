package com.effective.cards.dto;

import com.effective.cards.enums.CardStatus;
import com.effective.cards.model.Card;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Объект, который содержит информацию о карте")
public record MyCardInfo(
        @Schema(description = "Маска карты", example = "**** **** **** 1908")
        String maskedCardNumber,


        @Schema(description = "ФИО владельца", example = "Петров Петр Петрович")
        String fullName,

        @Schema(description = "Баланс на карте", example = "23.50")
        BigDecimal balance,

        @Schema(description = "Время истечения карты", example = "2028-12-31T23:59:59")
        LocalDateTime expiredAt,

        @Schema(description = "Время создания карты", example = "2024-12-31T23:59:59")
        LocalDateTime createdAt,

        @Schema(description = "Статус карты", allowableValues = {"BLOCKED", "ACTIVE", "EXPIRED"})
        CardStatus status,

        @Schema(description = "Флаг, сообщающий о том, есть ли запрос на блокировку карты")
        boolean blockRequested
)
{
    public MyCardInfo(Card card) {
        this(
                "**** **** **** " + card.getLast4Digits(),
                card.getOwner().getSurname() + " " +
                        card.getOwner().getName() + " " +
                        card.getOwner().getPatronymic(),
                card.getBalance(),
                card.getExpirationDate(),
                card.getCreatedAt(),
                card.getStatus(),
                card.isBlockRequested()
        );
    }
}
