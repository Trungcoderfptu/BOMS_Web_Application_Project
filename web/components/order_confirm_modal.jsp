<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />

<div id="orderConfirmModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0"><fmt:message key="menu.modal.confirm_title" /></h3>
            <button type="button" class="btn-modal-close btn-close-order-confirm" id="btnCloseOrderConfirmModal">&times;</button>
        </div>
        <div class="modal-body">
            <c:if test="${not empty CURRENT_TABLE}">
                <p class="modal-table-info">
                    <strong><fmt:message key="menu.heading.customer" />:</strong> <span class="badge-table">${CURRENT_TABLE}</span>
                </p>
            </c:if>
            <c:if test="${not empty sessionScope.USER_SESSION}">
                <p class="modal-table-info">
                    <strong><fmt:message key="menu.cashier" />:</strong> <c:out value="${sessionScope.USER_SESSION.fullName}" />
                </p>
            </c:if>
            <div class="modal-cart-summary">
                <table class="modal-items-table">
                    <thead>
                        <tr>
                            <th><fmt:message key="menu.cart.table.product" /></th>
                            <th class="text-center"><fmt:message key="menu.cart.table.quantity" /></th>
                            <th class="text-right"><fmt:message key="menu.cart.table.total" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${sessionScope.CURRENT_CART.orderDetails}">
                            <tr>
                                <td><c:out value="${item.product.productName}" /></td>
                                <td class="text-center">x${item.quantity}</td>
                                <td class="text-right"><fmt:formatNumber value="${item.unitPrice * item.quantity}" pattern="#,###" /> ₫</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            <div class="modal-total-section">
                <span><fmt:message key="menu.cart.total_amount" />:</span>
                <strong class="modal-total-price"><fmt:formatNumber value="${sessionScope.CURRENT_CART.totalAmount}" pattern="#,###" /> ₫</strong>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn-modal-cancel btn-close-order-confirm" id="btnCancelOrderConfirmModal">
                <fmt:message key="menu.modal.btn_cancel" />
            </button>
            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                <input type="hidden" name="action" value="checkout" />
                <c:if test="${not empty CURRENT_TABLE}">
                    <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                </c:if>
                <button type="submit" class="btn-register-action m-0">
                    <fmt:message key="menu.modal.btn_confirm" />
                </button>
            </form>
        </div>
    </div>
</div>
