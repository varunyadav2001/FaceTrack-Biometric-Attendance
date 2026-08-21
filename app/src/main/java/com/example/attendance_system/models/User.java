package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * User Model representing system authentication users (Admin, Teacher, Student).
 */
public class User implements Serializable {
    private String uid;
    private String email;
    private String name;
    private String role; // "ADMIN", "TEACHER", "STUDENT"
    private String phone;
    private String profileImageUrl;
    private long createdAt;
    private boolean active;

    public User() {
        // Required for Firestore deserialization
    }

    public User(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public User(String uid, String email, String name, String role, String phone, String profileImageUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    private boolean faceRegistered;
    private java.util.List<Float> faceEmbedding;
    private String faceProfileId;
    private long registrationTimestamp;

    public boolean isFaceRegistered() {
        return faceRegistered;
    }

    public void setFaceRegistered(boolean faceRegistered) {
        this.faceRegistered = faceRegistered;
    }

    public java.util.List<Float> getFaceEmbedding() {
        return faceEmbedding;
    }

    public void setFaceEmbedding(java.util.List<Float> faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }

    public String getFaceProfileId() {
        return faceProfileId;
    }

    public void setFaceProfileId(String faceProfileId) {
        this.faceProfileId = faceProfileId;
    }

    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public void setRegistrationTimestamp(long registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }
}
