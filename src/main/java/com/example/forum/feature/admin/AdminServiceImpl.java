package com.example.forum.feature.admin;

import com.example.forum.common.constant.AppConstants;
import com.example.forum.common.constant.MessageConstants;
import com.example.forum.feature.user.dto.UserSummaryDto;
import com.example.forum.feature.auth.dto.request.RegisterRequest;
import com.example.forum.domain.Role;
import com.example.forum.domain.UserEntity;
import com.example.forum.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserSummaryDto createAdmin(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException(MessageConstants.EMAIL_ALREADY_EXISTS);
        }

        Role adminRole = roleRepository.findByName(AppConstants.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalArgumentException(MessageConstants.ROLE_NOT_FOUND));

        UserEntity admin = new UserEntity();
        admin.setUserName(request.getUserName());
        admin.setEmail(request.getEmail());
        admin.setUserPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRoles(Set.of(adminRole));
        admin.setIsVerified(true);

        UserEntity savedAdmin= userRepository.save(admin);

        return UserSummaryDto.builder()
                .userId(savedAdmin.getUserId())
                .username(savedAdmin.displayUsername())
                .email(savedAdmin.getEmail())
                .avatarUrl(savedAdmin.getAvatarUrl())
                .build();
    }
}

