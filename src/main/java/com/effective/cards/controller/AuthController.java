package com.effective.cards.controller;

import com.effective.cards.dto.AuthorizationForm;
import com.effective.cards.dto.ErrorResponse;
import com.effective.cards.dto.JwtResponse;
import com.effective.cards.service.AuthService;
import com.effective.cards.service.GeneralJwtService;
import com.effective.cards.service.RefreshJwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Авторизация", description = "Вход, выход и обновление токенов")
@AllArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;
    private final RefreshJwtService refreshJwtService;
    private final GeneralJwtService generalJwtService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно",
                    content = {@Content(schema = @Schema(implementation = JwtResponse.class))}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы данные",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED – неверный пароль",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "NOT FOUND - пользователь не найден",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "API для входа",
            description = "Метод, позволяющий пользователю войти в свой аккаунт")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> logIn(@RequestBody @Valid AuthorizationForm authorizationForm) {
        String[] JWT = authService.logIn(authorizationForm);

        return ResponseEntity.ok().body(new JwtResponse(JWT[0], JWT[1]));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы данные",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "API для выхода из аккаунта",
            description = "Метод, позволяющий пользователю выйти из своего аккаунта")
    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(@Schema(description = "Токен обновления",
            example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIyMiIsInBob25lIG51bWJlciI6Ijg5MDQwOTUzMzUwIiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NTYwNjg4NDMsImV4cCI6MTc1ODY2MDg0M30.o5lILNr9Q1vOlVYrhooGFft0UlR9BPrP997UDWNf76ONiwdqfjJm8rEeVZBpz1gQ")
                                    @RequestBody @NotBlank(message = "Token is required")
                                    String refreshToken) {
        refreshJwtService.removeToken(refreshToken);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK – запрос выполнен успешно",
                    content = {@Content(schema = @Schema(implementation = JwtResponse.class))}),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST - неверно переданы данные",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED – невалидный или истекший токен",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR – внутренняя ошибка сервера",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "API для обновления токенов",
            description = "Метод, позволяющий обновить токены доступа и обновления")
    @PostMapping("/refresh-tokens")
    public ResponseEntity<JwtResponse> refreshTokens(@Schema(description = "Токен обновления",
            example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIyMiIsInBob25lIG51bWJlciI6Ijg5MDQwOTUzMzUwIiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NTYwNjg4NDMsImV4cCI6MTc1ODY2MDg0M30.o5lILNr9Q1vOlVYrhooGFft0UlR9BPrP997UDWNf76ONiwdqfjJm8rEeVZBpz1gQ")
                                           @RequestBody @NotBlank(message = "Token is required")
                                           String refreshToken) {
        String[] tokens = generalJwtService.refreshTokens(refreshToken);
        return ResponseEntity.ok().body(new JwtResponse(tokens[0], tokens[1]));
    }
}
