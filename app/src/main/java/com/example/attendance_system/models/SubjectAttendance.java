package com.example.attendance_system.models;

import java.io.Serializable;

public class SubjectAttendance implements Serializable {
    private String subjectId;
    private String subjectName;
    private String subjectCode;
    private int presentClasses;
    private int absentClasses;
    private int leaveClasses;
    private int totalClasses;
    private double percentage;
    private String teacherName;
    private String roomNumber;

    public SubjectAttendance() {
    }

    public SubjectAttendance(String subjectId, String subjectName, String subjectCode, int presentClasses, int absentClasses, int leaveClasses, int totalClasses) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.presentClasses = presentClasses;
        this.absentClasses = absentClasses;
        this.leaveClasses = leaveClasses;
        this.totalClasses = totalClasses;
        this.percentage = totalClasses > 0 ? ((double) presentClasses / totalClasses) * 100.0 : 0.0;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public int getPresentClasses() {
        return presentClasses;
    }

    public void setPresentClasses(int presentClasses) {
        this.presentClasses = presentClasses;
        calculatePercentage();
    }

    public int getAbsentClasses() {
        return absentClasses;
    }

    public void setAbsentClasses(int absentClasses) {
        this.absentClasses = absentClasses;
    }

    public int getLeaveClasses() {
        return leaveClasses;
    }

    public void setLeaveClasses(int leaveClasses) {
        this.leaveClasses = leaveClasses;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
        calculatePercentage();
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    private void calculatePercentage() {
        this.percentage = totalClasses > 0 ? ((double) presentClasses / totalClasses) * 100.0 : 0.0;
    }
}
