package com.effective.cards.service;

import com.effective.cards.dto.AuthorizationForm;
import com.effective.cards.enums.UserStatus;
import com.effective.cards.model.User;
import com.effective.cards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final GeneralJwtService generalJwtService;

    public String[] logIn(AuthorizationForm authorizationForm) {
        User user = userRepository.findByPhone(authorizationForm.phone()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is blocked");
        }

        if (!passwordEncoder.matches(authorizationForm.password(), user.getPassword())) {
            System.out.println("Incorrect password");
            throw new BadCredentialsException("Incorrect password");
        }

        userRepository.save(user);
        return generalJwtService.generateTokens(user.getId(), user.getPhone(), user.getRole().name());
    }
}
