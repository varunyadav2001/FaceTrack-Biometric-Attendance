package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * Notice Model for announcements, holidays, emergency alerts, exam updates.
 */
public class Notice implements Serializable {
    private String noticeId;
    private String title;
    private String message;
    private String targetRole; // "ALL", "STUDENT", "TEACHER"
    private String type; // "HOLIDAY", "EMERGENCY", "EXAM", "GENERAL"
    private String senderName;
    private long timestamp;

    public Notice() {
    }

    public Notice(String noticeId, String title, String message, String targetRole, String type, String senderName) {
        this.noticeId = noticeId;
        this.title = title;
        this.message = message;
        this.targetRole = targetRole;
        this.type = type;
        this.senderName = senderName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
