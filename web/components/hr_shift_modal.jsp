<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<%-- ================= MODAL TẠO / SỬA KHUNG GIỜ CA ================= --%>
<div id="shiftModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 id="shiftModalTitle" class="modal-title">
                <fmt:message key="hr.shift.modal.create_title" />
            </h3>
            <button type="button" class="btn-close-modal" data-close-modal="shiftModal">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/HrManagementController" method="POST">
            <input type="hidden" name="action" id="shiftModalAction" value="createShift" />
            <input type="hidden" name="shiftId" id="shiftModalId" value="0" />
            <input type="hidden" name="tab" value="shifts" />
            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />

            <div class="form-group mb-15">
                <label for="shiftNameInput" class="form-label font-bold">
                    <fmt:message key="hr.shift.label.name" /> <span class="text-danger">*</span>
                </label>
                <input type="text" id="shiftNameInput" name="shiftName" class="form-input" required maxlength="50" placeholder="<fmt:message key="hr.shift.placeholder.name" />" />
            </div>

            <div class="flex-gap-15 mb-20">
                <div class="w-50">
                    <label for="startTimeInput" class="form-label font-bold">
                        <fmt:message key="hr.shift.label.start_time" /> <span class="text-danger">*</span>
                    </label>
                    <input type="time" id="startTimeInput" name="startTime" class="form-input" required />
                </div>
                <div class="w-50">
                    <label for="endTimeInput" class="form-label font-bold">
                        <fmt:message key="hr.shift.label.end_time" /> <span class="text-danger">*</span>
                    </label>
                    <input type="time" id="endTimeInput" name="endTime" class="form-input" required />
                </div>
            </div>

            <div class="modal-footer flex-end gap-10">
                <button type="button" class="btn-secondary" data-close-modal="shiftModal">
                    <fmt:message key="hr.btn.cancel" />
                </button>
                <button type="submit" class="btn-primary" id="shiftModalSubmitBtn">
                    <fmt:message key="hr.btn.save" />
                </button>
            </div>
        </form>
    </div>
</div>

