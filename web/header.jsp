<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="role" value="${sessionScope.USER_SESSION.role}" />
<%-- Controller gốc (sau forward) -> biết đang ở trang nào, thuộc phân hệ nào --%>
<c:set var="navPath" value="${not empty requestScope['jakarta.servlet.forward.servlet_path'] ? requestScope['jakarta.servlet.forward.servlet_path'] : pageContext.request.servletPath}" />
<c:choose>
    <c:when test="${navPath == '/MenuController' or navPath == '/OrderManagementController' or navPath == '/DispatchController' or navPath == '/ShipperController'}"><c:set var="module" value="sales" /></c:when>
    <c:when test="${navPath == '/MenuManagementController' or navPath == '/InventoryManagementController' or navPath == '/ReportManagementController' or navPath == '/ReportController'}"><c:set var="module" value="ops" /></c:when>
    <c:when test="${navPath == '/HrManagementController' or navPath == '/AttendanceController'}"><c:set var="module" value="hr" /></c:when>
    <c:when test="${navPath == '/AdminDashboardController'}"><c:set var="module" value="admin" /></c:when>
    <c:otherwise><c:set var="module" value="home" /></c:otherwise>
</c:choose>
<%-- Trang đầu tiên của từng phân hệ theo vai trò (đích của tab phân hệ). Rỗng = vai trò không có phân hệ đó --%>
<c:choose>
    <c:when test="${role == 'Staff'}"><c:set var="salesHome" value="/MenuController" /><c:set var="opsHome" value="/MenuManagementController" /><c:set var="hrHome" value="/AttendanceController" /></c:when>
    <c:when test="${role == 'Manager'}"><c:set var="salesHome" value="/OrderManagementController" /><c:set var="opsHome" value="/MenuManagementController" /><c:set var="hrHome" value="/HrManagementController" /></c:when>
    <c:when test="${role == 'Shipper'}"><c:set var="salesHome" value="/ShipperController" /></c:when>
