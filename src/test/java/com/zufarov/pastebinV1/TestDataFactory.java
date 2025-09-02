package com.zufarov.pastebinV1;

import com.zufarov.pastebinV1.pet.dtos.PasteRequestDto;
import com.zufarov.pastebinV1.pet.dtos.PasteResponseDto;
import com.zufarov.pastebinV1.pet.dtos.PasteUpdateDto;
import com.zufarov.pastebinV1.pet.dtos.PermissionDto;
import com.zufarov.pastebinV1.pet.models.Paste;
import com.zufarov.pastebinV1.pet.models.User;
import com.zufarov.pastebinV1.pet.util.PermissionType;

import java.time.LocalDateTime;

public class TestDataFactory {
    public static PasteResponseDto createDefaultPasteResponseDto() {
        return new PasteResponseDto(
                "default_title",
                "default_id",
                "public",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1,
                "default_content"
        );
    }
    public static PasteRequestDto createDefaultPasteRequestDto() {
        return new PasteRequestDto(
                "default_title",
                "public",
                LocalDateTime.now(),
                "default_content",
                "customId"
        );
    }

    public static PasteUpdateDto createDefaultPasteUpdateDto() {
        return new PasteUpdateDto(
                "default_title",
                "public",
                LocalDateTime.now(),
                "default_content"
        );
    }

    public static PermissionDto createPermissionDto(User user, Paste paste, PermissionType type) {
        return new PermissionDto(
                type,
                paste.getId(),
                user.getName()
        );
    }
}
