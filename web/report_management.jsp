<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%-- Giữ bộ lọc khi bấm sang ticket khác --%>
<c:url var="filterQs" value="">
    <c:param name="filterStatus" value="${CURRENT_FILTER_STATUS}" />
    <c:param name="filterDate" value="${CURRENT_FILTER_DATE}" />
    <c:param name="filterCategory" value="${CURRENT_FILTER_CATEGORY}" />
</c:url>

<%-- Bố cục B: danh sách + chi tiết. Trái: lọc + danh sách ticket (bấm để mở); phải: chi tiết + xử lý ticket đang chọn (UC-08) --%>
<main class="main-content">
    <div class="page-head">
        <h2><fmt:message key="report.management.heading" /></h2>
    </div>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
    </c:if>

    <div class="md-layout">
        <section class="panel md-list">
            <form action="${ctx}/ReportManagementController" method="GET" class="md-filter">
                <select id="filterStatus" name="filterStatus" class="filter-input" aria-label="<fmt:message key="report.label.status" />" data-auto-submit="true">
                    <option value="ALL" ${CURRENT_FILTER_STATUS == 'ALL' ? 'selected' : ''}><fmt:message key="report.filter.all" /></option>
                    <option value="Pending" ${CURRENT_FILTER_STATUS == 'Pending' ? 'selected' : ''}><fmt:message key="report.status.Pending" /></option>
                    <option value="In Progress" ${CURRENT_FILTER_STATUS == 'In Progress' ? 'selected' : ''}><fmt:message key="report.status.In_Progress" /></option>
                    <option value="Resolved" ${CURRENT_FILTER_STATUS == 'Resolved' ? 'selected' : ''}><fmt:message key="report.status.Resolved" /></option>
                    <option value="Rejected" ${CURRENT_FILTER_STATUS == 'Rejected' ? 'selected' : ''}><fmt:message key="report.status.Rejected" /></option>
                </select>
                <select id="filterCategory" name="filterCategory" class="filter-input" aria-label="<fmt:message key="report.label.category" />" data-auto-submit="true">
                    <option value=""><fmt:message key="report.filter.all" /></option>
                    <c:forEach var="cat" items="${CATEGORY_LIST}">
                        <option value="${cat.categoryId}" ${CURRENT_FILTER_CATEGORY == cat.categoryId ? 'selected' : ''}><c:out value="${cat.categoryName}" /></option>
                    </c:forEach>
                </select>
                <input type="date" id="filterDate" name="filterDate" value="${fn:escapeXml(CURRENT_FILTER_DATE)}" class="filter-input" aria-label="<fmt:message key="report.label.submitted_date" />" data-auto-submit="true" />
            </form>

            <c:choose>
                <c:when test="${empty REPORT_LIST}">
                    <p class="todo-empty"><fmt:message key="report.management.empty" /></p>
                </c:when>
                <c:otherwise>
                    <ul class="md-rows">
                        <c:forEach var="r" items="${REPORT_LIST}">
                            <li>
                                <a href="${ctx}/ReportManagementController${filterQs}&reportId=${r.reportId}" class="md-row ${REPORT_DETAIL.reportId == r.reportId ? 'active' : ''}">
                                    <span class="md-row-top">
                                        <strong><c:out value="${r.title}" /></strong>
                                        <span class="order-badge report-badge-${fn:toLowerCase(r.statusKey)}"><fmt:message key="report.status.${r.statusKey}" /></span>
                                    </span>
                                    <span class="muted"><c:out value="${r.ticketId}" /> · <c:out value="${r.categoryName}" /> · ${r.submittedAt}</span>
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>

        <c:choose>
            <c:when test="${not empty REPORT_DETAIL}">
                <jsp:include page="components/report_detail_panel.jsp" />
            </c:when>
            <c:otherwise>
                <section class="panel md-detail md-placeholder"><fmt:message key="md.pick.report" /></section>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<jsp:include page="footer.jsp" />
