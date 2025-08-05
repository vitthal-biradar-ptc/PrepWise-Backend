package com.PrepWise.dto;

import java.util.List;

public class ResumeParseResponse {
    private String domain;
    private List<SkillDto> skills;
    private List<CertificationDto> certifications;
    private List<AchievementDto> achievements;
    private DomainDistributionDto domainDistribution;

    public static class SkillDto {
        private String name;
        private String proficiency;

        public SkillDto() {}
        public SkillDto(String name, String proficiency) {
            this.name = name;
            this.proficiency = proficiency;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProficiency() { return proficiency; }
        public void setProficiency(String proficiency) { this.proficiency = proficiency; }
    }

    public static class CertificationDto {
        private String name;
        private String issuer;
        private String date;

        public CertificationDto() {}
        public CertificationDto(String name, String issuer, String date) {
            this.name = name;
            this.issuer = issuer;
            this.date = date;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class AchievementDto {
        private String name;
        private String description;
        private String date;

        public AchievementDto() {}
        public AchievementDto(String name, String description, String date) {
            this.name = name;
            this.description = description;
            this.date = date;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }

    public static class DomainDistributionDto {
        private List<String> labels;
        private List<Integer> data;

        public DomainDistributionDto() {}
        public DomainDistributionDto(List<String> labels, List<Integer> data) {
            this.labels = labels;
            this.data = data;
        }

        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<Integer> getData() { return data; }
        public void setData(List<Integer> data) { this.data = data; }
    }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }
    public List<CertificationDto> getCertifications() { return certifications; }
    public void setCertifications(List<CertificationDto> certifications) { this.certifications = certifications; }
    public List<AchievementDto> getAchievements() { return achievements; }
    public void setAchievements(List<AchievementDto> achievements) { this.achievements = achievements; }
    public DomainDistributionDto getDomainDistribution() { return domainDistribution; }
    public void setDomainDistribution(DomainDistributionDto domainDistribution) { this.domainDistribution = domainDistribution; }
}
