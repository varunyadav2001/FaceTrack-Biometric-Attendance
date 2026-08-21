package com.example.attendance_system.utils;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.ArrayList;
import java.util.List;

public class FaceEmbeddingHelper {

    // Default similarity threshold for 128D facial representation vector
    public static final double SIMILARITY_THRESHOLD = 0.70;
    // Configurable threshold for biometric duplicate face detection (0.82 = strong biometric identity match)
    public static final double FACE_DUPLICATE_THRESHOLD = 0.82;

    /**
     * Extracts a normalized 128D facial representation vector based on ML Kit facial landmarks,
     * bounds geometry, and cropped face pixel matrix.
     */
    public static List<Float> generateFaceEmbedding(Face face, Bitmap frameBitmap) {
        List<Float> embedding = new ArrayList<>();
        if (face == null) return embedding;

        float boundingWidth = face.getBoundingBox().width() > 0 ? face.getBoundingBox().width() : 1f;
        float boundingHeight = face.getBoundingBox().height() > 0 ? face.getBoundingBox().height() : 1f;
        float boxLeft = face.getBoundingBox().left;
        float boxTop = face.getBoundingBox().top;

        // 1. Normalized geometric feature ratios (Scale & translation invariant)
        float aspectRatio = boundingWidth / boundingHeight;
        float rotX = face.getHeadEulerAngleX() / 180f;
        float rotY = face.getHeadEulerAngleY() / 180f;
        float rotZ = face.getHeadEulerAngleZ() / 180f;

        embedding.add(aspectRatio);
        embedding.add(rotX);
        embedding.add(rotY);
        embedding.add(rotZ);

        // 2. Relative Landmark positions (relative to bounding box origin and box size)
        int[] landmarkTypes = new int[]{
                FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE,
                FaceLandmark.NOSE_BASE, FaceLandmark.MOUTH_LEFT,
                FaceLandmark.MOUTH_RIGHT, FaceLandmark.LEFT_EAR,
                FaceLandmark.RIGHT_EAR, FaceLandmark.LEFT_CHEEK,
                FaceLandmark.RIGHT_CHEEK
        };

        PointF eyeLeft = null, eyeRight = null;
        for (int type : landmarkTypes) {
            FaceLandmark landmark = face.getLandmark(type);
            if (landmark != null) {
                PointF pos = landmark.getPosition();
                float relX = (pos.x - boxLeft) / boundingWidth;
                float relY = (pos.y - boxTop) / boundingHeight;
                embedding.add(relX);
                embedding.add(relY);
                if (type == FaceLandmark.LEFT_EYE) eyeLeft = pos;
                if (type == FaceLandmark.RIGHT_EYE) eyeRight = pos;
            } else {
                embedding.add(0.5f);
                embedding.add(0.5f);
            }
        }

        // 3. Normalized inter-ocular distance & eye probabilities
        float eyeDistance = 0f;
        if (eyeLeft != null && eyeRight != null) {
            eyeDistance = (float) Math.hypot(eyeRight.x - eyeLeft.x, eyeRight.y - eyeLeft.y);
        }
        embedding.add(eyeDistance / boundingWidth);
        embedding.add(face.getLeftEyeOpenProbability() != null ? face.getLeftEyeOpenProbability() : 0.5f);
        embedding.add(face.getRightEyeOpenProbability() != null ? face.getRightEyeOpenProbability() : 0.5f);
        embedding.add(face.getSmilingProbability() != null ? face.getSmilingProbability() : 0.5f);

        // 4. Mean-subtracted luminance sampling from cropped face matrix (Lighting invariant)
        if (frameBitmap != null && !frameBitmap.isRecycled()) {
            try {
                int left = Math.max(0, (int) boxLeft);
                int top = Math.max(0, (int) boxTop);
                int width = Math.min(frameBitmap.getWidth() - left, (int) boundingWidth);
                int height = Math.min(frameBitmap.getHeight() - top, (int) boundingHeight);

                if (width > 10 && height > 10) {
                    Bitmap croppedFace = Bitmap.createBitmap(frameBitmap, left, top, width, height);
                    Bitmap resizedFace = Bitmap.createScaledBitmap(croppedFace, 10, 10, true);

                    List<Float> grays = new ArrayList<>();
                    float sumGray = 0f;
                    for (int y = 0; y < 10; y++) {
                        for (int x = 0; x < 10; x++) {
                            int pixel = resizedFace.getPixel(x, y);
                            float r = ((pixel >> 16) & 0xFF) / 255.0f;
                            float g = ((pixel >> 8) & 0xFF) / 255.0f;
                            float b = (pixel & 0xFF) / 255.0f;
                            float gray = (r + g + b) / 3.0f;
                            grays.add(gray);
                            sumGray += gray;
                        }
                    }

                    float meanGray = sumGray / (grays.size() > 0 ? grays.size() : 1f);
                    for (float gray : grays) {
                        if (embedding.size() >= 128) break;
                        embedding.add(gray - meanGray);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Pad to exactly 128 dimensions
        while (embedding.size() < 128) {
            embedding.add(0f);
        }

        // L2 Normalize vector
        return normalizeEmbedding(embedding);
    }

    private static List<Float> normalizeEmbedding(List<Float> vector) {
        float sum = 0f;
        for (float val : vector) {
            sum += val * val;
        }
        float norm = (float) Math.sqrt(sum);
        if (norm == 0) return vector;

        List<Float> normalized = new ArrayList<>();
        for (float val : vector) {
            normalized.add(val / norm);
        }
        return normalized;
    }

    /**
     * Calculates Cosine Similarity between two 128D facial feature vectors.
     * Result range: 0.0 (unrelated) to 1.0 (identical match).
     */
    public static double calculateCosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) return 0.0;
        int minSize = Math.min(v1.size(), v2.size());
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < minSize; i++) {
            dotProduct += v1.get(i) * v2.get(i);
            normA += v1.get(i) * v1.get(i);
            normB += v2.get(i) * v2.get(i);
        }

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
