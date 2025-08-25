package com.effective.cards.service;

import com.effective.cards.dto.UserCreationForm;
import com.effective.cards.dto.UserStatusUpdate;
import com.effective.cards.model.User;
import com.effective.cards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@AllArgsConstructor
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void createUser(UserCreationForm userCreationForm) {
        if (userRepository.existsByPhone(userCreationForm.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Phone number is already in use");
        }

        String encryptedPassword = passwordEncoder.encode(userCreationForm.password());
        User user = new User(userCreationForm, encryptedPassword);
        userRepository.save(user);
    }

    public void changeUserStatus(UserStatusUpdate userStatusUpdate) {
        User user = userRepository.findByPhone(userStatusUpdate.phone()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        user.setStatus(userStatusUpdate.status());
        userRepository.save(user);
    }
}
