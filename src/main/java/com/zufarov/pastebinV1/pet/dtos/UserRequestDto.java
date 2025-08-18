package com.zufarov.pastebinV1.pet.dtos;

public record UserRequestDto(
        String name,
        String email,
        String password
) {}
