package com.zufarov.pastebinV1.pet.models.RequestModels;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class RequestPermission {
    private String type;

    private String pasteId;

    private String username;

    @Override
    public String toString() {
        return "RequestPermission{" +
                "type='" + type + '\'' +
                ", pasteId='" + pasteId + '\'' +
                ", username=" + username +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RequestPermission that = (RequestPermission) o;
        return Objects.equals(username, that.username) && Objects.equals(type, that.type) && Objects.equals(pasteId, that.pasteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, pasteId, username);
    }
}
