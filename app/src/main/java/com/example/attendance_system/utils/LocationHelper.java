package com.example.attendance_system.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.attendance_system.models.CompanySettings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationHelper {

    public static class LocationTelemetry {
        public double latitude;
        public double longitude;
        public float accuracy;
        public double distanceMeters;
        public boolean isInsideGeofence;
        public boolean isMockLocation;
        public String statusMessage;
        public boolean isValid;

        public LocationTelemetry(double latitude, double longitude, float accuracy,
                                 double distanceMeters, boolean isInsideGeofence,
                                 boolean isMockLocation, String statusMessage, boolean isValid) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.distanceMeters = distanceMeters;
            this.isInsideGeofence = isInsideGeofence;
            this.isMockLocation = isMockLocation;
            this.statusMessage = statusMessage;
            this.isValid = isValid;
        }
    }

    public interface LocationResultCallback {
        void onLocationResult(LocationTelemetry telemetry);
        void onError(String errorMessage);
    }

    public static boolean hasLocationPermissions(Context context) {
        if (context == null) return false;
        int fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED;
    }

    public static void getAccurateLocation(Context context, CompanySettings companySettings, LocationResultCallback callback) {
        if (context == null || callback == null) return;

        if (!hasLocationPermissions(context)) {
            callback.onError("Location permissions (FINE/COARSE) not granted");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && (System.currentTimeMillis() - location.getTime()) < 30000) {
                    processLocation(location, companySettings, callback);
                } else {
                    requestFreshLocation(fusedLocationClient, companySettings, callback);
                }
            }).addOnFailureListener(e -> {
                requestFreshLocation(fusedLocationClient, companySettings, callback);
            });
        } catch (SecurityException e) {
            callback.onError("SecurityException accessing location: " + e.getLocalizedMessage());
        }
    }

    private static void requestFreshLocation(FusedLocationProviderClient fusedClient, CompanySettings companySettings, LocationResultCallback callback) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .setMaxUpdates(1)
                .build();

        try {
            fusedClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    fusedClient.removeLocationUpdates(this);
                    if (locationResult.getLastLocation() != null) {
                        processLocation(locationResult.getLastLocation(), companySettings, callback);
                    } else {
                        callback.onError("Location unavailable. Please ensure GPS is enabled.");
                    }
                }
            }, null);
        } catch (SecurityException e) {
            callback.onError("Permission denied requesting location update.");
        }
    }

    public static void processLocation(Location userLocation, CompanySettings companySettings, LocationResultCallback callback) {
        if (userLocation == null) {
            callback.onError("User location is null.");
            return;
        }

        double userLat = userLocation.getLatitude();
        double userLng = userLocation.getLongitude();
        float accuracy = userLocation.getAccuracy();

        boolean isMock = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
            isMock = userLocation.isFromMockProvider();
        }

        if (isMock) {
            LocationTelemetry mockTelemetry = new LocationTelemetry(userLat, userLng, accuracy,
                    9999.0, false, true, "Fake/mock location detected. Attendance cannot be recorded.", false);
            callback.onLocationResult(mockTelemetry);
            return;
        }

        double compLat = companySettings != null ? companySettings.getCompanyLatitude() : 16.8493056;
        double compLng = companySettings != null ? companySettings.getCompanyLongitude() : 74.596352;
        double radius = companySettings != null ? companySettings.getAllowedRadiusMeters() : 200.0;

        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, compLat, compLng, results);
        double distanceMeters = Math.round(results[0] * 10.0) / 10.0;

        boolean isInside = distanceMeters <= radius;
        String statusMsg;
        if (isInside) {
            statusMsg = "Inside " + (int) radius + "m Office Geofence ✓";
        } else {
            statusMsg = "Outside Office Geofence ✗ (" + (int) distanceMeters + "m away)";
        }

        Log.d("LocationHelper", "Company GPS: (" + compLat + ", " + compLng + ")");
        Log.d("LocationHelper", "User GPS: (" + userLat + ", " + userLng + ")");
        Log.d("LocationHelper", "Calculated Distance: " + distanceMeters + "m, Allowed Radius: " + radius + "m");

        LocationTelemetry telemetry = new LocationTelemetry(userLat, userLng, accuracy,
                distanceMeters, isInside, false, statusMsg, true);
        callback.onLocationResult(telemetry);
    }
}
