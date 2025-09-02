package com.zufarov.pastebinV1.pet.dtos;

import com.zufarov.pastebinV1.pet.util.PermissionType;

public record PermissionDto(
        PermissionType type,
        String pasteId,
        String username
) {}
