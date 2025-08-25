package com.effective.cards.model;


import com.effective.cards.dto.UserCreationForm;
import com.effective.cards.enums.Role;
import com.effective.cards.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    public User () {}

    public User (UserCreationForm userCreationForm, String encryptedPassword) {
        name = userCreationForm.name();
        surname = userCreationForm.surname();
        patronymic = userCreationForm.patronymic();
        role = Role.ROLE_USER;
        status = UserStatus.ACTIVE;
        phone = userCreationForm.phone();
        password = encryptedPassword;
    }
}