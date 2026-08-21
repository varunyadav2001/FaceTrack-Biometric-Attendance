package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * Subject Model representing courses / subjects under departments.
 */
public class Subject implements Serializable {
    private String subjectId;
    private String code;
    private String name;
    private String departmentId;
    private String departmentName;
    private String semester;
    private int credits;
    private String teacherId;
    private String teacherName;

    public Subject() {
    }

    public Subject(String subjectId, String code, String name, String departmentId, String departmentName,
                   String semester, int credits, String teacherId, String teacherName) {
        this.subjectId = subjectId;
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.semester = semester;
        this.credits = credits;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
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
}
