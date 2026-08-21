package com.example.attendance_system.models;

import java.io.Serializable;

/**
 * Attendance Model tracking daily status (PRESENT, ABSENT, LEAVE, LATE).
 */
public class Attendance implements Serializable {
    private String attendanceId;
    private String date; // YYYY-MM-DD
    private String day; // Monday, Tuesday, etc.
    private String studentId;
    private String studentUid;
    private String studentName;
    private String rollNo;
    private String departmentId;
    private String departmentName;
    private String section; // Also used as divisionId
    private String semester;
    private String subjectId;
    private String subjectName;
    private String teacherId;
    private String teacherName;
    private String status; // "PRESENT", "ABSENT", "LEAVE", "LATE"
    private String markedByUid;
    private String timetableId;
    private int lectureNumber;
    private String startTime;
    private String endTime;
    private String lectureTime; // Display string: startTime-endTime
    private long timestamp;

    public Attendance() {
    }

    public Attendance(String attendanceId, String date, String studentId, String studentUid, String studentName, String rollNo,
                      String departmentId, String departmentName, String section, String semester,
                      String subjectId, String subjectName, String status, String markedByUid) {
        this.attendanceId = attendanceId;
        this.date = date;
        this.studentId = studentId;
        this.studentUid = studentUid;
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.section = section;
        this.semester = semester;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.status = status;
        this.markedByUid = markedByUid;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
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

    public String getStatus() {
        if (punchInTime != null && !punchInTime.trim().isEmpty()) {
            if (punchOutTime == null || punchOutTime.trim().isEmpty()) {
                return "WORKING";
            }
            long diffMinutes = 0;
            if (punchInTimestamp > 0 && punchOutTimestamp > punchInTimestamp) {
                diffMinutes = (punchOutTimestamp - punchInTimestamp) / (60 * 1000);
            } else if (workingDuration != null && workingDuration.contains("h")) {
                try {
                    String[] parts = workingDuration.split("h");
                    long hours = Long.parseLong(parts[0].trim());
                    long mins = 0;
                    if (parts.length > 1 && parts[1].contains("m")) {
                        mins = Long.parseLong(parts[1].replace("m", "").trim());
                    }
                    diffMinutes = hours * 60 + mins;
                } catch (Exception ignored) {}
            } else {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US);
                    java.util.Date inD = sdf.parse(punchInTime);
                    java.util.Date outD = sdf.parse(punchOutTime);
                    if (inD != null && outD != null) {
                        long diffMs = outD.getTime() - inD.getTime();
                        if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000L;
                        diffMinutes = diffMs / (60 * 1000);
                    }
                } catch (Exception ignored) {}
            }
            return (diffMinutes >= 480) ? "FULL DAY" : "HALF DAY";
        }
        return status != null ? status : "PRESENT";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMarkedByUid() {
        return markedByUid;
    }

    public void setMarkedByUid(String markedByUid) {
        this.markedByUid = markedByUid;
    }

    public String getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(String timetableId) {
        this.timetableId = timetableId;
    }

    public int getLectureNumber() {
        return lectureNumber;
    }

