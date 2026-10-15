/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Entity đại diện cho bảng ReportEvidences (UC-07, UC-08)
 *
 * @author AD
 */
public class ReportEvidence {

    private int evidenceId;
    private int reportId;
    private String fileName;
    private String filePath;
    private String fileType;
    private long fileSize;

    public ReportEvidence() {
    }

    public ReportEvidence(int evidenceId, int reportId, String fileName, String filePath, String fileType, long fileSize) {
        this.evidenceId = evidenceId;
        this.reportId = reportId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public int getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(int evidenceId) {
        this.evidenceId = evidenceId;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
