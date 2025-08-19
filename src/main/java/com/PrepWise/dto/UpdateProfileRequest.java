package com.PrepWise.dto;

import java.util.List;
import java.util.Map;

public class UpdateProfileRequest {
    private List<SkillDto> skills;
    private String githubUrl;
    private String profilePhoto;
    private List<AchievementDto> achievements;
    private String linkedinUrl;
    private String name;
    private String domainBadge;
    private String location;
    private Map<String, Object> domainData;
    private List<CertificationDto> certifications;
    private String email;
    private String portfolioLink;

    // Default constructor
    public UpdateProfileRequest() {}

    // Getters and Setters
    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public List<AchievementDto> getAchievements() { return achievements; }
    public void setAchievements(List<AchievementDto> achievements) { this.achievements = achievements; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomainBadge() { return domainBadge; }
    public void setDomainBadge(String domainBadge) { this.domainBadge = domainBadge; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Map<String, Object> getDomainData() { return domainData; }
    public void setDomainData(Map<String, Object> domainData) { this.domainData = domainData; }

    public List<CertificationDto> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationDto> certifications) { this.certifications = certifications; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPortfolioLink() { return portfolioLink; }
    public void setPortfolioLink(String portfolioLink) { this.portfolioLink = portfolioLink; }

    // Inner classes for nested objects
    public static class SkillDto {
        private String name;
        private Long id;
        private String proficiency;

        public SkillDto() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getProficiency() { return proficiency; }
        public void setProficiency(String proficiency) { this.proficiency = proficiency; }
    }

    public static class AchievementDto {
        private String date;
        private String name;
        private String description;
        private Long id;

        public AchievementDto() {}

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class CertificationDto {
        private Long id;
        private String name;
        private String issuer;
        private String date;

        public CertificationDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }
}