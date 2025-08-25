package com.effective.cards.service;

import com.effective.cards.dto.UserCreationForm;
import com.effective.cards.dto.UserStatusUpdate;
import com.effective.cards.enums.UserStatus;
import com.effective.cards.model.Card;
import com.effective.cards.model.User;
import com.effective.cards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void createUser_PhoneInUse() {
        UserCreationForm userCreationForm = new UserCreationForm(
                "a", "b", "c", "89005553535", "smth");
        when(userRepository.existsByPhone(userCreationForm.phone())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(userCreationForm));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Phone number is already in use", exception.getReason());
    }

    @Test
    public void createUser_Success() {
        UserCreationForm userCreationForm = new UserCreationForm(
                "a", "b", "c", "89005553535", "smth");
        when(userRepository.existsByPhone(userCreationForm.phone())).thenReturn(false);
        when(passwordEncoder.encode(userCreationForm.password())).thenReturn("password");

        userService.createUser(userCreationForm);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("password", savedUser.getPassword());
    }

    @Test
    public void changeUserStatus_UserNotFound() {
        UserStatusUpdate userStatusUpdate = new UserStatusUpdate("89005553535", UserStatus.ACTIVE);
        when(userRepository.findByPhone(userStatusUpdate.phone())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.changeUserStatus(userStatusUpdate));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    public void changeUserStatus_Success() {
        User user = new User();
        user.setId(1L);
        user.setStatus(UserStatus.BLOCKED);

        UserStatusUpdate userStatusUpdate = new UserStatusUpdate("89005553535", UserStatus.ACTIVE);
        when(userRepository.findByPhone(userStatusUpdate.phone())).thenReturn(Optional.of(user));

        userService.changeUserStatus(userStatusUpdate);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
    }
}
