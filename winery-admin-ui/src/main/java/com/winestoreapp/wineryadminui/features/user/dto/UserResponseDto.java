package com.winestoreapp.wineryadminui.features.user.dto;

import lombok.Getter;

import java.util.Set;

@Getter
public class UserResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Set<String> roles;
    private Long telegramChatId;
}
