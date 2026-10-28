<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="bx" tagdir="/WEB-INF/tags"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<%-- Bố cục A: Kanban theo trạng thái. Mỗi cột = 1 nhóm trạng thái, mỗi đơn = 1 thẻ (bx:orderCard) có nút bước tiếp theo.
     Lọc ngày gửi lại Controller (GET filterDate); lọc loại đơn làm ở trình duyệt (main.js filterKanbanByType). --%>
<main class="main-content">
    <div class="page-head">
        <h2><fmt:message key="order.management.heading" /></h2>
    </div>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success text-center"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
    </c:if>

    <div class="kanban-toolbar">
        <div class="chip-filter">
            <button type="button" class="active" data-order-filter="ALL"><fmt:message key="order.management.tab.all" /> (${not empty ORDER_COUNTS.all ? ORDER_COUNTS.all : 0})</button>
            <button type="button" data-order-filter="Counter"><fmt:message key="order.management.tab.counter" /> (${not empty ORDER_COUNTS.counter ? ORDER_COUNTS.counter : 0})</button>
            <button type="button" data-order-filter="Table"><fmt:message key="order.management.tab.table" /> (${not empty ORDER_COUNTS.table ? ORDER_COUNTS.table : 0})</button>
            <button type="button" data-order-filter="Delivery"><fmt:message key="order.management.tab.delivery" /> (${not empty ORDER_COUNTS.delivery ? ORDER_COUNTS.delivery : 0})</button>
        </div>
        <form action="${pageContext.request.contextPath}/OrderManagementController" method="GET" class="m-0 flex-align-center">
            <input type="hidden" name="filterStatus" value="ALL" />
            <label for="filterDate"><fmt:message key="order.management.filter.date" /></label>
            <input type="date" id="filterDate" name="filterDate" value="${CURRENT_FILTER_DATE}" class="filter-input" data-auto-submit="true" />
        </form>
    </div>

    <%-- Đếm số đơn từng cột --%>
    <c:set var="nNew" value="0" /><c:set var="nPrep" value="0" /><c:set var="nShip" value="0" /><c:set var="nDone" value="0" /><c:set var="nCancel" value="0" />
    <c:forEach var="o" items="${ORDER_LIST}">
        <c:choose>
            <c:when test="${o.status == 'Accepted'}"><c:set var="nNew" value="${nNew + 1}" /></c:when>
            <c:when test="${o.status == 'Prepared'}"><c:set var="nPrep" value="${nPrep + 1}" /></c:when>
            <c:when test="${o.status == 'Paid' or o.status == 'Delivered'}"><c:set var="nDone" value="${nDone + 1}" /></c:when>
            <c:when test="${o.status == 'Cancelled' or o.status == 'Returned'}"><c:set var="nCancel" value="${nCancel + 1}" /></c:when>
            <c:otherwise><c:set var="nShip" value="${nShip + 1}" /></c:otherwise>
        </c:choose>
    </c:forEach>

    <div class="kanban">
        <section class="kanban-col">
            <div class="kanban-col-head"><span><fmt:message key="order.kanban.col.new" /></span><span class="kanban-count">${nNew}</span></div>
            <c:forEach var="o" items="${ORDER_LIST}"><c:if test="${o.status == 'Accepted'}"><bx:orderCard order="${o}" /></c:if></c:forEach>
            <c:if test="${nNew == 0}"><p class="kanban-empty"><fmt:message key="order.kanban.empty" /></p></c:if>
        </section>

        <section class="kanban-col">
            <div class="kanban-col-head"><span><fmt:message key="order.kanban.col.preparing" /></span><span class="kanban-count">${nPrep}</span></div>
            <c:forEach var="o" items="${ORDER_LIST}"><c:if test="${o.status == 'Prepared'}"><bx:orderCard order="${o}" /></c:if></c:forEach>
            <c:if test="${nPrep == 0}"><p class="kanban-empty"><fmt:message key="order.kanban.empty" /></p></c:if>
        </section>

        <section class="kanban-col">
            <div class="kanban-col-head"><span><fmt:message key="order.kanban.col.delivery" /></span><span class="kanban-count">${nShip}</span></div>
            <c:forEach var="o" items="${ORDER_LIST}">
                <c:if test="${o.status == 'Waiting for dispatch' or o.status == 'Waiting for delivery' or o.status == 'Picked up' or o.status == 'Delivering'}"><bx:orderCard order="${o}" /></c:if>
            </c:forEach>
            <c:if test="${nShip == 0}"><p class="kanban-empty"><fmt:message key="order.kanban.empty" /></p></c:if>
        </section>

        <section class="kanban-col">
            <div class="kanban-col-head"><span><fmt:message key="order.kanban.col.done" /></span><span class="kanban-count">${nDone}</span></div>
            <c:forEach var="o" items="${ORDER_LIST}"><c:if test="${o.status == 'Paid' or o.status == 'Delivered'}"><bx:orderCard order="${o}" /></c:if></c:forEach>
            <c:if test="${nDone == 0}"><p class="kanban-empty"><fmt:message key="order.kanban.empty" /></p></c:if>
        </section>

        <section class="kanban-col">
            <div class="kanban-col-head"><span><fmt:message key="order.kanban.col.cancelled" /></span><span class="kanban-count">${nCancel}</span></div>
            <c:forEach var="o" items="${ORDER_LIST}"><c:if test="${o.status == 'Cancelled' or o.status == 'Returned'}"><bx:orderCard order="${o}" /></c:if></c:forEach>
            <c:if test="${nCancel == 0}"><p class="kanban-empty"><fmt:message key="order.kanban.empty" /></p></c:if>
        </section>
    </div>

    <jsp:include page="components/staff_cancel_modal.jsp" />
    <jsp:include page="components/delivery_info_modal.jsp" />
    <jsp:include page="components/revert_delivery_modal.jsp" />
</main>
<jsp:include page="footer.jsp" />