</c:choose>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><fmt:message key="header.title" /></title>
        <%-- Logo + favicon: ảnh chính thức của Bingxue (tải từ bingxuevietnam.com, lưu tại images/brand) --%>
        <link rel="icon" href="${ctx}/images/brand/bingxue-favicon-32.png">
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&amp;display=swap">
        <%-- Icon: Uicons by Flaticon (Regular Rounded), cần ghi công ở chân trang --%>
        <link rel="stylesheet" href="https://cdn-uicons.flaticon.com/4.0.0/uicons-regular-rounded/css/uicons-regular-rounded.css">
        <link rel="stylesheet" type="text/css" href="${ctx}/css/global.css">
        <link rel="stylesheet" type="text/css" href="${ctx}/css/layout.css">
    </head>
    <%-- Khung H: thanh trên = logo + tab phân hệ + công cụ; trái = menu con của phân hệ đang mở; phải = vùng làm việc --%>
    <body class="${not empty sessionScope.USER_SESSION ? 'has-shell' : 'no-shell'}">
        <header class="app-header topbar">
            <div class="topbar-left">
                <c:if test="${not empty sessionScope.USER_SESSION}">
                    <button id="btnHamburger" type="button" aria-label="<fmt:message key='nav.menu' />" aria-controls="menuDoc" aria-expanded="false"><i class="fi fi-rr-menu-burger" aria-hidden="true"></i></button>
                </c:if>
                <a href="${ctx}/DashboardController" class="topbar-brand">
                    <span class="brand-mark"><img src="${ctx}/images/brand/bingxue-logo.png" alt="" /></span>
                    <span class="topbar-brand-text"><fmt:message key="app.brand" /></span>
                </a>
            </div>

            <c:if test="${not empty sessionScope.USER_SESSION}">
                <nav class="module-tabs" aria-label="<fmt:message key='nav.modules' />">
                    <a href="${ctx}/DashboardController" class="module-tab ${module == 'home' ? 'active' : ''}"><fmt:message key="nav.module.home" /></a>
                    <c:if test="${not empty salesHome}"><a href="${ctx}${salesHome}" class="module-tab ${module == 'sales' ? 'active' : ''}"><fmt:message key="nav.module.sales" /></a></c:if>
                    <c:if test="${not empty opsHome}"><a href="${ctx}${opsHome}" class="module-tab ${module == 'ops' ? 'active' : ''}"><fmt:message key="nav.module.ops" /></a></c:if>
                    <c:if test="${not empty hrHome}"><a href="${ctx}${hrHome}" class="module-tab ${module == 'hr' ? 'active' : ''}"><fmt:message key="nav.module.hr" /></a></c:if>
                    <c:if test="${role == 'Admin'}"><a href="${ctx}/AdminDashboardController" class="module-tab ${module == 'admin' ? 'active' : ''}"><fmt:message key="nav.module.admin" /></a></c:if>
                </nav>
            </c:if>

            <div class="topbar-tools">
                <jsp:include page="components/language_switcher.jsp" />
                <jsp:include page="components/theme_toggle.jsp" />
                <c:if test="${not empty sessionScope.USER_SESSION}">
                    <%-- Task 11.4: Nút Phát loa toàn cục (Admin / Manager) --%>
                    <c:if test="${role == 'Admin' or role == 'Manager'}">
                        <button id="btnBroadcastMegaphone" type="button" class="btn-icon-alert" title="<fmt:message key='broadcast.btn.title' />" data-action="open-broadcast"><i class="fi fi-rr-megaphone" aria-hidden="true"></i></button>
                    </c:if>
                    <%-- Cảnh báo kho nguyên liệu (Staff / Manager) --%>
                    <c:if test="${role == 'Staff' or role == 'Manager'}">
                        <button id="btnInventoryAlertBell" type="button" class="btn-icon-alert" title="<fmt:message key='alert.bell.title' />" data-action="open-inventory-alert">
                            <i class="fi fi-rr-triangle-warning" aria-hidden="true"></i><span id="inventoryAlertBadge" class="alert-badge d-none">0</span>
                        </button>
                    </c:if>
                    <%-- Task 11.3: Quả chuông Thông báo hệ thống & Dropdown --%>
                    <div class="notification-wrapper position-relative">
                        <button id="btnNotificationBell" type="button" class="btn-icon-alert" title="<fmt:message key='notification.bell.title' />" data-action="toggle-notification-dropdown">
                            <i class="fi fi-rr-bell" aria-hidden="true"></i><span id="unreadNotifBadge" class="alert-badge d-none">0</span>
                        </button>
                        <jsp:include page="components/notification_dropdown.jsp" />
                    </div>
                </c:if>
                <div class="dropdown-wrapper">
                    <c:choose>
                        <c:when test="${empty sessionScope.USER_SESSION}">
                            <c:if test="${not fn:contains(pageContext.request.requestURI, 'login.jsp') and not fn:contains(pageContext.request.requestURI, 'register.jsp')}">
                                <a href="${ctx}/login.jsp" class="btn-login"><fmt:message key="header.btn.login" /></a>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <button id="btnAvatar" type="button">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.USER_SESSION.avatar}">
                                        <img src="${ctx}/${sessionScope.USER_SESSION.avatar}" alt="Avatar" class="header-avatar-img">
                                    </c:when>
                                    <c:otherwise>${fn:toUpperCase(fn:substring(sessionScope.USER_SESSION.username, 0, 2))}</c:otherwise>
                                </c:choose>
                            </button>
                            <div id="menuTaiKhoan">
                                <strong><c:out value="${sessionScope.USER_SESSION.fullName}" /></strong>
                                <hr>
                                <a href="${ctx}/ProfileController"><fmt:message key="header.menu.profile" /></a><br><br>
                                <a href="${ctx}/SettingsController"><fmt:message key="header.menu.settings" /></a><br><br>
                                <a href="${ctx}/LogoutController" class="text-danger"><fmt:message key="header.menu.logout" /></a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </header>

        <c:if test="${not empty sessionScope.USER_SESSION and (role == 'Staff' or role == 'Manager')}">
            <jsp:include page="components/inventory_alert_modal.jsp" />
        </c:if>
        <c:if test="${not empty sessionScope.USER_SESSION and (role == 'Admin' or role == 'Manager')}">
            <jsp:include page="components/broadcast_modal.jsp" />
        </c:if>

        <c:if test="${not empty sessionScope.USER_SESSION}">
            <%-- Menu con của phân hệ đang mở (giữ id menuDoc / btnDongMenuDoc cho main.js: nút ☰ mở trên điện thoại) --%>
            <aside id="menuDoc" class="subnav">
                <button id="btnDongMenuDoc" type="button"><i class="fi fi-rr-cross" aria-hidden="true"></i> <fmt:message key="header.sidebar.close" /></button>
                <p class="subnav-title"><fmt:message key="nav.module.${module}" /></p>
                <ul class="no-bullet">
                    <c:choose>
                        <c:when test="${module == 'sales'}">
                            <c:if test="${role == 'Staff'}"><li><a class="${navPath == '/MenuController' ? 'active' : ''}" href="${ctx}/MenuController"><i class="fi fi-rr-mug-hot" aria-hidden="true"></i> <fmt:message key="header.sidebar.menu" /></a></li></c:if>
                            <c:if test="${role == 'Staff' or role == 'Manager'}"><li><a class="${navPath == '/OrderManagementController' ? 'active' : ''}" href="${ctx}/OrderManagementController"><i class="fi fi-rr-receipt" aria-hidden="true"></i> <fmt:message key="header.sidebar.order" /></a></li></c:if>
                            <c:if test="${role == 'Manager'}"><li><a class="${navPath == '/DispatchController' ? 'active' : ''}" href="${ctx}/DispatchController"><i class="fi fi-rr-truck-side" aria-hidden="true"></i> <fmt:message key="header.sidebar.dispatch" /></a></li></c:if>
                            <c:if test="${role == 'Shipper'}"><li><a class="${navPath == '/ShipperController' ? 'active' : ''}" href="${ctx}/ShipperController"><i class="fi fi-rr-box" aria-hidden="true"></i> <fmt:message key="header.sidebar.my_tasks" /></a></li></c:if>
                        </c:when>
                        <c:when test="${module == 'ops'}">
                            <li><a class="${navPath == '/MenuManagementController' ? 'active' : ''}" href="${ctx}/MenuManagementController"><i class="fi fi-rr-list" aria-hidden="true"></i> <fmt:message key="header.sidebar.menu_management" /></a></li>
                            <li><a class="${navPath == '/InventoryManagementController' ? 'active' : ''}" href="${ctx}/InventoryManagementController"><i class="fi fi-rr-boxes" aria-hidden="true"></i> <fmt:message key="header.sidebar.inventory" /></a></li>
                            <c:if test="${role == 'Manager'}"><li><a class="${navPath == '/ReportManagementController' ? 'active' : ''}" href="${ctx}/ReportManagementController"><i class="fi fi-rr-document" aria-hidden="true"></i> <fmt:message key="header.sidebar.reports" /></a></li></c:if>
                            <c:if test="${role == 'Staff'}"><li><a class="${navPath == '/ReportController' ? 'active' : ''}" href="${ctx}/ReportController"><i class="fi fi-rr-comment-alt" aria-hidden="true"></i> <fmt:message key="header.sidebar.anonymous_report" /></a></li></c:if>
                        </c:when>
                        <c:when test="${module == 'hr'}">
                            <c:if test="${role == 'Manager'}"><li><a class="${navPath == '/HrManagementController' ? 'active' : ''}" href="${ctx}/HrManagementController"><i class="fi fi-rr-users" aria-hidden="true"></i> <fmt:message key="header.sidebar.hr" /></a></li></c:if>
                            <c:if test="${role == 'Staff'}"><li><a class="${navPath == '/AttendanceController' ? 'active' : ''}" href="${ctx}/AttendanceController"><i class="fi fi-rr-clock" aria-hidden="true"></i> <fmt:message key="header.sidebar.attendance" /></a></li></c:if>
                        </c:when>
                        <c:when test="${module == 'admin'}">
                            <li><a class="active" href="${ctx}/AdminDashboardController"><i class="fi fi-rr-shield-check" aria-hidden="true"></i> <fmt:message key="header.sidebar.admin_daskboard" /></a></li>
                        </c:when>
                        <c:otherwise>
                            <li><a class="${navPath == '/DashboardController' or navPath == '/index.jsp' ? 'active' : ''}" href="${ctx}/DashboardController"><i class="fi fi-rr-home" aria-hidden="true"></i> <fmt:message key="header.sidebar.deshbard" /></a></li>
                            <li><a class="${navPath == '/ProfileController' ? 'active' : ''}" href="${ctx}/ProfileController"><i class="fi fi-rr-user" aria-hidden="true"></i> <fmt:message key="header.menu.profile" /></a></li>
                            <li><a class="${navPath == '/SettingsController' ? 'active' : ''}" href="${ctx}/SettingsController"><i class="fi fi-rr-settings" aria-hidden="true"></i> <fmt:message key="header.menu.settings" /></a></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </aside>
            <%-- Lớp phủ tối phía sau menu trái khi mở trên màn hẹp, chạm vào để đóng --%>
            <div id="menuDocBackdrop" class="menu-backdrop"></div>
        </c:if>
