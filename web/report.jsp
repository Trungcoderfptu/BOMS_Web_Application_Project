<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <h2><fmt:message key="report.heading" /></h2>
    <p class="report-note"><fmt:message key="report.anonymous_note" /></p>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <div class="ticket-box">
            <p class="msg-success m-0"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
            <c:if test="${not empty LAST_TICKET_ID}">
                <p class="m-0"><fmt:message key="report.ticket.your_ticket" />: <strong class="ticket-code"><c:out value="${LAST_TICKET_ID}" /></strong></p>
                <p class="report-hint m-0"><fmt:message key="report.ticket.keep_hint" /></p>
            </c:if>
        </div>
    </c:if>

    <c:set var="activeTab" value="${ACTIVE_TAB == 'track' ? 'track' : 'submit'}" />
    <div class="tab-nav d-flex">
        <button type="button" class="tab-btn ${activeTab == 'submit' ? 'active' : ''}" data-tab="tab-submit"><fmt:message key="report.tab.submit" /></button>
        <button type="button" class="tab-btn ${activeTab == 'track' ? 'active' : ''}" data-tab="tab-track"><fmt:message key="report.tab.track" /></button>
    </div>

    <%-- ===== UC-07: Form gửi báo cáo ===== --%>
    <div id="tab-submit" class="tab-content ${activeTab == 'submit' ? 'active' : 'd-none'}">
        <form action="${pageContext.request.contextPath}/ReportController" method="POST" enctype="multipart/form-data" class="report-form">
            <div class="report-field">
                <label for="categoryId"><fmt:message key="report.label.category" /> *</label>
                <select id="categoryId" name="categoryId" class="filter-input ${ERROR_MESSAGE == 'E0300' ? 'input-error' : ''}" required>
                    <option value=""><fmt:message key="report.label.category.choose" /></option>
                    <c:forEach var="cat" items="${CATEGORY_LIST}">
                        <option value="${cat.categoryId}" ${param.categoryId == cat.categoryId ? 'selected' : ''}><c:out value="${cat.categoryName}" /></option>
                    </c:forEach>
                </select>
            </div>

            <div class="report-field">
                <label for="txtTitle"><fmt:message key="report.label.title" /> *</label>
                <input type="text" id="txtTitle" name="txtTitle" maxlength="200" value="${fn:escapeXml(param.txtTitle)}"
                       class="filter-input ${ERROR_MESSAGE == 'E0301' || ERROR_MESSAGE == 'E0303' ? 'input-error' : ''}" required />
            </div>

            <div class="report-field">
                <label for="txtDescription"><fmt:message key="report.label.description" /> *</label>
                <textarea id="txtDescription" name="txtDescription" rows="6"
                          class="filter-input ${ERROR_MESSAGE == 'E0302' ? 'input-error' : ''}" required><c:out value="${param.txtDescription}" /></textarea>
            </div>

            <div class="report-field">
                <label for="evidenceFiles"><fmt:message key="report.label.evidence" /> *</label>
                <input type="file" id="evidenceFiles" name="evidenceFiles" multiple required
                       accept=".jpg,.jpeg,.png,.mp4,.pdf,.docx"
                       data-max-files="5" data-max-size="20971520"
                       data-msg-files="<fmt:message key='E0307' />"
                       data-msg-size="<fmt:message key='E0306' />"
                       data-msg-type="<fmt:message key='E0305' />"
                       class="${ERROR_MESSAGE == 'E0304' || ERROR_MESSAGE == 'E0305' || ERROR_MESSAGE == 'E0306' || ERROR_MESSAGE == 'E0307' || ERROR_MESSAGE == 'E0315' ? 'input-error' : ''}" />
                <small class="report-hint"><fmt:message key="report.hint.evidence" /></small>
                <c:if test="${not empty ERROR_MESSAGE}">
                    <small class="report-hint"><fmt:message key="report.hint.reselect_files" /></small>
                </c:if>
            </div>

            <div class="report-actions">
                <button type="submit" class="btn-register-action m-0 w-auto"><fmt:message key="report.btn.submit" /></button>
                <%-- 7.1 Cancel Reporting: bỏ dữ liệu đã nhập và quay lại màn hình trước --%>
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn-report-cancel"><fmt:message key="report.btn.cancel" /></a>
            </div>
        </form>
    </div>

    <%-- ===== Tra cứu phản hồi theo Ticket ID (UC-08 POST-3) ===== --%>
    <div id="tab-track" class="tab-content ${activeTab == 'track' ? 'active' : 'd-none'}">
        <form action="${pageContext.request.contextPath}/ReportController" method="GET" class="order-filter-panel">
            <div class="filter-item">
                <label for="ticketId"><fmt:message key="report.label.ticket" /></label>
                <input type="text" id="ticketId" name="ticketId" value="${fn:escapeXml(param.ticketId)}" placeholder="TK-XXXXXXXX"
                       class="filter-input ${ERROR_MESSAGE == 'E0316' || ERROR_MESSAGE == 'E0309' ? 'input-error' : ''}" />
            </div>
            <button type="submit" class="btn-status-save"><fmt:message key="report.btn.track" /></button>
        </form>

        <c:if test="${not empty TICKET_RESULT}">
            <table class="admin-table report-info-table">
                <tr><th><fmt:message key="report.label.ticket" /></th><td><strong><c:out value="${TICKET_RESULT.ticketId}" /></strong></td></tr>
                <tr><th><fmt:message key="report.label.category" /></th><td><c:out value="${TICKET_RESULT.categoryName}" /></td></tr>
                <tr><th><fmt:message key="report.label.title" /></th><td><c:out value="${TICKET_RESULT.title}" /></td></tr>
                <tr><th><fmt:message key="report.label.status" /></th>
                    <td><span class="order-badge report-badge-${fn:toLowerCase(TICKET_RESULT.statusKey)}"><fmt:message key="report.status.${TICKET_RESULT.statusKey}" /></span></td></tr>
                <tr><th><fmt:message key="report.label.submitted_at" /></th><td>${TICKET_RESULT.submittedAt}</td></tr>
                <tr><th><fmt:message key="report.label.updated_at" /></th><td>${empty TICKET_RESULT.updatedAt ? '-' : TICKET_RESULT.updatedAt}</td></tr>
                <tr><th><fmt:message key="report.label.feedback" /></th>
                    <td class="report-pre">
                        <c:choose>
                            <c:when test="${not empty TICKET_RESULT.feedback}"><c:out value="${TICKET_RESULT.feedback}" /></c:when>
                            <c:otherwise><fmt:message key="report.feedback.none" /></c:otherwise>
                        </c:choose>
                    </td></tr>
            </table>
        </c:if>
    </div>
</main>

<jsp:include page="footer.jsp" />
