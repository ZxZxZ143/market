package org.example.session.data.dtos.request.auth;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdateProfileDto {
    private String fullName;
}
