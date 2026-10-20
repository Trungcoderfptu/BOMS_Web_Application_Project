<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />
<jsp:include page="components/shipper_complete_modal.jsp" />
<jsp:include page="components/shipper_fail_modal.jsp" />

<main class="main-content">
    <div class="dispatch-container">
        <h2 class="page-title"><fmt:message key="shipper.title" />: ${sessionScope.USER_SESSION.fullName}</h2>

        <c:if test="${not empty ERROR_MESSAGE}">
            <div class="alert alert-danger"><fmt:message key="${ERROR_MESSAGE}" /></div>
        </c:if>

        <c:if test="${not empty SUCCESS_MESSAGE}">
            <div class="alert alert-success"><fmt:message key="${SUCCESS_MESSAGE}" /></div>
        </c:if>

        <!-- BẢNG 1: ĐƠN HÀNG MỚI CHỜ TIẾP NHẬN TRONG KHU VỰC -->
        <div class="task-section available-section">
            <h3 class="section-title"><fmt:message key="shipper.available.title" /></h3>
            <table class="table dispatch-table">
                <thead>
                    <tr>
                        <th><fmt:message key="shipper.table.ordercode" /></th>
                        <th><fmt:message key="shipper.table.customer" /></th>
                        <th><fmt:message key="shipper.table.address" /></th>
                        <th><fmt:message key="shipper.table.amount" /></th>
                        <th><fmt:message key="shipper.table.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty AVAILABLE_TASKS}">
                            <tr>
                                <td colspan="5" class="text-center empty-msg"><fmt:message key="shipper.available.empty" /></td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="task" items="${AVAILABLE_TASKS}">
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
                                    <td class="action-cell">
                                        <form action="ShipperController" method="POST" class="inline-form">
                                            <input type="hidden" name="action" value="claimTask">
                                            <input type="hidden" name="txtTaskId" value="${task.taskId}">
                                            <button type="submit" class="btn btn-claim"><fmt:message key="shipper.btn.claim" /></button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <hr class="section-divider" />

        <!-- BẢNG 2: ĐƠN ĐƯỢC ĐIỀU PHỐI GIAO (CHỜ XÁC NHẬN) -->
        <div class="task-section pending-section">
            <h3 class="section-title"><fmt:message key="shipper.pending.title" /></h3>
            <table class="table dispatch-table">
                <thead>
                    <tr>
                        <th><fmt:message key="shipper.table.ordercode" /></th>
                        <th><fmt:message key="shipper.table.customer" /></th>
                        <th><fmt:message key="shipper.table.address" /></th>
                        <th><fmt:message key="shipper.table.amount" /></th>
                        <th><fmt:message key="shipper.table.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty PENDING_TASKS}">
                            <tr>
                                <td colspan="5" class="text-center empty-msg"><fmt:message key="shipper.pending.empty" /></td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="task" items="${PENDING_TASKS}">
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
                                    <td class="action-cell">
                                        <!-- Nút Nhận Đơn -->
                                        <form action="ShipperController" method="POST" class="inline-form">
                                            <input type="hidden" name="action" value="acceptTask">
                                            <input type="hidden" name="txtTaskId" value="${task.taskId}">
                                            <button type="submit" class="btn btn-accept"><fmt:message key="shipper.btn.accept" /></button>
                                        </form>

                                        <!-- Nút Từ Chối (Đã tích hợp hộp thoại xác nhận JS) -->
                                        <form action="ShipperController" method="POST" class="inline-form" onsubmit="return confirm('<fmt:message key="shipper.confirm.reject" />');">
                                            <input type="hidden" name="action" value="rejectTask">
                                            <input type="hidden" name="txtTaskId" value="${task.taskId}">
                                            <button type="submit" class="btn btn-reject"><fmt:message key="shipper.btn.reject" /></button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <hr class="section-divider" />

        <!-- BẢNG 3: ĐƠN HÀNG CỦA TÔI -->
        <div class="task-section my-tasks-section">
            <h3 class="section-title"><fmt:message key="shipper.mytasks.title" /></h3>
            <table class="table dispatch-table">
                <thead>
                    <tr>
                        <th><fmt:message key="shipper.table.ordercode" /></th>
                        <th><fmt:message key="shipper.table.customer" /></th>
                        <th><fmt:message key="shipper.table.address" /></th>
                        <th><fmt:message key="shipper.table.amount" /></th>
                        <th><fmt:message key="shipper.table.status" /></th>
                        <th><fmt:message key="shipper.table.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty MY_TASKS}">
                            <tr>
                                <td colspan="6" class="text-center empty-msg"><fmt:message key="shipper.mytasks.empty" /></td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="task" items="${MY_TASKS}">
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
                                        <strong><fmt:message key="status.${task.status}" /></strong>
                                    </td>
                                    <td class="action-cell">
                                        <c:if test="${task.status != 'Delivered' && task.status != 'Returned'}">
                                            <div class="inline-form">
                                                <button type="button" class="btn btn-accept" onclick="openShipperCompleteModal('${task.taskId}', '${task.orderCode}')">
                                                    <fmt:message key="shipper.btn.complete" />
                                                </button>
                                                <button type="button" class="btn btn-reject" onclick="openShipperFailModal('${task.taskId}', '${task.orderCode}')">
                                                    <fmt:message key="shipper.btn.fail" />
                                                </button>
                                            </div>
                                        </c:if>
                                        <c:if test="${task.status == 'Delivered' || task.status == 'Returned'}">
                                            <span class="text-muted"><fmt:message key="shipper.text.closed" /></span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>
</main>

<jsp:include page="footer.jsp" />