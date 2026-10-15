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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import service.ReportService;
import utility.Constants;
import utility.ValidationException;

/**
 * UC-08 (8.2 Download Evidence): Tải file bằng chứng về máy Manager.
 * File nằm ngoài thư mục web nên chỉ tải được qua Controller này (có kiểm tra quyền).
 *
 * @author AD
 */
public class EvidenceController extends HttpServlet {

    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        reportService = new ReportService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        try {
            Map<String, Object> evidence = reportService.getEvidenceForDownload(request.getParameter("id"), userSession);
            setDownloadHeaders(response, evidence);
            reportService.writeEvidenceTo(evidence, response.getOutputStream());
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            response.sendRedirect(request.getContextPath() + "/ReportManagementController" + buildBackQuery(request));
        }
    }

    private void setDownloadHeaders(HttpServletResponse response, Map<String, Object> evidence) {
        String encodedName = URLEncoder.encode((String) evidence.get("fileName"), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType((String) evidence.get("contentType"));
        response.setContentLengthLong((Long) evidence.get("fileSize"));
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
    }

    private String buildBackQuery(HttpServletRequest request) {
        String reportId = request.getParameter("reportId");
        return (reportId != null && reportId.matches("\\d+")) ? "?reportId=" + reportId : "";
    }

    @Override
    public String getServletInfo() {
        return "Evidence Download Controller (UC-08 8.2)";
    }
}
