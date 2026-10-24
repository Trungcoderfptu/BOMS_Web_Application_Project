package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import service.DeliveryService;
import utility.Constants;
import utility.ImageUploadUtility;
import utility.FlashMessageUtil;
import utility.ValidationException;

@WebServlet(name = "ShipperController", urlPatterns = {"/ShipperController"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 10 * 1024 * 1024,       // 10 MB
        maxRequestSize = 15 * 1024 * 1024     // 15 MB
)
public class ShipperController extends HttpServlet {

    private DeliveryService deliveryService;

    @Override
    public void init() throws ServletException {
        deliveryService = new DeliveryService();
    }

    // =========================================================
    // 1. MAIN CONTROLLER (Nhạc trưởng điều phối luồng)
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!validateSessionAndRole(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        FlashMessageUtil.forwardFlashMessages(request, session);

        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        String currentShipper = loginUser.get("username");

        loadDashboardData(request, currentShipper);

        request.getRequestDispatcher("shipper_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!validateSessionAndRole(request, response)) {
            return;
        }

        Map<String, String> loginUser = (Map<String, String>) request.getSession(false).getAttribute(Constants.USER_SESSION);
        String currentShipper = loginUser.get("username");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        try {
            if ("claimTask".equals(action)) {
                processClaimTask(request, session, currentShipper);
            } else if ("acceptTask".equals(action)) {
                processAcceptTask(request, session, currentShipper);
            } else if ("rejectTask".equals(action)) {
                processRejectTask(request, session, currentShipper);
            } else if ("completeDelivery".equals(action)) {
                processCompleteDelivery(request, session, currentShipper);
            } else if ("failDelivery".equals(action)) {
                processFailDelivery(request, session, currentShipper);
            } else if ("updateStatus".equals(action)) {
                processUpdateStatus(request, session, currentShipper);
            } else {
                throw new ValidationException(Constants.ERROR_INVALID_ACTION);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        response.sendRedirect(request.getContextPath() + "/ShipperController");
    }

    // =========================================================
    // 2. HELPER: BẢO VỆ VÀ RẼ NHÁNH
    // =========================================================
    private boolean validateSessionAndRole(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            response.sendRedirect("login.jsp");
            return false;
        }

        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        String role = loginUser.get("role");

        if (Constants.ROLE_MANAGER.equalsIgnoreCase(role) || Constants.ROLE_STAFF.equalsIgnoreCase(role)) {
            response.sendRedirect("DispatchController");
            return false;
        } else if (!Constants.ROLE_SHIPPER.equalsIgnoreCase(role)) {
            request.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_LOGIN_FAIL_ROLE);
            request.getRequestDispatcher("delivery_unauthorized.jsp").forward(request, response);
            return false;
        }
        return true;
    }

    // =========================================================
    // 3. HELPER: NẠP DỮ LIỆU VIEW
    // =========================================================
    private void loadDashboardData(HttpServletRequest request, String currentShipper) {
        try {
            request.setAttribute("AVAILABLE_TASKS", deliveryService.getAvailableTasksForShipper(currentShipper));
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        List<Map<String, String>> allTasks = new ArrayList<>();
        try {
            allTasks = deliveryService.getTasksForShipper(currentShipper);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        List<Map<String, String>> pendingTasks = new ArrayList<>();
        List<Map<String, String>> myTasks = new ArrayList<>();

        for (Map<String, String> task : allTasks) {
            if (Constants.STATUS_WAITING_DELIVERY.equals(task.get("status"))) {
                pendingTasks.add(task);
            } else {
                myTasks.add(task);
            }
        }

        request.setAttribute("PENDING_TASKS", pendingTasks);
        request.setAttribute("MY_TASKS", myTasks);
    }

    // =========================================================
    // 4. HELPER: XỬ LÝ LOGIC NÚT BẤM (Băm nhỏ)
    // =========================================================
    private void processClaimTask(HttpServletRequest request, HttpSession session, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        deliveryService.claimTask(taskIdStr, currentShipper);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CLAIM_TASK);
    }

    private void processAcceptTask(HttpServletRequest request, HttpSession session, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        deliveryService.acceptTask(taskIdStr, currentShipper);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ACCEPT_TASK);
    }

    private void processRejectTask(HttpServletRequest request, HttpSession session, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        deliveryService.rejectTask(taskIdStr, currentShipper);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REJECT_TASK);
    }

    private void processCompleteDelivery(HttpServletRequest request, HttpSession session, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        String proofImage = null;
        try {
            Part filePart = request.getPart("proofImage");
            if (filePart != null && filePart.getSize() > 0) {
                proofImage = ImageUploadUtility.saveImage(filePart, "delivery");
            }
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            // Part read exception fallback
        }
        deliveryService.updateShipperTaskStatus(taskIdStr, currentShipper, Constants.STATUS_COMPLETED, null, proofImage);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_COMPLETE_DELIVERY);
    }

    private void processFailDelivery(HttpServletRequest request, HttpSession session, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        String failReason = request.getParameter("failReason");
        deliveryService.updateShipperTaskStatus(taskIdStr, currentShipper, Constants.STATUS_FAILED, failReason, null);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_FAIL_DELIVERY);
    }

    private void processUpdateStatus(HttpServletRequest request, HttpSession session, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        String newStatus = request.getParameter("txtStatus");
        deliveryService.updateShipperTaskStatus(taskIdStr, currentShipper, newStatus);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_DELIVERY_STATUS);
    }

}
