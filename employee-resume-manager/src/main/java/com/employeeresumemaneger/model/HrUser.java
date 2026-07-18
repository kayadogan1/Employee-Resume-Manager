package com.employeeresumemaneger.model;

import com.employeeresumemaneger.util.HrRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "hr_users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HrUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private HrRole role;

    private boolean enabled;

    @Column(name = "created_at")
    private Instant createdAt;
}
