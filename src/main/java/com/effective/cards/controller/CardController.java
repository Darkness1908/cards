package com.effective.cards.controller;

import com.effective.cards.config.CustomUserDetails;
import com.effective.cards.dto.*;
import com.effective.cards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Tag(name = "Карты", description = "Операции с картами ")
@RequestMapping("/cards")
@AllArgsConstructor
@RestController
public class CardController {

    private final CardService cardService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно передан номер телефона",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "NOT FOUND – пользователь не найден",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод создания карты",
            description = "Метод, доступный администратору, для создания карты определенному пользователю")
    @PostMapping("")
    public ResponseEntity<Void> createCard(@RequestBody @Valid PhoneNumber phoneNumber) {
        cardService.createCard(phoneNumber.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно передан номер карты",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод удаления карты",
            description = "Метод, доступный администратору, для удаления карты определенного пользователя")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteCard(@RequestBody @Valid CardNumber cardNumber) {
        cardService.deleteCard(cardNumber.cardNumber());
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы данные",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "NOT FOUND – карта не найден",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "CONFLICT - срок действия карты истек",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод изменения статуса карты",
            description = "Метод, доступный администратору, для изменения статуса карты")
    @PutMapping("")
    public ResponseEntity<Void> changeCardStatus(@RequestBody @Valid CardStatusUpdate cardStatusUpdate) {
        cardService.changeCardStatus(cardStatusUpdate);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно",
                    content = {@Content(schema = @Schema(implementation = CardInfo.class))}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы параметры для пагинации",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод, возвращающий информацию о картах",
            description = "Метод, доступный администратору, который возвращает информацию о всех картах пользователей")
    @GetMapping("")
    public ResponseEntity<List<CardInfo>> getCardsInfo(@RequestParam @Min(0)
                                                       @Parameter(
                                                               name = "Страница",
                                                               description = "Номер страницы (>=0)",
                                                               required = true,
                                                               example = "0",
                                                               schema = @Schema(type = "integer", minimum = "0")
                                                       ) int page,
                                                       @RequestParam @Min(1)
                                                       @Parameter(
                                                               name = "Размер страницы ",
                                                               description = "Количество записей на странице (>=1)",
                                                               required = true,
                                                               example = "1",
                                                               schema = @Schema(type = "integer", minimum = "1")
                                                       )int pageSize) {
        List<CardInfo> card = cardService.getCardsInfo(page, pageSize);
        return ResponseEntity.ok(card);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно",
                    content = {@Content(schema = @Schema(implementation = CardInfo.class))}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы параметры для пагинации",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод, возвращающий информацию о картах, которые ожидают блокировку",
            description = "Метод, доступный администратору, который возвращает информацию о картах, на которые" +
                    "пользователи отправили запрос на блокировку")
    @GetMapping("/card-block-requests")
    public ResponseEntity<List<CardInfo>> getCardBlockRequests(@RequestParam @Min(0)
                                                               @Parameter(
                                                                       name = "Страница",
                                                                       description = "Номер страницы (>=0)",
                                                                       required = true,
                                                                       example = "0",
                                                                       schema = @Schema(type = "integer", minimum = "0")
                                                               ) int page,
                                                               @RequestParam @Min(1)
                                                               @Parameter(
                                                                       name = "Размер страницы ",
                                                                       description = "Количество записей на странице (>=1)",
                                                                       required = true,
                                                                       example = "1",
                                                                       schema = @Schema(type = "integer", minimum = "1")
                                                               ) int pageSize) {
        List<CardInfo> card = cardService.getCardBlockRequests(page, pageSize);
        return ResponseEntity.ok(card);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно передан номер карты",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "NOT FOUND – карта не найден",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "CONFLICT - карта неактивна",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод для запроса блокировки карты",
            description = "Метод, который позволяет пользователю запросить блокировку своей карты")
    @PutMapping("/me/status")
    public ResponseEntity<Void> sendBlockRequest(@RequestBody @Valid CardNumber cardNumber,
                                              Principal principal) {
        Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        cardService.sendBlockRequest(cardNumber.cardNumber(), userId);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно",
                    content = {@Content(schema = @Schema(implementation = BigDecimal.class, example = "123.45"))}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно передан номер карты",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод проверки баланса",
            description = "Метод, который возвращает пользователю баланс его карты")
    @GetMapping("/me/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestBody @Valid CardNumber cardNumber,
                                        Principal principal) {
        Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        BigDecimal balance = cardService.getBalance(cardNumber.cardNumber(), userId);
        return ResponseEntity.ok(balance);
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно передан номер карты",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "NOT FOUND – одна (или обе) из карт не найдена",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "CONFLICT - недостаточно средств для списания",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод для перевода денег между своими картами",
            description = "Метод, который позволяет пользователю сделать перевод с одной своей карты на другую")
    @PostMapping("/me/balance")
    public ResponseEntity<Void> createTransaction(@RequestBody @Valid TransactionMoney transactionMoney,
                                               Principal principal) {
        Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        cardService.createTransaction(transactionMoney, userId);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно",
                    content = {@Content(schema = @Schema(implementation = MyCardInfo.class))}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы параметры для пагинации",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод, возвращающий информацию о картах авторизованного пользователя",
            description = "Метод, который возвращает пользователю информацию о его картах")
    @GetMapping("/me")
    public ResponseEntity<List<MyCardInfo>> getMyCardsInfo(@RequestParam @Min(0)
                                                           @Parameter(
                                                                   name = "Страница",
                                                                   description = "Номер страницы (>=0)",
                                                                   required = true,
                                                                   example = "0",
                                                                   schema = @Schema(type = "integer", minimum = "0")
                                                           ) int page,
                                                           @RequestParam @Min(1)
                                                           @Parameter(
                                                                   name = "Размер страницы ",
                                                                   description = "Количество записей на странице (>=1)",
                                                                   required = true,
                                                                   example = "1",
                                                                   schema = @Schema(type = "integer", minimum = "1")
                                                           ) int pageSize,
                                                           Principal principal) {
        Long userId = ((CustomUserDetails) ((Authentication) principal).getPrincipal()).getId();
        List<MyCardInfo> card = cardService.getMyCardsInfo(page, pageSize, userId);
        return ResponseEntity.ok(card);
    }



}
