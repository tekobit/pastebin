package com.zufarov.pastebinV1.pet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Pastes", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Paste implements Serializable {
    @Id
    @NonNull
    @Column(name = "id")
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "content_location")
    private String contentLocation;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime expiresAt;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "last_visited", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastVisited;


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt.truncatedTo(ChronoUnit.MICROS);
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt.truncatedTo(ChronoUnit.MICROS);
    }

    public void setLastVisited(LocalDateTime lastVisited) {
        this.lastVisited = lastVisited.truncatedTo(ChronoUnit.MICROS);
    }

    public Paste(String id, String title, String contentLocation, LocalDateTime createdAt, LocalDateTime expiresAt, String visibility, LocalDateTime lastVisited, User owner) {
        this.id = id;
        this.title = title;
        this.contentLocation = contentLocation;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.visibility = visibility;
        this.lastVisited = lastVisited;
        this.owner = owner;
    }

    // connect to users table
    @OneToMany(mappedBy = "paste")
    private List<Permission> permissions;

    // connect to permissions table
    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User owner;

    @Override
    public String toString() {
        return "Paste{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", contentLocation='" + contentLocation + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", visibility='" + visibility  +
                ", owner=," + owner.getId() + '\''+
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Paste paste = (Paste) o;
        return Objects.equals(id, paste.id) && Objects.equals(title, paste.title) && Objects.equals(contentLocation, paste.contentLocation) && Objects.equals(createdAt, paste.createdAt) && Objects.equals(expiresAt, paste.expiresAt) && Objects.equals(visibility, paste.visibility) && Objects.equals(owner, paste.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, contentLocation, createdAt, expiresAt, visibility,  owner);
    }

}
