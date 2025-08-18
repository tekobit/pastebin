package com.zufarov.pastebinV1.pet.dtos;

import java.time.LocalDateTime;

public record PasteRequestDto(
        String title,
        String visibility,
        LocalDateTime expiresAt,
        String content,
        String customId) {
}