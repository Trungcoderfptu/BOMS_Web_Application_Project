<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="bx" tagdir="/WEB-INF/tags"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<c:choose>
    <c:when test="${empty ORDER_LIST or ORDER_COUNTS.delivery == 0}">
        <div class="empty-cart-container">
            <h3 class="msg-info"><fmt:message key="order.management.empty.delivery" /></h3>
        </div>
    </c:when>
    <c:otherwise>
        <table class="admin-table">
            <thead>
                <tr>
                    <th><fmt:message key="order.management.table.id" /></th>
                    <th><fmt:message key="order.management.table.recipient" /></th>
                    <th><fmt:message key="order.management.table.delivery_address" /></th>
                    <th><fmt:message key="order.management.table.shipper" /></th>
                    <th><fmt:message key="order.management.table.created_at" /></th>
                    <th><fmt:message key="order.management.table.items" /></th>
                    <th><fmt:message key="order.management.table.total" /></th>
                    <th><fmt:message key="order.management.table.status" /></th>
                    <th><fmt:message key="order.management.table.action" /></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="order" items="${ORDER_LIST}">
                    <c:if test="${order.orderType == 'Delivery'}">
                        <tr>
                            <td><strong>#${order.orderId}</strong></td>
                            <td>
                                <strong><c:out value="${order.customerName}" /></strong>
                                <c:if test="${not empty order.customerPhone}">
                                    <br><span class="text-secondary"><c:out value="${order.customerPhone}" /></span>
                                </c:if>
                            </td>
                            <td>
                                <div><c:out value="${order.deliveryAddress}" /></div>
                                <c:if test="${not empty order.areaName}">
                                    <div class="mt-5"><span class="badge-table"><i class="fi fi-rr-marker" aria-hidden="true"></i> <c:out value="${order.areaName}" /></span></div>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty order.shipperName}">
                                        <span class="badge-stock-ok"><i class="fi fi-rr-truck-side" aria-hidden="true"></i> <c:out value="${order.shipperName}" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-secondary"><em><fmt:message key="order.management.shipper.unassigned" /></em></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${order.createdAt}</td>
                            <td>
                                <ul class="order-items-summary">
                                    <c:forEach var="detail" items="${order.details}">
                                        <li class="order-item-row">
                                            <div class="order-item-info">
                                                <strong><c:out value="${detail.productName}" /></strong> x${detail.quantity} 
                                                (<fmt:formatNumber value="${detail.unitPrice}" pattern="#,###" /> ₫)
                                                <bx:itemOptions sizeName="${detail.sizeName}" iceLevel="${detail.iceLevel}" sugarLevel="${detail.sugarLevel}" toppings="${detail.toppings}" note="${detail.note}" />
                                            </div>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </td>
                            <td><strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,###" /> ₫</strong></td>
                            <td>
                                <c:choose>
                                    <c:when test="${order.status == 'Waiting for dispatch'}">
                                        <span class="order-badge order-badge-dispatch"><fmt:message key="order.status.waiting_dispatch" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Waiting for delivery'}">
                                        <span class="order-badge order-badge-prepared"><fmt:message key="status.${order.status}" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Picked up'}">
                                        <span class="order-badge order-badge-prepared"><fmt:message key="status.${order.status}" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Delivering'}">
                                        <span class="order-badge order-badge-delivering"><fmt:message key="status.${order.status}" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Delivered'}">
                                        <span class="order-badge order-badge-delivered"><fmt:message key="status.${order.status}" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Returned'}">
                                        <span class="order-badge order-badge-cancelled"><fmt:message key="status.${order.status}" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Paid'}">
                                        <span class="order-badge order-badge-paid"><fmt:message key="status.${order.status}" /></span>
                                    </c:when>
                                    <c:when test="${order.status == 'Cancelled'}">
                                        <span class="order-badge order-badge-cancelled"><fmt:message key="status.${order.status}" /></span>
                                        <c:choose>
                                            <c:when test="${order.isCancelLocked}">
                                                <div class="mt-5"><span class="badge-locked"><fmt:message key="order.cancel.locked" /></span></div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="cancel-countdown-wrapper mt-5" id="deliveryCancelCountdownWrapper-${order.orderId}">
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
                                    <c:choose>
                                        <c:when test="${order.isDeliveryUnderShipper}">
                                            <span class="badge-locked text-sm"><i class="fi fi-rr-lock" aria-hidden="true"></i> <fmt:message key="order.management.action.under_shipper" /></span>
                                        </c:when>
                                        <c:when test="${order.isClosedStatus}">
                                            <span class="text-secondary text-sm"><i class="fi fi-rr-lock" aria-hidden="true"></i> <fmt:message key="order.management.action.closed" /></span>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="${pageContext.request.contextPath}/OrderManagementController" method="POST" class="m-0 flex-align-center order-status-form"
                                                  data-customer-name="${fn:escapeXml(order.customerName)}"
                                                  data-customer-phone="${fn:escapeXml(order.customerPhone)}"
                                                  data-delivery-address="${fn:escapeXml(order.deliveryAddress)}"
                                                  data-area-id="${order.areaId}"
                                                  data-order-type="${order.orderType}"
                                                  data-current-status="${order.status}"
                                                  data-order-id="${order.orderId}">
                                                <input type="hidden" name="action" value="updateStatus" />
                                                <input type="hidden" name="orderId" value="${order.orderId}" />
                                                <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                                                <input type="hidden" name="filterStatus" value="${CURRENT_FILTER_STATUS}" />
                                                <input type="hidden" name="activeTab" value="tab-delivery-orders" />
                                                <select name="newStatus" class="status-select status-select-${order.orderId}" ${order.isStatusUpdateLocked ? 'disabled' : ''}>
                                                    <option value="Accepted" ${order.status == 'Accepted' ? 'selected' : ''}>Accepted (<fmt:message key="order.revert.option.label" />)</option>
                                                    <option value="Prepared" ${order.status == 'Prepared' ? 'selected' : ''}>Prepared</option>
                                                    <option value="Waiting for dispatch" ${order.status == 'Waiting for dispatch' ? 'selected' : ''}><fmt:message key="order.status.waiting_dispatch" /></option>
                                                    <option value="Waiting for delivery" ${order.status == 'Waiting for delivery' ? 'selected' : ''}>Waiting for delivery</option>
                                                    <option value="Delivering" ${order.status == 'Delivering' ? 'selected' : ''}>Delivering</option>
                                                    <option value="Delivered" ${order.status == 'Delivered' ? 'selected' : ''}>Delivered</option>
                                                    <option value="Returned" ${order.status == 'Returned' ? 'selected' : ''}>Returned</option>
                                                    <option value="Paid" ${order.status == 'Paid' ? 'selected' : ''}>Paid</option>
                                                    <option value="Cancelled" ${order.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                                </select>
                                                <button type="submit" class="btn-status-save btn-status-save-${order.orderId}" ${order.isStatusUpdateLocked ? 'disabled' : ''}><fmt:message key="order.management.btn.update" /></button>
                                            </form>

                                            <c:if test="${order.canCancelOrder}">
                                                <button type="button" class="btn-order-cancel btn-open-staff-cancel mt-5" data-order-id="${order.orderId}" data-tab-key="tab-delivery-orders"><fmt:message key="order.management.btn.cancel" /></button>
                                            </c:if>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                        </tr>
                    </c:if>
                </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>
