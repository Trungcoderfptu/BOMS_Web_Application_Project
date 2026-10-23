/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.hr;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Map;
import service.UserService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ImageUploadUtility;
import utility.ValidationException;

/**
 * Controller điều phối thông tin hồ sơ người dùng và ảnh đại diện
 * Tuân thủ Rule_BOMS.md: Không import Model/DAO, PRG pattern, FlashMessageUtil.
 *
 * @author AD
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 10 * 1024 * 1024,       // 10 MB
        maxRequestSize = 15 * 1024 * 1024     // 15 MB
)
public class ProfileController extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (loginUser == null || loginUser.get("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        FlashMessageUtil.forwardFlashMessages(request, session);

        try {
            int userId = Integer.parseInt(loginUser.get("userId"));
            Map<String, String> profile = userService.getUserProfile(userId);

            request.setAttribute(Constants.USER_PROFILE, profile);
            request.getRequestDispatcher("profile.jsp").forward(request, response);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (loginUser == null || loginUser.get("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int userId = Integer.parseInt(loginUser.get("userId"));
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "uploadAvatar";
        }

        try {
            if ("deleteAvatar".equals(action)) {
                String oldAvatar = loginUser.get("avatar");
                userService.deleteAvatar(userId);
                if (oldAvatar != null && !oldAvatar.trim().isEmpty()) {
                    ImageUploadUtility.deleteImage(oldAvatar);
                }
                loginUser.put("avatar", "");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_DELETE_AVATAR);
            } else {
                Part filePart = getPartSafely(request, "fileAvatar");
                if (filePart == null || filePart.getSize() <= 0) {
                    throw new ValidationException(Constants.ERROR_EMPTY_AVATAR);
                }
                String oldAvatar = loginUser.get("avatar");
                String uploadedPath = ImageUploadUtility.saveImage(filePart, "avatars");
                if (uploadedPath == null) {
                    throw new ValidationException(Constants.ERROR_IMAGE_UPLOAD_FAILED);
                }
                userService.updateAvatar(userId, uploadedPath);
                if (oldAvatar != null && !oldAvatar.trim().isEmpty()) {
                    ImageUploadUtility.deleteImage(oldAvatar);
                }
                loginUser.put("avatar", uploadedPath);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_AVATAR);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_IMAGE_UPLOAD_FAILED);
        }

        response.sendRedirect(request.getContextPath() + "/ProfileController");
    }

    private Part getPartSafely(HttpServletRequest request, String partName) {
        try {
            return request.getPart(partName);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Profile and Avatar Controller";
    }
}
