/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import service.ReportService;
import utility.Constants;
import utility.ValidationException;

/**
 * UC-08: Manager xem danh sách, xem chi tiết, cập nhật trạng thái và phản hồi báo cáo ẩn danh.
 * Tuyệt đối không import Model hoặc DAO (tuân thủ Rule_BOMS.md)
 *
 * @author AD
 */
public class ReportManagementController extends HttpServlet {

    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        Map<String, String> userSession = getUserSession(session);
        if (userSession == null || !reportService.canManageReports(userSession.get("role"))) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        forwardFlashMessages(request, session);

        String reportId = request.getParameter("reportId");
        if (reportId != null) {
            showDetail(request, response, session, reportId, userSession);
        } else {
            showList(request, response, userSession);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        Map<String, String> userSession = getUserSession(session);
        if (userSession == null || !reportService.canManageReports(userSession.get("role"))) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String reportId = request.getParameter("reportId");
        try {
            reportService.updateReport(reportId, request.getParameter("newStatus"), request.getParameter("txtFeedback"), userSession);
            // Bước 11: Báo thành công và quay về danh sách
            session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_REPORT);
            response.sendRedirect(request.getContextPath() + "/ReportManagementController");
        } catch (ValidationException e) {
            // 8.0.E1: Ở lại màn hình chi tiết, giữ nguyên dữ liệu đã nhập và tô sáng ô lỗi
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            showDetail(request, response, session, reportId, userSession);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response, Map<String, String> userSession)
            throws ServletException, IOException {
        String filterStatus = defaultIfEmpty(request.getParameter("filterStatus"), Constants.REPORT_STATUS_PENDING);
        String filterDate = defaultIfEmpty(request.getParameter("filterDate"), "");
        String filterCategory = defaultIfEmpty(request.getParameter("filterCategory"), "");
        try {
            request.setAttribute("REPORT_LIST", reportService.getReportsForView(userSession, filterStatus, filterDate, filterCategory));
            request.setAttribute("CATEGORY_LIST", reportService.getCategoriesForView());
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
        request.setAttribute("CURRENT_FILTER_STATUS", filterStatus);
        request.setAttribute("CURRENT_FILTER_DATE", filterDate);
        request.setAttribute("CURRENT_FILTER_CATEGORY", filterCategory);
        request.getRequestDispatcher("report_management.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, HttpSession session,
            String reportId, Map<String, String> userSession) throws ServletException, IOException {
        try {
            request.setAttribute("REPORT_DETAIL", reportService.getReportDetailForView(reportId, userSession));
            request.getRequestDispatcher("report_detail.jsp").forward(request, response);
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            response.sendRedirect(request.getContextPath() + "/ReportManagementController");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getUserSession(HttpSession session) {
        return (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    private void forwardFlashMessages(HttpServletRequest request, HttpSession session) {
        moveSessionToRequest(request, session, Constants.SUCCESS_MESSAGE);
        moveSessionToRequest(request, session, Constants.ERROR_MESSAGE);
    }

    private void moveSessionToRequest(HttpServletRequest request, HttpSession session, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    @Override
    public String getServletInfo() {
        return "Report Management Controller (UC-08)";
    }
}
