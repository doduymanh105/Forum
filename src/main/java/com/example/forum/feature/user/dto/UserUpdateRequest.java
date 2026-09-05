package com.example.forum.feature.user.dto;

import com.example.forum.domain.Enum.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

@Data
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "Username can not blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @URL(message = "Invalid avatar url")
    @Size(max = 255, message = "Avatar url can not be exceed 255 characters")
    private String avatarUrl;

    @Size(max = 255, message = "Bio can not be exceed 255 characters")
    private String bio;

    private Map<SocialPlatform, @URL(message = "Invalid url") String> socialLinks;
}
