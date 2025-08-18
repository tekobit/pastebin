package com.zufarov.pastebinV1.pet.mappers;

import com.zufarov.pastebinV1.pet.dtos.UserRequestDto;
import com.zufarov.pastebinV1.pet.models.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "role", constant = "ROLE_USER")
    @Mapping(target = "createdAt",expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastLogin",expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "password",expression = "java(passwordEncoder.encode(userRequestDto.password()))")
    User toUser(UserRequestDto userRequestDto,@Context PasswordEncoder passwordEncoder);

}
