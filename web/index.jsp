<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<%-- Tự động chuyển tiếp đến DashboardController để nạp dữ liệu cá nhân hóa nếu chưa nạp --%>
<c:if test="${not empty sessionScope.USER_SESSION and empty DASHBOARD_ATTENDANCE}">
    <c:redirect url="/DashboardController" />
</c:if>

<jsp:include page="header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="role" value="${sessionScope.USER_SESSION.role}" />

<main class="main-content dashboard-main">
    <c:choose>
        <%-- TRƯỜNG HỢP 1: KHÁCH VÃNG LAI (CHƯA ĐĂNG NHẬP) --%>
        <c:when test="${empty sessionScope.USER_SESSION}">
            <div class="guest-hero-container text-center">
                <img class="auth-logo guest-logo" src="${ctx}/images/brand/bingxue-logo.png" alt="" />
                <h1 class="guest-hero-title mb-15 font-xxl font-bold">
                    <fmt:message key="dashboard.guest.hero_title" />
                </h1>
                <p class="guest-hero-desc mb-25 text-muted mx-auto max-w-600">
                    <fmt:message key="dashboard.guest.hero_desc" />
                </p>
                <div class="flex-center gap-15">
                    <a href="${ctx}/login.jsp" class="btn-primary btn-lg">
                        <fmt:message key="dashboard.guest.btn_login" />
                    </a>
                </div>
            </div>
        </c:when>

        <%-- TRƯỜNG HỢP 2: ĐÃ ĐĂNG NHẬP - Bố cục E "trung tâm việc cần làm":
             hàng số liệu -> trái: việc đang chờ chính người này xử lý (mỗi dòng có nút làm luôn) -> phải: hoạt động gần đây --%>
        <c:otherwise>
            <div class="page-head">
                <h2><fmt:message key="dashboard.welcome_user" /> <c:out value="${sessionScope.USER_SESSION.fullName}" /></h2>
                <span class="muted">@<c:out value="${sessionScope.USER_SESSION.username}" /></span>
            </div>

            <section class="kpi-row">
                <div class="kpi">
                    <span class="kpi-label"><fmt:message key="attendance.card.clock" /></span>
                    <span id="serverClockDisplay" class="kpi-value" data-server-date="${DASHBOARD_SERVER_DATE}" data-server-time="${DASHBOARD_SERVER_TIME}">${DASHBOARD_SERVER_TIME}</span>
                    <span class="muted">${DASHBOARD_SERVER_DATE}</span>
                </div>
                <div class="kpi">
                    <span class="kpi-label"><fmt:message key="dashboard.stats.unread_notifs" /></span>
                    <span class="kpi-value">${DASHBOARD_UNREAD_COUNT}</span>
                </div>
                <c:if test="${role == 'Manager'}">
                    <div class="kpi">
                        <span class="kpi-label"><fmt:message key="dashboard.stats.pending_approvals" /></span>
                        <span class="kpi-value">${DASHBOARD_PENDING_APPROVALS}</span>
                    </div>
                    <div class="kpi">
                        <span class="kpi-label"><fmt:message key="dashboard.stats.inventory_alerts" /></span>
                        <span class="kpi-value">${DASHBOARD_ACTIVE_ALERTS}</span>
                    </div>
                </c:if>
                <c:if test="${role == 'Staff' and DASHBOARD_ATTENDANCE.hasShift}">
                    <div class="kpi">
                        <span class="kpi-label"><fmt:message key="attendance.card.today_shift" /></span>
                        <span class="kpi-value"><c:out value="${DASHBOARD_ATTENDANCE.shiftName}" /></span>
                        <span class="muted">${DASHBOARD_ATTENDANCE.startTime} - ${DASHBOARD_ATTENDANCE.endTime}</span>
                    </div>
                </c:if>
            </section>

            <div class="home-grid">
                <section class="panel">
                    <h3 class="panel-title"><fmt:message key="home.todo.title" /></h3>
                    <c:set var="hasTodo" value="false" />
                    <ul class="todo-list">
                        <c:if test="${role == 'Manager' and DASHBOARD_PENDING_APPROVALS > 0}">
                            <c:set var="hasTodo" value="true" />
                            <li class="todo-item">
                                <span class="todo-count">${DASHBOARD_PENDING_APPROVALS}</span>
                                <span class="todo-text"><fmt:message key="home.todo.approvals" /></span>
                                <a href="${ctx}/HrManagementController?tab=tracking" class="btn-next"><fmt:message key="home.todo.btn" /></a>
                            </li>
                        </c:if>
                        <c:if test="${role == 'Manager' and DASHBOARD_ACTIVE_ALERTS > 0}">
                            <c:set var="hasTodo" value="true" />
                            <li class="todo-item">
                                <span class="todo-count">${DASHBOARD_ACTIVE_ALERTS}</span>
                                <span class="todo-text"><fmt:message key="home.todo.alerts" /></span>
                                <button type="button" class="btn-next" data-action="open-inventory-alert"><fmt:message key="home.todo.btn" /></button>
                            </li>
                        </c:if>
                        <c:if test="${role == 'Staff' and DASHBOARD_ATTENDANCE.hasShift and not DASHBOARD_ATTENDANCE.hasCheckedOut}">
                            <c:set var="hasTodo" value="true" />
                            <li class="todo-item">
                                <span class="todo-count">${DASHBOARD_ATTENDANCE.startTime}</span>
                                <span class="todo-text">
                                    <c:choose>
                                        <c:when test="${DASHBOARD_ATTENDANCE.hasCheckedIn}"><fmt:message key="home.todo.shift_on" /></c:when>
                                        <c:otherwise><fmt:message key="home.todo.shift_wait" /></c:otherwise>
                                    </c:choose>
                                </span>
                                <a href="${ctx}/AttendanceController" class="btn-next"><fmt:message key="home.todo.btn" /></a>
                            </li>
                        </c:if>
                        <c:if test="${role == 'Shipper'}">
                            <c:set var="hasTodo" value="true" />
                            <li class="todo-item">
                                <span class="todo-text"><fmt:message key="home.todo.delivery" /></span>
                                <a href="${ctx}/ShipperController" class="btn-next"><fmt:message key="home.todo.btn" /></a>
                            </li>
                        </c:if>
                        <c:if test="${role == 'Admin'}">
                            <c:set var="hasTodo" value="true" />
                            <li class="todo-item">
                                <span class="todo-text"><fmt:message key="home.todo.users" /></span>
                                <a href="${ctx}/AdminDashboardController" class="btn-next"><fmt:message key="home.todo.btn" /></a>
                            </li>
                        </c:if>
                        <c:if test="${DASHBOARD_UNREAD_COUNT > 0}">
                            <c:set var="hasTodo" value="true" />
                            <li class="todo-item">
                                <span class="todo-count">${DASHBOARD_UNREAD_COUNT}</span>
                                <span class="todo-text"><fmt:message key="home.todo.notifs" /></span>
                                <button type="button" class="btn-plain" data-action="mark-all-notifications"><fmt:message key="dashboard.notifications.mark_all_read" /></button>
                            </li>
                        </c:if>
                    </ul>
                    <c:if test="${not hasTodo}">
                        <p class="todo-empty"><fmt:message key="home.todo.none" /></p>
                    </c:if>
                </section>

                <section class="panel">
                    <h3 class="panel-title"><fmt:message key="home.activity.title" /></h3>
                    <c:choose>
                        <c:when test="${empty DASHBOARD_NOTIFICATIONS}">
                            <p class="todo-empty"><fmt:message key="dashboard.notifications.empty" /></p>
                        </c:when>
                        <c:otherwise>
                            <ul class="activity-list">
                                <c:forEach var="notif" items="${DASHBOARD_NOTIFICATIONS}">
                                    <li id="feedItem_${notif.notificationId}" class="activity-item ${notif.isRead ? '' : 'unread'}" data-action="mark-notification" data-notif-id="${notif.notificationId}">
                                        <c:out value="${notif.message}" />
                                        <div class="muted">${notif.createdAt}</div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="footer.jsp" />
