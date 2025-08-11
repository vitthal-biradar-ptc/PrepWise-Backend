package com.PrepWise.services;

import com.PrepWise.dto.ResumeParseResponse;
import com.PrepWise.entities.Achievement;
import com.PrepWise.entities.Certification;
import com.PrepWise.entities.Skill;
import com.PrepWise.entities.User;
import com.PrepWise.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void updateUserProfileFromResume(String username, ResumeParseResponse resumeData) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Update domain badge
        if (resumeData.getDomain() != null && !resumeData.getDomain().trim().isEmpty()) {
            user.setDomainBadge(resumeData.getDomain().trim());
        }

        // Update domain distribution
        if (resumeData.getDomainDistribution() != null) {
            try {
                Map<String, Object> domainData = new HashMap<>();
                domainData.put("labels", resumeData.getDomainDistribution().getLabels());

                Map<String, Object> dataset = new HashMap<>();
                dataset.put("data", resumeData.getDomainDistribution().getData());
                domainData.put("datasets", List.of(dataset));

                String domainDistributionJson = objectMapper.writeValueAsString(domainData);
                user.setDomainDistribution(domainDistributionJson);
            } catch (Exception e) {
                System.err.println("Error serializing domain distribution: " + e.getMessage());
            }
        }

        // Merge skills instead of clearing
        if (resumeData.getSkills() != null) {
            mergeSkills(user, resumeData.getSkills());
        }

        // Merge certifications instead of clearing
        if (resumeData.getCertifications() != null) {
            mergeCertifications(user, resumeData.getCertifications());
        }

        // Merge achievements instead of clearing
        if (resumeData.getAchievements() != null) {
            mergeAchievements(user, resumeData.getAchievements());
        }

        userRepository.save(user);
    }

    private void mergeSkills(User user, List<ResumeParseResponse.SkillDto> newSkills) {
        if (newSkills == null || newSkills.isEmpty()) {
            return;
        }

        for (ResumeParseResponse.SkillDto skillDto : newSkills) {
            if (skillDto.getName() == null || skillDto.getName().trim().isEmpty()) {
                continue;
            }

            // Check if skill already exists (case-insensitive)
            Optional<Skill> existingSkill = user.getSkills().stream()
                    .filter(skill -> skill.getName().equalsIgnoreCase(skillDto.getName().trim()))
                    .findFirst();

            if (existingSkill.isPresent()) {
                // Update proficiency if new level is higher
                String newProficiency = skillDto.getProficiency();
                String currentProficiency = existingSkill.get().getProficiency();

                if (isProficiencyHigher(newProficiency, currentProficiency)) {
                    existingSkill.get().setProficiency(newProficiency);
                }
            } else {
                // Add new skill
                Skill newSkill = new Skill(skillDto.getName().trim(), skillDto.getProficiency());
                newSkill.setUser(user);
                user.getSkills().add(newSkill);
            }
        }
    }

    private boolean isProficiencyHigher(String newProficiency, String currentProficiency) {
        Map<String, Integer> proficiencyLevels = Map.of(
                "Beginner", 1,
                "Intermediate", 2,
                "Advanced", 3,
                "Expert", 4
        );

        int newLevel = proficiencyLevels.getOrDefault(newProficiency, 1);
        int currentLevel = proficiencyLevels.getOrDefault(currentProficiency, 1);

        return newLevel > currentLevel;
    }

    private void mergeCertifications(User user, List<ResumeParseResponse.CertificationDto> newCertifications) {
        if (newCertifications == null || newCertifications.isEmpty()) {
            return;
        }

        for (ResumeParseResponse.CertificationDto certDto : newCertifications) {
            if (certDto.getName() == null || certDto.getName().trim().isEmpty()) {
                continue;
            }

            // Check if certification already exists (case-insensitive by name and issuer)
            Optional<Certification> existingCert = user.getCertifications().stream()
                    .filter(cert -> cert.getName().equalsIgnoreCase(certDto.getName().trim()) &&
                                  (certDto.getIssuer() == null || cert.getIssuer().equalsIgnoreCase(certDto.getIssuer().trim())))
                    .findFirst();

            if (existingCert.isPresent()) {
                // Update date if new date is more recent or current is "Unknown"
                String newDate = certDto.getDate();
                String currentDate = existingCert.get().getDate();

                if (isDateMoreRecent(newDate, currentDate)) {
                    existingCert.get().setDate(newDate);
                }

                // Update issuer if it was null or empty
                if (certDto.getIssuer() != null && !certDto.getIssuer().trim().isEmpty() &&
                    (existingCert.get().getIssuer() == null || existingCert.get().getIssuer().trim().isEmpty())) {
                    existingCert.get().setIssuer(certDto.getIssuer().trim());
                }
            } else {
                // Add new certification
                String issuer = (certDto.getIssuer() != null && !certDto.getIssuer().trim().isEmpty())
                    ? certDto.getIssuer().trim() : "Unknown";
                String date = (certDto.getDate() != null && !certDto.getDate().trim().isEmpty())
                    ? certDto.getDate().trim() : "Unknown";

                Certification newCert = new Certification(certDto.getName().trim(), issuer, date);
                newCert.setUser(user);
                user.getCertifications().add(newCert);
            }
        }
    }

    private void mergeAchievements(User user, List<ResumeParseResponse.AchievementDto> newAchievements) {
        if (newAchievements == null || newAchievements.isEmpty()) {
            return;
        }

        for (ResumeParseResponse.AchievementDto achDto : newAchievements) {
            if (achDto.getName() == null || achDto.getName().trim().isEmpty()) {
                continue;
            }

            // Check if achievement already exists (case-insensitive by name)
            Optional<Achievement> existingAch = user.getAchievements().stream()
                    .filter(ach -> ach.getName().equalsIgnoreCase(achDto.getName().trim()))
                    .findFirst();

            if (existingAch.isPresent()) {
                // Update description if new description is available and longer/more detailed
                String newDescription = achDto.getDescription();
                String currentDescription = existingAch.get().getDescription();

                if (newDescription != null && !newDescription.trim().isEmpty() &&
                    (currentDescription == null || currentDescription.trim().isEmpty() ||
                     newDescription.length() > currentDescription.length())) {
                    existingAch.get().setDescription(newDescription.trim());
                }

                // Update date if new date is more recent
                String newDate = achDto.getDate();
                String currentDate = existingAch.get().getDate();

                if (isDateMoreRecent(newDate, currentDate)) {
                    existingAch.get().setDate(newDate);
                }
            } else {
                // Add new achievement
                String description = (achDto.getDescription() != null && !achDto.getDescription().trim().isEmpty())
                    ? achDto.getDescription().trim() : "";
                String date = (achDto.getDate() != null && !achDto.getDate().trim().isEmpty())
                    ? achDto.getDate().trim() : "Unknown";

                Achievement newAch = new Achievement(achDto.getName().trim(), description, date);
                newAch.setUser(user);
                user.getAchievements().add(newAch);
            }
        }
    }

    private boolean isDateMoreRecent(String newDate, String currentDate) {
        // If current date is "Unknown" or null, new date is better
        if (currentDate == null || currentDate.equalsIgnoreCase("Unknown") || currentDate.trim().isEmpty()) {
            return newDate != null && !newDate.equalsIgnoreCase("Unknown") && !newDate.trim().isEmpty();
        }

        // If new date is "Unknown", keep current date
        if (newDate == null || newDate.equalsIgnoreCase("Unknown") || newDate.trim().isEmpty()) {
            return false;
        }

        // Simple year comparison if both are numeric years
        try {
            int newYear = Integer.parseInt(newDate.replaceAll("[^0-9]", ""));
            int currentYear = Integer.parseInt(currentDate.replaceAll("[^0-9]", ""));
            return newYear > currentYear;
        } catch (NumberFormatException e) {
            // If can't parse as years, prefer new date over current
            return true;
        }
    }
}
