<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <div class="dispatch-container">
        <h2 class="page-title"><fmt:message key="dispatch.title" /></h2>

        <!-- Vùng hiển thị thông báo -->
        <c:if test="${not empty SUCCESS_MESSAGE}">
            <div class="alert alert-success"><fmt:message key="${SUCCESS_MESSAGE}" /></div>
        </c:if>
        <c:if test="${not empty ERROR_MESSAGE}">
            <div class="alert alert-danger"><fmt:message key="${ERROR_MESSAGE}" /></div>
        </c:if>

        <h3 class="section-title"><fmt:message key="dispatch.list.title" /></h3>
        <table class="table dispatch-table">
            <thead>
                <tr>
                    <th><fmt:message key="dispatch.table.ordercode" /></th>
                    <th><fmt:message key="dispatch.table.customer" /></th>
                    <th><fmt:message key="dispatch.table.address" /></th>
                    <th><fmt:message key="dispatch.table.amount" /></th>
                    <th><fmt:message key="dispatch.table.status" /></th>
                    <th><fmt:message key="dispatch.table.action" /></th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty UNASSIGNED_TASKS}">
                        <tr>
                            <td colspan="6" class="text-center empty-msg"><fmt:message key="dispatch.msg.empty" /></td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="task" items="${UNASSIGNED_TASKS}">
                            <tr>
                                <td><strong>${task.orderCode}</strong></td>
                                <td>
                                    ${task.customerName}
                                    <c:if test="${not empty task.customerPhone}">
                                        <br><small class="text-muted">${task.customerPhone}</small>
                                    </c:if>
                                </td>
                                <td>
                                    ${task.customerAddress}
                                    <c:if test="${not empty task.areaName}">
                                        <br><small class="badge badge-info">${task.areaName}</small>
                                    </c:if>
                                </td>
                                <td>${task.totalAmount} đ</td>
                                <td class="status-cell">
                                    <c:choose>
                                        <c:when test="${task.status == 'Waiting for dispatch'}">
                                            <span class="order-badge order-badge-dispatch"><fmt:message key="order.status.waiting_dispatch" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:message key="status.${task.status}" />
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="action-cell">
                                    <form action="DispatchController" method="POST" class="assign-form">
                                        <input type="hidden" name="taskId" value="${task.taskId}">
                                        
                                        <select name="shipperUsername" class="select-shipper" required>
                                            <option value="" disabled selected><fmt:message key="dispatch.select.default" /></option>
                                            
                                            <c:forEach var="shipper" items="${SHIPPER_LIST}">
                                                <c:choose>
                                                    <c:when test="${shipper.activeOrders >= shipper.maxOrders}">
                                                        <option value="${shipper.username}" disabled>
                                                            ${shipper.fullName} (<fmt:message key="dispatch.shipper.busy" />: ${shipper.activeOrders}/${shipper.maxOrders})
                                                        </option>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <option value="${shipper.username}">
                                                            ${shipper.fullName} (<fmt:message key="dispatch.shipper.free" />: ${shipper.activeOrders}/${shipper.maxOrders})
                                                        </option>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </select>
                                        
                                        <button type="submit" class="btn btn-assign"><fmt:message key="dispatch.btn.assign" /></button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>

        <hr class="section-divider" />

        <!-- BẢNG 2: DANH SÁCH SHIPPER TRỰC CHIẾN -->
        <h3 class="section-title"><fmt:message key="dispatch.shipper_list_title" /></h3>
        <table class="table dispatch-table">
            <thead>
                <tr>
                    <th><fmt:message key="dispatch.table.username" /></th>
                    <th><fmt:message key="dispatch.table.fullname" /></th>
                    <th><fmt:message key="dispatch.table.region" /></th>
                    <th><fmt:message key="dispatch.table.working_status" /></th>
                    <th><fmt:message key="dispatch.table.active_orders" /></th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty SHIPPER_LIST}">
                        <tr>
                            <td colspan="5" class="text-center empty-msg"><fmt:message key="dispatch.msg.no_active_shippers" /></td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="shipper" items="${SHIPPER_LIST}">
                            <tr>
                                <td>${shipper.username}</td>
                                <td>${shipper.fullName}</td>
                                <td>${shipper.address}</td>
                                <td class="status-cell">
                                    <strong><fmt:message key="shipper.work.${shipper.workingStatus}" /></strong>
                                </td>
                                <td>${shipper.activeOrders} / ${shipper.maxOrders} <fmt:message key="dispatch.label.orders" /></td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</main>

<jsp:include page="footer.jsp" />