    public void setLectureNumber(int lectureNumber) {
        this.lectureNumber = lectureNumber;
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

    public String getLectureTime() {
        return lectureTime;
    }

    public void setLectureTime(String lectureTime) {
        this.lectureTime = lectureTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private String punchInTime;
    private String punchOutTime;
    private long punchInTimestamp;
    private long punchOutTimestamp;
    private double punchInLatitude;
    private double punchInLongitude;
    private double punchOutLatitude;
    private double punchOutLongitude;
    private double punchInAccuracy;
    private double punchInDistanceFromOffice;
    private boolean punchInGeofenceVerified;
    private double punchOutAccuracy;
    private double punchOutDistanceFromOffice;
    private boolean punchOutGeofenceVerified;
    private double distanceFromOfficeMeters;
    private double locationAccuracyMeters;
    private boolean geofenceVerified;
    private boolean faceVerified;
    private boolean livenessVerified;
    private boolean identityVerified;
    private double faceMatchScore;
    private boolean punchOutFaceVerified;
    private boolean punchOutLivenessVerified;
    private boolean punchOutIdentityVerified;
    private double punchOutFaceMatchScore;
    private String workingDuration;
    private String punchInStatus;
    private String punchOutStatus;
    private boolean sessionCompleted;
    private long verificationTimestamp;
    private String verificationResult;
    private String deviceId;
    private String platform;
    private String appVersion;
    private String verificationImage;

    public String getPunchInTime() {
        return punchInTime;
    }

    public void setPunchInTime(String punchInTime) {
        this.punchInTime = punchInTime;
    }

    public String getPunchOutTime() {
        return punchOutTime;
    }

    public void setPunchOutTime(String punchOutTime) {
        this.punchOutTime = punchOutTime;
    }

    public long getPunchInTimestamp() {
        return punchInTimestamp;
    }

    public void setPunchInTimestamp(long punchInTimestamp) {
        this.punchInTimestamp = punchInTimestamp;
    }

    public long getPunchOutTimestamp() {
        return punchOutTimestamp;
    }

    public void setPunchOutTimestamp(long punchOutTimestamp) {
        this.punchOutTimestamp = punchOutTimestamp;
    }

    public double getPunchInLatitude() {
        return punchInLatitude;
    }

    public void setPunchInLatitude(double punchInLatitude) {
        this.punchInLatitude = punchInLatitude;
    }

    public double getPunchInLongitude() {
        return punchInLongitude;
    }

    public void setPunchInLongitude(double punchInLongitude) {
        this.punchInLongitude = punchInLongitude;
    }

    public double getPunchOutLatitude() {
        return punchOutLatitude;
    }

    public void setPunchOutLatitude(double punchOutLatitude) {
        this.punchOutLatitude = punchOutLatitude;
    }

    public double getPunchOutLongitude() {
        return punchOutLongitude;
    }

    public void setPunchOutLongitude(double punchOutLongitude) {
        this.punchOutLongitude = punchOutLongitude;
    }

    public double getPunchInAccuracy() {
        return punchInAccuracy > 0 ? punchInAccuracy : locationAccuracyMeters;
    }

    public void setPunchInAccuracy(double punchInAccuracy) {
        this.punchInAccuracy = punchInAccuracy;
    }

    public double getPunchInDistanceFromOffice() {
        return punchInDistanceFromOffice > 0 ? punchInDistanceFromOffice : distanceFromOfficeMeters;
    }

    public void setPunchInDistanceFromOffice(double punchInDistanceFromOffice) {
        this.punchInDistanceFromOffice = punchInDistanceFromOffice;
    }

    public boolean isPunchInGeofenceVerified() {
        return punchInGeofenceVerified || geofenceVerified;
    }

    public void setPunchInGeofenceVerified(boolean punchInGeofenceVerified) {
        this.punchInGeofenceVerified = punchInGeofenceVerified;
    }

    public double getPunchOutAccuracy() {
        return punchOutAccuracy;
    }

    public void setPunchOutAccuracy(double punchOutAccuracy) {
        this.punchOutAccuracy = punchOutAccuracy;
    }

    public double getPunchOutDistanceFromOffice() {
        return punchOutDistanceFromOffice;
    }

    public void setPunchOutDistanceFromOffice(double punchOutDistanceFromOffice) {
        this.punchOutDistanceFromOffice = punchOutDistanceFromOffice;
    }

    public boolean isPunchOutGeofenceVerified() {
        return punchOutGeofenceVerified;
    }

    public void setPunchOutGeofenceVerified(boolean punchOutGeofenceVerified) {
        this.punchOutGeofenceVerified = punchOutGeofenceVerified;
    }

    public double getDistanceFromOfficeMeters() {
        return distanceFromOfficeMeters;
    }

    public void setDistanceFromOfficeMeters(double distanceFromOfficeMeters) {
        this.distanceFromOfficeMeters = distanceFromOfficeMeters;
    }

    public double getLocationAccuracyMeters() {
        return locationAccuracyMeters;
    }

    public void setLocationAccuracyMeters(double locationAccuracyMeters) {
        this.locationAccuracyMeters = locationAccuracyMeters;
    }

    public boolean isGeofenceVerified() {
        return geofenceVerified;
    }

    public void setGeofenceVerified(boolean geofenceVerified) {
        this.geofenceVerified = geofenceVerified;
    }

    public boolean isFaceVerified() {
        return faceVerified;
    }

    public void setFaceVerified(boolean faceVerified) {
        this.faceVerified = faceVerified;
    }

    public boolean isLivenessVerified() {
        return livenessVerified;
    }

    public void setLivenessVerified(boolean livenessVerified) {
        this.livenessVerified = livenessVerified;
    }

    public boolean isIdentityVerified() {
        return identityVerified || faceVerified;
    }

    public void setIdentityVerified(boolean identityVerified) {
        this.identityVerified = identityVerified;
    }

    public double getFaceMatchScore() {
        return faceMatchScore;
    }

    public void setFaceMatchScore(double faceMatchScore) {
        this.faceMatchScore = faceMatchScore;
    }

    public String getWorkingDuration() {
        return workingDuration != null && !workingDuration.isEmpty() ? workingDuration : "N/A";
    }

    public void setWorkingDuration(String workingDuration) {
        this.workingDuration = workingDuration;
    }

    public String getPunchInStatus() {
        return punchInStatus != null ? punchInStatus : (punchInTime != null ? "COMPLETED" : "PENDING");
    }

    public void setPunchInStatus(String punchInStatus) {
        this.punchInStatus = punchInStatus;
    }

    public String getPunchOutStatus() {
        return punchOutStatus != null ? punchOutStatus : (punchOutTime != null ? "COMPLETED" : "PENDING");
    }

    public void setPunchOutStatus(String punchOutStatus) {
        this.punchOutStatus = punchOutStatus;
    }

    public boolean isSessionCompleted() {
        return sessionCompleted || (punchOutTime != null && !punchOutTime.isEmpty());
    }

    public void setSessionCompleted(boolean sessionCompleted) {
        this.sessionCompleted = sessionCompleted;
    }

    public long getVerificationTimestamp() {
        return verificationTimestamp > 0 ? verificationTimestamp : timestamp;
    }

    public void setVerificationTimestamp(long verificationTimestamp) {
        this.verificationTimestamp = verificationTimestamp;
    }

    public String getVerificationResult() {
        return verificationResult != null ? verificationResult : "PASSED";
    }

    public void setVerificationResult(String verificationResult) {
        this.verificationResult = verificationResult;
    }

    public String getDeviceId() {
        return deviceId != null ? deviceId : "AndroidDevice";
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatform() {
        return platform != null ? platform : "Android";
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAppVersion() {
        return appVersion != null ? appVersion : "1.0";
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getVerificationImage() {
        return verificationImage;
    }

    public void setVerificationImage(String verificationImage) {
        this.verificationImage = verificationImage;
    }

    public boolean isPunchOutFaceVerified() {
        return punchOutFaceVerified;
    }

    public void setPunchOutFaceVerified(boolean punchOutFaceVerified) {
        this.punchOutFaceVerified = punchOutFaceVerified;
    }

    public boolean isPunchOutLivenessVerified() {
        return punchOutLivenessVerified;
    }

    public void setPunchOutLivenessVerified(boolean punchOutLivenessVerified) {
        this.punchOutLivenessVerified = punchOutLivenessVerified;
    }

    public boolean isPunchOutIdentityVerified() {
        return punchOutIdentityVerified;
    }

    public void setPunchOutIdentityVerified(boolean punchOutIdentityVerified) {
        this.punchOutIdentityVerified = punchOutIdentityVerified;
    }

    public double getPunchOutFaceMatchScore() {
        return punchOutFaceMatchScore;
    }

    public void setPunchOutFaceMatchScore(double punchOutFaceMatchScore) {
        this.punchOutFaceMatchScore = punchOutFaceMatchScore;
    }

    public static String calculateWorkingDuration(String punchInTimeStr, String punchOutTimeStr) {
        if (punchInTimeStr == null || punchOutTimeStr == null || punchInTimeStr.isEmpty() || punchOutTimeStr.isEmpty()) {
            return "N/A";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US);
            java.util.Date inDate = sdf.parse(punchInTimeStr);
            java.util.Date outDate = sdf.parse(punchOutTimeStr);
            if (inDate != null && outDate != null) {
                long diffMs = outDate.getTime() - inDate.getTime();
                if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000L;
                long diffMinutes = diffMs / (60 * 1000);
                long hours = diffMinutes / 60;
                long mins = diffMinutes % 60;
                return hours + "h " + mins + "m";
            }
        } catch (Exception e) {
            try {
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US);
                java.util.Date inDate = sdf2.parse(punchInTimeStr);
                java.util.Date outDate = sdf2.parse(punchOutTimeStr);
                if (inDate != null && outDate != null) {
                    long diffMs = outDate.getTime() - inDate.getTime();
                    if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000L;
                    long diffMinutes = diffMs / (60 * 1000);
                    long hours = diffMinutes / 60;
                    long mins = diffMinutes % 60;
                    return hours + "h " + mins + "m";
                }
            } catch (Exception ignored) {}
        }
        return "N/A";
    }
}
