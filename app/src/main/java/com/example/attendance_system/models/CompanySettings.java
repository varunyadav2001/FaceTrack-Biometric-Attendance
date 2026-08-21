package com.example.attendance_system.models;

import java.io.Serializable;

public class CompanySettings implements Serializable {

    private String companyName;
    private double companyLatitude;
    private double companyLongitude;
    private double allowedRadiusMeters;
    private String officeAddress;
    private String shiftStartTime;
    private String shiftEndTime;
    private boolean active;

    public CompanySettings() {
        // Default settings for INFOYASHONAND TECHNOLOGY PVT LTD - Sangli
        this.companyName = "INFOYASHONAND TECHNOLOGY PVT. LTD.";
        this.companyLatitude = 16.8493056; // Exact Google Maps office latitude
        this.companyLongitude = 74.596352;  // Exact Google Maps office longitude
        this.allowedRadiusMeters = 200.0;
        this.officeAddress = "Pearl Enclave, S.No 4360/K, Flat No F-3, Walchand College Corner, MSEB Road, Opp. Safa Bakery, Sangli, Maharashtra 416416";
        this.shiftStartTime = "09:00 AM";
        this.shiftEndTime = "06:00 PM";
        this.active = true;
    }

    public CompanySettings(String companyName, double companyLatitude, double companyLongitude,
                           double allowedRadiusMeters, String officeAddress,
                           String shiftStartTime, String shiftEndTime, boolean active) {
        this.companyName = companyName;
        this.companyLatitude = companyLatitude;
        this.companyLongitude = companyLongitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
        this.officeAddress = officeAddress;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
        this.active = active;
    }

    public String getCompanyName() {
        return companyName != null ? companyName : "INFOYASHONAND TECHNOLOGY PVT. LTD.";
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getCompanyLatitude() {
        if (companyLatitude == 0.0) {
            return 16.8493056; // Fallback initial office latitude
        }
        return companyLatitude;
    }

    public void setCompanyLatitude(double companyLatitude) {
        this.companyLatitude = companyLatitude;
    }

    public double getCompanyLongitude() {
        if (companyLongitude == 0.0) {
            return 74.596352; // Fallback initial office longitude
        }
        return companyLongitude;
    }

    public void setCompanyLongitude(double companyLongitude) {
        this.companyLongitude = companyLongitude;
    }

    public double getAllowedRadiusMeters() {
        return allowedRadiusMeters > 0 ? allowedRadiusMeters : 200.0;
    }

    public void setAllowedRadiusMeters(double allowedRadiusMeters) {
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public String getOfficeAddress() {
        return officeAddress != null ? officeAddress : "1st Floor, Pearl Enclave, M.S.E.B. Road, Opposite Safa Bakery, Vishrambag, Sangli, Maharashtra 416416";
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getShiftStartTime() {
        return shiftStartTime != null ? shiftStartTime : "09:00 AM";
    }

    public void setShiftStartTime(String shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public String getShiftEndTime() {
        return shiftEndTime != null ? shiftEndTime : "06:00 PM";
    }

    public void setShiftEndTime(String shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
