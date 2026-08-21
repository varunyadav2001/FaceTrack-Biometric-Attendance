package com.example.attendance_system.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRCodeUtils {

    public static Bitmap generateQRCode(String text, int width, int height) {
        if (text == null || text.trim().isEmpty()) return null;
        
        try {
            com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            int actualWidth = bitMatrix.getWidth();
            int actualHeight = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            
            for (int x = 0; x < actualWidth; x++) {
                for (int y = 0; y < actualHeight; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createAttendanceQRPayload(String subjectId, String teacherUid, String date) {
        return "ATTENDANCE_QR|" + subjectId + "|" + teacherUid + "|" + date + "|" + System.currentTimeMillis();
    }
}
