package com.employeeresumemaneger.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "candidates")
public class Candidate {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long Id;
    private String name;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email",unique = true,nullable = false)
    private String email;
    @Column(name = "linkedin_url",unique = true,nullable = false)
    private String linkedinUrl;
    @Column(name = "motivation_message")
    private String motivationMessage;
    @Column(name = "cv_name")
    private String cvName;
    @Column(name = "created_at")
    private Instant createdAt;




}
