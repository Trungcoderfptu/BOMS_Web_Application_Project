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
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import service.ReportService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

/**
 * UC-07: Nhân viên gửi báo cáo ẩn danh kèm bằng chứng và tra cứu phản hồi theo Ticket ID.
 * Cấu hình multipart (giới hạn 20MB/file) được khai báo trong web.xml.
 * Tuyệt đối không import Model hoặc DAO (tuân thủ Rule_BOMS.md)
 *
 * @author AD
 */
public class ReportController extends HttpServlet {

    private final String EVIDENCE_FIELD = "evidenceFiles";
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
        FlashMessageUtil.forwardFlashMessages(request, session);
        moveSessionToRequest(request, session, Constants.LAST_TICKET_ID);

        String ticketId = request.getParameter("ticketId");
        if (ticketId != null) {
            request.setAttribute(Constants.ACTIVE_TAB, "track");
            lookupTicket(request, ticketId);
        }
        loadCategories(request);
        request.getRequestDispatcher("report.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            List<Part> files = collectEvidenceParts(request);
            String ticketId = reportService.submitReport(
                    request.getParameter("categoryId"),
                    request.getParameter("txtTitle"),
                    request.getParameter("txtDescription"),
                    files);
            HttpSession session = request.getSession();
            session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_SUBMIT_REPORT);
            session.setAttribute(Constants.LAST_TICKET_ID, ticketId);
            response.sendRedirect(request.getContextPath() + "/ReportController");
        } catch (ValidationException e) {
            forwardBackWithError(request, response, e.getMessage());
        } catch (IllegalStateException e) {
            // Tomcat ném lỗi này khi file vượt max-file-size hoặc request vượt max-request-size (7.0.E1)
            forwardBackWithError(request, response, Constants.ERROR_REPORT_FILE_TOO_LARGE);
        }
    }

    private List<Part> collectEvidenceParts(HttpServletRequest request) throws IOException, ServletException {
        List<Part> files = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (EVIDENCE_FIELD.equals(part.getName())
                    && part.getSubmittedFileName() != null
                    && !part.getSubmittedFileName().isEmpty()) {
                files.add(part);
            }
        }
        return files;
    }

    private void lookupTicket(HttpServletRequest request, String ticketId) {
        try {
            request.setAttribute("TICKET_RESULT", reportService.getReportByTicketForView(ticketId));
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
    }

    private void loadCategories(HttpServletRequest request) {
        try {
            request.setAttribute("CATEGORY_LIST", reportService.getCategoriesForView());
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
    }

    private void forwardBackWithError(HttpServletRequest request, HttpServletResponse response, String errorCode)
            throws ServletException, IOException {
        request.setAttribute(Constants.ERROR_MESSAGE, errorCode);
        loadCategories(request);
        request.getRequestDispatcher("report.jsp").forward(request, response);
    }

    private void moveSessionToRequest(HttpServletRequest request, HttpSession session, String key) {
        if (session == null || request == null) {
            return;
        }
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    @Override
    public String getServletInfo() {
        return "Anonymous Report Controller (UC-07)";
    }
}
