package org.example.session.data.dtos.request.auth;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@RequiredArgsConstructor
public class RegisterDto {
    private String email;

    private String password;

    private String fullName;
}
