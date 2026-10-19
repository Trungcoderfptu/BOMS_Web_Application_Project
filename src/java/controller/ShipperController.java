package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.DeliveryService;
import utility.Constants;
import utility.ValidationException;

@WebServlet(name = "ShipperController", urlPatterns = {"/ShipperController"})
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

        // Gọi ông bảo vệ ra soi thẻ, nếu fail thì dừng luôn
        if (!validateSessionAndRole(request, response)) {
            return;
        }

        Map<String, String> loginUser = (Map<String, String>) request.getSession(false).getAttribute(Constants.USER_SESSION);
        String currentShipper = loginUser.get("username");

        // Nhờ bộ phận kho nạp dữ liệu vào request
        loadDashboardData(request, currentShipper);

        // Đẩy ra View
        request.getRequestDispatcher("shipper_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Gọi ông bảo vệ
        if (!validateSessionAndRole(request, response)) {
            return;
        }

        Map<String, String> loginUser = (Map<String, String>) request.getSession(false).getAttribute(Constants.USER_SESSION);
        String currentShipper = loginUser.get("username");
        String action = request.getParameter("action");

        // Nhạc trưởng rẽ nhánh thao tác (Không để logic lằng nhằng ở đây)
        try {
            if ("acceptTask".equals(action)) {
                processAcceptTask(request, currentShipper);
            } else if ("rejectTask".equals(action)) {
                processRejectTask(request, currentShipper);
            } else {
                processUpdateStatus(request, currentShipper);
            }
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        // Làm xong việc thì gọi lại doGet để tải lại trang mượt mà
        doGet(request, response);
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

        if ("Manager".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role)) {
            response.sendRedirect("DispatchController");
            return false;
        } else if (!"Shipper".equalsIgnoreCase(role)) {
            request.setAttribute(Constants.ERROR_MESSAGE, "error.unauthorized");
            request.getRequestDispatcher("delivery_unauthorized.jsp").forward(request, response);
            return false;
        }
        return true;
    }

    // =========================================================
    // 3. HELPER: NẠP DỮ LIỆU VIEW
    // =========================================================
    private void loadDashboardData(HttpServletRequest request, String currentShipper) {
        List<Map<String, String>> allTasks = new ArrayList<>();
        try {
            allTasks = deliveryService.getTasksForShipper(currentShipper);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        List<Map<String, String>> pendingTasks = new ArrayList<>();
        List<Map<String, String>> myTasks = new ArrayList<>();

        for (Map<String, String> task : allTasks) {
            if (Constants.STATUS_PENDING.equals(task.get("status"))) {
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
    private void processAcceptTask(HttpServletRequest request, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        deliveryService.updateShipperTaskStatus(taskIdStr, currentShipper, Constants.STATUS_DELIVERING);
        request.setAttribute(Constants.SUCCESS_MESSAGE, "shipper.msg.accept_success");
    }

    private void processRejectTask(HttpServletRequest request, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        deliveryService.rejectTask(taskIdStr, currentShipper);
        request.setAttribute(Constants.SUCCESS_MESSAGE, "shipper.msg.reject_success");
    }

    private void processUpdateStatus(HttpServletRequest request, String currentShipper) throws ValidationException {
        String taskIdStr = request.getParameter("txtTaskId");
        String newStatus = request.getParameter("txtStatus");
        deliveryService.updateShipperTaskStatus(taskIdStr, currentShipper, newStatus);
        request.setAttribute(Constants.SUCCESS_MESSAGE, "shipper.msg.update_success");
    }
}
