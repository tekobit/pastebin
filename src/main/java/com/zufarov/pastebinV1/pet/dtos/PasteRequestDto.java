
package com.zufarov.pastebinV1.pet.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PasteRequestDto {

    private String title;

    private String visibility;

    private LocalDateTime expiresAt;

    private String content;

    private String customId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PasteRequestDto pasteRequestDto = (PasteRequestDto) o;
        return Objects.equals(title, pasteRequestDto.title) && Objects.equals(visibility, pasteRequestDto.visibility) && Objects.equals(expiresAt, pasteRequestDto.expiresAt) && Objects.equals(content, pasteRequestDto.content) && Objects.equals(customId, pasteRequestDto.customId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, visibility, expiresAt, content, customId);
    }

}