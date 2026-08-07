package com.example.forum.feature.admin;

import com.example.forum.feature.auth.dto.request.RegisterRequest;
import com.example.forum.feature.user.dto.UserSummaryDto;

public interface AdminService {
    UserSummaryDto createAdmin(RegisterRequest request);
}
