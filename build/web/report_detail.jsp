<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <a href="${pageContext.request.contextPath}/ReportManagementController" class="report-back-link">&larr; <fmt:message key="report.btn.back_to_list" /></a>
    <h2><fmt:message key="report.detail.heading" />: <c:out value="${REPORT_DETAIL.ticketId}" /></h2>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>

    <%-- Bước 4: Hiển thị chi tiết báo cáo --%>
    <table class="admin-table report-info-table">
        <tr><th><fmt:message key="report.label.category" /></th><td><c:out value="${REPORT_DETAIL.categoryName}" /></td></tr>
        <tr><th><fmt:message key="report.label.status" /></th>
            <td><span class="order-badge report-badge-${fn:toLowerCase(REPORT_DETAIL.statusKey)}"><fmt:message key="report.status.${REPORT_DETAIL.statusKey}" /></span></td></tr>
        <tr><th><fmt:message key="report.label.submitted_at" /></th><td>${REPORT_DETAIL.submittedAt}</td></tr>
        <tr><th><fmt:message key="report.label.updated_at" /></th><td>${empty REPORT_DETAIL.updatedAt ? '-' : REPORT_DETAIL.updatedAt}</td></tr>
        <tr><th><fmt:message key="report.label.title" /></th><td><strong><c:out value="${REPORT_DETAIL.title}" /></strong></td></tr>
        <tr><th><fmt:message key="report.label.description" /></th><td class="report-pre"><c:out value="${REPORT_DETAIL.description}" /></td></tr>
        <tr><th><fmt:message key="report.label.evidence" /></th>
            <td>
                <c:choose>
                    <c:when test="${empty REPORT_DETAIL.evidences}"><fmt:message key="report.evidence.none" /></c:when>
                    <c:otherwise>
                        <%-- 8.2 Download Evidence --%>
                        <ul class="no-bullet evidence-list">
                            <c:forEach var="ev" items="${REPORT_DETAIL.evidences}">
                                <li>
                                    <a href="${pageContext.request.contextPath}/EvidenceController?id=${ev.evidenceId}&reportId=${REPORT_DETAIL.reportId}">
                                        <c:out value="${ev.fileName}" />
                                    </a>
                                    <span class="report-hint">(${fn:toUpperCase(ev.fileType)}, ${ev.fileSizeKb} KB)</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </td></tr>
        <tr><th><fmt:message key="report.label.feedback" /></th>
            <td class="report-pre">
                <c:choose>
                    <c:when test="${not empty REPORT_DETAIL.feedback}"><c:out value="${REPORT_DETAIL.feedback}" /></c:when>
                    <c:otherwise><fmt:message key="report.feedback.none" /></c:otherwise>
                </c:choose>
            </td></tr>
    </table>

    <c:choose>
        <c:when test="${REPORT_DETAIL.closed}">
            <p class="msg-warning mt-15"><fmt:message key="report.detail.closed" /></p>
        </c:when>
        <c:otherwise>
            <%-- Bước 6 - 8: Chọn trạng thái mới, nhập phản hồi và gửi --%>
            <h3 class="mt-15"><fmt:message key="report.detail.process_heading" /></h3>
            <form action="${pageContext.request.contextPath}/ReportManagementController" method="POST" class="report-form">
                <input type="hidden" name="reportId" value="${REPORT_DETAIL.reportId}" />
                <div class="report-field">
                    <label for="newStatus"><fmt:message key="report.label.new_status" /> *</label>
                    <select id="newStatus" name="newStatus" class="filter-input ${ERROR_MESSAGE == 'E0310' ? 'input-error' : ''}" required>
                        <option value="In Progress" ${param.newStatus == 'In Progress' ? 'selected' : ''}><fmt:message key="report.status.In_Progress" /></option>
                        <option value="Resolved" ${param.newStatus == 'Resolved' ? 'selected' : ''}><fmt:message key="report.status.Resolved" /></option>
                        <option value="Rejected" ${param.newStatus == 'Rejected' ? 'selected' : ''}><fmt:message key="report.status.Rejected" /></option>
                    </select>
                </div>
                <div class="report-field">
                    <label for="txtFeedback"><fmt:message key="report.label.feedback_notes" /></label>
                    <textarea id="txtFeedback" name="txtFeedback" rows="5"
                              class="filter-input ${ERROR_MESSAGE == 'E0311' ? 'input-error' : ''}"><c:out value="${param.txtFeedback}" /></textarea>
                    <small class="report-hint"><fmt:message key="report.hint.reject_feedback" /></small>
                </div>
                <div class="report-actions">
                    <button type="submit" class="btn-register-action m-0 w-auto"><fmt:message key="report.btn.submit_update" /></button>
                    <%-- 8.1 Cancel Action: bỏ dữ liệu đã nhập, giữ trạng thái cũ, quay lại màn hình chi tiết --%>
                    <a href="${pageContext.request.contextPath}/ReportManagementController?reportId=${REPORT_DETAIL.reportId}" class="btn-report-cancel"><fmt:message key="report.btn.cancel" /></a>
                </div>
            </form>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="footer.jsp" />
