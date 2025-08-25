package com.effective.cards.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "jwt_refresh")
public class RefreshJwt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;

    @Column(name = "token")
    String token;

    @Column(name = "expires_at")
    Date expiresAt;

    public RefreshJwt() {}

    public RefreshJwt(String token, Date expiresAt)
    {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}