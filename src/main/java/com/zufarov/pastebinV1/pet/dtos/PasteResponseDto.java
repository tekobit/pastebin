package com.zufarov.pastebinV1.pet.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PasteResponseDto {

    private String title;

    private String id;

    private String visibility;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private int userId;

    private String content;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PasteResponseDto that = (PasteResponseDto) o;
        return userId == that.userId && Objects.equals(title, that.title) && Objects.equals(id, that.id) && Objects.equals(visibility, that.visibility) && Objects.equals(createdAt, that.createdAt) && Objects.equals(expiresAt, that.expiresAt) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, id, visibility, createdAt, expiresAt, userId, content);
    }
}
