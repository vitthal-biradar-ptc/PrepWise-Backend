package com.PrepWise.utils;

import org.springframework.stereotype.Service;

@Service
public class FileUploadService {

    public String generateDefaultAvatar(String email) {
        if (email != null && !email.isEmpty()) {
            return "https://i.pravatar.cc/150?u=" + Math.abs(email.hashCode());
        }
        return "https://i.pravatar.cc/150?u=" + System.currentTimeMillis();
    }
}
