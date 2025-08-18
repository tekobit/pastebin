package com.zufarov.pastebinV1.pet.dtos;

public record PermissionDto(
        String type,
        String pasteId,
        String username
) {}
