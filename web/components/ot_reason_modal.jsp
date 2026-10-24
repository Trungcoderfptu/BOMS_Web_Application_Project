<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<div id="otReasonModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0"><fmt:message key="attendance.modal.ot_title" /></h3>
            <button type="button" class="btn-modal-close" data-close-modal="otReasonModal">&times;</button>
        </div>
        <form id="otReasonForm" action="${pageContext.request.contextPath}/AttendanceController" method="POST" class="m-0">
            <input type="hidden" name="action" value="checkOut" />
            <div class="modal-body">
                <p class="modal-hint-text mb-15">
                    <fmt:message key="attendance.modal.ot_desc" />
                </p>
                <div class="form-group mb-15">
                    <label for="otReasonInput" class="form-label font-bold">
                        <fmt:message key="attendance.modal.ot_label" /> <span class="text-danger">*</span>
                    </label>
                    <textarea id="otReasonInput" name="otReason" rows="3" required
                              class="filter-input w-100"
                              placeholder="<fmt:message key="attendance.modal.ot_placeholder" />"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" data-close-modal="otReasonModal">
                    <fmt:message key="attendance.modal.btn_cancel" />
                </button>
                <button type="submit" class="btn-login m-0">
                    <fmt:message key="attendance.modal.btn_confirm" />
                </button>
            </div>
        </form>
    </div>
</div>
