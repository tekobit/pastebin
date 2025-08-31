package com.zufarov.pastebinV1.pet.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "Permissions",schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class Permission implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "type")
    private String type;

    //connect to pastes table
    @ManyToOne
    @JoinColumn(name = "paste_id",referencedColumnName = "id")
    private Paste paste;

    //connect to users table
    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User user;

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt.truncatedTo(ChronoUnit.MICROS);
    }


    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", paste=" + paste +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return id == that.id && Objects.equals(createdAt, that.createdAt) && Objects.equals(paste.getId(), that.paste.getId()) && Objects.equals(user.getId(), that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, paste, user);
    }

    public Permission(User user, Paste paste) {
        this.user = user;
        this.paste = paste;
    }

}
