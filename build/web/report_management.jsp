<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <h2><fmt:message key="report.management.heading" /></h2>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
    </c:if>

    <%-- Bộ lọc theo Trạng thái, Ngày, Loại (UC-08 Other Information) --%>
    <form action="${pageContext.request.contextPath}/ReportManagementController" method="GET" class="order-filter-panel">
        <div class="filter-item">
            <label for="filterStatus"><fmt:message key="report.label.status" /></label>
            <select id="filterStatus" name="filterStatus" class="filter-input">
                <option value="ALL" ${CURRENT_FILTER_STATUS == 'ALL' ? 'selected' : ''}><fmt:message key="report.filter.all" /></option>
                <option value="Pending" ${CURRENT_FILTER_STATUS == 'Pending' ? 'selected' : ''}><fmt:message key="report.status.Pending" /></option>
                <option value="In Progress" ${CURRENT_FILTER_STATUS == 'In Progress' ? 'selected' : ''}><fmt:message key="report.status.In_Progress" /></option>
                <option value="Resolved" ${CURRENT_FILTER_STATUS == 'Resolved' ? 'selected' : ''}><fmt:message key="report.status.Resolved" /></option>
                <option value="Rejected" ${CURRENT_FILTER_STATUS == 'Rejected' ? 'selected' : ''}><fmt:message key="report.status.Rejected" /></option>
            </select>
        </div>
        <div class="filter-item">
            <label for="filterDate"><fmt:message key="report.label.submitted_date" /></label>
            <input type="date" id="filterDate" name="filterDate" value="${fn:escapeXml(CURRENT_FILTER_DATE)}" class="filter-input" />
        </div>
        <div class="filter-item">
            <label for="filterCategory"><fmt:message key="report.label.category" /></label>
            <select id="filterCategory" name="filterCategory" class="filter-input">
                <option value=""><fmt:message key="report.filter.all" /></option>
                <c:forEach var="cat" items="${CATEGORY_LIST}">
                    <option value="${cat.categoryId}" ${CURRENT_FILTER_CATEGORY == cat.categoryId ? 'selected' : ''}><c:out value="${cat.categoryName}" /></option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="btn-status-save"><fmt:message key="report.btn.filter" /></button>
    </form>

    <c:choose>
        <c:when test="${empty REPORT_LIST}">
            <p class="text-center"><fmt:message key="report.management.empty" /></p>
        </c:when>
        <c:otherwise>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th><fmt:message key="report.label.ticket" /></th>
                        <th><fmt:message key="report.label.category" /></th>
                        <th><fmt:message key="report.label.title" /></th>
                        <th><fmt:message key="report.label.submitted_at" /></th>
                        <th><fmt:message key="report.label.evidence_count" /></th>
                        <th><fmt:message key="report.label.status" /></th>
                        <th><fmt:message key="report.label.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="r" items="${REPORT_LIST}">
                        <tr>
                            <td><strong><c:out value="${r.ticketId}" /></strong></td>
                            <td><c:out value="${r.categoryName}" /></td>
                            <td><c:out value="${r.title}" /></td>
                            <td>${r.submittedAt}</td>
                            <td class="text-center">${r.evidenceCount}</td>
                            <td><span class="order-badge report-badge-${fn:toLowerCase(r.statusKey)}"><fmt:message key="report.status.${r.statusKey}" /></span></td>
                            <td><a href="${pageContext.request.contextPath}/ReportManagementController?reportId=${r.reportId}" class="btn-status-save report-link-btn"><fmt:message key="report.btn.view" /></a></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="footer.jsp" />
