package org.example.session.data.dtos.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserResDto {
    private Integer id;
    private String email;
    private String fullName;
    private String status;
    private String role;
}
