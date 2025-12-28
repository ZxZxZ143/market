package org.example.session.data.dtos.request.auth;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ChangePassDto {
    private String oldPassword;

    private String newPassword;
}
