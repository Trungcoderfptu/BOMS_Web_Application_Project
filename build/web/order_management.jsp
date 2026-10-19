<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">

        <div class="flex-between mt-15 mb-20">
            <h2 class="m-0"><fmt:message key="order.management.heading" /></h2>
        </div>

        <c:if test="${not empty ERROR_MESSAGE}">
            <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
        </c:if>
        <c:if test="${not empty SUCCESS_MESSAGE}">
            <p class="msg-success text-center"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
        </c:if>

        <form action="${pageContext.request.contextPath}/OrderManagementController" method="GET" class="order-filter-panel">
            <div class="filter-item">
                <label for="filterDate"><fmt:message key="order.management.filter.date" /></label>
                <input type="date" id="filterDate" name="filterDate" value="${CURRENT_FILTER_DATE}" class="filter-input" />
            </div>

            <div class="filter-item">
                <label for="filterStatus"><fmt:message key="order.management.filter.status" /></label>
                <select id="filterStatus" name="filterStatus" class="filter-input">
                    <option value="ALL" ${CURRENT_FILTER_STATUS == 'ALL' ? 'selected' : ''}><fmt:message key="order.management.filter.status.all" /></option>
                    <option value="Accepted" ${CURRENT_FILTER_STATUS == 'Accepted' ? 'selected' : ''}>Accepted</option>
                    <option value="Prepared" ${CURRENT_FILTER_STATUS == 'Prepared' ? 'selected' : ''}>Prepared</option>
                    <option value="Delivering" ${CURRENT_FILTER_STATUS == 'Delivering' ? 'selected' : ''}>Delivering</option>
                    <option value="Delivered" ${CURRENT_FILTER_STATUS == 'Delivered' ? 'selected' : ''}>Delivered</option>
                    <option value="Paid" ${CURRENT_FILTER_STATUS == 'Paid' ? 'selected' : ''}>Paid</option>
                    <option value="Cancelled" ${CURRENT_FILTER_STATUS == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                </select>
            </div>

            <button type="submit" class="btn-login"><fmt:message key="order.management.filter.btn" /></button>
        </form>

        <c:choose>
            <c:when test="${empty ORDER_LIST}">
                <div class="empty-cart-container">
                    <h3 class="msg-info"><fmt:message key="order.management.empty" /></h3>
                </div>
            </c:when>
            <c:otherwise>
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th><fmt:message key="order.management.table.id" /></th>
                            <th><fmt:message key="order.management.table.type" /></th>
                            <th><fmt:message key="order.management.table.source" /></th>
                            <th><fmt:message key="order.management.table.created_at" /></th>
                            <th><fmt:message key="order.management.table.cooking_at" /></th>
                            <th><fmt:message key="order.management.table.items" /></th>
                            <th><fmt:message key="order.management.table.total" /></th>
                            <th><fmt:message key="order.management.table.status" /></th>
                            <th><fmt:message key="order.management.table.action" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="order" items="${ORDER_LIST}">
                            <tr>
                                <td><strong>#${order.orderId}</strong></td>
                                <td>${order.orderType}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${order.orderType == 'Table'}">
                                            Bàn #${order.tableId}
                                        </c:when>
                                        <c:when test="${order.orderType == 'Counter'}">
                                            Quầy (NV #${order.staffId})
                                        </c:when>
                                        <c:otherwise>
                                            ${order.customerName} (${order.customerPhone})
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${order.createdAt}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty order.cookingStartedAt}">
                                            ${order.cookingStartedAt}
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-secondary"><fmt:message key="order.management.status.not_started" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <ul class="order-items-summary">
                                        <c:forEach var="detail" items="${order.details}">
                                            <li class="order-item-row">
                                                <div class="order-item-info">
                                                    <strong>${detail.productName}</strong> x${detail.quantity} 
                                                    (<fmt:formatNumber value="${detail.unitPrice}" pattern="#,###" /> ₫)
                                                </div>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </td>
                                <td><strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,###" /> ₫</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${order.status == 'Accepted'}">
                                            <span class="order-badge order-badge-accepted">Accepted</span>
                                        </c:when>
                                        <c:when test="${order.status == 'Prepared'}">
                                            <span class="order-badge order-badge-prepared">Prepared</span>
                                        </c:when>
                                        <c:when test="${order.status == 'Delivering'}">
                                            <span class="order-badge order-badge-delivering">Delivering</span>
                                        </c:when>
                                        <c:when test="${order.status == 'Delivered'}">
                                            <span class="order-badge order-badge-delivered">Delivered</span>
                                        </c:when>
                                        <c:when test="${order.status == 'Paid'}">
                                            <span class="order-badge order-badge-paid">Paid</span>
                                        </c:when>
                                        <c:when test="${order.status == 'Cancelled'}">
                                            <span class="order-badge order-badge-cancelled">Cancelled</span>
                                            <c:choose>
                                                <c:when test="${order.isCancelLocked}">
                                                    <div class="mt-5"><span class="badge-locked"><fmt:message key="order.cancel.locked" /></span></div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="cancel-countdown-wrapper mt-5" id="cancelCountdownWrapper-${order.orderId}">
                                                        <span class="text-secondary text-sm"><fmt:message key="order.cancel.countdown" /></span>
                                                        <span class="cancel-countdown-timer font-bold"
                                                              data-order-id="${order.orderId}"
                                                              data-seconds="${order.remainingCancelSeconds}"
                                                              data-locked-text="<fmt:message key="order.cancel.locked" />">--:--</span>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="order-badge">${order.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="order-action-group">
                                        <form action="${pageContext.request.contextPath}/OrderManagementController" method="POST" class="m-0 flex-align-center order-status-form" onsubmit="return handleStaffStatusSubmit(event, this, '${order.orderId}');">
                                            <input type="hidden" name="action" value="updateStatus" />
                                            <input type="hidden" name="orderId" value="${order.orderId}" />
                                            <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                                            <input type="hidden" name="filterStatus" value="${CURRENT_FILTER_STATUS}" />
                                            <select name="newStatus" class="status-select status-select-${order.orderId}" ${order.isCancelLocked ? 'disabled' : ''}>
                                                <option value="Accepted" ${order.status == 'Accepted' ? 'selected' : ''}>Accepted</option>
                                                <option value="Prepared" ${order.status == 'Prepared' ? 'selected' : ''}>Prepared</option>
                                                <option value="Delivering" ${order.status == 'Delivering' ? 'selected' : ''}>Delivering</option>
                                                <option value="Delivered" ${order.status == 'Delivered' ? 'selected' : ''}>Delivered</option>
                                                <option value="Paid" ${order.status == 'Paid' ? 'selected' : ''}>Paid</option>
                                                <option value="Cancelled" ${order.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                            </select>
                                            <button type="submit" class="btn-status-save btn-status-save-${order.orderId}" ${order.isCancelLocked ? 'disabled' : ''}><fmt:message key="order.management.btn.update" /></button>
                                        </form>

                                        <c:if test="${order.canCancelOrder}">
                                            <button type="button" class="btn-order-cancel mt-5" onclick="openStaffCancelModal('${order.orderId}')"><fmt:message key="order.management.btn.cancel" /></button>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>

        <jsp:include page="components/staff_cancel_modal.jsp" />
        </main>
        <jsp:include page="footer.jsp" />
