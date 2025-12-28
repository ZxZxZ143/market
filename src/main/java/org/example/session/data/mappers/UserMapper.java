package org.example.session.data.mappers;

import org.example.session.data.dtos.request.auth.LoginDto;
import org.example.session.data.dtos.request.auth.RegisterDto;
import org.example.session.data.dtos.response.UserResDto;
import org.example.session.db.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", source = "role.name")
    UserResDto toResponse(User user);

    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "passwordHash",  ignore = true)
    @Mapping(target = "status",  ignore = true)
    @Mapping(target = "role",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    User toEntity(RegisterDto registerDto);

    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "fullName",  ignore = true)
    @Mapping(target = "passwordHash",  ignore = true)
    @Mapping(target = "status",  ignore = true)
    @Mapping(target = "role",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    User toEntity(LoginDto loginDto);
}
