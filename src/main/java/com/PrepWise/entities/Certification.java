package com.PrepWise.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "certifications")
public class Certification {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String issuer;

    @Column(nullable = false)
    private String date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Certification() {}

    public Certification(String name, String issuer, String date) {
        this.name = name;
        this.issuer = issuer;
        this.date = date;
    }

    public void setId(Long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setIssuer(String issuer) { this.issuer = issuer; }

    public void setDate(String date) { this.date = date; }

    public void setUser(User user) { this.user = user; }
}

