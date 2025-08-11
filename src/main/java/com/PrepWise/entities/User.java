package com.PrepWise.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String profilePhoto;

    @Column(nullable = false)
    private String location;

    private String domainBadge;

    @Column(columnDefinition = "TEXT")
    private String domainDistribution;

    private String githubUrl;
    private String linkedinUrl;
    private String portfolioLink;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Achievement> achievements = new ArrayList<>();

    // Constructors
    public User() {}

    public User(String username, String email, String password, String name, String profilePhoto, String location) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.profilePhoto = profilePhoto;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDomainBadge() { return domainBadge; }
    public void setDomainBadge(String domainBadge) { this.domainBadge = domainBadge; }

    public String getDomainDistribution() { return domainDistribution; }
    public void setDomainDistribution(String domainDistribution) { this.domainDistribution = domainDistribution; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getPortfolioLink() { return portfolioLink; }
    public void setPortfolioLink(String portfolioLink) { this.portfolioLink = portfolioLink; }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) {
        this.skills.clear();
        if (skills != null) {
            skills.forEach(skill -> skill.setUser(this));
            this.skills.addAll(skills);
        }
    }

    public List<Certification> getCertifications() { return certifications; }
    public void setCertifications(List<Certification> certifications) {
        this.certifications.clear();
        if (certifications != null) {
            certifications.forEach(cert -> cert.setUser(this));
            this.certifications.addAll(certifications);
        }
    }

    public List<Achievement> getAchievements() { return achievements; }
    public void setAchievements(List<Achievement> achievements) {
        this.achievements.clear();
        if (achievements != null) {
            achievements.forEach(achievement -> achievement.setUser(this));
            this.achievements.addAll(achievements);
        }
    }
}
