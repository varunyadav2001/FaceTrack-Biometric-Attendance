package com.example.attendance_system.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher Model representing teacher entities in Firestore.
 */
public class Teacher implements Serializable {
    private String teacherId;
    private String uid;
    private String name;
    private String email;
    private String departmentId;
    private String departmentName;
    private String designation;
    private String phone;
    private String profileImageUrl;
    private List<String> assignedSubjects;
    private List<String> assignedClasses;
    private long createdAt;

    public Teacher() {
        this.assignedSubjects = new ArrayList<>();
        this.assignedClasses = new ArrayList<>();
    }

    public Teacher(String teacherId, String uid, String name, String email, String departmentId,
                   String departmentName, String designation, String phone) {
        this.teacherId = teacherId;
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.designation = designation;
        this.phone = phone;
        this.assignedSubjects = new ArrayList<>();
        this.assignedClasses = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Teacher(String teacherId, String uid, String name, String email, String departmentId,
                   String departmentName, String designation, String phone, String profileImageUrl) {
        this.teacherId = teacherId;
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.designation = designation;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.assignedSubjects = new ArrayList<>();
        this.assignedClasses = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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

    public List<String> getAssignedSubjects() {
        return assignedSubjects;
    }

    public void setAssignedSubjects(List<String> assignedSubjects) {
        this.assignedSubjects = assignedSubjects;
    }

    public List<String> getAssignedClasses() {
        return assignedClasses;
    }

    public void setAssignedClasses(List<String> assignedClasses) {
        this.assignedClasses = assignedClasses;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
