package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * Department Model (MCA, BCA, BSc IT, BTech, MBA).
 */
public class Department implements Serializable {
    private String departmentId;
    private String code;
    private String name;
    private String description;
    private long createdAt;

    public Department() {
    }

    public Department(String departmentId, String code, String name, String description) {
        this.departmentId = departmentId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
