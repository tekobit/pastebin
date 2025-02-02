package com.zufarov.pastebinV1.pet.models.RequestModels;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateRequestPaste {

    private String title;

    private String visibility;

    private LocalDateTime expiresAt;

    private String content;

    private String customId;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CreateRequestPaste that = (CreateRequestPaste) o;
        return Objects.equals(title, that.title) && Objects.equals(visibility, that.visibility) && Objects.equals(expiresAt, that.expiresAt) && Objects.equals(content, that.content) && Objects.equals(customId, that.customId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, visibility, expiresAt, content, customId);
    }

    @Override
    public String toString() {
        return "PostRequestPaste{" +
                "title='" + title + '\'' +
                ", visibility='" + visibility + '\'' +
                ", expiresAt=" + expiresAt +
                ", content='" + content + '\'' +
                ", customId='" + customId + '\'' +
                '}';
    }
}
