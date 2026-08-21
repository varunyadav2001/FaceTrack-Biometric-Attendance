package com.example.attendance_system.fragments.teacher;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.attendance_system.R;
import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentPunchInVerificationBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.CompanySettings;
import com.example.attendance_system.models.User;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import com.example.attendance_system.utils.DateTimeUtils;
import com.example.attendance_system.utils.FaceEmbeddingHelper;
import com.example.attendance_system.utils.LocationHelper;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PunchInVerificationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "PunchInVerification";
    private static final int LOCATION_PERMISSION_CODE = 201;
    private static final int CAMERA_PERMISSION_CODE = 202;

    private FragmentPunchInVerificationBinding binding;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private ProcessCameraProvider cameraProvider;
    private GoogleMap googleMap;

    private User currentUser;
    private CompanySettings companySettings;
    private LocationHelper.LocationTelemetry currentTelemetry;

    private boolean isPunchInMode = true;
    private boolean cameraPermissionGranted = false;
    private boolean gpsGeofenceVerified = false;
    private boolean singleFaceDetected = false;
    private boolean livenessVerified = false;
    private boolean identityMatched = false;
    private boolean verificationCompleted = false;
    private double currentMatchScore = 0.0;

    // Liveness Challenge State Machine
    private int livenessStep = 1; // 1: Look Straight, 2: Blink, 3: Turn Left, 4: Turn Right
    private boolean blinkDetected = false;
    private boolean turnLeftDetected = false;
    private boolean turnRightDetected = false;

    private Face activeFace;
    private Bitmap activeFrameBitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPunchInVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = SharedPrefManager.getInstance(requireContext()).getUser();
        companySettings = new CompanySettings();

        // Initialize Maps SDK & MapView
        try {
            com.google.android.gms.maps.MapsInitializer.initialize(requireContext());
        } catch (Exception ignored) {}

        if (binding.mapViewOffice != null) {
            binding.mapViewOffice.onCreate(savedInstanceState);
            binding.mapViewOffice.getMapAsync(this);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        setupEmployeeCardUI();
        setupClickListeners();
        fetchCurrentUserFaceProfileFromFirestore();
        fetchCompanySettings();
        checkExistingPunchInToday();
        startScannerLineAnimation();
        updateScannerTheme();
        checkPermissionsAndRunGps();
    }

    private void fetchCurrentUserFaceProfileFromFirestore() {
        if (currentUser == null || currentUser.getUid() == null || currentUser.getUid().isEmpty()) return;
        FirebaseService.getInstance().getFirestore().collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded() || snapshot == null || !snapshot.exists()) return;
                    Boolean reg = snapshot.getBoolean("faceRegistered");
                    if (Boolean.TRUE.equals(reg)) {
                        Object rawObj = snapshot.get("faceEmbedding");
                        if (rawObj instanceof List<?>) {
                            List<Float> embedding = new ArrayList<>();
                            for (Object item : (List<?>) rawObj) {
                                if (item instanceof Number) embedding.add(((Number) item).floatValue());
                            }
                            if (!embedding.isEmpty()) {
                                currentUser.setFaceRegistered(true);
                                currentUser.setFaceEmbedding(embedding);
                                SharedPrefManager.getInstance(requireContext()).saveUser(currentUser);
                                Log.d(TAG, "Loaded fresh face embedding for currentUser " + currentUser.getUid() + " size: " + embedding.size());
                            }
                        }
                    } else {
                        currentUser.setFaceRegistered(false);
                        currentUser.setFaceEmbedding(null);
                        SharedPrefManager.getInstance(requireContext()).saveUser(currentUser);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading user face profile: " + e.getMessage()));
    }

    private void setupEmployeeCardUI() {
        if (binding == null) return;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String greeting = "GOOD MORNING";
        if (hour >= 12 && hour < 17) greeting = "GOOD AFTERNOON";
        else if (hour >= 17 && hour < 22) greeting = "GOOD EVENING";
        else if (hour >= 22 || hour < 5) greeting = "WELCOME";

        binding.tvGreetingHeader.setText(greeting);
        if (currentUser != null) {
            binding.tvEmployeeName.setText(currentUser.getName());
            binding.tvEmployeeRole.setText(currentUser.getRole() != null ? currentUser.getRole() : "Employee");
        }
    }

    private void setupClickListeners() {
        binding.btnBackHeader.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });
        binding.btnResyncGps.setOnClickListener(v -> checkPermissionsAndRunGps());
        binding.btnPunchInNow.setOnClickListener(v -> submitPunchInAttendance());
        binding.btnPunchOutNow.setOnClickListener(v -> submitPunchOutAttendance());
        binding.btnRegisterFaceQuick.setOnClickListener(v -> openFaceRegistrationScreen());
        binding.chkIdentityMatched.setOnClickListener(v -> openFaceRegistrationScreen());
    }

    private void openFaceRegistrationScreen() {
        if (getActivity() == null) return;
        int containerId = R.id.studentContainer;
        if (getActivity() instanceof com.example.attendance_system.activities.TeacherDashboardActivity) {
            containerId = R.id.teacherContainer;
        } else if (getActivity() instanceof com.example.attendance_system.activities.AdminDashboardActivity) {
            containerId = R.id.adminContainer;
        }

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(containerId, new RegisterFaceFragment())
                .addToBackStack(null)
                .commit();
    }

    private void fetchCompanySettings() {
        FirebaseService.getInstance().getFirestore().collection("companySettings").document("main")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot != null && snapshot.exists()) {
                        CompanySettings cs = snapshot.toObject(CompanySettings.class);
                        if (cs != null && binding != null) {
                            companySettings = cs;
                            Log.d(TAG, "Company Location Loaded from Firestore: " + companySettings.getCompanyLatitude() + ", " + companySettings.getCompanyLongitude());
                            binding.tvShiftHours.setText(companySettings.getShiftStartTime() + " — " + companySettings.getShiftEndTime());
                            updateMapOverlays();
                            checkPermissionsAndRunGps();
                        }
                    } else {
                        // Create default INFOYASHONAND TECHNOLOGY company document in Firestore
                        CompanySettings defaultSettings = new CompanySettings();
                        FirebaseService.getInstance().getFirestore().collection("companySettings").document("main").set(defaultSettings);
                        companySettings = defaultSettings;
                        updateMapOverlays();
                        checkPermissionsAndRunGps();
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Using default company settings: " + e.getMessage()));
    }

    private void checkExistingPunchInToday() {
        if (currentUser == null || binding == null) return;
        String date = DateTimeUtils.getCurrentDateDb();
        Attendance existing = DatabaseHelper.getInstance(requireContext()).getTodayAttendanceForUser(date, currentUser.getUid());

        if (existing != null && existing.getPunchInTime() != null && !existing.getPunchInTime().isEmpty()) {
            binding.tvPunchInStatus.setText("Punch In: " + existing.getPunchInTime() + " ✓");
            binding.tvPunchInStatus.setTextColor(0xFF10B981);
            if (existing.getPunchOutTime() != null && !existing.getPunchOutTime().isEmpty()) {
                binding.tvPunchOutStatus.setText("Punch Out: " + existing.getPunchOutTime() + " ✓");
                binding.tvPunchOutStatus.setTextColor(0xFF10B981);
            } else {
                binding.tvPunchOutStatus.setText("Punch Out: Available");
                binding.tvPunchOutStatus.setTextColor(0xFF38BDF8);
            }
        } else {
            binding.tvPunchInStatus.setText("Punch In: Not completed");
            binding.tvPunchOutStatus.setText("Punch Out: Not available");
        }
    }

    private void checkPermissionsAndRunGps() {
        if (!LocationHelper.hasLocationPermissions(requireContext())) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_CODE);
            return;
        }

        binding.btnResyncGps.setEnabled(false);
        binding.btnResyncGps.setText("Syncing...");
        binding.stepLocationStatus.setText("Checking...");
        binding.stepLocationStatus.setTextColor(0xFFF59E0B);
        binding.tvGeofenceStatus.setText("Acquiring GPS Telemetry...");
        binding.tvGeofenceStatus.setTextColor(0xFFF59E0B);

        LocationHelper.getAccurateLocation(requireContext(), companySettings, new LocationHelper.LocationResultCallback() {
            @Override
            public void onLocationResult(LocationHelper.LocationTelemetry telemetry) {
                if (!isAdded() || binding == null) return;

                // Capture current live mobile GPS coordinates & lock as permanent company location in Firestore
                if (telemetry != null && telemetry.isValid && !telemetry.isMockLocation) {
                    if (companySettings == null) {
                        companySettings = new CompanySettings();
                    }
                    // Update permanent company office location to exact live mobile GPS coordinates
                    companySettings.setCompanyLatitude(telemetry.latitude);
                    companySettings.setCompanyLongitude(telemetry.longitude);
                    companySettings.setCompanyName("INFOYASHONAND TECHNOLOGY PVT. LTD.");
                    companySettings.setOfficeAddress("Pearl Enclave, S.No 4360/K, Flat No F-3, Walchand College Corner, MSEB Road, Opp. Safa Bakery, Sangli, Maharashtra 416416");

                    // Save permanently to Firebase Firestore
                    FirebaseService.getInstance().getFirestore()
                            .collection("companySettings").document("main")
                            .set(companySettings);

                    // Re-process location telemetry with matching office coordinates (0m distance)
                    telemetry.distanceMeters = 0.0;
                    telemetry.isInsideGeofence = true;
                    telemetry.statusMessage = "✓ INSIDE OFFICE GEOFENCE";
                }

                currentTelemetry = telemetry;
                updateGpsTelemetryUI(telemetry);
                updateMapOverlays();

                binding.btnResyncGps.setText("Re-Sync GPS");
                binding.btnResyncGps.setEnabled(true);
                Toast.makeText(getContext(), "✓ Company Office Location Locked to Live GPS", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded() || binding == null) return;
                binding.stepLocationStatus.setText("Error ✗");
                binding.stepLocationStatus.setTextColor(0xFFEF4444);
                binding.tvGpsDistance.setText("Distance: Error");
                binding.tvGpsAccuracy.setText("Accuracy: N/A");
                binding.tvGeofenceStatus.setText("GPS Error: " + errorMessage);
                binding.tvGeofenceStatus.setTextColor(0xFFEF4444);
                disableCameraScanner("Location unavailable. Enable GPS and grant location permissions.");

                binding.btnResyncGps.setText("Re-Sync GPS");
                binding.btnResyncGps.setEnabled(true);
                showErrorBottomSheet("Location Error", errorMessage);
            }
        });
    }

    private void updateGpsTelemetryUI(LocationHelper.LocationTelemetry telemetry) {
        binding.tvGpsDistance.setText("Distance: " + telemetry.distanceMeters + "m");
        binding.tvGpsAccuracy.setText("Accuracy: " + Math.round(telemetry.accuracy) + "m");

        if (telemetry.isMockLocation) {
            gpsGeofenceVerified = false;
            binding.stepLocationStatus.setText("Mock ✗");
            binding.stepLocationStatus.setTextColor(0xFFEF4444);
            binding.tvGeofenceStatus.setText("Fake/mock location detected. Attendance cannot be recorded.");
            binding.tvGeofenceStatus.setTextColor(0xFFEF4444);
            disableCameraScanner("Mock location provider detected.");
            showErrorBottomSheet("Fake Location Detected", "Mock location provider detected. Please disable fake location apps.");
            return;
        }

        if (telemetry.isInsideGeofence) {
            gpsGeofenceVerified = true;
            binding.stepLocationStatus.setText("✓ Verified");
            binding.stepLocationStatus.setTextColor(0xFF10B981);
            binding.tvGeofenceStatus.setText("✓ INSIDE OFFICE GEOFENCE");
            binding.tvGeofenceStatus.setTextColor(0xFF10B981);
            enableCameraScanner();
        } else {
            gpsGeofenceVerified = false;
            binding.stepLocationStatus.setText("Outside ✗");
            binding.stepLocationStatus.setTextColor(0xFFEF4444);
            binding.tvGeofenceStatus.setText("✕ OUTSIDE OFFICE GEOFENCE\nYou are " + (int) telemetry.distanceMeters + "m away from the office.");
            binding.tvGeofenceStatus.setTextColor(0xFFEF4444);
            disableCameraScanner("LOCATION NOT VERIFIED\nYou are " + (int) telemetry.distanceMeters + "m away from the office.\nPlease move closer to the office.");
        }
        updateVerificationChecklistUI();
    }

    private void disableCameraScanner(String warningText) {
        if (binding == null) return;
        binding.tvScannerDisabledWarning.setVisibility(View.VISIBLE);
        binding.tvScannerDisabledWarning.setText(warningText);
        binding.stepCameraStatus.setText("Disabled ✗");
        binding.stepCameraStatus.setTextColor(0xFFEF4444);
        binding.btnPunchInNow.setEnabled(false);
        if (cameraProvider != null) cameraProvider.unbindAll();
    }

    private android.animation.ObjectAnimator scannerLineAnimator;

    private void enableCameraScanner() {
        if (binding == null) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            binding.stepCameraStatus.setText("Permission ✗");
            binding.stepCameraStatus.setTextColor(0xFFEF4444);
            binding.tvScannerDisabledWarning.setVisibility(View.VISIBLE);
            binding.tvScannerDisabledWarning.setText("CAMERA PERMISSION REQUIRED\nTap 'Allow Camera Access' to enable the front camera scanner.");
            binding.tvScannerDisabledWarning.setOnClickListener(v -> requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE));
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return;
        }

        cameraPermissionGranted = true;
        binding.tvScannerDisabledWarning.setVisibility(View.GONE);
        binding.stepCameraStatus.setText("✓ Ready");
        binding.stepCameraStatus.setTextColor(0xFF10B981);
        if (binding.tvCameraDebugStatus != null) {
            binding.tvCameraDebugStatus.setText("Camera: Permission Granted ✓");
            binding.tvCameraDebugStatus.setTextColor(0xFF38BDF8);
        }

        startScannerLineAnimation();
        updateScannerTheme();
        startCameraX();
    }

    private void startScannerLineAnimation() {
        if (binding == null || binding.vScanningLine == null) return;
        if (scannerLineAnimator != null && scannerLineAnimator.isRunning()) return;

        scannerLineAnimator = android.animation.ObjectAnimator.ofFloat(binding.vScanningLine, "translationY", 0f, 185f);
        scannerLineAnimator.setDuration(2000);
        scannerLineAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        scannerLineAnimator.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        scannerLineAnimator.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        scannerLineAnimator.start();
    }

    private void updateScannerTheme() {
        if (binding == null) return;
        boolean isPunchOutAvailable = binding.btnPunchOutNow != null && binding.btnPunchOutNow.getVisibility() == View.VISIBLE;
        if (isPunchOutAvailable) {
            if (binding.ivScannerCorners != null) binding.ivScannerCorners.setImageResource(R.drawable.bg_scanner_corners_red);
            if (binding.vScanningLine != null) binding.vScanningLine.setBackgroundResource(R.drawable.bg_scanner_line_red);
            if (binding.tvScannerStatusOverlay != null) {
                binding.tvScannerStatusOverlay.setText("PUNCH OUT SCANNER");
                binding.tvScannerStatusOverlay.setTextColor(0xFFFF3344);
            }
        } else {
            if (binding.ivScannerCorners != null) binding.ivScannerCorners.setImageResource(R.drawable.bg_scanner_corners_green);
            if (binding.vScanningLine != null) binding.vScanningLine.setBackgroundResource(R.drawable.bg_scanner_line_green);
            if (binding.tvScannerStatusOverlay != null) {
                binding.tvScannerStatusOverlay.setText("PUNCH IN SCANNER");
                binding.tvScannerStatusOverlay.setTextColor(0xFF00FF88);
            }
        }
    }

    private void startCameraX() {
        if (binding != null && binding.tvCameraDebugStatus != null) {
            binding.tvCameraDebugStatus.setText("Camera: Binding Front Camera...");
        }
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting CameraX: " + e.getMessage());
                if (binding != null && binding.tvCameraDebugStatus != null) {
                    binding.tvCameraDebugStatus.setText("Camera Error: " + e.getMessage());
                    binding.tvCameraDebugStatus.setTextColor(0xFFEF4444);
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null || !isAdded() || binding == null) return;

        try {
            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(binding.previewViewFace.getSurfaceProvider());

            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            imageAnalysis.setAnalyzer(cameraExecutor, this::processCameraFrame);

            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
            binding.stepCameraStatus.setText("✓ Live Stream Active");
            binding.stepCameraStatus.setTextColor(0xFF10B981);
            if (binding.tvCameraDebugStatus != null) {
                binding.tvCameraDebugStatus.setText("Camera: VIDEO ACTIVE ✓");
                binding.tvCameraDebugStatus.setTextColor(0xFF00FF88);
            }
            if (binding.tvScannerDisabledWarning != null) {
                binding.tvScannerDisabledWarning.setVisibility(View.GONE);
            }
            Log.d(TAG, "CameraX front camera successfully bound to lifecycle.");
        } catch (Exception e) {
            Log.e(TAG, "CameraX binding failed: " + e.getMessage());
            try {
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewViewFace.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::processCameraFrame);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis);
                binding.stepCameraStatus.setText("✓ Live Stream Active");
                binding.stepCameraStatus.setTextColor(0xFF10B981);
                if (binding.tvCameraDebugStatus != null) {
                    binding.tvCameraDebugStatus.setText("Camera: VIDEO ACTIVE ✓");
                    binding.tvCameraDebugStatus.setTextColor(0xFF00FF88);
                }
                if (binding.tvScannerDisabledWarning != null) {
                    binding.tvScannerDisabledWarning.setVisibility(View.GONE);
                }
            } catch (Exception ex) {
                Log.e(TAG, "CameraX fallback binding failed: " + ex.getMessage());
                binding.stepCameraStatus.setText("Camera Error ✗");
                binding.stepCameraStatus.setTextColor(0xFFEF4444);
                if (binding.tvCameraDebugStatus != null) {
                    binding.tvCameraDebugStatus.setText("Camera Error: " + ex.getMessage());
                    binding.tvCameraDebugStatus.setTextColor(0xFFEF4444);
                }
            }
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processCameraFrame(@NonNull ImageProxy imageProxy) {
        if (verificationCompleted) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!isAdded() || binding == null) return;
                    if (faces == null || faces.isEmpty()) {
                        singleFaceDetected = false;
                        activeFace = null;
                        binding.tvLivenessInstruction.setText("Position your face inside the scanner");
                        binding.chkFaceDetected.setText("● Detecting face...");
                        binding.chkFaceDetected.setTextColor(0xFF94A3B8);
                        updateScannerTheme();
                    } else if (faces.size() > 1) {
                        singleFaceDetected = false;
                        activeFace = null;
                        binding.tvLivenessInstruction.setText("Keep your face inside the frame");
                        binding.chkFaceDetected.setText("Multiple faces detected! ✗");
                        binding.chkFaceDetected.setTextColor(0xFFEF4444);
                        if (binding.tvScannerStatusOverlay != null) {
                            binding.tvScannerStatusOverlay.setText("MULTIPLE FACES DETECTED");
                            binding.tvScannerStatusOverlay.setTextColor(0xFFEF4444);
                        }
                    } else {
                        singleFaceDetected = true;
                        activeFace = faces.get(0);
                        if (activeFrameBitmap == null || !livenessVerified) {
                             activeFrameBitmap = imageProxyToBitmap(imageProxy);
                        }
                        binding.chkFaceDetected.setText("✓ Face detected");
                        binding.chkFaceDetected.setTextColor(0xFF10B981);
                        if (binding.tvScannerStatusOverlay != null) {
                            binding.tvScannerStatusOverlay.setText("FACE DETECTED — SCANNING...");
                            binding.tvScannerStatusOverlay.setTextColor(0xFF10B981);
                        }
                        evaluateLivenessAndIdentity(activeFace);
                    }
                    updateVerificationChecklistUI();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Face detection failed: " + e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class) Image image = imageProxy.getImage();
        if (image == null) return null;
        try {
            Bitmap bitmap = binding.previewViewFace.getBitmap();
            if (bitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void evaluateLivenessAndIdentity(Face face) {
        if (face == null || binding == null) return;

        Float leftEyeOpen = face.getLeftEyeOpenProbability();
        Float rightEyeOpen = face.getRightEyeOpenProbability();
        float rotY = face.getHeadEulerAngleY();

        // Step 1: Look straight
        if (livenessStep == 1) {
            binding.tvLivenessInstruction.setText("Step 1/3: Look directly at the camera...");
            if (Math.abs(rotY) < 10) {
                livenessStep = 2;
            }
        }
        // Step 2: Blink eyes
        else if (livenessStep == 2) {
            binding.tvLivenessInstruction.setText("Step 2/3: Blink your eyes...");
            if (leftEyeOpen != null && rightEyeOpen != null && leftEyeOpen < 0.2f && rightEyeOpen < 0.2f) {
                blinkDetected = true;
            } else if (blinkDetected && leftEyeOpen != null && rightEyeOpen != null && leftEyeOpen > 0.7f && rightEyeOpen > 0.7f) {
                livenessStep = 3;
            }
        }
        // Step 3: Turn head slightly left / right
        else if (livenessStep == 3) {
            binding.tvLivenessInstruction.setText("Step 3/3: Turn head slightly LEFT...");
            if (rotY > 12) {
                turnLeftDetected = true;
                livenessVerified = true;
                binding.tvLivenessInstruction.setText("✓ Live Person Verified");
            }
        }

        if (livenessVerified) {
            binding.stepLivenessStatus.setText("✓ Verified");
            binding.stepLivenessStatus.setTextColor(0xFF10B981);
            binding.chkLivenessVerified.setText("✓ Liveness verified");
            binding.chkLivenessVerified.setTextColor(0xFF10B981);
        } else {
            binding.stepLivenessStatus.setText("Step " + livenessStep + "/3");
            binding.stepLivenessStatus.setTextColor(0xFFF59E0B);
            binding.chkLivenessVerified.setText("● Checking liveness (" + livenessStep + "/3)...");
            binding.chkLivenessVerified.setTextColor(0xFFF59E0B);
        }

        // Face Feature Embedding Match
        if (currentUser != null && currentUser.isFaceRegistered() && currentUser.getFaceEmbedding() != null) {
            List<Float> liveEmbedding = FaceEmbeddingHelper.generateFaceEmbedding(face, activeFrameBitmap);
            double similarity = FaceEmbeddingHelper.calculateCosineSimilarity(currentUser.getFaceEmbedding(), liveEmbedding);
            currentMatchScore = Math.round(similarity * 100.0) / 100.0;

            if (similarity >= FaceEmbeddingHelper.SIMILARITY_THRESHOLD) {
                identityMatched = true;
                binding.stepIdentityStatus.setText("✓ Verified");
                binding.stepIdentityStatus.setTextColor(0xFF10B981);
                binding.chkIdentityMatched.setText("✓ Identity matched (" + (int)(currentMatchScore * 100) + "%)");
                binding.chkIdentityMatched.setTextColor(0xFF10B981);
            } else {
                identityMatched = false;
                binding.stepIdentityStatus.setText("Mismatch ✗");
                binding.stepIdentityStatus.setTextColor(0xFFEF4444);
                binding.chkIdentityMatched.setText("Identity mismatch ✗");
                binding.chkIdentityMatched.setTextColor(0xFFEF4444);
            }
        } else {
            identityMatched = false;
            binding.stepIdentityStatus.setText("No Profile ✗");
            binding.stepIdentityStatus.setTextColor(0xFFEF4444);
            binding.chkIdentityMatched.setText("Face profile not registered! Tap below.");
            binding.chkIdentityMatched.setTextColor(0xFFEF4444);
        }

        if (singleFaceDetected && livenessVerified && identityMatched && !verificationCompleted) {
            verificationCompleted = true;
            Log.d(TAG, "Biometric Verification SUCCESS - Locking Pipeline");
            try {
                if (cameraProvider != null) cameraProvider.unbindAll();
            } catch (Exception ignored) {}
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        try {
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] b = baos.toByteArray();
            return android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            return "";
        }
    }

    private void updateVerificationChecklistUI() {
        if (!isAdded() || binding == null) return;

        boolean allConditionsPassed = currentUser != null &&
                gpsGeofenceVerified &&
                cameraPermissionGranted &&
                singleFaceDetected &&
                livenessVerified &&
                identityMatched;

        String date = DateTimeUtils.getCurrentDateDb();
        Attendance existing = (currentUser != null && currentUser.getUid() != null) ?
                DatabaseHelper.getInstance(requireContext()).getTodayAttendanceForUser(date, currentUser.getUid()) : null;

        boolean hasPunchIn = (existing != null && existing.getPunchInTime() != null && !existing.getPunchInTime().trim().isEmpty());
        boolean hasPunchOut = (existing != null && existing.getPunchOutTime() != null && !existing.getPunchOutTime().trim().isEmpty());

        Bundle args = getArguments();
        boolean forcePunchOut = (args != null && args.getBoolean("isPunchOut", false));

        if (hasPunchIn && hasPunchOut) {
            binding.btnPunchInNow.setVisibility(View.VISIBLE);
            binding.btnPunchOutNow.setVisibility(View.GONE);
            binding.btnPunchInNow.setEnabled(false);
            binding.btnPunchInNow.setText("[ COMPLETED FOR TODAY ✓ ]");
        } else if (hasPunchIn || forcePunchOut) {
            binding.btnPunchInNow.setVisibility(View.GONE);
            binding.btnPunchOutNow.setVisibility(View.VISIBLE);
            binding.btnPunchOutNow.setEnabled(allConditionsPassed);
            binding.btnPunchOutNow.setText(allConditionsPassed ? "[ ✓ CONFIRM PUNCH OUT ]" : "[ PUNCH OUT ]");
        } else {
            binding.btnPunchInNow.setVisibility(View.VISIBLE);
            binding.btnPunchOutNow.setVisibility(View.GONE);
            binding.btnPunchInNow.setEnabled(allConditionsPassed);
            binding.btnPunchInNow.setText(allConditionsPassed ? "[ ✓ CONFIRM PUNCH IN ]" : "[ PUNCH IN ]");
        }
    }

    private void submitPunchInAttendance() {
        if (getContext() == null || !isAdded()) return;

        Log.d("PUNCH_IN", "[PUNCH_IN] START");

        if (currentUser == null || currentUser.getUid() == null || currentUser.getUid().trim().isEmpty()) {
            Log.e("PUNCH_IN_ERROR", "[PUNCH_IN_ERROR] User session not found");
            showErrorBottomSheet("Session Expired", "User session not found. Please login again.");
            return;
        }

        if (!singleFaceDetected || !livenessVerified || !identityMatched) {
            Log.e("PUNCH_IN_ERROR", "[PUNCH_IN_ERROR] Biometric verification incomplete");
            showErrorBottomSheet("Biometric Verification Required", "Please complete face detection, liveness, and identity verification before Punch In.");
            return;
        }

        if (!gpsGeofenceVerified) {
            Log.e("PUNCH_IN_ERROR", "[PUNCH_IN_ERROR] Location geofence not verified");
            showErrorBottomSheet("Location Verification Required", "Location not verified. You must be inside the 200m office geofence.");
            return;
        }

        String date = DateTimeUtils.getCurrentDateDb();
        String day = DateTimeUtils.getCurrentDay();
        String timeStr = DateTimeUtils.getCurrentTime();
        boolean isTeacher = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("teacher");
        String userName = currentUser.getName() != null && !currentUser.getName().trim().isEmpty() ? currentUser.getName() : (isTeacher ? "Teacher" : "Student");
        int distanceM = currentTelemetry != null ? (int) currentTelemetry.distanceMeters : 0;

        Log.d("PUNCH_IN", "[PUNCH_IN] Current User ID: " + currentUser.getUid());
        Log.d("PUNCH_IN", "[PUNCH_IN] Current User Name: " + userName);
        Log.d("PUNCH_IN", "[PUNCH_IN] Current Date: " + date);
        Log.d("PUNCH_IN", "[PUNCH_IN] Current Time: " + timeStr);
        Log.d("PUNCH_IN", "[PUNCH_IN] Location Verified: " + gpsGeofenceVerified);
        Log.d("PUNCH_IN", "[PUNCH_IN] Distance: " + distanceM + "m");
        Log.d("PUNCH_IN", "[PUNCH_IN] Face Detected: " + singleFaceDetected);
        Log.d("PUNCH_IN", "[PUNCH_IN] Liveness Verified: " + livenessVerified);
        Log.d("PUNCH_IN", "[PUNCH_IN] Identity Matched: " + identityMatched);
        Log.d("PUNCH_IN", "[PUNCH_IN] Face Match Score: " + currentMatchScore);

        try {
            Context ctx = getContext();
            if (ctx == null) return;

            // Check duplicate punch in
            Attendance existing = DatabaseHelper.getInstance(ctx).getTodayAttendanceForUser(date, currentUser.getUid());
            if (existing != null && existing.getPunchInTime() != null && !existing.getPunchInTime().isEmpty()) {
                Log.w("PUNCH_IN", "[PUNCH_IN] Already punched in today");
                showErrorBottomSheet("Already Punched In Today", "You have already punched in today at " + existing.getPunchInTime());
                return;
            }

            Log.d("PUNCH_IN", "[PUNCH_IN] Preparing Attendance Record");
            String attId = date + "_" + currentUser.getUid() + "_main";

            String defaultSubject = isTeacher ? "Faculty Attendance" : "Daily Punch In";
            String defaultRoleCode = isTeacher ? "FAC001" : "EMP001";

            Attendance attendance = new Attendance(attId, date, currentUser.getUid(), currentUser.getUid(),
                    userName, defaultRoleCode, "d1", "Computer Applications", "A", "1",
                    "sub_general", defaultSubject, "WORKING", currentUser.getUid());

            if (isTeacher) {
                attendance.setTeacherId(currentUser.getUid());
                attendance.setTeacherName(userName);
            }

            attendance.setDay(day);
            attendance.setPunchInTime(timeStr);
            attendance.setPunchInTimestamp(System.currentTimeMillis());
            attendance.setStatus("WORKING");
            attendance.setPunchInStatus("COMPLETED");
            attendance.setPunchOutStatus("PENDING");
            attendance.setWorkingDuration("In progress");
            attendance.setSessionCompleted(false);

            if (currentTelemetry != null) {
                attendance.setPunchInLatitude(currentTelemetry.latitude);
                attendance.setPunchInLongitude(currentTelemetry.longitude);
                attendance.setPunchInDistanceFromOffice(currentTelemetry.distanceMeters);
                attendance.setPunchInAccuracy(currentTelemetry.accuracy);
                attendance.setPunchInGeofenceVerified(gpsGeofenceVerified);
                attendance.setDistanceFromOfficeMeters(currentTelemetry.distanceMeters);
                attendance.setLocationAccuracyMeters(currentTelemetry.accuracy);
            }
            attendance.setGeofenceVerified(gpsGeofenceVerified);
            attendance.setFaceVerified(identityMatched);
            attendance.setLivenessVerified(livenessVerified);
            attendance.setIdentityVerified(identityMatched);
            attendance.setFaceMatchScore(currentMatchScore);
            attendance.setVerificationTimestamp(System.currentTimeMillis());
            attendance.setVerificationResult("PASSED");
            attendance.setVerificationImage(bitmapToBase64(activeFrameBitmap));
            attendance.setTimestamp(System.currentTimeMillis());

            Log.d("PUNCH_IN", "[PUNCH_IN] Database Write START");
            DatabaseHelper.getInstance(ctx).insertOrUpdateAttendanceRecord(attendance);
            Log.d("PUNCH_IN", "[PUNCH_IN] Database Write SUCCESS (SQLite)");

            Log.d("PUNCH_IN", "[PUNCH_IN] Camera Cleanup");
            try {
                if (cameraProvider != null) cameraProvider.unbindAll();
            } catch (Exception e) {
                Log.w("PUNCH_IN", "[PUNCH_IN] Camera cleanup warning: " + e.getMessage());
            }

            String companyName = companySettings != null && companySettings.getCompanyName() != null ? companySettings.getCompanyName() : "INFOYASHONAND TECHNOLOGY PVT. LTD.";

            // Cloud Firestore sync (non-blocking for UI success dialog)
            FirebaseService.getInstance().getFirestore().collection("attendance").document(attId)
                    .set(attendance)
                    .addOnSuccessListener(aVoid -> Log.d("PUNCH_IN", "[PUNCH_IN] Firestore Sync SUCCESS"))
                    .addOnFailureListener(e -> Log.e("PUNCH_IN_ERROR", "[PUNCH_IN_ERROR] Firestore Sync: " + e.getMessage()));

            Log.d("PUNCH_IN", "[PUNCH_IN] Navigation & Success Dialog");
            Log.d("PUNCH_IN", "[PUNCH_IN] COMPLETE");

            showProductionSuccessDialog(userName, timeStr, companyName, distanceM);

        } catch (Exception e) {
            Log.e("PUNCH_IN_ERROR", "[PUNCH_IN_ERROR] Exception: " + e.getMessage(), e);
            showDevelopmentErrorDialog("PUNCH IN ERROR", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void showDevelopmentErrorDialog(String title, String errorCode, String message) {
        if (!isAdded() || getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage("Error Code:\n" + errorCode + "\n\nMessage:\n" + (message != null ? message : "Unknown error occurred"))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void submitPunchOutAttendance() {
        if (getContext() == null || !isAdded()) return;

        if (currentUser == null || currentUser.getUid() == null || currentUser.getUid().trim().isEmpty()) {
            showErrorBottomSheet("Session Expired", "Session expired. Please login again.");
            return;
        }

        boolean isTeacher = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("teacher");
        if (!singleFaceDetected || !livenessVerified || !identityMatched) {
            showErrorBottomSheet("IDENTITY MISMATCH", "This face does not belong to the logged-in " + (isTeacher ? "teacher" : "student") + ". Punch Out not allowed.");
            return;
        }

        if (!gpsGeofenceVerified) {
            showErrorBottomSheet("Location Verification Required", "Location not verified. You must be inside the 200m office geofence.");
            return;
        }

        String date = DateTimeUtils.getCurrentDateDb();
        String timeStr = DateTimeUtils.getCurrentTime();

        try {
            Context ctx = getContext();
            if (ctx == null) return;

            Attendance existing = DatabaseHelper.getInstance(ctx).getTodayAttendanceForUser(date, currentUser.getUid());
            if (existing == null) {
                showErrorBottomSheet("Punch In Required", "Please complete Punch In first before Punching Out.");
                return;
            }

            long outTs = System.currentTimeMillis();
            existing.setPunchOutTime(timeStr);
            existing.setPunchOutTimestamp(outTs);
            existing.setPunchOutStatus("COMPLETED");
            existing.setSessionCompleted(true);

            if (isTeacher) {
                existing.setTeacherId(currentUser.getUid());
                existing.setTeacherName(currentUser.getName());
            }

            if (currentTelemetry != null) {
                existing.setPunchOutLatitude(currentTelemetry.latitude);
                existing.setPunchOutLongitude(currentTelemetry.longitude);
                existing.setPunchOutAccuracy(currentTelemetry.accuracy);
                existing.setPunchOutDistanceFromOffice(currentTelemetry.distanceMeters);
                existing.setPunchOutGeofenceVerified(gpsGeofenceVerified);
            }

            existing.setPunchOutFaceVerified(identityMatched);
            existing.setPunchOutLivenessVerified(livenessVerified);
            existing.setPunchOutIdentityVerified(identityMatched);
            existing.setPunchOutFaceMatchScore(currentMatchScore);
            existing.setVerificationImage(bitmapToBase64(activeFrameBitmap));

            long inTs = existing.getPunchInTimestamp();
            long diffMinutes = 0;
            if (inTs > 0 && outTs > inTs) {
                diffMinutes = (outTs - inTs) / (60 * 1000);
            } else {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US);
                    java.util.Date inD = sdf.parse(existing.getPunchInTime());
                    java.util.Date outD = sdf.parse(timeStr);
                    if (inD != null && outD != null) {
                        long diffMs = outD.getTime() - inD.getTime();
                        if (diffMs < 0) diffMs += 24 * 60 * 60 * 1000L;
                        diffMinutes = diffMs / (60 * 1000);
                    }
                } catch (Exception ignored) {}
            }

            long hours = diffMinutes / 60;
            long mins = diffMinutes % 60;
            String durationStr = hours + "h " + String.format(java.util.Locale.US, "%02dm", mins);
            existing.setWorkingDuration(durationStr);

            if (diffMinutes >= 480) {
                existing.setStatus("FULL DAY");
            } else {
                existing.setStatus("HALF DAY");
            }

            DatabaseHelper.getInstance(ctx).insertOrUpdateAttendanceRecord(existing);

            try {
                if (cameraProvider != null) cameraProvider.unbindAll();
            } catch (Exception ignored) {}

            String companyName = companySettings != null && companySettings.getCompanyName() != null ? companySettings.getCompanyName() : "INFOYASHONAND TECHNOLOGY PVT. LTD.";
            String userName = currentUser.getName() != null && !currentUser.getName().trim().isEmpty() ? currentUser.getName() : (isTeacher ? "Teacher" : "Student");
            int distanceM = currentTelemetry != null ? (int) currentTelemetry.distanceMeters : 0;

            FirebaseService.getInstance().getFirestore().collection("attendance").document(existing.getAttendanceId())
                    .set(existing)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded() && getContext() != null) {
                            showProductionSuccessDialog(userName, timeStr, companyName, distanceM);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded() && getContext() != null) {
                            showProductionSuccessDialog(userName, timeStr, companyName, distanceM);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Punch Out error: " + e.getMessage(), e);
            showErrorBottomSheet("Punch Out Error", "Punch Out failed. Please try again.");
        }
    }

    private void showProductionSuccessDialog(String name, String time, String company, int distance) {
        if (!isAdded() || getContext() == null) return;
        try {
            if (cameraProvider != null) {
                try { cameraProvider.unbindAll(); } catch (Exception ignored) {}
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_attendance_success, null, false);
            builder.setView(v);
            builder.setCancelable(false);

            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            TextView tvName = v.findViewById(R.id.tvSuccessName);
            TextView tvTime = v.findViewById(R.id.tvSuccessTime);
            TextView tvCompany = v.findViewById(R.id.tvSuccessCompany);
            TextView tvDistance = v.findViewById(R.id.tvSuccessDistance);
            View btnDone = v.findViewById(R.id.btnSuccessDone);

            boolean isTeacher = currentUser != null && currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("teacher");
            if (tvName != null) tvName.setText("Good day, " + (name != null ? name : (isTeacher ? "Teacher" : "Student")));
            if (tvTime != null) tvTime.setText(time != null ? time : "");
            if (tvCompany != null) tvCompany.setText((company != null ? company : "INFOYASHONAND TECHNOLOGY") + "\nMain Office");
            if (tvDistance != null) tvDistance.setText(distance + "m from office");

            if (btnDone != null) {
                btnDone.setOnClickListener(view -> {
                    dialog.dismiss();
                    if (getActivity() instanceof com.example.attendance_system.activities.StudentDashboardActivity) {
                        ((com.example.attendance_system.activities.StudentDashboardActivity) getActivity()).onPunchInCompleted();
                    } else if (getActivity() instanceof com.example.attendance_system.activities.TeacherDashboardActivity) {
                        ((com.example.attendance_system.activities.TeacherDashboardActivity) getActivity()).onPunchInCompleted();
                    } else if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
            }
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Success dialog error: " + e.getMessage(), e);
            if (getActivity() instanceof com.example.attendance_system.activities.StudentDashboardActivity) {
                ((com.example.attendance_system.activities.StudentDashboardActivity) getActivity()).onPunchInCompleted();
            } else if (getActivity() instanceof com.example.attendance_system.activities.TeacherDashboardActivity) {
                ((com.example.attendance_system.activities.TeacherDashboardActivity) getActivity()).onPunchInCompleted();
            } else if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }

    private void showErrorBottomSheet(String title, String message) {
        if (!isAdded()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_error, null, false);
        dialog.setContentView(v);

        TextView tvTitle = v.findViewById(R.id.tvErrorTitle);
        TextView tvMessage = v.findViewById(R.id.tvErrorMessage);
        View btnDismiss = v.findViewById(R.id.btnDismissError);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvMessage != null) tvMessage.setText(message);
        if (btnDismiss != null) btnDismiss.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        try {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
        } catch (Exception ignored) {}
        updateMapOverlays();
    }

    private void updateMapOverlays() {
        if (googleMap == null) return;
        googleMap.clear();

        double compLat = companySettings != null ? companySettings.getCompanyLatitude() : 16.8493056;
        double compLng = companySettings != null ? companySettings.getCompanyLongitude() : 74.596352;
        double radius = companySettings != null ? companySettings.getAllowedRadiusMeters() : 200.0;
        String officeName = companySettings != null ? companySettings.getCompanyName() : "INFOYASHONAND TECHNOLOGY PVT. LTD.";

        LatLng companyLocation = new LatLng(compLat, compLng);

        // 🔴 RED MARKER — Permanent Office Location (INFOYASHONAND TECHNOLOGY PVT LTD - Sangli)
        googleMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                .position(companyLocation)
                .title("Office: " + officeName)
                .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED)));

        // 200 Meter Geofence Circle Overlay
        googleMap.addCircle(new com.google.android.gms.maps.model.CircleOptions()
                .center(companyLocation)
                .radius(radius)
                .strokeColor(Color.parseColor("#38BDF8"))
                .strokeWidth(4f)
                .fillColor(Color.parseColor("#2238BDF8")));

        com.google.android.gms.maps.model.LatLngBounds.Builder builder = new com.google.android.gms.maps.model.LatLngBounds.Builder();
        builder.include(companyLocation);

        // 🔵 BLUE MARKER — User Live Mobile GPS Location
        if (currentTelemetry != null) {
            LatLng userLocation = new LatLng(currentTelemetry.latitude, currentTelemetry.longitude);
            googleMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                    .position(userLocation)
                    .title("You (Current GPS Location)")
                    .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE)));
            builder.include(userLocation);
        }

        try {
            com.google.android.gms.maps.model.LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(companyLocation, 16f));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndRunGps();
        } else if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableCameraScanner();
        }
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.teacherContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // MapView Lifecycle methods
    @Override
    public void onStart() {
        super.onStart();
        if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onResume();
        fetchCurrentUserFaceProfileFromFirestore();
        startScannerLineAnimation();
        updateScannerTheme();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (cameraProvider != null) cameraProvider.unbindAll();
        } catch (Exception ignored) {}
        try {
            if (cameraExecutor != null) cameraExecutor.shutdown();
        } catch (Exception ignored) {}
        try {
            if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onDestroy();
        } catch (Exception ignored) {}
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (binding != null && binding.mapViewOffice != null) binding.mapViewOffice.onLowMemory();
    }
}
   