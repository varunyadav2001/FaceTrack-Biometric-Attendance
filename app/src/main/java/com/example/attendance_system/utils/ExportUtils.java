package com.example.attendance_system.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.attendance_system.models.Attendance;
import com.example.attendance_system.models.Student;
import com.example.attendance_system.models.SubjectAttendance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportUtils {

    /**
     * Export attendance record list to PDF using native android.graphics.pdf.PdfDocument.
     * Saves directly to device internal/external storage.
     */
    public static File exportAttendanceToPDF(Context context, List<Attendance> attendanceList, String title) throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 Size
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Title Header
        titlePaint.setColor(Color.parseColor("#1E3A8A"));
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);
        canvas.drawText("Smart Attendance System - Official Report", 40, 50, titlePaint);

        // Subtitle / Filters
        paint.setColor(Color.BLACK);
        paint.setTextSize(13f);
        canvas.drawText("Title: " + (title != null ? title : "Daily Attendance"), 40, 80, paint);
        canvas.drawText("Generated On: " + DateTimeUtils.getCurrentDateDisplay(), 40, 100, paint);

        // Table Header Line
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(40, 120, 555, 120, paint);

        // Table Column Headers
        paint.setFakeBoldText(true);
        paint.setTextSize(12f);
        canvas.drawText("Date", 45, 140, paint);
        canvas.drawText("Roll No", 135, 140, paint);
        canvas.drawText("Student Name", 215, 140, paint);
        canvas.drawText("Subject", 350, 140, paint);
        canvas.drawText("Status", 490, 140, paint);
        canvas.drawLine(40, 150, 555, 150, paint);

        paint.setFakeBoldText(false);
        int yPos = 175;
        int presentCount = 0, absentCount = 0, leaveCount = 0;

        if (attendanceList != null) {
            for (Attendance item : attendanceList) {
                String dateStr = item.getDate() != null ? item.getDate() : DateTimeUtils.getCurrentDateDb();
                String rollStr = item.getRollNo() != null ? item.getRollNo() : "-";
                String nameStr = item.getStudentName() != null ? item.getStudentName() : "Student";
                
                String subjStr = item.getSubjectName();
                if (subjStr == null || subjStr.trim().isEmpty() || "sub1".equalsIgnoreCase(subjStr)) {
                    subjStr = "MCA-401: Adv Java";
                } else if ("sub2".equalsIgnoreCase(subjStr)) {
                    subjStr = "MCA-402: Cloud";
                } else if ("sub3".equalsIgnoreCase(subjStr)) {
                    subjStr = "MCA-403: Data Science";
                }
                
                String statusStr = item.getStatus() != null ? item.getStatus().toUpperCase() : Constants.STATUS_PRESENT;

                canvas.drawText(dateStr, 45, yPos, paint);
                canvas.drawText(rollStr, 135, yPos, paint);
                
                String displayStrName = nameStr.length() > 18 ? nameStr.substring(0, 16) + ".." : nameStr;
                canvas.drawText(displayStrName, 215, yPos, paint);

                String displaySubj = subjStr.length() > 20 ? subjStr.substring(0, 18) + ".." : subjStr;
                canvas.drawText(displaySubj, 350, yPos, paint);

                // Color-Coded Status Column
                Paint statusPaint = new Paint(paint);
                statusPaint.setFakeBoldText(true);
                if (Constants.STATUS_PRESENT.equalsIgnoreCase(statusStr)) {
                    statusPaint.setColor(Color.parseColor("#10B981"));
                    presentCount++;
                } else if (Constants.STATUS_ABSENT.equalsIgnoreCase(statusStr)) {
                    statusPaint.setColor(Color.parseColor("#EF4444"));
                    absentCount++;
                } else {
                    statusPaint.setColor(Color.parseColor("#F59E0B"));
                    leaveCount++;
                }
                canvas.drawText(statusStr, 490, yPos, statusPaint);

                yPos += 24;
                if (yPos > 750) {
                    break;
                }
            }
        }

        // Summary Divider Line
        canvas.drawLine(40, yPos + 5, 555, yPos + 5, paint);
        Paint summaryPaint = new Paint();
        summaryPaint.setTextSize(12f);
        summaryPaint.setFakeBoldText(true);
        summaryPaint.setColor(Color.parseColor("#1E3A8A"));
        canvas.drawText("Summary: Present: " + presentCount + "  |  Absent: " + absentCount + "  |  On Leave: " + leaveCount, 45, yPos + 25, summaryPaint);

        document.finishPage(page);

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "Attendance_Report_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"application/pdf"}, null);
        return file;
    }

    public static File exportStudentAttendanceReport(Context context, Student student, List<SubjectAttendance> subjectAttendanceList, double overallPercent) throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint headerPaint = new Paint();

        // Header
        headerPaint.setColor(Color.parseColor("#1E3A8A"));
        headerPaint.setTextSize(22f);
        headerPaint.setFakeBoldText(true);
        canvas.drawText("STUDENT ATTENDANCE REPORT", 40, 60, headerPaint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(14f);
        paint.setFakeBoldText(true);
        canvas.drawText("Student Name: ", 40, 100, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(student.getName(), 150, 100, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Student ID: ", 40, 120, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(student.getRollNo(), 150, 120, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Department: ", 40, 140, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(student.getDepartmentName(), 150, 140, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Division: ", 40, 160, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(student.getSection(), 150, 160, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Semester: ", 40, 180, paint);
        paint.setFakeBoldText(false);
        canvas.drawText(student.getSemester(), 150, 180, paint);

        // Overall Attendance
        paint.setFakeBoldText(true);
        paint.setTextSize(16f);
        paint.setColor(Color.parseColor("#1E3A8A"));
        canvas.drawText("Overall Attendance: " + String.format("%.1f%%", overallPercent), 40, 220, paint);

        // Table Header
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2f);
        canvas.drawLine(40, 240, 555, 240, paint);
        paint.setTextSize(12f);
        paint.setFakeBoldText(true);
        canvas.drawText("Subject", 45, 260, paint);
        canvas.drawText("Present", 280, 260, paint);
        canvas.drawText("Absent", 350, 260, paint);
        canvas.drawText("Total", 420, 260, paint);
        canvas.drawText("Percentage", 480, 260, paint);
        canvas.drawLine(40, 270, 555, 270, paint);

        paint.setFakeBoldText(false);
        int y = 290;
        for (SubjectAttendance sa : subjectAttendanceList) {
            String name = sa.getSubjectName().length() > 30 ? sa.getSubjectName().substring(0, 28) + ".." : sa.getSubjectName();
            canvas.drawText(name, 45, y, paint);
            canvas.drawText(String.valueOf(sa.getPresentClasses()), 280, y, paint);
            canvas.drawText(String.valueOf(sa.getTotalClasses() - sa.getPresentClasses()), 350, y, paint);
            canvas.drawText(String.valueOf(sa.getTotalClasses()), 420, y, paint);
            canvas.drawText(String.format("%.1f%%", sa.getPercentage()), 480, y, paint);
            y += 25;
            if (y > 800) break;
        }

        document.finishPage(page);

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir, "Student_Attendance_Report_" + student.getRollNo() + "_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream fos = new FileOutputStream(file);
        document.writeTo(fos);
        document.close();
        fos.close();

        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"application/pdf"}, null);
        return file;
    }

    /**
     * Share PDF file via WhatsApp, Gmail, Drive, or external apps.
     */
    public static void sharePDF(Context context, File pdfFile) {
        shareFile(context, pdfFile, "application/pdf", "Smart Attendance PDF Report");
    }

    /**
     * Export attendance record list to Excel-compatible CSV file on device storage.
     */
    public static File exportAttendanceToExcelCSV(Context context, List<Attendance> attendanceList, String title) throws IOException {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "Attendance_Report_" + System.currentTimeMillis() + ".csv");
        FileWriter writer = new FileWriter(file);

        // Header Row
        writer.append("Date,Roll No,Student Name,Subject,Department,Status\n");

        if (attendanceList != null && !attendanceList.isEmpty()) {
            for (Attendance item : attendanceList) {
                String dateStr = item.getDate() != null ? item.getDate() : DateTimeUtils.getCurrentDateDb();
                String rollStr = item.getRollNo() != null ? item.getRollNo() : "-";
                String nameStr = item.getStudentName() != null ? item.getStudentName() : "Student";
                String subjStr = item.getSubjectName();
                if (subjStr == null || subjStr.trim().isEmpty() || "sub1".equalsIgnoreCase(subjStr)) {
                    subjStr = "MCA-401: Adv Java";
                }
                String deptStr = item.getDepartmentId() != null ? item.getDepartmentId() : "MCA";
                String statusStr = item.getStatus() != null ? item.getStatus().toUpperCase() : Constants.STATUS_PRESENT;

                writer.append(escapeCsv(dateStr)).append(",");
                writer.append(escapeCsv(rollStr)).append(",");
                writer.append(escapeCsv(nameStr)).append(",");
                writer.append(escapeCsv(subjStr)).append(",");
                writer.append(escapeCsv(deptStr)).append(",");
                writer.append(escapeCsv(statusStr)).append("\n");
            }
        }

        writer.flush();
        writer.close();

        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{"text/csv"}, null);
        return file;
    }

    /**
     * Share any generated file (PDF or CSV) using Android FileProvider.
     */
    public static void shareFile(Context context, File file, String mimeType, String title) {
        if (file == null || !file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            android.net.Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );

            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Attached is the official " + title + ".");
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share " + title + " via"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error sharing file: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}
