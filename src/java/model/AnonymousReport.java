/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng AnonymousReports (UC-07, UC-08)
 * KHÔNG có thuộc tính UserID người gửi -> đảm bảo ẩn danh
 *
 * @author AD
 */
public class AnonymousReport {

    private int reportId;
    private String ticketId;
    private int categoryId;
    private String title;
    private String description;
    private String status;
    private String feedback;
    private Integer handledBy;
    private Timestamp submittedAt;
    private Timestamp updatedAt;

    // Chỉ dùng hiển thị, không lưu trực tiếp vào bảng AnonymousReports
    private String categoryName;
    private int evidenceCount;
    private List<ReportEvidence> evidences;

    public AnonymousReport() {
        this.evidences = new ArrayList<>();
    }

    public AnonymousReport(int reportId, String ticketId, int categoryId, String title, String description, String status, String feedback, Integer handledBy, Timestamp submittedAt, Timestamp updatedAt) {
        this.reportId = reportId;
        this.ticketId = ticketId;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.feedback = feedback;
        this.handledBy = handledBy;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
        this.evidences = new ArrayList<>();
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Integer handledBy) {
        this.handledBy = handledBy;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getEvidenceCount() {
        return evidenceCount;
    }

    public void setEvidenceCount(int evidenceCount) {
        this.evidenceCount = evidenceCount;
    }

    public List<ReportEvidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(List<ReportEvidence> evidences) {
        this.evidences = evidences;
    }
}
