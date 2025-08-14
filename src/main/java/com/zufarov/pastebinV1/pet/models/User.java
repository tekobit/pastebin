package com.zufarov.pastebinV1.pet.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Users", schema = "public")
@Data
@NoArgsConstructor
public class User implements Serializable {
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

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
