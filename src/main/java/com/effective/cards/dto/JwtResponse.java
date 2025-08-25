package com.effective.cards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Объект, содержащий токены для авторизации")
public record JwtResponse(
        @Schema(description = "Токен доступа", example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIyMiIsInBob25lIG51bWJlciI6Ijg5MDQwOTUzMzUwIiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NTYwNjg4NDMsImV4cCI6MTc1NjA2OTc0M30.hNOBssEIjNRd5F-ZYuKybSNkHwFWQ8RZriTazjZsfgrkM7tn0BVBv96BsOuyGIoW")
        String accessToken,

        @Schema(description = "Токен обновления", example = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIyMiIsInBob25lIG51bWJlciI6Ijg5MDQwOTUzMzUwIiwicm9sZSI6IlJPTEVfQURNSU4iLCJpYXQiOjE3NTYwNjg4NDMsImV4cCI6MTc1ODY2MDg0M30.o5lILNr9Q1vOlVYrhooGFft0UlR9BPrP997UDWNf76ONiwdqfjJm8rEeVZBpz1gQ")
        String refreshToken
) {
}
