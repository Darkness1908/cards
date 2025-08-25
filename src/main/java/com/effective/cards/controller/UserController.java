package com.effective.cards.controller;

import com.effective.cards.dto.ErrorResponse;
import com.effective.cards.dto.UserCreationForm;
import com.effective.cards.dto.UserStatusUpdate;
import com.effective.cards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Пользователи", description = "Операции с пользователями")
@RequestMapping("/users")
@AllArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы данные",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "CONFLICT – номер телефона уже используется",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод создания пользователя",
            description = "Метод, доступный администратору, для создания пользователя в базе")
    @PostMapping("")
    public ResponseEntity<Void> createUser(@RequestBody @Valid UserCreationForm userCreationForm) {
        userService.createUser(userCreationForm);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы данные",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN – недостаточно прав",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND – пользователь не найден",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Parameter(name = "Authorization", description = "Токен доступа, необходимый для авторизации пользователя",
            required = true, in = ParameterIn.HEADER, example = "Authorization: Bearer <токен>")
    @Operation(summary = "Метод изменения статуса пользователю",
            description = "Метод, доступный администратору, для изменения пользователю статуса")
    @PutMapping("")
    public ResponseEntity<Void> changeUserStatus(@RequestBody @Valid UserStatusUpdate userStatusUpdate) {
        userService.changeUserStatus(userStatusUpdate);
        return ResponseEntity.ok().build();
    }
}
