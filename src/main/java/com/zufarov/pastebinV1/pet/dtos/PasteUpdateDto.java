package com.zufarov.pastebinV1.pet.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PasteUpdateDto {
    private String title;

    private String visibility;

    private LocalDateTime expiresAt;

    private String content;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PasteUpdateDto that = (PasteUpdateDto) o;
        return Objects.equals(title, that.title) && Objects.equals(visibility, that.visibility) && Objects.equals(expiresAt, that.expiresAt) && Objects.equals(content, that.content) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, visibility, expiresAt, content);
    }
}
