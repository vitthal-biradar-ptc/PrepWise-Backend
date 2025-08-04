package com.PrepWise.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeAnalysisResponse {
    private String domain;
    private List<String> suggestions;
}
