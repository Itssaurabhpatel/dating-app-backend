package com.dating.config;

public final class AppConstants {

    private AppConstants() {}

    public static final String API_VERSION = "/api/v1";
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    public static final int MAX_PROFILE_PHOTOS = 6;
    public static final int MAX_BIO_LENGTH = 500;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 100;

    public static final String MATCH_CREATED_EVENT = "match.created";
    public static final String MESSAGE_SENT_EVENT = "message.sent";
    public static final String PROFILE_LIKED_EVENT = "profile.liked";
    public static final String SUPER_LIKE_EVENT = "profile.superliked";
    public static final String SUBSCRIPTION_EXPIRED_EVENT = "subscription.expired";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
}
