package com.example.attendance_system.fragments.teacher;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.attendance_system.database.DatabaseHelper;
import com.example.attendance_system.databinding.FragmentRegisterFaceBinding;
import com.example.attendance_system.firebase.FirebaseService;
import com.example.attendance_system.models.User;
import com.example.attendance_system.utils.FaceEmbeddingHelper;
import com.example.attendance_system.utils.SharedPrefManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterFaceFragment extends Fragment {

    private static final String TAG = "RegisterFaceFragment";
    private static final int CAMERA_PERMISSION_CODE = 101;

    private FragmentRegisterFaceBinding binding;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private ProcessCameraProvider cameraProvider;

    private Face currentDetectedFace;
    private Bitmap currentFrameBitmap;
    private boolean isCapturing = false;
    private List<Float> inMemoryRegisteredEmbedding = null;
    private boolean isFaceRegisteredInDb = false;

    private List<User> allRegisteredUsersWithFaces = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterFaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        faceDetector = FaceDetection.getClient(options);

        binding.btnCaptureAndRegisterFace.setOnClickListener(v -> saveFaceProfile());

        loadAllRegisteredFaceProfilesFromFirebase();

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void loadAllRegisteredFaceProfilesFromFirebase() {
        isFaceRegisteredInDb = false;
        inMemoryRegisteredEmbedding = null;
        allRegisteredUsersWithFaces.clear();

        String currentUid = SharedPrefManager.getInstance(requireContext()).getUserUid();

        FirebaseService.getInstance().getFirestore().collection("users")
                .whereEqualTo("faceRegistered", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || queryDocumentSnapshots == null) return;
                    List<User> list = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null && u.getUid() != null && !u.getUid().trim().isEmpty()) {
                            Object rawObj = doc.get("faceEmbedding");
                            if (rawObj instanceof List<?>) {
                                List<Float> floatList = new ArrayList<>();
                                for (Object item : (List<?>) rawObj) {
                                    if (item instanceof Number) {
                                        floatList.add(((Number) item).floatValue());
                                    }
                                }
                                // STRICT VALIDATION: Must be exactly 128D valid embedding
                                if (floatList.size() == 128) {
                                    u.setFaceEmbedding(floatList);
                                    u.setFaceRegistered(true);

                                    if (currentUid != null && currentUid.equals(u.getUid())) {
                                        isFaceRegisteredInDb = true;
                                        inMemoryRegisteredEmbedding = floatList;
                                    } else {
                                        // Only add OTHER users to duplicate check list
                                        list.add(u);
                                    }
                                }
                            }
                        }
                    }
                    allRegisteredUsersWithFaces = list;
                    Log.d(TAG, "Loaded " + list.size() + " valid registered face profiles for duplicity check. Current User Registered: " + isFaceRegisteredInDb);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching all registered face profiles: " + e.getMessage()));
    }

    private void showFaceAlreadyRegisteredDialog() {
        if (!isAdded()) return;
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("FACE ALREADY REGISTERED")
                .setMessage("Your face profile is already registered.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .show();
    }

    private void showDuplicateFaceDialog() {
        if (!isAdded()) return;
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("FACE ALREADY REGISTERED")
                .setMessage("This face is already registered to another student.\n\nPlease use the correct student's registered face.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(getContext(), "Camera permission is required for face registration.", Toast.LENGTH_LONG).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting CameraX provider: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null || !isAdded()) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewViewRegisterFace.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::processCameraFrame);

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Camera use case binding failed: " + e.getMessage());
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processCameraFrame(@NonNull ImageProxy imageProxy) {
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
                        currentDetectedFace = null;
                        binding.tvRegisterFaceStatus.setText("⚪ FACE NOT DETECTED\nPosition your face inside camera frame.");
                        binding.tvRegisterFaceStatus.setTextColor(0xFF94A3B8);
                        binding.btnCaptureAndRegisterFace.setEnabled(false);
                        binding.btnCaptureAndRegisterFace.setText("[ CAPTURE & SAVE FACE PROFILE ]");
                    } else if (faces.size() > 1) {
                        currentDetectedFace = null;
                        binding.tvRegisterFaceStatus.setText("🔴 MULTIPLE FACES DETECTED\nOnly one face should be visible.");
                        binding.tvRegisterFaceStatus.setTextColor(0xFFEF4444);
                        binding.btnCaptureAndRegisterFace.setEnabled(false);
                        binding.btnCaptureAndRegisterFace.setText("[ CAPTURE & SAVE FACE PROFILE ]");
                    } else {
                        currentDetectedFace = faces.get(0);
                        currentFrameBitmap = imageProxyToBitmap(imageProxy);

                        List<Float> liveEmbedding = FaceEmbeddingHelper.generateFaceEmbedding(currentDetectedFace, currentFrameBitmap);
                        User duplicateMatch = null;
                        double highestSimilarity = 0.0;

                        String currentUid = SharedPrefManager.getInstance(requireContext()).getUserUid();

                        // DUPLICATE CHECK: Only run if liveEmbedding is valid AND database has other valid profiles
                        if (liveEmbedding.size() == 128 && allRegisteredUsersWithFaces != null && !allRegisteredUsersWithFaces.isEmpty()) {
                            for (User registeredUser : allRegisteredUsersWithFaces) {
                                if (registeredUser.getUid() != null && !registeredUser.getUid().equals(currentUid)
                                        && registeredUser.getFaceEmbedding() != null && registeredUser.getFaceEmbedding().size() == 128) {
                                    double similarity = FaceEmbeddingHelper.calculateCosineSimilarity(registeredUser.getFaceEmbedding(), liveEmbedding);
                                    if (similarity >= FaceEmbeddingHelper.FACE_DUPLICATE_THRESHOLD) {
                                        if (similarity > highestSimilarity) {
                                            highestSimilarity = similarity;
                                            duplicateMatch = registeredUser;
                                        }
                                    }
                                }
                            }
                        }

                        if (duplicateMatch != null) {
                            // DUPLICATE FACE REGISTERED TO ANOTHER STUDENT ACCOUNT!
                            binding.tvRegisterFaceStatus.setText("🔴 FACE ALREADY REGISTERED\nThis face is registered to another student.");
                            binding.tvRegisterFaceStatus.setTextColor(0xFFEF4444);
                            binding.btnCaptureAndRegisterFace.setEnabled(false);
                            binding.btnCaptureAndRegisterFace.setText("[ REJECTED: DUPLICATE FACE ]");
                        } else if (isFaceRegisteredInDb) {
                            // SAME STUDENT ACCOUNT (ALLOW UPDATE)
                            binding.tvRegisterFaceStatus.setText("✓ Your face profile is already registered!");
                            binding.tvRegisterFaceStatus.setTextColor(0xFF10B981);
                            binding.btnCaptureAndRegisterFace.setEnabled(true);
                            binding.btnCaptureAndRegisterFace.setText("[ UPDATE FACE PROFILE ]");
                        } else {
                            // NEW UNREGISTERED STUDENT (NO MATCH IN DB)
                            binding.tvRegisterFaceStatus.setText("🟢 Face detected ✓ Ready to capture!");
                            binding.tvRegisterFaceStatus.setTextColor(0xFF10B981);
                            binding.btnCaptureAndRegisterFace.setEnabled(true);
                            binding.btnCaptureAndRegisterFace.setText("[ CAPTURE & SAVE FACE PROFILE ]");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "ML Kit Face Detector failed: " + e.getMessage()))
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class) Image image = imageProxy.getImage();
        if (image == null) return null;
        try {
            Bitmap bitmap = binding.previewViewRegisterFace.getBitmap();
            if (bitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void saveFaceProfile() {
        if (currentDetectedFace == null) {
            Toast.makeText(getContext(), "Please position your face clearly in camera frame", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = SharedPrefManager.getInstance(requireContext()).getUser();
        if (user == null || user.getUid() == null) {
            Toast.makeText(getContext(), "User authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Float> liveEmbedding = FaceEmbeddingHelper.generateFaceEmbedding(currentDetectedFace, currentFrameBitmap);
        if (liveEmbedding.size() != 128) {
            Toast.makeText(getContext(), "Failed to generate face embedding. Retry.", Toast.LENGTH_SHORT).show();
            return;
        }

        // BIOMETRIC UNICITY CHECK AGAINST OTHER REGISTERED FACES
        User duplicateMatch = null;
        if (allRegisteredUsersWithFaces != null && !allRegisteredUsersWithFaces.isEmpty()) {
            for (User registeredUser : allRegisteredUsersWithFaces) {
                if (registeredUser.getUid() != null && !registeredUser.getUid().equals(user.getUid())
                        && registeredUser.getFaceEmbedding() != null && registeredUser.getFaceEmbedding().size() == 128) {
                    double similarity = FaceEmbeddingHelper.calculateCosineSimilarity(registeredUser.getFaceEmbedding(), liveEmbedding);
                    if (similarity >= FaceEmbeddingHelper.FACE_DUPLICATE_THRESHOLD) {
                        duplicateMatch = registeredUser;
                        break;
                    }
                }
            }
        }

        if (duplicateMatch != null) {
            showDuplicateFaceDialog();
            return;
        }

        isCapturing = true;
        binding.tvRegisterFaceStatus.setText("Saving face profile...");
        executeSaveNewFaceProfile(user, liveEmbedding);
    }

    private void executeSaveNewFaceProfile(User user, List<Float> embedding) {
        String faceProfileId = "face_" + user.getUid() + "_" + System.currentTimeMillis();
        long now = System.currentTimeMillis();

        user.setFaceRegistered(true);
        user.setFaceEmbedding(embedding);
        user.setFaceProfileId(faceProfileId);
        user.setRegistrationTimestamp(now);

        isFaceRegisteredInDb = true;
        inMemoryRegisteredEmbedding = embedding;

        // Update local SharedPref & SQLite
        SharedPrefManager.getInstance(requireContext()).saveUser(user);

        // Atomic Save to Firestore
        FirebaseService.getInstance().getFirestore().collection("users").document(user.getUid())
                .update("faceRegistered", true,
                        "faceEmbedding", embedding,
                        "faceProfileId", faceProfileId,
                        "registrationTimestamp", now)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle("FACE REGISTERED SUCCESSFULLY")
                                .setMessage("Your biometric face profile has been linked to your account.\n\nStudent: " + user.getName())
                                .setCancelable(false)
                                .setPositiveButton("BACK TO DASHBOARD", (dialog, which) -> {
                                    dialog.dismiss();
                                    if (getActivity() != null) {
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    }
                                })
                                .show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Face saved locally. Cloud sync: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        binding = null;
    }
}
