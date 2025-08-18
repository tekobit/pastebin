package com.zufarov.pastebinV1.pet.dtos;

import java.time.LocalDateTime;

public record PasteResponseDto(
        String title,
        String id,
        String visibility,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        int userId,
        String content
) {}
