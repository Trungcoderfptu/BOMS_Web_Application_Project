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

<main class="main-content dashboard-main">
    <c:choose>
        <%-- TRƯỜNG HỢP 1: KHÁCH VÃNG LAI (CHƯA ĐĂNG NHẬP) --%>
        <c:when test="${empty sessionScope.USER_SESSION}">
            <div class="guest-hero-container text-center">
                <h1 class="guest-hero-title mb-15 font-xxl font-bold">
                    <fmt:message key="dashboard.guest.hero_title" />
                </h1>
                <p class="guest-hero-desc mb-25 text-muted mx-auto max-w-600">
                    <fmt:message key="dashboard.guest.hero_desc" />
                </p>
                <div class="flex-center gap-15">
                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn-primary btn-lg">
                        🔐 <fmt:message key="dashboard.guest.btn_login" />
                    </a>
                </div>
            </div>
        </c:when>

        <%-- TRƯỜNG HỢP 2: NGƯỜI DÙNG ĐÃ ĐĂNG NHẬP (DASHBOARD TRUNG TÂM CÁ NHÂN HÓA) --%>
        <c:otherwise>
            <%-- ================= KHU VỰC 1: PROFILE CARD & TRẠNG THÁI LÀM VIỆC ================= --%>
            <section class="dashboard-profile-card mb-25">
                <div class="profile-card-left flex-align-center gap-20">
                    <div class="dashboard-avatar flex-center">
                        <c:choose>
                            <c:when test="${not empty sessionScope.USER_SESSION.avatar}">
                                <img src="${pageContext.request.contextPath}/${sessionScope.USER_SESSION.avatar}" alt="Avatar" class="dashboard-avatar-img">
                            </c:when>
                            <c:otherwise>
                                ${fn:substring(sessionScope.USER_SESSION.fullName, 0, 2).toUpperCase()}
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="profile-info-box">
                        <div class="flex-align-center gap-10 mb-5">
                            <h2 class="profile-name font-lg font-bold m-0">
                                <fmt:message key="dashboard.welcome_user" /> ${sessionScope.USER_SESSION.fullName} 👋
                            </h2>
                            <span class="role-badge role-${fn:toLowerCase(sessionScope.USER_SESSION.role)}">
                                ${sessionScope.USER_SESSION.role}
                            </span>
                        </div>
                        <p class="profile-username font-xs text-muted m-0 mb-10">
                            @${sessionScope.USER_SESSION.username}
                        </p>

                        <%-- HUY HIỆU TRẠNG THÁI LÀM VIỆC (ROLE-BASED STATUS BADGE) --%>
                        <div class="profile-work-status">
                            <c:choose>
                                <%-- Vai trò Staff: Kiểm tra ca làm hôm nay --%>
                                <c:when test="${sessionScope.USER_SESSION.role == 'Staff'}">
                                    <c:choose>
                                        <c:when test="${DASHBOARD_ATTENDANCE.hasShift}">
                                            <c:choose>
                                                <c:when test="${DASHBOARD_ATTENDANCE.hasCheckedIn and not DASHBOARD_ATTENDANCE.hasCheckedOut}">
                                                    <span class="badge-work-status status-on-shift">
                                                        🟢 <fmt:message key="dashboard.status.on_shift" />: 
                                                        <strong>${DASHBOARD_ATTENDANCE.shiftName}</strong> (${DASHBOARD_ATTENDANCE.startTime} - ${DASHBOARD_ATTENDANCE.endTime})
                                                        <span class="status-sub-text"> | <fmt:message key="dashboard.badge.checked_in_at" /> ${DASHBOARD_ATTENDANCE.checkInTime}</span>
                                                    </span>
                                                </c:when>
                                                <c:when test="${DASHBOARD_ATTENDANCE.hasCheckedOut}">
                                                    <span class="badge-work-status status-completed">
                                                        ⚪ <fmt:message key="dashboard.status.completed" />: 
                                                        <strong>${DASHBOARD_ATTENDANCE.shiftName}</strong>
                                                        <span class="status-sub-text"> | <fmt:message key="dashboard.badge.working_hours" /> ${DASHBOARD_ATTENDANCE.workingHours}h</span>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-work-status status-pending">
                                                        🟡 <fmt:message key="dashboard.status.not_checked_in" />: 
                                                        <strong>${DASHBOARD_ATTENDANCE.shiftName}</strong> (${DASHBOARD_ATTENDANCE.startTime} - ${DASHBOARD_ATTENDANCE.endTime})
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-work-status status-off-shift">
                                                🔴 <fmt:message key="dashboard.status.no_shift" />
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>

                                <%-- Vai trò Manager hoặc Admin: Trạng thái điều hành --%>
                                <c:when test="${sessionScope.USER_SESSION.role == 'Manager' or sessionScope.USER_SESSION.role == 'Admin'}">
                                    <span class="badge-work-status status-on-shift">
                                        🟢 <fmt:message key="dashboard.status.active_duty" />
                                    </span>
                                </c:when>

                                <%-- Vai trò Shipper: Sẵn sàng nhận đơn --%>
                                <c:when test="${sessionScope.USER_SESSION.role == 'Shipper'}">
                                    <span class="badge-work-status status-shipper">
                                        🛵 <fmt:message key="dashboard.status.ready_delivery" />
                                    </span>
                                </c:when>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <%-- MINI STATS & ĐỒNG HỒ MÁY CHỦ THỜI GIAN THỰC TRONG PROFILE CARD --%>
                <div class="profile-card-right flex-align-center gap-15">
                    <%-- ĐỒNG HỒ HỆ THỐNG THỜI GIAN THỰC (LẤY THEO CHUẨN TRANG CHẤM CÔNG) --%>
                    <div class="dashboard-clock-card">
                        <div class="clock-card-header flex-align-center gap-5">
                            <span class="clock-icon">⏱️</span>
                            <span class="font-xs font-bold text-muted"><fmt:message key="attendance.card.clock" /></span>
                        </div>
                        <div id="serverClockDisplay" class="dashboard-clock-time" data-server-date="${DASHBOARD_SERVER_DATE}" data-server-time="${DASHBOARD_SERVER_TIME}">
                            ${DASHBOARD_SERVER_TIME}
                        </div>
                        <div class="dashboard-clock-date">
                            ${DASHBOARD_SERVER_DATE}
                        </div>
                    </div>

                    <div class="mini-stat-card">
                        <span class="mini-stat-icon">🔔</span>
                        <div>
                            <span class="mini-stat-label font-xs text-muted d-block"><fmt:message key="dashboard.stats.unread_notifs" /></span>
                            <strong class="mini-stat-value font-sm text-primary">${DASHBOARD_UNREAD_COUNT}</strong>
                        </div>
                    </div>
                    <c:if test="${sessionScope.USER_SESSION.role == 'Manager'}">
                        <div class="mini-stat-card">
                            <span class="mini-stat-icon">⏳</span>
                            <div>
                                <span class="mini-stat-label font-xs text-muted d-block"><fmt:message key="dashboard.stats.pending_approvals" /></span>
                                <strong class="mini-stat-value font-sm text-warning">${DASHBOARD_PENDING_APPROVALS}</strong>
                            </div>
                        </div>
                        <div class="mini-stat-card">
                            <span class="mini-stat-icon">⚠️</span>
                            <div>
                                <span class="mini-stat-label font-xs text-muted d-block"><fmt:message key="dashboard.stats.inventory_alerts" /></span>
                                <strong class="mini-stat-value font-sm text-danger">${DASHBOARD_ACTIVE_ALERTS}</strong>
                            </div>
                        </div>
                    </c:if>
                </div>
            </section>

            <%-- ================= DASHBOARD BODY 2 CỘT ================= --%>
            <div class="dashboard-grid-container">
                <%-- CỘT TRÁI: KHU VỰC 2 - QUICK ACTIONS GRID --%>
                <section class="dashboard-actions-section">
                    <div class="section-header mb-15">
                        <h3 class="font-md font-bold m-0 flex-align-center gap-10">
                            ⚡ <fmt:message key="dashboard.section.quick_actions" />
                        </h3>
                    </div>

                    <div class="quick-actions-grid">
                        <%-- CÁC NÚT CHO STAFF --%>
                        <c:if test="${sessionScope.USER_SESSION.role == 'Staff'}">
                            <a href="${pageContext.request.contextPath}/MenuController" class="action-card">
                                <div class="action-card-icon pos-icon">🛒</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.pos" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.pos_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/AttendanceController" class="action-card">
                                <div class="action-card-icon attendance-icon">⏱️</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.attendance" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.attendance_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/OrderManagementController" class="action-card">
                                <div class="action-card-icon orders-icon">📋</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.orders" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.orders_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/InventoryManagementController" class="action-card">
                                <div class="action-card-icon inventory-icon">🏬</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.inventory" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.inventory_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/ReportController" class="action-card">
                                <div class="action-card-icon report-icon">📝</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.anonymous_report" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.anonymous_report_desc" /></p>
                                </div>
                            </a>
                        </c:if>

                        <%-- CÁC NÚT CHO MANAGER --%>
                        <c:if test="${sessionScope.USER_SESSION.role == 'Manager'}">
                            <a href="${pageContext.request.contextPath}/OrderManagementController" class="action-card">
                                <div class="action-card-icon orders-icon">📋</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.orders" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.orders_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/InventoryManagementController" class="action-card">
                                <div class="action-card-icon inventory-icon">🏬</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.inventory" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.inventory_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/HrManagementController" class="action-card">
                                <div class="action-card-icon hr-icon">👥</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.hr" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.hr_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/MenuManagementController" class="action-card">
                                <div class="action-card-icon menu-icon">🍽️</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.menu" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.menu_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/DispatchController" class="action-card">
                                <div class="action-card-icon dispatch-icon">🛵</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.dispatch" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.dispatch_desc" /></p>
                                </div>
                            </a>
                            <a href="${pageContext.request.contextPath}/ReportManagementController" class="action-card">
                                <div class="action-card-icon report-icon">📊</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.reports" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.reports_desc" /></p>
                                </div>
                            </a>
                            <button type="button" class="action-card action-card-btn" onclick="openBroadcastModal()">
                                <div class="action-card-icon broadcast-icon">📢</div>
                                <div class="action-card-info text-left">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.broadcast" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.broadcast_desc" /></p>
                                </div>
                            </button>
                        </c:if>

                        <%-- CÁC NÚT CHO ADMIN --%>
                        <c:if test="${sessionScope.USER_SESSION.role == 'Admin'}">
                            <a href="${pageContext.request.contextPath}/AdminDashboardController" class="action-card">
                                <div class="action-card-icon admin-icon">👤</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.users" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.users_desc" /></p>
                                </div>
                            </a>
                            <button type="button" class="action-card action-card-btn" onclick="openBroadcastModal()">
                                <div class="action-card-icon broadcast-icon">📢</div>
                                <div class="action-card-info text-left">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.broadcast" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.broadcast_desc" /></p>
                                </div>
                            </button>
                        </c:if>

                        <%-- CÁC NÚT CHO SHIPPER --%>
                        <c:if test="${sessionScope.USER_SESSION.role == 'Shipper'}">
                            <a href="${pageContext.request.contextPath}/ShipperController" class="action-card">
                                <div class="action-card-icon dispatch-icon">🛵</div>
                                <div class="action-card-info">
                                    <h4 class="action-card-title"><fmt:message key="dashboard.action.shipper_tasks" /></h4>
                                    <p class="action-card-desc"><fmt:message key="dashboard.action.shipper_tasks_desc" /></p>
                                </div>
                            </a>
                        </c:if>

                        <%-- CÁC NÚT ĐIỀU HƯỚNG TÀI KHOẢN CHUNG CHO TẤT CẢ CÁC VAI TRÒ (ĐỒNG BỘ TỪ HEADER DROPDOWN) --%>
                        <a href="${pageContext.request.contextPath}/ProfileController" class="action-card">
                            <div class="action-card-icon profile-action-icon">💼</div>
                            <div class="action-card-info">
                                <h4 class="action-card-title"><fmt:message key="header.menu.profile" /></h4>
                                <p class="action-card-desc"><fmt:message key="dashboard.action.profile_desc" /></p>
                            </div>
                        </a>
                        <a href="${pageContext.request.contextPath}/SettingsController" class="action-card">
                            <div class="action-card-icon settings-icon">⚙️</div>
                            <div class="action-card-info">
                                <h4 class="action-card-title"><fmt:message key="header.menu.settings" /></h4>
                                <p class="action-card-desc"><fmt:message key="dashboard.action.settings_desc" /></p>
                            </div>
                        </a>
                        <a href="${pageContext.request.contextPath}/LogoutController" class="action-card action-card-logout" onclick="return confirm('<fmt:message key="dashboard.confirm.logout" />');">
                            <div class="action-card-icon logout-icon">🚪</div>
                            <div class="action-card-info">
                                <h4 class="action-card-title text-danger"><fmt:message key="header.menu.logout" /></h4>
                                <p class="action-card-desc"><fmt:message key="dashboard.action.logout_desc" /></p>
                            </div>
                        </a>
                    </div>
                </section>

                <%-- CỘT PHẢI: KHU VỰC 3 - NOTIFICATION FEED (BẢNG TIN THÔNG BÁO) --%>
                <section class="dashboard-notifications-section">
                    <div class="section-header flex-between align-center mb-15">
                        <h3 class="font-md font-bold m-0 flex-align-center gap-10">
                            🔔 <fmt:message key="dashboard.section.notifications" />
                        </h3>
                        <button type="button" class="btn-link font-xs text-primary border-0 bg-transparent cursor-pointer" onclick="markAllNotificationsAsRead()">
                            <fmt:message key="dashboard.notifications.mark_all_read" />
                        </button>
                    </div>

                    <div class="notification-feed-card">
                        <c:choose>
                            <c:when test="${empty DASHBOARD_NOTIFICATIONS}">
                                <div class="text-center p-30 text-muted font-xs">
                                    <div class="font-xl mb-10">📭</div>
                                    <fmt:message key="dashboard.notifications.empty" />
                                </div>
                            </c:when>
                            <c:otherwise>
                                <ul class="notification-feed-list no-bullet m-0 p-0">
                                    <c:forEach var="notif" items="${DASHBOARD_NOTIFICATIONS}">
                                        <li id="feedItem_${notif.notificationId}" class="feed-item ${notif.isRead ? '' : 'unread'}" onclick="markNotificationAsRead(${notif.notificationId})">
                                            <div class="feed-item-icon">
                                                ${notif.icon}
                                            </div>
                                            <div class="feed-item-body">
                                                <div class="feed-item-message">
                                                    ${notif.message}
                                                </div>
                                                <div class="feed-item-meta flex-between align-center mt-5">
                                                    <span class="feed-item-time font-xs text-muted">
                                                        ${notif.createdAt}
                                                    </span>
                                                    <c:if test="${not notif.isRead}">
                                                        <span class="notification-unread-dot"></span>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="footer.jsp" />
