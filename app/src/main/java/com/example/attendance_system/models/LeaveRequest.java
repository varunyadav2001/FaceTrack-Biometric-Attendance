package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * LeaveRequest Model representing student leave applications.
 */
public class LeaveRequest implements Serializable {
    private String leaveId;
    private String studentId;
    private String studentUid;
    private String studentName;
    private String rollNo;
    private String departmentId;
    private String departmentName;
    private String semester;
    private String section;
    private String reason;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String status; // "PENDING", "APPROVED", "REJECTED", "Pending", "Approved", "Rejected"
    private String remarks;
    private long appliedOn;
    private long submittedAt;
    private String teacherUid;
    private String teacherName;

    public LeaveRequest() {
    }

    public LeaveRequest(String leaveId, String studentId, String studentName, String departmentId,
                        String reason, String startDate, String endDate) {
        this.leaveId = leaveId;
        this.studentId = studentId;
        this.studentUid = studentId;
        this.studentName = studentName;
        this.departmentId = departmentId;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "Pending";
        this.remarks = "";
        this.appliedOn = System.currentTimeMillis();
        this.submittedAt = this.appliedOn;
    }

    public String getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(String studentUid) {
        this.studentUid = studentUid;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public long getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(long appliedOn) {
        this.appliedOn = appliedOn;
        this.submittedAt = appliedOn;
    }

    public long getSubmittedAt() {
        return submittedAt > 0 ? submittedAt : appliedOn;
    }

    public void setSubmittedAt(long submittedAt) {
        this.submittedAt = submittedAt;
        if (this.appliedOn == 0) this.appliedOn = submittedAt;
    }

    public String getTeacherUid() {
        return teacherUid;
    }

    public void setTeacherUid(String teacherUid) {
        this.teacherUid = teacherUid;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
