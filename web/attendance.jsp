<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <div class="flex-between mt-15 mb-20">
        <h2 class="m-0"><fmt:message key="attendance.heading" /></h2>
    </div>

    <%-- Flash Message Thông Báo Một Lần (Chuẩn PRG) --%>
    <c:if test="${not empty sessionScope.ERROR_MESSAGE}">
        <p class="msg-error text-center"><fmt:message key="${sessionScope.ERROR_MESSAGE}" /></p>
        <c:remove var="ERROR_MESSAGE" scope="session" />
    </c:if>
    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>

    <c:if test="${not empty sessionScope.SUCCESS_MESSAGE}">
        <p class="msg-success text-center"><fmt:message key="${sessionScope.SUCCESS_MESSAGE}" /></p>
        <c:remove var="SUCCESS_MESSAGE" scope="session" />
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success text-center"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
    </c:if>

    <%-- KHỐI 1: TRẠNG THÁI CA LÀM HÔM NAY & ĐỒNG HỒ MÁY CHỦ --%>
    <div class="attendance-top-grid mb-25">
        <%-- Thẻ Thông tin Ca làm việc hôm nay --%>
        <div class="attendance-card">
            <h3 class="attendance-card-title">
                📅 <fmt:message key="attendance.card.today_shift" />
            </h3>
            <c:choose>
                <c:when test="${TODAY_ATTENDANCE.hasShift}">
                    <div class="attendance-info-row">
                        <span class="info-label"><fmt:message key="attendance.label.shift" /></span>
                        <strong class="info-value font-highlight">${TODAY_ATTENDANCE.shiftName}</strong>
                    </div>
                    <div class="attendance-info-row">
                        <span class="info-label"><fmt:message key="attendance.label.time" /></span>
                        <span class="info-value">${TODAY_ATTENDANCE.startTime} - ${TODAY_ATTENDANCE.endTime}</span>
                    </div>
                    <div class="attendance-info-row">
                        <span class="info-label"><fmt:message key="attendance.label.status" /></span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${TODAY_ATTENDANCE.systemStatus == 'On Shift / Working'}">
                                    <span class="badge-status-working"><fmt:message key="attendance.status.on_shift" /></span>
                                </c:when>
                                <c:when test="${TODAY_ATTENDANCE.systemStatus == 'Shift Ended / Off Shift'}">
                                    <span class="badge-status-ended"><fmt:message key="attendance.status.shift_ended" /></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-status-waiting"><fmt:message key="attendance.status.not_started" /></span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <%-- Nút bấm hành động Chấm công --%>
                    <div class="attendance-actions mt-20">
                        <c:if test="${not TODAY_ATTENDANCE.hasCheckedIn}">
                            <form action="${pageContext.request.contextPath}/AttendanceController" method="POST" class="m-0">
                                <input type="hidden" name="action" value="checkIn" />
                                <button type="submit" class="btn-clock-in w-100">
                                    🟢 <fmt:message key="attendance.btn.checkin" />
                                </button>
                            </form>
                        </c:if>

                        <c:if test="${TODAY_ATTENDANCE.hasCheckedIn and not TODAY_ATTENDANCE.hasCheckedOut}">
                            <c:choose>
                                <c:when test="${TODAY_ATTENDANCE.isOvertimeEligible}">
                                    <button type="button" class="btn-clock-out w-100" onclick="openOtReasonModal()">
                                        🔴 <fmt:message key="attendance.btn.checkout" /> (OT)
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <form action="${pageContext.request.contextPath}/AttendanceController" method="POST" class="m-0">
                                        <input type="hidden" name="action" value="checkOut" />
                                        <button type="submit" class="btn-clock-out w-100">
                                            🔴 <fmt:message key="attendance.btn.checkout" />
                                        </button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>

                    <form action="${pageContext.request.contextPath}/AttendanceController" method="POST" class="mt-15 text-center">
                        <input type="hidden" name="action" value="resetDemoShift" />
                        <button type="submit" class="btn-link text-muted font-sm border-0 bg-transparent cursor-pointer" title="<fmt:message key="attendance.btn.reset_hint" />">
                            🔄 <fmt:message key="attendance.btn.reset_shift" />
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <div class="empty-shift-box">
                        <p class="msg-info"><fmt:message key="attendance.status.no_shift" /></p>
                        <form action="${pageContext.request.contextPath}/AttendanceController" method="POST" class="mt-15 flex-center gap-10">
                            <input type="hidden" name="action" value="assignDemoShift" />
                            <select name="shiftId" class="filter-input">
                                <c:forEach var="shift" items="${AVAILABLE_SHIFTS}">
                                    <option value="${shift.shiftId}">
                                        ${shift.shiftName} (${shift.timeRange})
                                    </option>
                                </c:forEach>
                            </select>
                            <button type="submit" class="btn-login btn-sm">
                                ➕ <fmt:message key="attendance.btn.demo_shift" />
                            </button>
                        </form>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- Thẻ Đồng hồ đếm giờ máy chủ (Server Time) --%>
        <div class="attendance-card text-center">
            <h3 class="attendance-card-title">
                ⏱️ <fmt:message key="attendance.card.clock" />
            </h3>
            <div id="serverClockDisplay" class="clock-display" data-server-date="${TODAY_ATTENDANCE.serverCurrentDate}" data-server-time="${TODAY_ATTENDANCE.serverCurrentTime}">
                ${TODAY_ATTENDANCE.serverCurrentTime}
            </div>
            <div class="clock-date-text mt-5 font-bold">
                ${TODAY_ATTENDANCE.serverCurrentDate}
            </div>

            <c:if test="${TODAY_ATTENDANCE.hasCheckedIn}">
                <div class="shift-summary-box mt-15">
                    <div class="flex-between mb-5">
                        <span><fmt:message key="attendance.label.checkin_time" />:</span>
                        <strong>${TODAY_ATTENDANCE.checkInTime} (${TODAY_ATTENDANCE.checkInStatus})</strong>
                    </div>
                    <c:if test="${TODAY_ATTENDANCE.hasCheckedOut}">
                        <div class="flex-between mb-5">
                            <span><fmt:message key="attendance.label.checkout_time" />:</span>
                            <strong>${TODAY_ATTENDANCE.checkOutTime} (${TODAY_ATTENDANCE.checkOutStatus})</strong>
                        </div>
                        <div class="flex-between border-top pt-5">
                            <span><fmt:message key="attendance.label.working_hours" />:</span>
                            <strong class="font-highlight">${TODAY_ATTENDANCE.workingHours} hrs</strong>
                        </div>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>

    <%-- KHỐI 2: CHỈ SỐ TỔNG KẾT THÁNG (TIMESHEET SUMMARY METRICS) --%>
    <div class="attendance-summary-cards mb-25">
        <div class="summary-metric-card">
            <div class="metric-number">${ATTENDANCE_SUMMARY.totalShifts}</div>
            <div class="metric-label"><fmt:message key="attendance.summary.total_shifts" /></div>
        </div>
        <div class="summary-metric-card">
            <div class="metric-number font-highlight">${ATTENDANCE_SUMMARY.totalHours}</div>
            <div class="metric-label"><fmt:message key="attendance.summary.total_hours" /> (h)</div>
        </div>
        <div class="summary-metric-card">
            <div class="metric-number text-success">${ATTENDANCE_SUMMARY.onTimeCount}</div>
            <div class="metric-label"><fmt:message key="attendance.summary.on_time" /></div>
        </div>
        <div class="summary-metric-card">
            <div class="metric-number text-warning">${ATTENDANCE_SUMMARY.lateCount}</div>
            <div class="metric-label"><fmt:message key="attendance.summary.late" /></div>
        </div>
        <div class="summary-metric-card">
            <div class="metric-number text-danger">${ATTENDANCE_SUMMARY.otCount}</div>
            <div class="metric-label"><fmt:message key="attendance.summary.ot" /></div>
        </div>
    </div>

    <%-- KHỐI 3: BẢNG LỊCH SỬ ĐIỂM DANH THEO THÁNG --%>
    <div class="attendance-history-panel">
        <div class="flex-between mb-15">
            <h3 class="m-0"><fmt:message key="attendance.history.title" /></h3>
            <form action="${pageContext.request.contextPath}/AttendanceController" method="GET" class="order-filter-panel m-0">
                <div class="filter-item">
                    <label for="filterMonth"><fmt:message key="attendance.history.filter_month" /></label>
                    <select id="filterMonth" name="month" class="filter-input" onchange="this.form.submit()">
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${CURRENT_MONTH == m ? 'selected' : ''}>
                                ${m}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="filter-item">
                    <label for="filterYear"><fmt:message key="attendance.history.filter_year" /></label>
                    <select id="filterYear" name="year" class="filter-input" onchange="this.form.submit()">
                        <c:forEach var="y" begin="2024" end="2027">
                            <option value="${y}" ${CURRENT_YEAR == y ? 'selected' : ''}>
                                ${y}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </form>
        </div>

        <c:choose>
            <c:when test="${empty ATTENDANCE_HISTORY}">
                <div class="empty-cart-container">
                    <h3 class="msg-info"><fmt:message key="attendance.history.empty" /></h3>
                </div>
            </c:when>
            <c:otherwise>
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th><fmt:message key="attendance.history.table.date" /></th>
                            <th><fmt:message key="attendance.history.table.shift" /></th>
                            <th><fmt:message key="attendance.history.table.time_range" /></th>
                            <th><fmt:message key="attendance.history.table.checkin" /></th>
                            <th><fmt:message key="attendance.history.table.checkout" /></th>
                            <th><fmt:message key="attendance.history.table.hours" /></th>
                            <th><fmt:message key="attendance.history.table.status" /></th>
                            <th><fmt:message key="attendance.history.table.ot_reason" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${ATTENDANCE_HISTORY}">
                            <tr>
                                <td><strong>${row.workDate}</strong></td>
                                <td>${row.shiftName}</td>
                                <td>${row.shiftTimeRange}</td>
                                <td>
                                    <strong>${row.checkInTime}</strong>
                                    <span class="badge-sub-status ${row.checkInStatus == 'On Time' ? 'text-success' : 'text-danger'}">
                                        (${row.checkInStatus})
                                    </span>
                                </td>
                                <td>
                                    <strong>${row.checkOutTime}</strong>
                                    <c:if test="${row.checkOutStatus != 'Working'}">
                                        <span class="badge-sub-status ${row.checkOutStatus == 'On Time' ? 'text-success' : 'text-danger'}">
                                            (${row.checkOutStatus})
                                        </span>
                                    </c:if>
                                </td>
                                <td class="font-bold">
                                    ${row.workingHours}
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${row.checkOutStatus == 'Working'}">
                                            <span class="badge-status-working"><fmt:message key="attendance.status.on_shift" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-status-ended"><fmt:message key="attendance.status.shift_ended" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty row.otReason}">
                                            <span class="ot-reason-bubble" title="${row.otReason}">${row.otReason}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted">---</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="components/ot_reason_modal.jsp" />
<jsp:include page="footer.jsp" />
<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
