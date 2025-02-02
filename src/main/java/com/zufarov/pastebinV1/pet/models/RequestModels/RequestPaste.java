package com.zufarov.pastebinV1.pet.models.RequestModels;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor

public class RequestPaste {

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
        RequestPaste that = (RequestPaste) o;
        return Objects.equals(title, that.title) && Objects.equals(id, that.id) && Objects.equals(visibility, that.visibility) && Objects.equals(createdAt, that.createdAt) && Objects.equals(expiresAt, that.expiresAt) && Objects.equals(userId, that.userId) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, id, visibility, createdAt, expiresAt, userId, content);
    }

    @Override
    public String toString() {
        return "GetRequestPaste{" +
                "title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", visibility='" + visibility + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", username='" + userId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
