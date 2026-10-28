<%@tag description="Thẻ 1 đơn trong bảng Kanban Quản lý đơn: nguồn đơn, món + tùy chọn, tổng tiền, nút bước tiếp theo" pageEncoding="UTF-8"%>
<%@attribute name="order" required="true" type="java.util.Map"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="bx" tagdir="/WEB-INF/tags"%>
<%-- Tag file không kế thừa locale của trang gọi -> tự khai báo --%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<article class="kanban-card" data-order-type="${order.orderType}">
    <div class="kanban-card-head">
        <span>#${order.orderId}</span>
        <span class="muted">
            <c:choose>
                <c:when test="${order.orderType == 'Table'}"><fmt:message key="order.management.source.table" /> ${order.tableId}</c:when>
                <c:when test="${order.orderType == 'Counter'}"><fmt:message key="order.management.tab.counter" /></c:when>
                <c:otherwise><c:out value="${order.customerName}" /></c:otherwise>
            </c:choose>
        </span>
    </div>
    <div class="muted">${order.createdAt}</div>

    <ul class="kanban-card-items">
        <c:forEach var="detail" items="${order.details}">
            <li>
                <strong>${detail.quantity}×</strong> <c:out value="${detail.productName}" />
                <bx:itemOptions sizeName="${detail.sizeName}" iceLevel="${detail.iceLevel}" sugarLevel="${detail.sugarLevel}" toppings="${detail.toppings}" note="${detail.note}" />
            </li>
        </c:forEach>
    </ul>

    <c:if test="${order.status == 'Cancelled' and not order.isCancelLocked}">
        <div class="cancel-countdown-wrapper" id="cancelCountdownWrapper-${order.orderId}">
            <span class="muted"><fmt:message key="order.cancel.countdown" /></span>
            <span class="cancel-countdown-timer font-bold" data-order-id="${order.orderId}" data-seconds="${order.remainingCancelSeconds}"
                  data-locked-text="<fmt:message key="order.cancel.locked" />">--:--</span>
        </div>
    </c:if>

    <div class="kanban-card-foot">
        <strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,###" /> ₫</strong>
        <div class="kanban-actions">
            <c:choose>
                <c:when test="${order.isDeliveryUnderShipper}">
                    <span class="muted"><fmt:message key="order.management.action.under_shipper" /></span>
                </c:when>
                <c:when test="${order.isClosedStatus or order.status == 'Cancelled'}"></c:when>
                <c:otherwise>
                    <%-- Bước tiếp theo theo trạng thái hiện tại; JS handleStaffStatusSubmit vẫn chặn/mở popup như cũ --%>
                    <c:choose>
                        <c:when test="${order.status == 'Accepted'}"><c:set var="nextStatus" value="Prepared" /><c:set var="nextKey" value="order.kanban.next.start" /></c:when>
                        <c:when test="${order.status == 'Prepared' and order.orderType != 'Delivery'}"><c:set var="nextStatus" value="Paid" /><c:set var="nextKey" value="order.kanban.next.paid" /></c:when>
                        <c:when test="${order.status == 'Prepared'}"><c:set var="nextStatus" value="Waiting for dispatch" /><c:set var="nextKey" value="order.kanban.next.dispatch" /></c:when>
                        <c:otherwise><c:set var="nextStatus" value="" /></c:otherwise>
                    </c:choose>
                    <c:if test="${not empty nextStatus}">
                        <form action="${ctx}/OrderManagementController" method="POST" class="order-status-form"
                              data-customer-name="${fn:escapeXml(order.customerName)}" data-customer-phone="${fn:escapeXml(order.customerPhone)}"
                              data-delivery-address="${fn:escapeXml(order.deliveryAddress)}" data-area-id="${order.areaId}"
                              data-order-type="${order.orderType}" data-current-status="${order.status}" data-order-id="${order.orderId}">
                            <input type="hidden" name="action" value="updateStatus" />
                            <input type="hidden" name="orderId" value="${order.orderId}" />
                            <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                            <input type="hidden" name="filterStatus" value="ALL" />
                            <input type="hidden" name="activeTab" value="tab-all-orders" />
                            <input type="hidden" name="newStatus" value="${nextStatus}" />
                            <button type="submit" class="btn-next"><fmt:message key="${nextKey}" /></button>
                        </form>
                    </c:if>
                    <%-- Đơn tại quầy/bàn đã pha xong: có thể chuyển sang giao hàng (JS mở popup nhập địa chỉ) --%>
                    <c:if test="${order.status == 'Prepared' and order.orderType != 'Delivery'}">
                        <form action="${ctx}/OrderManagementController" method="POST" class="order-status-form"
                              data-customer-name="${fn:escapeXml(order.customerName)}" data-customer-phone="${fn:escapeXml(order.customerPhone)}"
                              data-delivery-address="${fn:escapeXml(order.deliveryAddress)}" data-area-id="${order.areaId}"
                              data-order-type="${order.orderType}" data-current-status="${order.status}" data-order-id="${order.orderId}">
                            <input type="hidden" name="action" value="updateStatus" />
                            <input type="hidden" name="orderId" value="${order.orderId}" />
                            <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                            <input type="hidden" name="filterStatus" value="ALL" />
                            <input type="hidden" name="activeTab" value="tab-all-orders" />
                            <input type="hidden" name="newStatus" value="Waiting for dispatch" />
                            <button type="submit" class="btn-plain"><fmt:message key="order.kanban.next.dispatch" /></button>
                        </form>
                    </c:if>
                    <c:if test="${order.canCancelOrder}">
                        <button type="button" class="btn-plain btn-open-staff-cancel" data-order-id="${order.orderId}" data-tab-key="tab-all-orders"><fmt:message key="order.management.btn.cancel" /></button>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</article>
