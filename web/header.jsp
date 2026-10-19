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
                    <c:if test="${not empty sessionScope.USER_SESSION and (sessionScope.USER_SESSION.role == 'Staff' or sessionScope.USER_SESSION.role == 'Manager' or sessionScope.USER_SESSION.role == 'Admin')}">
                        <button id="btnInventoryAlertBell" type="button" class="btn-icon-alert" title="<fmt:message key='alert.bell.title' />" onclick="openInventoryAlertModal()">
                            🔔<span id="inventoryAlertBadge" class="alert-badge d-none">0</span>
                        </button>
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
                                    ${sessionScope.USER_SESSION.username.substring(0,2).toUpperCase()}
                                </button>
                                <div id="menuTaiKhoan">
                                    <strong>${sessionScope.USER_SESSION.fullName}</strong>
                                    <hr>
                                    <a href="${pageContext.request.contextPath}/ProfileController"><fmt:message key="header.menu.profile" /></a><br><br>
                                    <a href="${pageContext.request.contextPath}/AttendanceController"><fmt:message key="header.sidebar.attendance" /></a><br><br>
                                    <a href="${pageContext.request.contextPath}/SettingsController"><fmt:message key="header.menu.settings" /></a><br><br>
                                    <a href="${pageContext.request.contextPath}/LogoutController" class="text-danger"><fmt:message key="header.menu.logout" /></a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </header>

        <c:if test="${not empty sessionScope.USER_SESSION and (sessionScope.USER_SESSION.role == 'Staff' or sessionScope.USER_SESSION.role == 'Manager' or sessionScope.USER_SESSION.role == 'Admin')}">
            <jsp:include page="components/inventory_alert_modal.jsp" />
        </c:if>

        <aside id="menuDoc">
            <button id="btnDongMenuDoc">✖ <fmt:message key="header.sidebar.close" /></button>
            <ul class="no-bullet">
                <li><a href="${pageContext.request.contextPath}/index.jsp"><fmt:message key="header.sidebar.deshbard" /></a></li>
                    <c:if test="${not empty sessionScope.USER_SESSION}">
                    <li><a href="${pageContext.request.contextPath}/AttendanceController"><fmt:message key="header.sidebar.attendance" /></a></li>
                    </c:if>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Admin' or sessionScope.USER_SESSION.role == 'Manager'}">
                    <li><a href="${pageContext.request.contextPath}/AdminDashboardController"><fmt:message key="header.sidebar.hr" /></a></li>
                    <li><a href="#"><fmt:message key="header.sidebar.reports" /></a></li>
                    <li><a href="#"><fmt:message key="header.sidebar.hr" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/ReportManagementController"><fmt:message key="header.sidebar.reports" /></a></li>
                    </c:if>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Staff' or sessionScope.USER_SESSION.role == 'Manager'}">
                    <li><a href="${pageContext.request.contextPath}/ReportController"><fmt:message key="header.sidebar.anonymous_report" /></a></li>
                    </c:if>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Staff' or sessionScope.USER_SESSION.role == 'Manager'}">
                    <li><a href="${pageContext.request.contextPath}/OrderManagementController"><fmt:message key="header.sidebar.order" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/MenuManagementController"><fmt:message key="header.sidebar.menu_management" /></a></li>
                    <li><a href="${pageContext.request.contextPath}/InventoryManagementController"><fmt:message key="header.sidebar.inventory" /></a></li>
                    </c:if>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Shipper' or sessionScope.USER_SESSION.role == 'Manager'}">
                    <li><a href="#"><fmt:message key="header.sidebar.shipper" /></a></li>
                    </c:if>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Admin'}">
                    <li><a href="${pageContext.request.contextPath}/AdminDashboardController"><fmt:message key="header.sidebar.admin_daskboard" /></a></li>
                    </c:if>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Staff'}">
                    <li><a href="${pageContext.request.contextPath}/MenuController"><fmt:message key="header.sidebar.menu" /></a></li>
                    </c:if>
            </ul>
        </aside>