<%-- ================= MODAL PHÂN CA LÀM VIỆC (HỖ TRỢ PHÂN CA ĐƠN & HÀNG LOẠT - TASK 14.1) ================= --%>
<div id="assignShiftModal" class="modal-overlay d-none">
    <div class="modal-card modal-card-wide">
        <div class="modal-header">
            <h3 class="modal-title">
                <fmt:message key="hr.assign.modal.title" />
            </h3>
            <button type="button" class="btn-close-modal" data-close-modal="assignShiftModal">&times;</button>
        </div>
        <form id="assignShiftForm" action="${pageContext.request.contextPath}/HrManagementController" method="POST">
            <input type="hidden" name="action" id="assignFormAction" value="assignShift" />
            <input type="hidden" name="tab" value="shifts" />
            <input type="hidden" name="filterDate" value="${FILTER_DATE}" />

            <%-- Chuyển đổi chế độ: Phân ca đơn lẻ vs Phân ca liên hoàn --%>
            <div class="assign-mode-toggle mb-15">
                <button type="button" id="btnModeSingle" class="btn-mode-tab active" data-assign-mode="single">
                    📌 <fmt:message key="hr.assign.mode.single" />
                </button>
                <button type="button" id="btnModeBulk" class="btn-mode-tab" data-assign-mode="bulk">
                    🗓️ <fmt:message key="hr.assign.mode.bulk" />
                </button>
            </div>

            <div class="form-group mb-15">
                <label for="assignUserIdSelect" class="form-label font-bold">
                    <fmt:message key="hr.assign.label.employee" /> <span class="text-danger">*</span>
                </label>
                <select id="assignUserIdSelect" name="userId" class="form-input" required>
                    <option value=""><fmt:message key="hr.assign.placeholder.select_employee" /></option>
                    <c:forEach var="staff" items="${STAFF_LIST}">
                        <option value="${staff.userId}">
                            ${staff.fullName} (${staff.role})
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group mb-15">
                <label for="assignShiftIdSelect" class="form-label font-bold">
                    <fmt:message key="hr.assign.label.shift" /> <span class="text-danger">*</span>
                </label>
                <select id="assignShiftIdSelect" name="shiftId" class="form-input" required>
                    <option value=""><fmt:message key="hr.assign.placeholder.select_shift" /></option>
                    <c:forEach var="shift" items="${SHIFTS}">
                        <option value="${shift.shiftId}">
                            ${shift.shiftName} (${shift.timeRange})
                        </option>
                    </c:forEach>
                </select>
            </div>

            <%-- Chế độ 1: Phân ca 1 ngày --%>
            <div id="sectionSingleDate" class="form-group mb-20">
                <label for="assignWorkDateInput" class="form-label font-bold">
                    <fmt:message key="hr.assign.label.date" /> <span class="text-danger">*</span>
                </label>
                <input type="date" id="assignWorkDateInput" name="workDate" class="form-input" value="${FILTER_DATE}" required />
            </div>

            <%-- Chế độ 2: Phân ca liên hoàn theo dải ngày & thứ (Task 14.1) --%>
            <div id="sectionBulkDate" class="bulk-assign-section d-none mb-20">
                <div class="flex-gap-15 mb-15">
                    <div class="w-50">
                        <label for="bulkStartDateInput" class="form-label font-bold">
                            <fmt:message key="hr.assign.label.start_date" /> <span class="text-danger">*</span>
                        </label>
                        <input type="date" id="bulkStartDateInput" name="startDate" class="form-input" value="${FILTER_DATE}" />
                    </div>
                    <div class="w-50">
                        <label for="bulkEndDateInput" class="form-label font-bold">
                            <fmt:message key="hr.assign.label.end_date" /> <span class="text-danger">*</span>
                        </label>
                        <input type="date" id="bulkEndDateInput" name="endDate" class="form-input" value="${FILTER_DATE}" />
                    </div>
                </div>

                <div class="form-group">
                    <div class="flex-between align-center mb-5">
                        <label class="form-label font-bold m-0">
                            <fmt:message key="hr.assign.label.days_of_week" /> <span class="text-danger">*</span>
                        </label>
                        <div class="flex-gap-5">
                            <button type="button" class="btn-quick-select" data-quick-select="all">
                                <fmt:message key="hr.assign.btn.select_all_weekdays" />
                            </button>
                            <button type="button" class="btn-quick-select" data-quick-select="workdays">
                                <fmt:message key="hr.assign.btn.select_mon_to_fri" />
                            </button>
                        </div>
                    </div>
                    <div class="days-checkbox-grid">
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="1" class="day-cb" checked />
                            <span><fmt:message key="hr.assign.day.mon" /></span>
                        </label>
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="2" class="day-cb" checked />
                            <span><fmt:message key="hr.assign.day.tue" /></span>
                        </label>
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="3" class="day-cb" checked />
                            <span><fmt:message key="hr.assign.day.wed" /></span>
                        </label>
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="4" class="day-cb" checked />
                            <span><fmt:message key="hr.assign.day.thu" /></span>
                        </label>
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="5" class="day-cb" checked />
                            <span><fmt:message key="hr.assign.day.fri" /></span>
                        </label>
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="6" class="day-cb" />
                            <span><fmt:message key="hr.assign.day.sat" /></span>
                        </label>
                        <label class="day-checkbox-label">
                            <input type="checkbox" name="daysOfWeek" value="7" class="day-cb" />
                            <span><fmt:message key="hr.assign.day.sun" /></span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="modal-footer flex-end gap-10">
                <button type="button" class="btn-secondary" data-close-modal="assignShiftModal">
                    <fmt:message key="hr.btn.cancel" />
                </button>
                <button type="submit" class="btn-primary" id="btnSubmitAssign">
                    <fmt:message key="hr.btn.assign" />
                </button>
            </div>
        </form>
    </div>
</div>
