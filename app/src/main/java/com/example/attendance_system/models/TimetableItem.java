package com.example.attendance_system.models;

import java.io.Serializable;

public class TimetableItem implements Serializable {
    private String id;
    private String day; // Monday, Tuesday, etc.
    private String startTime; // 11:00 AM
    private String endTime; // 12:00 PM
    private String subjectId;
    private String subjectName;
    private String teacherId;
    private String teacherName;
    private String roomNumber;
    private String departmentId;
    private String departmentName;
    private String division;
    private String semester;
    private int lectureNumber;
    private boolean isBreak;

    public TimetableItem() {
    }

    public TimetableItem(String id, String day, String startTime, String endTime, String subjectId,
                         String subjectName, String teacherId, String teacherName, String roomNumber,
                         String departmentId, String departmentName, String division, String semester,
                         int lectureNumber, boolean isBreak) {
        this.id = id;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.roomNumber = roomNumber;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.division = division;
        this.semester = semester;
        this.lectureNumber = lectureNumber;
        this.isBreak = isBreak;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
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

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getLectureNumber() {
        return lectureNumber;
    }

    public void setLectureNumber(int lectureNumber) {
        this.lectureNumber = lectureNumber;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    public String getTimeSlot() {
        return startTime + "–" + endTime;
    }
}
