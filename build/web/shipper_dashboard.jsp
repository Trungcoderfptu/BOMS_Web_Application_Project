<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <h2><fmt:message key="shipper.title" />: ${sessionScope.USER_SESSION.fullName}</h2>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>

    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
    </c:if>

    <!-- BẢNG 1: ĐƠN ĐƯỢC GIAO (CHỜ XÁC NHẬN) -->
    <div class="task-section pending-section">
        <h3 class="section-title"><fmt:message key="shipper.pending.title" /></h3>
        <table class="table dispatch-table">
            <thead>
                <tr>
                    <th><fmt:message key="shipper.table.ordercode" /></th>
                    <th><fmt:message key="shipper.table.address" /></th>
                    <th><fmt:message key="shipper.table.action" /></th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty PENDING_TASKS}">
                        <tr>
                            <td colspan="3" class="text-center empty-msg"><fmt:message key="shipper.pending.empty" /></td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="task" items="${PENDING_TASKS}">
                            <tr>
                                <td>${task.orderCode}</td>
                                <td>${task.customerAddress}</td>
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

    <!-- Thanh vạch ngang tạo khoảng cách giữa 2 bảng -->
    <hr class="section-divider" />

    <!-- BẢNG 2: ĐƠN HÀNG CỦA TÔI -->
    <div class="task-section my-tasks-section">
        <h3 class="section-title"><fmt:message key="shipper.mytasks.title" /></h3>
        <table class="table dispatch-table">
            <thead>
                <tr>
                    <th><fmt:message key="shipper.table.ordercode" /></th>
                    <th><fmt:message key="shipper.table.address" /></th>
                    <th><fmt:message key="shipper.table.status" /></th>
                    <th><fmt:message key="shipper.table.action" /></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="task" items="${MY_TASKS}">
                    <tr>
                        <td>${task.orderCode}</td>
                        <td>${task.customerAddress}</td>
                        <td class="status-cell">
                            <strong><fmt:message key="status.${task.status}" /></strong>
                        </td>
                        <td class="action-cell">
                            <!-- Kiểm tra nếu đơn chưa đóng luồng thì hiện dropdown cập nhật -->
                            <c:if test="${task.status != 'Delivered' && task.status != 'Returned'}">
                                <form action="ShipperController" method="POST" class="inline-form">
                                    <input type="hidden" name="action" value="updateStatus">
                                    <input type="hidden" name="txtTaskId" value="${task.taskId}">
                                    <select name="txtStatus" class="status-select">
                                        <option value="Delivering" ${task.status == 'DELIVERING' || task.status == 'Delivering' ? 'selected' : ''}>
                                            <fmt:message key="status.DELIVERING" />
                                        </option>
                                        <option value="Delivered">
                                            <fmt:message key="status.COMPLETED" />
                                        </option>
                                        <option value="Returned">
                                            <fmt:message key="status.FAILED" />
                                        </option>
                                    </select>
                                    <button type="submit" class="btn btn-update"><fmt:message key="shipper.btn.update" /></button>
                                </form>
                            </c:if>
                            <!-- Nếu đơn đã hoàn thành hoặc bom hàng thì khóa luồng -->
                            <c:if test="${task.status == 'Delivered' || task.status == 'Returned'}">

                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</main>

<jsp:include page="footer.jsp" />