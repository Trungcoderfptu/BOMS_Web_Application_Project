<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<!DOCTYPE html>
<html lang="${currentLang}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><fmt:message key="hr.page.title" /> - BOMS</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
</head>
<body class="page-container">
    <jsp:include page="header.jsp" />

    <main class="main-content">
        <div class="content-header flex-between align-center mb-20">
            <div>
                <h1 class="page-main-title">
                    👥 <fmt:message key="hr.page.title" />
                </h1>
                <p class="text-muted mt-5 font-sm">
                    <fmt:message key="hr.page.subtitle" />
                </p>
            </div>
        </div>

        <%-- ================= FLASH MESSAGES ================= --%>
        <c:if test="${not empty SUCCESS_MESSAGE}">
            <div class="alert-box alert-success mb-20 flex-between align-center">
                <span>✅ <fmt:message key="${SUCCESS_MESSAGE}" /></span>
                <span class="close-alert cursor-pointer" onclick="this.parentElement.remove()">&times;</span>
            </div>
            <c:remove var="SUCCESS_MESSAGE" scope="session" />
        </c:if>
        <c:if test="${not empty ERROR_MESSAGE}">
            <div class="alert-box alert-danger mb-20 flex-between align-center">
                <span>⚠️ <fmt:message key="${ERROR_MESSAGE}" /></span>
                <span class="close-alert cursor-pointer" onclick="this.parentElement.remove()">&times;</span>
            </div>
            <c:remove var="ERROR_MESSAGE" scope="session" />
        </c:if>

        <%-- ================= NAVIGATION TABS ================= --%>
        <div class="tab-header mb-20">
            <button type="button" class="tab-btn ${ACTIVE_TAB == 'shifts' ? 'active' : ''}" onclick="openTab(event, 'tab-shifts')">
                🗓️ <fmt:message key="hr.tab.shifts" />
            </button>
            <button type="button" class="tab-btn ${ACTIVE_TAB == 'tracking' ? 'active' : ''}" onclick="openTab(event, 'tab-tracking')">
                ⏱️ <fmt:message key="hr.tab.tracking" />
                <c:if test="${not empty PENDING_APPROVALS}">
                    <span class="badge-pending-count ml-5">${PENDING_APPROVALS.size()}</span>
                </c:if>
            </button>
            <button type="button" class="tab-btn ${ACTIVE_TAB == 'summary' ? 'active' : ''}" onclick="openTab(event, 'tab-summary')">
                📊 <fmt:message key="hr.tab.summary" />
            </button>
        </div>

        <%-- ================= TAB 1: PHÂN CA & KHUNG GIỜ ================= --%>
        <div id="tab-shifts" class="tab-content ${ACTIVE_TAB == 'shifts' ? 'active' : 'd-none'}">
            <div class="panel-card mb-25">
                <div class="panel-header flex-between align-center mb-15">
                    <h2 class="panel-title font-md">
                        🕒 <fmt:message key="hr.shifts.section_title" />
                    </h2>
                    <button type="button" class="btn-primary btn-sm" onclick="openCreateShiftModal()">
                        ➕ <fmt:message key="hr.btn.add_shift" />
                    </button>
                </div>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><fmt:message key="hr.shift.label.name" /></th>
                                <th><fmt:message key="hr.shift.label.time_range" /></th>
                                <th><fmt:message key="hr.shift.label.status" /></th>
                                <th class="text-center"><fmt:message key="hr.table.action" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty SHIFTS}">
                                    <c:forEach var="shift" items="${SHIFTS}" varStatus="loop">
                                        <tr>
                                            <td>${loop.index + 1}</td>
                                            <td class="font-bold">${shift.shiftName}</td>
                                            <td>${shift.timeRange}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${shift.isAssigned}">
                                                        <span class="status-badge badge-active"><fmt:message key="hr.shift.status.in_use" /></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge badge-neutral"><fmt:message key="hr.shift.status.available" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <button type="button" class="btn-secondary btn-xs" onclick="openEditShiftModal(${shift.shiftId}, '${shift.shiftName}', '${shift.startTime}', '${shift.endTime}')">
                                                    ✏️ <fmt:message key="hr.btn.edit" />
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="5" class="text-center text-muted p-20">
                                            <fmt:message key="hr.empty.shifts" />
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <%-- ================= BẢNG MA TRẬN PHÂN CA TUẦN CHO QUẢN LÝ (TASK 14.3) ================= --%>
            <div class="panel-card mb-25">
                <div class="panel-header flex-between align-center mb-15">
                    <div>
                        <h2 class="panel-title font-md">
                            🗓️ <fmt:message key="hr.matrix.title" />
                        </h2>
                        <span class="text-muted font-xs font-bold">${MANAGER_MATRIX.weekLabel}</span>
                    </div>
                    <div class="flex-center gap-10">
                        <div class="week-nav-bar flex-center gap-5">
                            <a href="${pageContext.request.contextPath}/HrManagementController?tab=shifts&weekDate=${MANAGER_MATRIX.prevWeekDate}&date=${FILTER_DATE}" class="btn-secondary btn-xs">
                                <fmt:message key="hr.matrix.prev_week" />
                            </a>
                            <a href="${pageContext.request.contextPath}/HrManagementController?tab=shifts&weekDate=${MANAGER_MATRIX.currentWeekDate}&date=${FILTER_DATE}" class="btn-secondary btn-xs font-bold">
                                <fmt:message key="hr.matrix.current_week" />
                            </a>
                            <a href="${pageContext.request.contextPath}/HrManagementController?tab=shifts&weekDate=${MANAGER_MATRIX.nextWeekDate}&date=${FILTER_DATE}" class="btn-secondary btn-xs">
                                <fmt:message key="hr.matrix.next_week" />
                            </a>
                        </div>
                        <button type="button" class="btn-primary btn-sm" onclick="openAssignShiftModal()">
                            ➕ <fmt:message key="hr.btn.assign_shift" />
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="data-table matrix-table">
                        <thead>
                            <tr>
                                <th class="w-180"><fmt:message key="hr.assign.label.employee" /></th>
                                <th class="w-100"><fmt:message key="hr.table.role" /></th>
                                <c:forEach var="day" items="${MANAGER_MATRIX.days}">
                                    <th class="text-center ${day.isToday ? 'th-today' : ''}">
                                        ${day.dayName}<br>
                                        <span class="font-xs text-muted">${day.shortDate}</span>
                                    </th>
                                </c:forEach>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty MANAGER_MATRIX.matrix}">
                                    <c:forEach var="staffRow" items="${MANAGER_MATRIX.matrix}">
                                        <tr>
                                            <td class="font-bold">${staffRow.fullName}</td>
                                            <td><span class="role-badge role-${staffRow.role.toLowerCase()}">${staffRow.role}</span></td>
                                            <c:forEach var="cell" items="${staffRow.slots}">
                                                <td class="matrix-cell ${cell.isToday ? 'cell-today' : ''}">
                                                    <c:choose>
                                                        <c:when test="${cell.hasAssignment}">
                                                            <div class="matrix-shift-badge">
                                                                <div class="font-xs font-bold">${cell.shiftName}</div>
                                                                <div class="font-xxs text-muted">${cell.timeRange}</div>
                                                                <c:choose>
                                                                    <c:when test="${cell.isLocked}">
                                                                        <span class="badge-sub-status text-success font-xxs">🔒 <fmt:message key="hr.status.locked" /></span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST" class="m-0 d-inline" onsubmit="return confirm('<fmt:message key="hr.confirm.delete_assignment" />');">
                                                                            <input type="hidden" name="action" value="deleteAssignment" />
                                                                            <input type="hidden" name="assignmentId" value="${cell.assignmentId}" />
                                                                            <input type="hidden" name="tab" value="shifts" />
                                                                            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />
                                                                            <button type="submit" class="btn-matrix-del" title="<fmt:message key="hr.btn.remove" />">&times;</button>
                                                                        </form>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <button type="button" class="btn-matrix-add" onclick="openQuickAssignModal(${cell.userId}, '${cell.fullName}', '${cell.date}')" title="<fmt:message key="hr.matrix.quick_assign" />">+</button>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </c:forEach>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="text-center text-muted p-20">
                                            <fmt:message key="hr.empty.shifts" />
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="panel-card">
                <div class="panel-header flex-between align-center mb-15">
                    <div>
                        <h2 class="panel-title font-md">
                            📋 <fmt:message key="hr.assignment.section_title" />
                        </h2>
                    </div>
                    <div class="flex-center gap-10">
                        <form action="${pageContext.request.contextPath}/HrManagementController" method="GET" class="filter-form m-0">
                            <input type="hidden" name="tab" value="shifts" />
                            <input type="date" name="date" class="form-input form-input-sm" value="${FILTER_DATE}" onchange="this.form.submit()" />
                            <button type="submit" class="btn-secondary btn-sm">
                                🔍 <fmt:message key="hr.btn.view" />
                            </button>
                        </form>
                        <button type="button" class="btn-primary btn-sm" onclick="openAssignShiftModal()">
                            ➕ <fmt:message key="hr.btn.assign_shift" />
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><fmt:message key="hr.assign.label.employee" /></th>
                                <th><fmt:message key="hr.table.role" /></th>
                                <th><fmt:message key="hr.assign.label.shift" /></th>
                                <th><fmt:message key="hr.shift.label.time_range" /></th>
                                <th><fmt:message key="hr.table.attendance_status" /></th>
                                <th class="text-center"><fmt:message key="hr.table.action" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty DAILY_ASSIGNMENTS}">
                                    <c:forEach var="item" items="${DAILY_ASSIGNMENTS}" varStatus="loop">
                                        <tr>
                                            <td>${loop.index + 1}</td>
                                            <td class="font-bold">${item.fullName}</td>
                                            <td><span class="role-badge role-${item.role.toLowerCase()}">${item.role}</span></td>
                                            <td>${item.shiftName}</td>
                                            <td>${item.timeRange}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${!item.hasAttendance}">
                                                        <span class="status-badge badge-neutral"><fmt:message key="hr.status.not_checked_in" /></span>
                                                    </c:when>
                                                    <c:when test="${empty item.checkOutTime}">
                                                        <span class="status-badge badge-active"><fmt:message key="hr.status.working" /> (${item.checkInTimeStr})</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge badge-completed"><fmt:message key="hr.status.completed" /> (${item.workingHours}h)</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${!item.hasAttendance}">
                                                        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST" class="m-0" onsubmit="return confirm('<fmt:message key="hr.confirm.delete_assignment" />');">
                                                            <input type="hidden" name="action" value="deleteAssignment" />
                                                            <input type="hidden" name="assignmentId" value="${item.assignmentId}" />
                                                            <input type="hidden" name="tab" value="shifts" />
                                                            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />
                                                            <button type="submit" class="btn-danger btn-xs">
                                                                🗑️ <fmt:message key="hr.btn.remove" />
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted font-xs">🔒 <fmt:message key="hr.status.locked" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" class="text-center text-muted p-20">
                                            <fmt:message key="hr.empty.assignments" />
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <%-- ================= TAB 2: THEO DÕI ĐIỂM DANH & DUYỆT OT ================= --%>
        <div id="tab-tracking" class="tab-content ${ACTIVE_TAB == 'tracking' ? 'active' : 'd-none'}">
            <%-- Phần Danh sách Yêu cầu chờ duyệt --%>
            <div class="panel-card mb-25 border-warning">
                <div class="panel-header mb-15">
                    <h2 class="panel-title font-md text-warning">
                        ⚡ <fmt:message key="hr.tracking.pending_title" />
                    </h2>
                </div>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><fmt:message key="hr.assign.label.employee" /></th>
                                <th><fmt:message key="hr.assign.label.shift" /></th>
                                <th><fmt:message key="hr.assign.label.date" /></th>
                                <th><fmt:message key="hr.table.checkin" /></th>
                                <th><fmt:message key="hr.table.checkout" /></th>
                                <th><fmt:message key="hr.table.working_hours" /></th>
                                <th><fmt:message key="hr.table.ot_reason" /></th>
                                <th class="text-center"><fmt:message key="hr.table.approval_action" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty PENDING_APPROVALS}">
                                    <c:forEach var="req" items="${PENDING_APPROVALS}" varStatus="loop">
                                        <tr class="highlight-row-warning">
                                            <td>${loop.index + 1}</td>
                                            <td class="font-bold">${req.fullName}</td>
                                            <td>${req.shiftName}</td>
                                            <td>${req.workDate}</td>
                                            <td>
                                                ${req.checkInTimeStr}
                                                <c:if test="${req.checkInStatus == 'Pending Early Approval'}">
                                                    <span class="status-badge badge-warning font-xs"><fmt:message key="hr.status.pending_early" /></span>
                                                </c:if>
                                            </td>
                                            <td>
                                                ${req.checkOutTimeStr}
                                                <c:if test="${req.checkOutStatus == 'Pending OT Approval'}">
                                                    <span class="status-badge badge-warning font-xs"><fmt:message key="hr.status.pending_ot" /></span>
                                                </c:if>
                                            </td>
                                            <td>${req.workingHours}h</td>
                                            <td class="text-muted">${not empty req.otReason ? req.otReason : '-'}</td>
                                            <td class="text-center">
                                                <div class="flex-center gap-5">
                                                    <c:if test="${req.checkOutStatus == 'Pending OT Approval'}">
                                                        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST" class="m-0">
                                                            <input type="hidden" name="action" value="approveOt" />
                                                            <input type="hidden" name="attendanceId" value="${req.attendanceId}" />
                                                            <input type="hidden" name="tab" value="tracking" />
                                                            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />
                                                            <button type="submit" class="btn-primary btn-xs" title="<fmt:message key="hr.btn.approve_ot" />">
                                                                ✅ <fmt:message key="hr.btn.approve" />
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST" class="m-0">
                                                            <input type="hidden" name="action" value="rejectOt" />
                                                            <input type="hidden" name="attendanceId" value="${req.attendanceId}" />
                                                            <input type="hidden" name="tab" value="tracking" />
                                                            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />
                                                            <button type="submit" class="btn-danger btn-xs" title="<fmt:message key="hr.btn.reject_ot" />">
                                                                ❌ <fmt:message key="hr.btn.reject" />
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                    <c:if test="${req.checkInStatus == 'Pending Early Approval'}">
                                                        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST" class="m-0">
                                                            <input type="hidden" name="action" value="approveEarly" />
                                                            <input type="hidden" name="attendanceId" value="${req.attendanceId}" />
                                                            <input type="hidden" name="tab" value="tracking" />
                                                            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />
                                                            <button type="submit" class="btn-primary btn-xs" title="<fmt:message key="hr.btn.approve_early" />">
                                                                ✅ <fmt:message key="hr.btn.approve_early" />
                                                            </button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST" class="m-0">
                                                            <input type="hidden" name="action" value="rejectEarly" />
                                                            <input type="hidden" name="attendanceId" value="${req.attendanceId}" />
                                                            <input type="hidden" name="tab" value="tracking" />
                                                            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />
                                                            <button type="submit" class="btn-danger btn-xs" title="<fmt:message key="hr.btn.reject_early" />">
                                                                ❌ <fmt:message key="hr.btn.reject" />
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="text-center text-muted p-20">
                                            ✅ <fmt:message key="hr.empty.pending" />
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

            <%-- Bảng điểm danh toàn diện theo ngày --%>
            <div class="panel-card">
                <div class="panel-header flex-between align-center mb-15">
                    <h2 class="panel-title font-md">
                        📅 <fmt:message key="hr.tracking.daily_title" /> (${FILTER_DATE})
                    </h2>
                    <form action="${pageContext.request.contextPath}/HrManagementController" method="GET" class="filter-form m-0">
                        <input type="hidden" name="tab" value="tracking" />
                        <input type="date" name="date" class="form-input form-input-sm" value="${FILTER_DATE}" onchange="this.form.submit()" />
                        <button type="submit" class="btn-secondary btn-sm">
                            🔍 <fmt:message key="hr.btn.view" />
                        </button>
                    </form>
                </div>

                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><fmt:message key="hr.assign.label.employee" /></th>
                                <th><fmt:message key="hr.assign.label.shift" /></th>
                                <th><fmt:message key="hr.table.checkin" /></th>
                                <th><fmt:message key="hr.table.checkin_status" /></th>
                                <th><fmt:message key="hr.table.checkout" /></th>
                                <th><fmt:message key="hr.table.checkout_status" /></th>
                                <th><fmt:message key="hr.table.working_hours" /></th>
                                <th><fmt:message key="hr.table.approved_by" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty DAILY_ASSIGNMENTS}">
                                    <c:forEach var="row" items="${DAILY_ASSIGNMENTS}" varStatus="loop">
                                        <tr>
                                            <td>${loop.index + 1}</td>
                                            <td class="font-bold">${row.fullName}</td>
                                            <td>${row.shiftName} (${row.timeRange})</td>
                                            <td>${row.checkInTimeStr}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${row.checkInStatus == 'On Time'}">
                                                        <span class="status-badge badge-active"><fmt:message key="attendance.badge.ontime" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkInStatus == 'Late'}">
                                                        <span class="status-badge badge-danger"><fmt:message key="attendance.badge.late" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkInStatus == 'Pending Early Approval'}">
                                                        <span class="status-badge badge-warning"><fmt:message key="hr.status.pending_early" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkInStatus == 'Approved'}">
                                                        <span class="status-badge badge-active">✅ <fmt:message key="hr.status.approved" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkInStatus == 'Rejected'}">
                                                        <span class="status-badge badge-danger">❌ <fmt:message key="hr.status.rejected" /></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge badge-neutral">-</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${row.checkOutTimeStr}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${row.checkOutStatus == 'On Time'}">
                                                        <span class="status-badge badge-active"><fmt:message key="attendance.badge.ontime" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkOutStatus == 'Left Early'}">
                                                        <span class="status-badge badge-danger"><fmt:message key="attendance.badge.left_early" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkOutStatus == 'Pending OT Approval'}">
                                                        <span class="status-badge badge-warning"><fmt:message key="attendance.badge.pending_ot" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkOutStatus == 'Approved'}">
                                                        <span class="status-badge badge-active">✅ <fmt:message key="hr.status.approved" /></span>
                                                    </c:when>
                                                    <c:when test="${row.checkOutStatus == 'Rejected'}">
                                                        <span class="status-badge badge-danger">❌ <fmt:message key="hr.status.rejected" /></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge badge-neutral">-</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${not empty row.workingHours ? row.workingHours : 0}h</td>
                                            <td class="text-muted font-sm">${not empty row.approverName ? row.approverName : '-'}</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="text-center text-muted p-20">
                                            <fmt:message key="hr.empty.assignments" />
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <%-- ================= TAB 3: BẢNG TỔNG HỢP CÔNG THÁNG ================= --%>
        <div id="tab-summary" class="tab-content ${ACTIVE_TAB == 'summary' ? 'active' : 'd-none'}">
            <div class="panel-card">
                <div class="panel-header flex-between align-center mb-15">
                    <h2 class="panel-title font-md">
                        📊 <fmt:message key="hr.summary.section_title" /> (${CURRENT_MONTH}/${CURRENT_YEAR})
                    </h2>
                    <form action="${pageContext.request.contextPath}/HrManagementController" method="GET" class="filter-form m-0">
                        <input type="hidden" name="tab" value="summary" />
                        <label for="filterMonth" class="form-label mr-5 font-sm"><fmt:message key="attendance.filter.month" />:</label>
                        <select id="filterMonth" name="month" class="form-input form-input-sm">
                            <c:forEach var="m" begin="1" end="12">
                                <option value="${m}" ${CURRENT_MONTH == m ? 'selected' : ''}>${m}</option>
                            </c:forEach>
                        </select>
                        <label for="filterYear" class="form-label ml-10 mr-5 font-sm"><fmt:message key="attendance.filter.year" />:</label>
                        <select id="filterYear" name="year" class="form-input form-input-sm">
                            <c:forEach var="y" begin="2024" end="2030">
                                <option value="${y}" ${CURRENT_YEAR == y ? 'selected' : ''}>${y}</option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn-primary btn-sm ml-10">
                            🔍 <fmt:message key="hr.btn.view" />
                        </button>
                    </form>
                </div>

                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th><fmt:message key="hr.assign.label.employee" /></th>
                                <th><fmt:message key="hr.table.role" /></th>
                                <th class="text-center"><fmt:message key="hr.summary.total_assigned" /></th>
                                <th class="text-center"><fmt:message key="hr.summary.total_attended" /></th>
                                <th class="text-center"><fmt:message key="hr.summary.total_hours" /></th>
                                <th class="text-center"><fmt:message key="hr.summary.ontime_count" /></th>
                                <th class="text-center"><fmt:message key="hr.summary.late_count" /></th>
                                <th class="text-center"><fmt:message key="hr.summary.ot_count" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty MONTHLY_SUMMARY}">
                                    <c:forEach var="emp" items="${MONTHLY_SUMMARY}" varStatus="loop">
                                        <tr>
                                            <td>${loop.index + 1}</td>
                                            <td class="font-bold">${emp.fullName}</td>
                                            <td><span class="role-badge role-${emp.role.toLowerCase()}">${emp.role}</span></td>
                                            <td class="text-center font-bold">${emp.totalAssignedShifts}</td>
                                            <td class="text-center">${emp.totalAttendedShifts}</td>
                                            <td class="text-center font-bold text-primary">${emp.totalWorkingHours}h</td>
                                            <td class="text-center text-success">${emp.onTimeCount}</td>
                                            <td class="text-center text-danger">${emp.lateCount}</td>
                                            <td class="text-center text-warning">${emp.otCount}</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="text-center text-muted p-20">
                                            <fmt:message key="hr.empty.summary" />
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <jsp:include page="components/hr_shift_modal.jsp" />
    <jsp:include page="footer.jsp" />
</body>
</html>
