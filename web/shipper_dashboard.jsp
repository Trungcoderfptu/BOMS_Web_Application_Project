<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />
<jsp:include page="components/shipper_complete_modal.jsp" />
<jsp:include page="components/shipper_fail_modal.jsp" />

<%-- Đơn đang giao đầu tiên (chưa đóng) -> thẻ tập trung trên cùng --%>
<c:set var="focusTask" value="${null}" />
<c:forEach var="t" items="${MY_TASKS}">
    <c:if test="${empty focusTask and t.status != 'Delivered' and t.status != 'Returned'}"><c:set var="focusTask" value="${t}" /></c:if>
</c:forEach>

<%-- Bố cục G: điện thoại, 1 việc 1 màn. Trên cùng: đơn đang giao (gọi khách, giao xong / thất bại);
     dưới: các nhóm đơn; đáy: thanh điều hướng tới từng nhóm. Giữ nguyên action claimTask/acceptTask/rejectTask và 2 popup --%>
<main class="main-content ship-page">
    <c:if test="${not empty ERROR_MESSAGE}">
        <div class="alert alert-danger"><fmt:message key="${ERROR_MESSAGE}" /></div>
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <div class="alert alert-success"><fmt:message key="${SUCCESS_MESSAGE}" /></div>
    </c:if>

    <c:if test="${not empty focusTask}">
        <section class="panel ship-focus">
            <p class="subnav-title"><fmt:message key="ship.focus.title" /></p>
            <div class="kanban-card-head"><span>${focusTask.orderCode}</span><span>${focusTask.totalAmount} đ</span></div>
            <p class="ship-focus-name"><c:out value="${focusTask.customerName}" /></p>
            <p class="muted"><c:out value="${focusTask.customerAddress}" /> <c:if test="${not empty focusTask.areaName}">· <c:out value="${focusTask.areaName}" /></c:if></p>
            <c:if test="${not empty focusTask.customerPhone}">
                <a href="tel:${fn:escapeXml(focusTask.customerPhone)}" class="btn-plain ship-call"><fmt:message key="ship.focus.call" /> <c:out value="${focusTask.customerPhone}" /></a>
            </c:if>
            <div class="ship-focus-actions">
                <button type="button" class="btn-next btn-open-complete-modal" data-task-id="${focusTask.taskId}" data-order-code="${focusTask.orderCode}"><fmt:message key="shipper.btn.complete" /></button>
                <button type="button" class="btn-plain btn-open-fail-modal" data-task-id="${focusTask.taskId}" data-order-code="${focusTask.orderCode}"><fmt:message key="shipper.btn.fail" /></button>
            </div>
        </section>
    </c:if>

    <section id="ship-mine" class="ship-section">
        <h3 class="panel-title"><fmt:message key="shipper.mytasks.title" /> (${fn:length(MY_TASKS)})</h3>
        <c:choose>
            <c:when test="${empty MY_TASKS}"><p class="panel todo-empty"><fmt:message key="shipper.mytasks.empty" /></p></c:when>
            <c:otherwise>
                <c:forEach var="task" items="${MY_TASKS}">
                    <article class="kanban-card">
                        <div class="kanban-card-head"><span>${task.orderCode}</span><span class="muted"><fmt:message key="status.${task.status}" /></span></div>
                        <div><c:out value="${task.customerName}" /> · <c:out value="${task.customerAddress}" /></div>
                        <c:if test="${task.status != 'Delivered' && task.status != 'Returned' && task.taskId != focusTask.taskId}">
                            <div class="ship-focus-actions">
                                <button type="button" class="btn-next btn-open-complete-modal" data-task-id="${task.taskId}" data-order-code="${task.orderCode}"><fmt:message key="shipper.btn.complete" /></button>
                                <button type="button" class="btn-plain btn-open-fail-modal" data-task-id="${task.taskId}" data-order-code="${task.orderCode}"><fmt:message key="shipper.btn.fail" /></button>
                            </div>
                        </c:if>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="ship-pending" class="ship-section">
        <h3 class="panel-title"><fmt:message key="shipper.pending.title" /> (${fn:length(PENDING_TASKS)})</h3>
        <c:choose>
            <c:when test="${empty PENDING_TASKS}"><p class="panel todo-empty"><fmt:message key="shipper.pending.empty" /></p></c:when>
            <c:otherwise>
                <c:forEach var="task" items="${PENDING_TASKS}">
                    <article class="kanban-card">
                        <div class="kanban-card-head"><span>${task.orderCode}</span><span>${task.totalAmount} đ</span></div>
                        <div><c:out value="${task.customerName}" /> · <c:out value="${task.customerAddress}" /></div>
                        <div class="ship-focus-actions">
                            <form action="ShipperController" method="POST" class="m-0">
                                <input type="hidden" name="action" value="acceptTask">
                                <input type="hidden" name="txtTaskId" value="${task.taskId}">
                                <button type="submit" class="btn-next"><fmt:message key="shipper.btn.accept" /></button>
                            </form>
                            <form action="ShipperController" method="POST" class="m-0 form-reject-task" data-confirm-msg="<fmt:message key="shipper.confirm.reject" />">
                                <input type="hidden" name="action" value="rejectTask">
                                <input type="hidden" name="txtTaskId" value="${task.taskId}">
                                <button type="submit" class="btn-plain"><fmt:message key="shipper.btn.reject" /></button>
                            </form>
                        </div>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="ship-new" class="ship-section">
        <h3 class="panel-title"><fmt:message key="shipper.available.title" /> (${fn:length(AVAILABLE_TASKS)})</h3>
        <c:choose>
            <c:when test="${empty AVAILABLE_TASKS}"><p class="panel todo-empty"><fmt:message key="shipper.available.empty" /></p></c:when>
            <c:otherwise>
                <c:forEach var="task" items="${AVAILABLE_TASKS}">
                    <article class="kanban-card">
                        <div class="kanban-card-head"><span>${task.orderCode}</span><span>${task.totalAmount} đ</span></div>
                        <div><c:out value="${task.customerName}" /> · <c:out value="${task.customerAddress}" /></div>
                        <form action="ShipperController" method="POST" class="m-0 ship-focus-actions">
                            <input type="hidden" name="action" value="claimTask">
                            <input type="hidden" name="txtTaskId" value="${task.taskId}">
                            <button type="submit" class="btn-next"><fmt:message key="shipper.btn.claim" /></button>
                        </form>
                    </article>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- Thanh điều hướng đáy màn hình (chỉ hiện trên điện thoại) --%>
    <nav class="bottom-nav" aria-label="<fmt:message key="nav.modules" />">
        <a href="#ship-mine"><fmt:message key="ship.nav.mine" /> (${fn:length(MY_TASKS)})</a>
        <a href="#ship-pending"><fmt:message key="ship.nav.pending" /> (${fn:length(PENDING_TASKS)})</a>
        <a href="#ship-new"><fmt:message key="ship.nav.new" /> (${fn:length(AVAILABLE_TASKS)})</a>
    </nav>
</main>

<jsp:include page="footer.jsp" />
