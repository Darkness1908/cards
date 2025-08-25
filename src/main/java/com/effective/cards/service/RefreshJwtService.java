package com.effective.cards.service;

import com.effective.cards.model.RefreshJwt;
import com.effective.cards.repository.RefreshJwtRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@AllArgsConstructor
public class RefreshJwtService {
    private final RefreshJwtRepository refreshJwtRepository;

    public boolean isTokenExists(String token) {
        return refreshJwtRepository.existsByToken(token);
    }

    public void addToken(String token, Date expired) {
        refreshJwtRepository.save(new RefreshJwt(token, expired));
    }

    public void removeToken(String token) {
        refreshJwtRepository.findByToken(token).ifPresent(refreshJwtRepository::delete);
    }
}
