package com.example.attendance_system.models;

import java.io.Serializable;

public class NotificationItem implements Serializable {
    private String id;
    private String studentId;
    private String title;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String type; // CLASS_CANCELLED, LOW_ATTENDANCE, ANNOUNCEMENT, TIMETABLE_UPDATE

    public NotificationItem() {
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public NotificationItem(String id, String studentId, String title, String message, String type) {
        this.id = id;
        this.studentId = studentId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
