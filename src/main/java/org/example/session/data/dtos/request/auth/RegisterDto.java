package org.example.session.data.dtos.request.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class RegisterDto {
    private String email;

    private String password;

    private String fullName;
}
