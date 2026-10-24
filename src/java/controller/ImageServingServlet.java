/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import utility.ImageUploadUtility;

/**
 * Servlet chuyên trách phục vụ và phát trực tiếp (stream) tệp tin hình ảnh từ thư mục lưu trữ độc lập.
 * Cho phép các trang View truy cập ảnh qua URL ảo /uploads/* mà không phụ thuộc vào thư mục build của Tomcat.
 *
 * @author AD
 */
@WebServlet(name = "ImageServingServlet", urlPatterns = {"/uploads/*"})
public class ImageServingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File imageFile = ImageUploadUtility.getImageFile(pathInfo);
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(imageFile.getName());
        if (mimeType == null) {
            String lowerName = imageFile.getName().toLowerCase();
            if (lowerName.endsWith(".png")) {
                mimeType = "image/png";
            } else if (lowerName.endsWith(".webp")) {
                mimeType = "image/webp";
            } else {
                mimeType = "image/jpeg";
            }
        }

        response.setContentType(mimeType);
        response.setContentLengthLong(imageFile.length());
        response.setHeader("Cache-Control", "public, max-age=86400"); // Cache 1 ngày để tăng tốc độ tải
        response.setHeader("X-Content-Type-Options", "nosniff"); // Chống MIME-Sniffing XSS

        OutputStream out = response.getOutputStream();
        try (FileInputStream in = new FileInputStream(imageFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    @Override
    public String getServletInfo() {
        return "Image Serving Controller";
    }
}
