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

        // Clear existing data
        user.getSkills().clear();
        user.getCertifications().clear();
        user.getAchievements().clear();

        // Add skills
        if (resumeData.getSkills() != null) {
            List<Skill> skills = resumeData.getSkills().stream()
                    .map(skillDto -> {
                        Skill skill = new Skill(skillDto.getName(), skillDto.getProficiency());
                        skill.setUser(user);
                        return skill;
                    })
                    .collect(Collectors.toList());
            user.setSkills(skills);
        }

        // Add certifications
        if (resumeData.getCertifications() != null) {
            List<Certification> certifications = resumeData.getCertifications().stream()
                    .map(certDto -> {
                        Certification cert = new Certification(certDto.getName(), certDto.getIssuer(), certDto.getDate());
                        cert.setUser(user);
                        return cert;
                    })
                    .collect(Collectors.toList());
            user.setCertifications(certifications);
        }

        // Add achievements
        if (resumeData.getAchievements() != null) {
            List<Achievement> achievements = resumeData.getAchievements().stream()
                    .map(achDto -> {
                        Achievement achievement = new Achievement(achDto.getName(), achDto.getDescription(), achDto.getDate());
                        achievement.setUser(user);
                        return achievement;
                    })
                    .collect(Collectors.toList());
            user.setAchievements(achievements);
        }

        userRepository.save(user);
    }
}
