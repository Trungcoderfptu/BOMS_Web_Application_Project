<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<%-- Gom tên khu vực (không trùng) của các đơn chờ gán, ngăn bởi "|"; đơn chưa có khu vực gom vào "-" --%>
<c:set var="areas" value="|" />
<c:forEach var="task" items="${UNASSIGNED_TASKS}">
    <c:set var="areaKey" value="${empty task.areaName ? '-' : fn:replace(task.areaName, '|', '/')}" />
    <c:set var="needle" value="|${areaKey}|" />
    <c:if test="${not fn:contains(areas, needle)}">
        <c:set var="areas" value="${areas}${areaKey}|" />
    </c:if>
</c:forEach>

<%-- Bố cục F: khu vực + hàng chờ. Trái: mỗi khu vực 1 cột đơn chờ gán (chọn shipper ngay trên thẻ);
     phải: tải từng shipper + đơn đã gán đang chờ shipper nhận (thu hồi được). Giữ nguyên form gán / thu hồi của DispatchController --%>
<main class="main-content">
    <div class="page-head">
        <h2><fmt:message key="dispatch.title" /></h2>
    </div>

    <c:if test="${not empty SUCCESS_MESSAGE}">
        <div class="alert alert-success"><fmt:message key="${SUCCESS_MESSAGE}" /></div>
    </c:if>
    <c:if test="${not empty ERROR_MESSAGE}">
        <div class="alert alert-danger"><fmt:message key="${ERROR_MESSAGE}" /></div>
    </c:if>

    <div class="dispatch-board">
        <section>
            <h3 class="panel-title"><fmt:message key="dispatch.list.title" /> (${fn:length(UNASSIGNED_TASKS)})</h3>
            <c:choose>
                <c:when test="${empty UNASSIGNED_TASKS}">
                    <p class="panel todo-empty"><fmt:message key="dispatch.msg.empty" /></p>
                </c:when>
                <c:otherwise>
                    <div class="area-columns">
                        <c:forTokens var="area" items="${areas}" delims="|">
                            <div class="kanban-col">
                                <div class="kanban-col-head">
                                    <span><c:choose><c:when test="${area == '-'}"><fmt:message key="dispatch.area.none" /></c:when><c:otherwise><c:out value="${area}" /></c:otherwise></c:choose></span>
                                </div>
                                <c:forEach var="task" items="${UNASSIGNED_TASKS}">
                                    <c:set var="taskArea" value="${empty task.areaName ? '-' : fn:replace(task.areaName, '|', '/')}" />
                                    <c:if test="${taskArea == area}">
                                        <article class="kanban-card">
                                            <div class="kanban-card-head"><span>${task.orderCode}</span><span>${task.totalAmount} đ</span></div>
                                            <div><c:out value="${task.customerName}" /> <span class="muted"><c:out value="${task.customerPhone}" /></span></div>
                                            <div class="muted"><c:out value="${task.customerAddress}" /></div>
                                            <form action="DispatchController" method="POST" class="assign-inline">
                                                <input type="hidden" name="taskId" value="${task.taskId}">
                                                <select name="shipperUsername" class="filter-input" required aria-label="<fmt:message key="dispatch.select.default" />">
                                                    <option value="" disabled selected><fmt:message key="dispatch.select.default" /></option>
                                                    <c:forEach var="shipper" items="${SHIPPER_LIST}">
                                                        <option value="${shipper.username}" ${shipper.activeOrders >= shipper.maxOrders ? 'disabled' : ''}>
                                                            <c:out value="${shipper.fullName}" /> (${shipper.activeOrders}/${shipper.maxOrders})
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <button type="submit" class="btn-next"><fmt:message key="dispatch.btn.assign" /></button>
                                            </form>
                                        </article>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:forTokens>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <aside class="dispatch-side">
            <section class="panel">
                <h3 class="panel-title"><fmt:message key="dispatch.shipper_list_title" /></h3>
                <c:choose>
                    <c:when test="${empty SHIPPER_LIST}">
                        <p class="todo-empty"><fmt:message key="dispatch.msg.no_active_shippers" /></p>
                    </c:when>
                    <c:otherwise>
                        <ul class="md-rows">
                            <c:forEach var="shipper" items="${SHIPPER_LIST}">
                                <li class="shipper-load">
                                    <div class="md-row-top">
                                        <strong><c:out value="${shipper.fullName}" /></strong>
                                        <span>${shipper.activeOrders}/${shipper.maxOrders}</span>
                                    </div>
                                    <meter min="0" max="${shipper.maxOrders}" value="${shipper.activeOrders}" low="${shipper.maxOrders - 1}" high="${shipper.maxOrders}" optimum="0"></meter>
                                    <div class="muted"><fmt:message key="shipper.work.${shipper.workingStatus}" /> · <c:out value="${shipper.address}" /></div>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="panel">
                <h3 class="panel-title"><fmt:message key="dispatch.pending_list_title" /> (${fn:length(ASSIGNED_PENDING_TASKS)})</h3>
                <c:choose>
                    <c:when test="${empty ASSIGNED_PENDING_TASKS}">
                        <p class="todo-empty"><fmt:message key="dispatch.msg.no_pending_tasks" /></p>
                    </c:when>
                    <c:otherwise>
                        <ul class="md-rows">
                            <c:forEach var="task" items="${ASSIGNED_PENDING_TASKS}">
                                <li class="shipper-load">
                                    <div class="md-row-top">
                                        <strong>${task.orderCode}</strong>
                                        <span class="muted"><c:out value="${task.shipperFullName}" /></span>
                                    </div>
                                    <div class="muted"><c:out value="${task.customerAddress}" /></div>
                                    <form action="DispatchController" method="POST" class="m-0 form-revoke-task" data-confirm-msg="<fmt:message key="dispatch.confirm.revoke" />">
                                        <input type="hidden" name="action" value="revoke" />
                                        <input type="hidden" name="taskId" value="${task.taskId}" />
                                        <button type="submit" class="btn-plain"><fmt:message key="dispatch.btn.revoke" /></button>
                                    </form>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </section>
        </aside>
    </div>
</main>

<jsp:include page="footer.jsp" />
