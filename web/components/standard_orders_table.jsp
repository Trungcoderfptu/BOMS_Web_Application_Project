<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<c:set var="hasOrders" value="false" />
<c:choose>
    <c:when test="${targetOrderType == 'Counter'}">
        <c:set var="hasOrders" value="${not empty ORDER_COUNTS.counter and ORDER_COUNTS.counter > 0}" />
        <c:set var="emptyKey" value="order.management.empty.counter" />
    </c:when>
    <c:when test="${targetOrderType == 'Table'}">
        <c:set var="hasOrders" value="${not empty ORDER_COUNTS.table and ORDER_COUNTS.table > 0}" />
        <c:set var="emptyKey" value="order.management.empty.table" />
    </c:when>
    <c:otherwise>
        <c:set var="hasOrders" value="${not empty ORDER_COUNTS.all and ORDER_COUNTS.all > 0}" />
        <c:set var="emptyKey" value="order.management.empty" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not hasOrders}">
        <div class="empty-cart-container">
            <h3 class="msg-info"><fmt:message key="${emptyKey}" /></h3>
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
                    <c:if test="${targetOrderType == 'ALL' or order.orderType == targetOrderType}">
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
                                    <c:when test="${order.status == 'Waiting for dispatch'}">
                                        <span class="order-badge order-badge-dispatch"><fmt:message key="order.status.waiting_dispatch" /></span>
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
                                    <form action="${pageContext.request.contextPath}/OrderManagementController" method="POST" class="m-0 flex-align-center order-status-form"
                                          data-customer-name="${fn:escapeXml(order.customerName)}"
                                          data-customer-phone="${fn:escapeXml(order.customerPhone)}"
                                          data-delivery-address="${fn:escapeXml(order.deliveryAddress)}"
                                          data-area-id="${order.areaId}"
                                          data-order-type="${order.orderType}"
                                          data-current-status="${order.status}"
                                          onsubmit="return handleStaffStatusSubmit(event, this, '${order.orderId}');">
                                        <input type="hidden" name="action" value="updateStatus" />
                                        <input type="hidden" name="orderId" value="${order.orderId}" />
                                        <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                                        <input type="hidden" name="filterStatus" value="${CURRENT_FILTER_STATUS}" />
                                        <input type="hidden" name="activeTab" value="${tabKey}" />
                                        <select name="newStatus" class="status-select status-select-${order.orderId}" ${order.isCancelLocked ? 'disabled' : ''}>
                                            <option value="Accepted" ${order.status == 'Accepted' ? 'selected' : ''}>Accepted</option>
                                            <option value="Prepared" ${order.status == 'Prepared' ? 'selected' : ''}>Prepared</option>
                                            <option value="Waiting for dispatch" ${order.status == 'Waiting for dispatch' ? 'selected' : ''}><fmt:message key="order.status.waiting_dispatch" /></option>
                                            <option value="Delivering" ${order.status == 'Delivering' ? 'selected' : ''}>Delivering</option>
                                            <option value="Delivered" ${order.status == 'Delivered' ? 'selected' : ''}>Delivered</option>
                                            <option value="Paid" ${order.status == 'Paid' ? 'selected' : ''}>Paid</option>
                                            <option value="Cancelled" ${order.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                        </select>
                                        <button type="submit" class="btn-status-save btn-status-save-${order.orderId}" ${order.isCancelLocked ? 'disabled' : ''}><fmt:message key="order.management.btn.update" /></button>
                                    </form>

                                    <c:if test="${order.canCancelOrder}">
                                        <button type="button" class="btn-order-cancel mt-5" onclick="openStaffCancelModal('${order.orderId}', '${tabKey}')"><fmt:message key="order.management.btn.cancel" /></button>
                                    </c:if>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>
