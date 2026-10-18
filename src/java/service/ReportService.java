/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.NotificationDAO;
import dao.ReportDAO;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import model.AnonymousReport;
import model.ReportCategory;
import model.ReportEvidence;
import utility.Constants;
import utility.ImageUploadUtility;
import utility.ValidationException;
import utility.Validator;

/**
 * Service xử lý nghiệp vụ Báo cáo ẩn danh
 * UC-07 Submit Anonymous Report with Evidence
 * UC-08 Approve and Respond to Report
 *
 * @author AD
 */
public class ReportService {

    private ReportDAO reportDAO;
    private NotificationDAO notificationDAO;
    private SimpleDateFormat dateTimeFormat;
    private final String EVIDENCE_SUB_FOLDER = "reports";

    public ReportService() {
        this.reportDAO = new ReportDAO();
        this.notificationDAO = new NotificationDAO();
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    // ==========================================
    // ========== 1. MAIN SERVICE (FACADE) ======
    // ==========================================
    public List<Map<String, Object>> getCategoriesForView() throws ValidationException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ReportCategory c : reportDAO.getAllCategories()) {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryId", c.getCategoryId());
            map.put("categoryName", c.getCategoryName());
            result.add(map);
        }
        return result;
    }

    /**
     * UC-07: Gửi báo cáo ẩn danh. Không nhận thông tin người gửi -> không thể lưu danh tính.
     *
     * @return Ticket ID để người gửi tra cứu phản hồi
     */
    public String submitReport(String categoryIdStr, String title, String description, List<Part> files) throws ValidationException {
        int categoryId = validateCategory(categoryIdStr);
        validateTitle(title);
        validateDescription(description);
        validateEvidenceFiles(files);

        AnonymousReport report = buildNewReport(categoryId, title, description);
        List<ReportEvidence> evidences = saveEvidenceFiles(files);
        int reportId = persistReport(report, evidences);
        notifyCategoryManagers(categoryId, reportId, report.getTicketId());
        return report.getTicketId();
    }

    public Map<String, Object> getReportByTicketForView(String ticketId) throws ValidationException {
        Validator.checkEmpty(ticketId, Constants.ERROR_REPORT_EMPTY_TICKET);
        AnonymousReport report = reportDAO.getReportByTicketId(normalizeTicketId(ticketId));
        if (report == null) {
            throw new ValidationException(Constants.ERROR_REPORT_TICKET_NOT_FOUND);
        }
        return convertReportToMap(report);
    }

    public boolean canManageReports(String role) {
        return isAdmin(role) || isManager(role);
    }

    public List<Map<String, Object>> getReportsForView(Map<String, String> userSession, String status, String submittedDate, String categoryIdStr) throws ValidationException {
        validateManageAccess(userSession);
        Integer managerScope = resolveManagerScope(userSession);
        List<AnonymousReport> reports = reportDAO.filterReports(
                managerScope,
                normalizeStatusFilter(status),
                normalizeDateFilter(submittedDate),
                parseOptionalInt(categoryIdStr));
        return convertReportsToViewList(reports);
    }

    public Map<String, Object> getReportDetailForView(String reportIdStr, Map<String, String> userSession) throws ValidationException {
        AnonymousReport report = validateAndGetAccessibleReport(reportIdStr, userSession);
        Map<String, Object> map = convertReportToMap(report);
        map.put("evidences", convertEvidencesToMapList(reportDAO.getEvidencesByReportId(report.getReportId())));
        map.put("closed", isClosedStatus(report.getStatus()));
        return map;
    }

    /**
     * UC-08: Cập nhật trạng thái và phản hồi. Phản hồi được gắn vào Ticket ID để người gửi tra cứu.
     */
    public void updateReport(String reportIdStr, String newStatus, String feedback, Map<String, String> userSession) throws ValidationException {
        AnonymousReport report = validateAndGetAccessibleReport(reportIdStr, userSession);
        validateReportStillOpen(report);
        validateNewStatus(newStatus);
        String cleanFeedback = normalizeFeedback(feedback);
        validateRejectFeedback(newStatus, cleanFeedback);
        boolean updated = reportDAO.updateReportStatus(report.getReportId(), newStatus, cleanFeedback, parseUserId(userSession));
        if (!updated) {
            throw new ValidationException(Constants.ERROR_REPORT_NOT_FOUND);
        }
    }

    /**
     * 8.2 Download Evidence: kiểm tra quyền TRƯỚC, sau đó mới lấy file trong vùng riêng tư của
     * ImageUploadUtility và trả về thông tin để Controller thiết lập Header.
     */
    public Map<String, Object> getEvidenceForDownload(String evidenceIdStr, Map<String, String> userSession) throws ValidationException {
        ReportEvidence evidence = validateAndGetEvidence(evidenceIdStr);
        validateAndGetAccessibleReport(String.valueOf(evidence.getReportId()), userSession);
        File file = ImageUploadUtility.getPrivateFile(evidence.getFilePath());
        if (file == null || !file.isFile()) {
            throw new ValidationException(Constants.ERROR_EVIDENCE_NOT_FOUND);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("fileName", evidence.getFileName());
        map.put("contentType", ImageUploadUtility.getContentType(file.getName()));
        map.put("fileSize", file.length());
        map.put("storedPath", file.getPath());
        return map;
    }

    public void writeEvidenceTo(Map<String, Object> evidenceInfo, OutputStream out) throws ValidationException {
        try {
            Files.copy(new File((String) evidenceInfo.get("storedPath")).toPath(), out);
        } catch (IOException e) {
            throw new ValidationException(Constants.ERROR_EVIDENCE_NOT_FOUND);
        }
    }

    // ==========================================
    // ========== 2. VALIDATION UC-07 ===========
    // ==========================================
    private int validateCategory(String categoryIdStr) throws ValidationException {
        int categoryId = Validator.checkInteger(categoryIdStr, Constants.ERROR_REPORT_EMPTY_CATEGORY);
        if (!reportDAO.existsCategory(categoryId)) {
            throw new ValidationException(Constants.ERROR_REPORT_EMPTY_CATEGORY);
        }
        return categoryId;
    }

    private void validateTitle(String title) throws ValidationException {
        Validator.checkEmpty(title, Constants.ERROR_REPORT_EMPTY_TITLE);
        if (title.trim().length() > Constants.REPORT_TITLE_MAX_LENG) {
            throw new ValidationException(Constants.ERROR_REPORT_TITLE_TOO_LONG);
        }
    }

    private void validateDescription(String description) throws ValidationException {
        Validator.checkEmpty(description, Constants.ERROR_REPORT_EMPTY_DESCRIPTION);
    }

    private void validateEvidenceFiles(List<Part> files) throws ValidationException {
        if (files == null || files.isEmpty()) {
            throw new ValidationException(Constants.ERROR_REPORT_NO_EVIDENCE);
        }
        if (files.size() > Constants.REPORT_MAX_EVIDENCE_FILES) {
            throw new ValidationException(Constants.ERROR_REPORT_TOO_MANY_FILES);
        }
        // Kiểm tra định dạng / dung lượng của TẤT CẢ file trước khi lưu bất kỳ file nào (tầng 1 - Utility)
        for (Part file : files) {
            ImageUploadUtility.validateFile(file, ImageUploadUtility.REPORT_EVIDENCE_RULE);
        }
    }

    // ==========================================
    // ========== 3. SUBMIT HELPERS UC-07 =======
    // ==========================================
    private AnonymousReport buildNewReport(int categoryId, String title, String description) throws ValidationException {
        AnonymousReport report = new AnonymousReport();
        report.setTicketId(generateUniqueTicketId());
        report.setCategoryId(categoryId);
        report.setTitle(title.trim());
        report.setDescription(description.trim());
        report.setStatus(Constants.REPORT_STATUS_PENDING);
        report.setSubmittedAt(new Timestamp(System.currentTimeMillis()));
        return report;
    }

    private String generateUniqueTicketId() throws ValidationException {
        String ticketId;
        do {
            ticketId = Constants.REPORT_TICKET_PREFIX
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (reportDAO.existsTicketId(ticketId));
        return ticketId;
    }

    /**
     * Lưu toàn bộ file bằng chứng vào vùng riêng tư của ImageUploadUtility.
     * Nếu 1 file lỗi thì xóa các file đã lưu trước đó để không để lại file rác.
     */
    private List<ReportEvidence> saveEvidenceFiles(List<Part> files) throws ValidationException {
        List<ReportEvidence> evidences = new ArrayList<>();
        try {
            for (Part file : files) {
                evidences.add(saveSingleEvidence(file));
            }
        } catch (ValidationException e) {
            deleteEvidenceFiles(evidences);
            throw e;
        }
        return evidences;
    }

    private ReportEvidence saveSingleEvidence(Part file) throws ValidationException {
        String storedPath = ImageUploadUtility.saveFile(file, EVIDENCE_SUB_FOLDER, ImageUploadUtility.REPORT_EVIDENCE_RULE);
        if (storedPath == null) {
            throw new ValidationException(Constants.ERROR_REPORT_NO_EVIDENCE);
        }
        ReportEvidence evidence = new ReportEvidence();
        evidence.setFileName(extractFileName(file));
        evidence.setFilePath(storedPath);
        evidence.setFileType(extractExtension(storedPath));
        evidence.setFileSize(file.getSize());
        return evidence;
    }

    private int persistReport(AnonymousReport report, List<ReportEvidence> evidences) throws ValidationException {
        try {
            return reportDAO.insertReportWithEvidences(report, evidences);
        } catch (ValidationException e) {
            deleteEvidenceFiles(evidences);
            throw e;
        }
    }

    private void deleteEvidenceFiles(List<ReportEvidence> evidences) {
        for (ReportEvidence evidence : evidences) {
            ImageUploadUtility.deleteFile(evidence.getFilePath());
        }
    }

    /**
     * POST-2: Thông báo cho các Manager phụ trách loại báo cáo (BR-13).
     * Nội dung thông báo là Ticket ID, không chứa danh tính người gửi.
     */
    private void notifyCategoryManagers(int categoryId, int reportId, String ticketId) throws ValidationException {
        for (Integer managerId : reportDAO.getManagerIdsByCategory(categoryId)) {
            notificationDAO.insertNotification(managerId, Constants.NOTIFICATION_TYPE_REPORT, reportId, ticketId);
        }
    }

    // ==========================================
    // ========== 4. VALIDATION UC-08 ===========
    // ==========================================
    private void validateManageAccess(Map<String, String> userSession) throws ValidationException {
        if (userSession == null || !canManageReports(userSession.get("role"))) {
            throw new ValidationException(Constants.ERROR_REPORT_NO_PERMISSION);
        }
    }

    private AnonymousReport validateAndGetAccessibleReport(String reportIdStr, Map<String, String> userSession) throws ValidationException {
        validateManageAccess(userSession);
        int reportId = Validator.checkInteger(reportIdStr, Constants.ERROR_REPORT_NOT_FOUND);
        AnonymousReport report = reportDAO.getReportById(reportId);
        if (report == null) {
            throw new ValidationException(Constants.ERROR_REPORT_NOT_FOUND);
        }
        validateCategoryAccess(report.getCategoryId(), userSession);
        return report;
    }

    private void validateCategoryAccess(int categoryId, Map<String, String> userSession) throws ValidationException {
        if (isAdmin(userSession.get("role"))) {
            return;
        }
        if (!reportDAO.isManagerOfCategory(parseUserId(userSession), categoryId)) {
            throw new ValidationException(Constants.ERROR_REPORT_NO_PERMISSION);
        }
    }

    private ReportEvidence validateAndGetEvidence(String evidenceIdStr) throws ValidationException {
        int evidenceId = Validator.checkInteger(evidenceIdStr, Constants.ERROR_EVIDENCE_NOT_FOUND);
        ReportEvidence evidence = reportDAO.getEvidenceById(evidenceId);
        if (evidence == null) {
            throw new ValidationException(Constants.ERROR_EVIDENCE_NOT_FOUND);
        }
        return evidence;
    }

    private void validateReportStillOpen(AnonymousReport report) throws ValidationException {
        if (isClosedStatus(report.getStatus())) {
            throw new ValidationException(Constants.ERROR_REPORT_ALREADY_CLOSED);
        }
    }

    private void validateNewStatus(String newStatus) throws ValidationException {
        if (!Constants.REPORT_STATUS_IN_PROGRESS.equals(newStatus)
                && !Constants.REPORT_STATUS_RESOLVED.equals(newStatus)
                && !Constants.REPORT_STATUS_REJECTED.equals(newStatus)) {
            throw new ValidationException(Constants.ERROR_REPORT_INVALID_STATUS);
        }
    }

    /**
     * 8.0.E1: Chọn Rejected bắt buộc phải nhập lý do.
     */
    private void validateRejectFeedback(String newStatus, String feedback) throws ValidationException {
        if (Constants.REPORT_STATUS_REJECTED.equals(newStatus) && feedback == null) {
            throw new ValidationException(Constants.ERROR_REPORT_REJECT_NEED_FEEDBACK);
        }
    }

    // ==========================================
    // ========== 5. DTO/MAP CONVERSION =========
    // ==========================================
    private List<Map<String, Object>> convertReportsToViewList(List<AnonymousReport> reports) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AnonymousReport report : reports) {
            result.add(convertReportToMap(report));
        }
        return result;
    }

    private Map<String, Object> convertReportToMap(AnonymousReport report) {
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", report.getReportId());
        map.put("ticketId", report.getTicketId());
        map.put("categoryId", report.getCategoryId());
        map.put("categoryName", report.getCategoryName());
        map.put("title", report.getTitle());
        map.put("description", report.getDescription());
        map.put("status", report.getStatus());
        map.put("statusKey", toStatusKey(report.getStatus()));
        map.put("feedback", report.getFeedback());
        map.put("submittedAt", formatTimestamp(report.getSubmittedAt()));
        map.put("updatedAt", formatTimestamp(report.getUpdatedAt()));
        map.put("evidenceCount", report.getEvidenceCount());
        return map;
    }

    private List<Map<String, Object>> convertEvidencesToMapList(List<ReportEvidence> evidences) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ReportEvidence e : evidences) {
            Map<String, Object> map = new HashMap<>();
            map.put("evidenceId", e.getEvidenceId());
            map.put("fileName", e.getFileName());
            map.put("fileType", e.getFileType());
            map.put("fileSizeKb", Math.max(1, e.getFileSize() / 1024));
            list.add(map);
        }
        return list;
    }

    /**
     * "In Progress" -> "In_Progress" để dùng làm key trong file properties.
     */
    private String toStatusKey(String status) {
        return status == null ? "" : status.replace(' ', '_');
    }

    private String formatTimestamp(Timestamp ts) {
        return ts == null ? null : dateTimeFormat.format(ts);
    }

    // ==========================================
    // ========== 6. SMALL HELPERS ==============
    // ==========================================
    private boolean isAdmin(String role) {
        return "Admin".equalsIgnoreCase(role);
    }

    private boolean isManager(String role) {
        return "Manager".equalsIgnoreCase(role);
    }

    private boolean isClosedStatus(String status) {
        return Constants.REPORT_STATUS_RESOLVED.equals(status) || Constants.REPORT_STATUS_REJECTED.equals(status);
    }

    /**
     * Admin xem toàn bộ (null). Manager chỉ xem các loại báo cáo được phân công.
     */
    private Integer resolveManagerScope(Map<String, String> userSession) throws ValidationException {
        return isAdmin(userSession.get("role")) ? null : parseUserId(userSession);
    }

    private int parseUserId(Map<String, String> userSession) throws ValidationException {
        return Validator.checkInteger(userSession.get("userId"), Constants.ERROR_REPORT_NO_PERMISSION);
    }

    private String normalizeStatusFilter(String status) {
        if (status == null || status.trim().isEmpty() || Constants.REPORT_FILTER_ALL.equalsIgnoreCase(status.trim())) {
            return null;
        }
        return status.trim();
    }

    private String normalizeDateFilter(String date) {
        if (date == null || !date.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return null;
        }
        return date.trim();
    }

    private Integer parseOptionalInt(String value) {
        try {
            return (value == null || value.trim().isEmpty()) ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeFeedback(String feedback) {
        return (feedback == null || feedback.trim().isEmpty()) ? null : feedback.trim();
    }

    private String normalizeTicketId(String ticketId) {
        return ticketId.trim().toUpperCase(Locale.ROOT);
    }

    private String extractFileName(Part file) {
        String submitted = file.getSubmittedFileName();
        if (submitted == null) {
            return "";
        }
        // Một số trình duyệt gửi kèm đường dẫn đầy đủ -> chỉ lấy tên file.
        // Không dùng Paths.get vì tên chứa ký tự Windows không cho phép (?, *, :...) sẽ gây lỗi 500.
        // Tên gốc chỉ để hiển thị, file trên ổ đĩa được lưu bằng tên UUID.
        String normalized = submitted.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1).trim();
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
