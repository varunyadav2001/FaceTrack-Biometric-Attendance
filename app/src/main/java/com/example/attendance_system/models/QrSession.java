package com.example.attendance_system.models;

import java.io.Serializable;

public class QrSession implements Serializable {
    private String sessionId;
    private String teacherUid;
    private String teacherName;
    private String subjectId;
    private String subjectName;
    private String departmentId;
    private String departmentName;
    private String classId;
    private String division;
    private String semester;
    private int lectureNumber;
    private String room;
    private String date;
    private long createdAt;
    private long expiresAt;
    private String status; // "active", "expired"
    private String type; // "attendance"

    public QrSession() {
        this.status = "active";
        this.type = "attendance";
    }

    public QrSession(String sessionId, String teacherUid, String teacherName, String subjectId, String subjectName,
                     String departmentId, String departmentName, String division, String semester, String date,
                     long createdAt, long expiresAt) {
        this.sessionId = sessionId;
        this.teacherUid = teacherUid;
        this.teacherName = teacherName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.classId = departmentId;
        this.division = division;
        this.semester = semester;
        this.date = date;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = "active";
        this.type = "attendance";
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTeacherUid() { return teacherUid; }
    public void setTeacherUid(String teacherUid) { this.teacherUid = teacherUid; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { 
        this.departmentId = departmentId; 
        if (this.classId == null || this.classId.isEmpty()) this.classId = departmentId;
    }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getClassId() { return classId != null ? classId : departmentId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getLectureNumber() { return lectureNumber; }
    public void setLectureNumber(int lectureNumber) { this.lectureNumber = lectureNumber; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status != null ? status : "active"; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
