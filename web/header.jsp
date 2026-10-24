<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><fmt:message key="header.title" /></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
    </head>
    <body>
        <header class="app-header">
            <div class="flex-between">
                <div>
                    <button id="btnHamburger">☰</button>
                    <div class="system-banner">
                        <span class="banner-text"><fmt:message key="header.title" /></span>
                    </div>
                </div>
                <div class="flex-align-center">
                    <div>
                        <jsp:include page="components/language_switcher.jsp" />
                        <jsp:include page="components/theme_toggle.jsp" />
                    </div>
                    <c:if test="${not empty sessionScope.USER_SESSION}">
                        <%-- Task 11.4: Nút Phát loa toàn cục (Admin / Manager) --%>
                        <c:if test="${sessionScope.USER_SESSION.role == 'Admin' or sessionScope.USER_SESSION.role == 'Manager'}">
                            <button id="btnBroadcastMegaphone" type="button" class="btn-icon-alert" title="<fmt:message key='broadcast.btn.title' />" data-action="open-broadcast">
                                📢
                            </button>
                        </c:if>

                        <%-- Cảnh báo kho nguyên liệu (Staff / Manager) --%>
                        <c:if test="${sessionScope.USER_SESSION.role == 'Staff' or sessionScope.USER_SESSION.role == 'Manager'}">
                            <button id="btnInventoryAlertBell" type="button" class="btn-icon-alert" title="<fmt:message key='alert.bell.title' />" data-action="open-inventory-alert">
                                ⚠️<span id="inventoryAlertBadge" class="alert-badge d-none">0</span>
                            </button>
                        </c:if>

                        <%-- Task 11.3: Quả chuông Thông báo hệ thống & Dropdown --%>
                        <div class="notification-wrapper position-relative">
                            <button id="btnNotificationBell" type="button" class="btn-icon-alert" title="<fmt:message key='notification.bell.title' />" data-action="toggle-notification-dropdown">
                                🔔<span id="unreadNotifBadge" class="alert-badge d-none">0</span>
                            </button>
                            <jsp:include page="components/notification_dropdown.jsp" />
                        </div>
                    </c:if>
                    <div class="dropdown-wrapper">
                        <c:choose>
                            <c:when test="${empty sessionScope.USER_SESSION}">
                                <%-- Dùng fn:contains để kiểm tra URL. Chỉ hiện nút khi KHÔNG ở trang login hoặc register --%>
                                <c:if test="${not fn:contains(pageContext.request.requestURI, 'login.jsp') and not fn:contains(pageContext.request.requestURI, 'register.jsp')}">
                                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn-login">
                                        <fmt:message key="header.btn.login" />
                                    </a>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <button id="btnAvatar">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.USER_SESSION.avatar}">
                                            <img src="${pageContext.request.contextPath}/${sessionScope.USER_SESSION.avatar}" alt="Avatar" class="header-avatar-img">
                                        </c:when>
                                        <c:otherwise>
                                            ${sessionScope.USER_SESSION.username.substring(0,2).toUpperCase()}
                                        </c:otherwise>
                                    </c:choose>
                                </button>
                                <div id="menuTaiKhoan">
                                    <strong><c:out value="${sessionScope.USER_SESSION.fullName}" /></strong>
                                    <hr>
                                    <a href="${pageContext.request.contextPath}/ProfileController"><fmt:message key="header.menu.profile" /></a><br><br>
                                    <c:if test="${sessionScope.USER_SESSION.role == 'Staff'}">
                                        <a href="${pageContext.request.contextPath}/AttendanceController"><fmt:message key="header.sidebar.attendance" /></a><br><br>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/SettingsController"><fmt:message key="header.menu.settings" /></a><br><br>
                                    <a href="${pageContext.request.contextPath}/LogoutController" class="text-danger"><fmt:message key="header.menu.logout" /></a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </header>

        <c:if test="${not empty sessionScope.USER_SESSION and (sessionScope.USER_SESSION.role == 'Staff' or sessionScope.USER_SESSION.role == 'Manager')}">
            <jsp:include page="components/inventory_alert_modal.jsp" />
        </c:if>
        <c:if test="${not empty sessionScope.USER_SESSION and (sessionScope.USER_SESSION.role == 'Admin' or sessionScope.USER_SESSION.role == 'Manager')}">
            <jsp:include page="components/broadcast_modal.jsp" />
        </c:if>

        <aside id="menuDoc">
            <button id="btnDongMenuDoc">✖ <fmt:message key="header.sidebar.close" /></button>
            <ul class="no-bullet">
                <li><a href="${pageContext.request.contextPath}/DashboardController"><fmt:message key="header.sidebar.deshbard" /></a></li>
                <%-- Admin: Chỉ phụ trách quản lý tài khoản --%>
                <c:if test="${sessionScope.USER_SESSION.role == 'Admin'}">
                    <li><a href="${pageContext.request.contextPath}/AdminDashboardController"><fmt:message key="header.sidebar.admin_daskboard" /></a></li>
                </c:if>

                <%-- Manager: Quản lý đơn hàng, Quản lý thực đơn, Quản lý kho, Quản lý báo cáo, Quản lý nhân sự, Điều phối shipper --%>
                <c:if test="${sessionScope.USER_SESSION.role == 'Manager'}">
                    <li><a href="${pageContext.request.contextPath}/OrderManagementController"><fmt:message key="header.sidebar.order" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/DispatchController"><fmt:message key="header.sidebar.dispatch" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/MenuManagementController"><fmt:message key="header.sidebar.menu_management" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/InventoryManagementController"><fmt:message key="header.sidebar.inventory" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/ReportManagementController"><fmt:message key="header.sidebar.reports" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/HrManagementController"><fmt:message key="header.sidebar.hr" /></a></li>
                </c:if>

                <%-- Staff: Đặt món tại quầy, Quản lý đơn hàng, Tạo thực đơn, Xem kho, Chấm công, Gửi báo cáo ẩn danh --%>
                <c:if test="${sessionScope.USER_SESSION.role == 'Staff'}">
                    <li><a href="${pageContext.request.contextPath}/MenuController"><fmt:message key="header.sidebar.menu" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/OrderManagementController"><fmt:message key="header.sidebar.order" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/MenuManagementController"><fmt:message key="header.sidebar.menu_management" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/InventoryManagementController"><fmt:message key="header.sidebar.inventory" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/AttendanceController"><fmt:message key="header.sidebar.attendance" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/ReportController"><fmt:message key="header.sidebar.anonymous_report" /></a></li>
                </c:if>

                <%-- Shipper: Chỉ thực hiện luồng shipper --%>
                <c:if test="${sessionScope.USER_SESSION.role == 'Shipper'}">
                    <li><a href="${pageContext.request.contextPath}/ShipperController"><fmt:message key="header.sidebar.my_tasks" /></a></li>
                </c:if>
            </ul>
        </aside>