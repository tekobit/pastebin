package com.zufarov.pastebinV1.pet.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Users", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLogin;

    @Column(name = "role")
    private String role;

    // connect to pastes table
    @OneToMany(mappedBy = "owner")
    private List<Paste> owningPastes;

    // connect to permissions table
    @OneToMany(mappedBy = "user")
    private List<Permission> permissions;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                ", role='" + role + '\'' +
                ", owningPastes=" + owningPastes +
                ", permissions=" + permissions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(password, user.password) && Objects.equals(createdAt, user.createdAt) && Objects.equals(lastLogin, user.lastLogin) && Objects.equals(role, user.role) && Objects.equals(owningPastes, user.owningPastes) && Objects.equals(permissions, user.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, createdAt, lastLogin, role, owningPastes, permissions);
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
