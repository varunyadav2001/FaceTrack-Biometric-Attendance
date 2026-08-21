package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * Student Model representing student entities in Firestore.
 */
public class Student implements Serializable {
    private String studentId;
    private String uid;
    private String name;
    private String email;
    private String rollNo;
    private String departmentId;
    private String departmentName;
    private String semester;
    private String section;
    private String phone;
    private String profileImageUrl;
    private String qrCodeData;
    private long createdAt;

    public Student() {
        // Default constructor for Firestore
    }

    public Student(String studentId, String uid, String name, String email, String rollNo,
                   String departmentId, String departmentName, String semester, String section,
                   String phone) {
        this.studentId = studentId;
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.rollNo = rollNo;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.semester = semester;
        this.section = section;
        this.phone = phone;
        this.createdAt = System.currentTimeMillis();
    }

    public Student(String studentId, String uid, String name, String email, String rollNo,
                   String departmentId, String departmentName, String semester, String section,
                   String phone, String profileImageUrl, String qrCodeData) {
        this.studentId = studentId;
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.rollNo = rollNo;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.semester = semester;
        this.section = section;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
        this.qrCodeData = qrCodeData;
        this.createdAt = System.currentTimeMillis();
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
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

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
