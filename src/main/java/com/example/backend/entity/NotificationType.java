package com.example.backend.entity;

public enum NotificationType {

    // Decision Interaction Notifications
    COMMENT,
    REPLY,
    COMMENT_EDIT,
    COMMENT_DELETE,

    VOTE,
    VOTE_CHANGED,
    VOTE_REMOVED,

    INVITATION,

    // Community Notifications
    COMMUNITY_UPDATED,
    DECISION_UPDATED,
    MEMBER_REMOVED,

    // Admin Notifications
    COMMUNITY_CREATED,
    DECISION_CREATED,
    MODERATOR_ACTION
}