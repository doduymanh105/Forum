package com.example.forum.common.constant;

import java.util.Set;

public final class AppConstants {

    private AppConstants(){}

    // Redis key prefix
    public static final String REVOKED_USER_KEY = "revoked_user:";
    public static final String REVOKED_DEVICE_KEY = "revoked_device:";
    public static final String PREFIX_FAIL_COUNT = "login:fail:";
    public static final String PREFIX_LOCK_LOGIN = "login:locked:";
    public static final String PREFIX_TEMP_2FA = "tempt:2af:";
    public static final String BLACKLIST_KEY="bl_token:";
    public static final String PREFIX_BLACKLIST_REFRESH_TOKEN ="bl_refresh:";

    // otp redis prefix
    public static final String PREFIX_VERIFICATION_OTP = "verification:opt:";
    public static final String PREFIX_VERIFICATION_ATTEMPT ="attempts:verification:otp:";
    public static final String PREFIX_RESET_TOKEN="password_reset_token:";

    // redis account value
    public static final String REDIS_VALUE_REVOKED ="revoked";
    public static final String REDIS_VALUE_LOGOUT ="logout";
    public static final String REDIS_VALUE_LOCKED="locked";


    // redis online
    public static final String ONLINE_KEY_PREFIX = "USER_ONLINE:";


    // user role
    public static final String ROLE_USER ="ROLE_USER";
    public static final String ROLE_ADMIN ="ROLE_ADMIN";

    public static final int OTP_GENERATION_BOUND = 1000000; // Để random ra 6 số
    public static final int INITIAL_ATTEMPT_VALUE = 1;

    public static final String DATE_TIME_FORMAT ="dd-MM-yyyy HH:mm:ss";

    // file validate
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    public static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    public static final String CLOUDINARY_FOLDER_AVATARS = "forum_avatars";

    // avatarURl
    public static final String DEFAULT_AVATAR_URL ="https://cdn-icons-png.flaticon.com/512/9815/9815472.png";

    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_DAYS = 300;
    public static final int TAG_JOB_SCHEDULE = 1800000;

    public static final String SUBJECT_OTP_MAIL = "Your verification code (OTP)";
    public static final String SUBJECT_NEW_DEVICE_LOGIN = "Security Alert: New Login Detected";
    public static final String OTP_SENT_SUCCESS = "OTP has been sent to your email.";
    public static final String REQUIRE_TWO_FACTOR_AUTHENTICATION = "Two-factor authentication is required";


    // POST CACHE
    public static final String CACHE_NULL_VALUE = "NOT_FOUND";
    public static final int NULL_CACHE_TTL = 30;
    public static final int NORMAL_CACHE_TTL = 60;
    public static final int MAX_JITTER_MINUTES = 10;
    public static final String POST_DETAIL_KEY ="post:detail:";


}
