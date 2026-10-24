/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.AnonymousReport;
import model.ReportCategory;
import model.ReportEvidence;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object cho Báo cáo ẩn danh (UC-07, UC-08)
 * Bảng: ReportCategories, CategoryManagers, AnonymousReports, ReportEvidences
 *
 * @author AD
 */
public class ReportDAO extends DBContext {

    private final String SELECT_REPORT_BASE
            = "SELECT r.ReportID, r.TicketID, r.CategoryID, c.CategoryName, r.Title, r.Description, r.Status, "
            + "r.Feedback, r.HandledBy, r.SubmittedAt, r.UpdatedAt, "
            + "(SELECT COUNT(*) FROM ReportEvidences e WHERE e.ReportID = r.ReportID) AS EvidenceCount "
            + "FROM AnonymousReports r JOIN ReportCategories c ON r.CategoryID = c.CategoryID ";

    // ==========================================
    // ========== 1. CATEGORY & MANAGER =========
    // ==========================================
    public List<ReportCategory> getAllCategories() throws ValidationException {
        List<ReportCategory> list = new ArrayList<>();
        String sql = "SELECT CategoryID, CategoryName FROM ReportCategories ORDER BY CategoryName";
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new ReportCategory(rs.getInt("CategoryID"), rs.getString("CategoryName")));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
        return list;
    }

    public boolean existsCategory(int categoryId) throws ValidationException {
        String sql = "SELECT 1 FROM ReportCategories WHERE CategoryID = ?";
        return existsById(sql, categoryId);
    }

    public List<Integer> getManagerIdsByCategory(int categoryId) throws ValidationException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT ManagerID FROM CategoryManagers WHERE CategoryID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, categoryId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("ManagerID"));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
        return list;
    }

    public boolean isManagerOfCategory(int managerId, int categoryId) throws ValidationException {
        String sql = "SELECT 1 FROM CategoryManagers WHERE ManagerID = ? AND CategoryID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, managerId);
            st.setInt(2, categoryId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
    }

    // ==========================================
    // ========== 2. INSERT (UC-07) =============
    // ==========================================
    public boolean existsTicketId(String ticketId) throws ValidationException {
        String sql = "SELECT 1 FROM AnonymousReports WHERE TicketID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, ticketId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
    }

    /**
     * Lưu báo cáo và toàn bộ bằng chứng trong cùng 1 transaction.
     * Nếu 1 bước lỗi thì rollback toàn bộ.
     */
    public int insertReportWithEvidences(AnonymousReport report, List<ReportEvidence> evidences) throws ValidationException {
        try {
            connection.setAutoCommit(false);
            int reportId = insertReport(report);
            if (evidences != null) {
                for (ReportEvidence evidence : evidences) {
                    insertEvidence(reportId, evidence);
                }
            }
            connection.commit();
            return reportId;
        } catch (Exception e) {
            rollbackQuietly();
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_REPORT);
        } finally {
            restoreAutoCommit();
        }
    }

    private int insertReport(AnonymousReport report) throws SQLException {
        String sql = "INSERT INTO AnonymousReports (TicketID, CategoryID, Title, Description, Status, SubmittedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, report.getTicketId());
            st.setInt(2, report.getCategoryId());
            st.setString(3, report.getTitle());
            st.setString(4, report.getDescription());
            st.setString(5, report.getStatus());
            st.setTimestamp(6, report.getSubmittedAt());
            st.executeUpdate();
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No generated key for AnonymousReports");
    }

    private void insertEvidence(int reportId, ReportEvidence evidence) throws SQLException {
        String sql = "INSERT INTO ReportEvidences (ReportID, FileName, FilePath, FileType, FileSize) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, reportId);
            st.setString(2, evidence.getFileName());
            st.setString(3, evidence.getFilePath());
            st.setString(4, evidence.getFileType());
            st.setLong(5, evidence.getFileSize());
            st.executeUpdate();
        }
    }

    // ==========================================
    // ========== 3. QUERY (UC-08) ==============
    // ==========================================
    /**
     * Lọc báo cáo theo trạng thái, ngày gửi, loại báo cáo.
     *
     * @param managerId null = xem tất cả (Admin), có giá trị = chỉ loại được phân công
     * @param status null = tất cả trạng thái
     * @param submittedDate định dạng yyyy-MM-dd, null = mọi ngày
     * @param categoryId null = mọi loại
     */
    public List<AnonymousReport> filterReports(Integer managerId, String status, String submittedDate, Integer categoryId) throws ValidationException {
        StringBuilder sql = new StringBuilder(SELECT_REPORT_BASE).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (managerId != null) {
            sql.append("AND r.CategoryID IN (SELECT CategoryID FROM CategoryManagers WHERE ManagerID = ?) ");
            params.add(managerId);
        }
        if (status != null) {
            sql.append("AND r.Status = ? ");
            params.add(status);
        }
        if (submittedDate != null) {
            sql.append("AND CAST(r.SubmittedAt AS DATE) = ? ");
            params.add(java.sql.Date.valueOf(submittedDate));
        }
        if (categoryId != null) {
            sql.append("AND r.CategoryID = ? ");
            params.add(categoryId);
        }
        sql.append("ORDER BY r.SubmittedAt DESC");

        List<AnonymousReport> list = new ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReport(rs));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
        return list;
    }

    public AnonymousReport getReportById(int reportId) throws ValidationException {
        String sql = SELECT_REPORT_BASE + "WHERE r.ReportID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, reportId);
            return querySingleReport(st);
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
    }

    public AnonymousReport getReportByTicketId(String ticketId) throws ValidationException {
        String sql = SELECT_REPORT_BASE + "WHERE r.TicketID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, ticketId);
            return querySingleReport(st);
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
    }

    public List<ReportEvidence> getEvidencesByReportId(int reportId) throws ValidationException {
        List<ReportEvidence> list = new ArrayList<>();
        String sql = "SELECT * FROM ReportEvidences WHERE ReportID = ? ORDER BY EvidenceID";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, reportId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEvidence(rs));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
        return list;
    }

    public ReportEvidence getEvidenceById(int evidenceId) throws ValidationException {
        String sql = "SELECT * FROM ReportEvidences WHERE EvidenceID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, evidenceId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? mapEvidence(rs) : null;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
    }

    // ==========================================
    // ========== 4. UPDATE (UC-08) =============
    // ==========================================
    public boolean updateReportStatus(int reportId, String status, String feedback, int handledBy) throws ValidationException {
        String sql = "UPDATE AnonymousReports SET Status = ?, Feedback = ?, HandledBy = ?, UpdatedAt = ? WHERE ReportID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, status);
            if (feedback == null) {
                st.setNull(2, java.sql.Types.NVARCHAR);
            } else {
                st.setString(2, feedback);
            }
            st.setInt(3, handledBy);
            st.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            st.setInt(5, reportId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_REPORT);
        }
    }

    // ==========================================
    // ========== 5. HELPERS ====================
    // ==========================================
    private boolean existsById(String sql, int id) throws ValidationException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_REPORT);
        }
    }

    private AnonymousReport querySingleReport(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            return rs.next() ? mapReport(rs) : null;
        }
    }

    private AnonymousReport mapReport(ResultSet rs) throws SQLException {
        AnonymousReport report = new AnonymousReport();
        report.setReportId(rs.getInt("ReportID"));
        report.setTicketId(rs.getString("TicketID"));
        report.setCategoryId(rs.getInt("CategoryID"));
        report.setCategoryName(rs.getString("CategoryName"));
        report.setTitle(rs.getString("Title"));
        report.setDescription(rs.getString("Description"));
        report.setStatus(rs.getString("Status"));
        report.setFeedback(rs.getString("Feedback"));
        int handledBy = rs.getInt("HandledBy");
        report.setHandledBy(rs.wasNull() ? null : handledBy);
        report.setSubmittedAt(rs.getTimestamp("SubmittedAt"));
        report.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
        report.setEvidenceCount(rs.getInt("EvidenceCount"));
        return report;
    }

    private ReportEvidence mapEvidence(ResultSet rs) throws SQLException {
        return new ReportEvidence(
                rs.getInt("EvidenceID"),
                rs.getInt("ReportID"),
                rs.getString("FileName"),
                rs.getString("FilePath"),
                rs.getString("FileType"),
                rs.getLong("FileSize"));
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            java.util.logging.Logger.getLogger(ReportDAO.class.getName()).log(java.util.logging.Level.WARNING, "Failed to rollback transaction", ignored);
        }
    }

    private void restoreAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ignored) {
            java.util.logging.Logger.getLogger(ReportDAO.class.getName()).log(java.util.logging.Level.WARNING, "Failed to restore auto-commit", ignored);
        }
    }
}
