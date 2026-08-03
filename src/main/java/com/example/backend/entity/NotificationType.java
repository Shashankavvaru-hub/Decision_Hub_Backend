package com.example.backend.entity;
public enum NotificationType {

    // Decision Interaction Notifications
    COMMENT,
    REPLY,
    COMMENT_EDIT,
    COMMENT_DELETE,

    VOTE,
    VOTE_UPDATED,
    VOTE_REMOVED,

    // Community Join Request Notifications
    JOIN_REQUEST,
    JOIN_REQUEST_APPROVED,
    JOIN_REQUEST_REJECTED,

    // Community Notifications
    COMMUNITY_UPDATED,
    COMMUNITY_DELETED,
    MEMBER_REMOVED,

    // Decision Notifications
    DECISION_UPDATED,

    // Legacy / Future (currently unused)
    INVITATION,

    // Admin Notifications
    COMMUNITY_CREATED,
    DECISION_CREATED,
    MODERATOR_ACTION,
    BROADCAST,
    ANNOUNCEMENT,
    WARNING
}