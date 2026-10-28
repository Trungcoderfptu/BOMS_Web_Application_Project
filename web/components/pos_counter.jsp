<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="bx" tagdir="/WEB-INF/tags"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/header.jsp" />

<%-- Bố cục C: POS 3 vùng cho nhân viên quầy - nhóm món (trái) | lưới món (giữa) | hóa đơn luôn hiện (phải).
     Giữ nguyên CartController (lineKey), #btnOpenCheckoutModal, popup chọn món; data-open-checkout cho "Thanh toán ngay" --%>
<main class="main-content" data-open-checkout="${OPEN_CHECKOUT ? 'true' : 'false'}">
    <div class="page-head">
        <h2>
            <c:choose>
                <c:when test="${not empty CURRENT_TABLE}"><fmt:message key="menu.heading.customer" />: <c:out value="${CURRENT_TABLE}" /></c:when>
                <c:otherwise><fmt:message key="menu.heading.counter" /></c:otherwise>
            </c:choose>
        </h2>
        <span class="muted"><fmt:message key="menu.cashier" />: <c:out value="${sessionScope.USER_SESSION.fullName}" /></span>
    </div>

    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success text-center">
            <fmt:message key="${SUCCESS_MESSAGE}" />
            <c:if test="${not empty LAST_ORDER_ID}"><strong> (#${LAST_ORDER_ID})</strong></c:if>
        </p>
    </c:if>

    <div class="pos">
        <jsp:include page="/components/menu_cats.jsp">
            <jsp:param name="layout" value="pos" />
        </jsp:include>

        <section class="pos-items">
            <jsp:include page="/components/menu_grid.jsp" />
        </section>

        <aside class="panel pos-ticket">
            <h3 class="panel-title"><fmt:message key="pos.ticket.title" /></h3>
            <c:choose>
                <c:when test="${empty sessionScope.CURRENT_CART or empty sessionScope.CURRENT_CART.orderDetails}">
                    <p class="pos-empty"><fmt:message key="pos.ticket.empty" /></p>
                </c:when>
                <c:otherwise>
                    <ul class="pos-ticket-lines">
                        <c:forEach var="line" items="${sessionScope.CURRENT_CART.orderDetails}">
                            <li class="pos-line">
                                <div class="pos-line-main">
                                    <strong><c:out value="${line.product.productName}" /></strong>
                                    <bx:itemOptions sizeName="${line.sizeName}" iceLevel="${line.iceLevel}" sugarLevel="${line.sugarLevel}" toppings="${line.toppings}" note="${line.note}" />
                                    <div class="pos-line-qty">
                                        <form action="${ctx}/CartController" method="POST">
                                            <input type="hidden" name="action" value="update" />
                                            <input type="hidden" name="lineKey" value="${fn:escapeXml(line.lineKey)}" />
                                            <input type="hidden" name="quantity" value="${line.quantity - 1}" />
                                            <c:if test="${not empty CURRENT_TABLE}"><input type="hidden" name="tableId" value="${fn:escapeXml(CURRENT_TABLE)}" /></c:if>
                                            <button type="submit" class="btn-qty">-</button>
                                        </form>
                                        <span>${line.quantity}</span>
                                        <form action="${ctx}/CartController" method="POST">
                                            <input type="hidden" name="action" value="update" />
                                            <input type="hidden" name="lineKey" value="${fn:escapeXml(line.lineKey)}" />
                                            <input type="hidden" name="quantity" value="${line.quantity + 1}" />
                                            <c:if test="${not empty CURRENT_TABLE}"><input type="hidden" name="tableId" value="${fn:escapeXml(CURRENT_TABLE)}" /></c:if>
                                            <button type="submit" class="btn-qty">+</button>
                                        </form>
                                        <form action="${ctx}/CartController" method="POST">
                                            <input type="hidden" name="action" value="remove" />
                                            <input type="hidden" name="lineKey" value="${fn:escapeXml(line.lineKey)}" />
                                            <c:if test="${not empty CURRENT_TABLE}"><input type="hidden" name="tableId" value="${fn:escapeXml(CURRENT_TABLE)}" /></c:if>
                                            <button type="submit" class="btn-cart-remove"><fmt:message key="menu.cart.btn.remove" /></button>
                                        </form>
                                    </div>
                                </div>
                                <span class="pos-line-price"><fmt:formatNumber value="${line.unitPrice * line.quantity}" pattern="#,###" /> ₫</span>
                            </li>
                        </c:forEach>
                    </ul>

                    <div class="pos-total">
                        <span><fmt:message key="menu.cart.total_amount" /></span>
                        <span><fmt:formatNumber value="${sessionScope.CURRENT_CART.totalAmount}" pattern="#,###" /> ₫</span>
                    </div>
                    <div class="pos-actions">
                        <form action="${ctx}/CartController" method="POST" class="m-0">
                            <input type="hidden" name="action" value="clear" />
                            <c:if test="${not empty CURRENT_TABLE}"><input type="hidden" name="tableId" value="${fn:escapeXml(CURRENT_TABLE)}" /></c:if>
                            <button type="submit" class="btn-plain"><fmt:message key="menu.cart.btn.clear" /></button>
                        </form>
                        <button type="button" class="btn-next" id="btnOpenCheckoutModal"><fmt:message key="menu.btn.checkout" /></button>
                    </div>
                </c:otherwise>
            </c:choose>
        </aside>
    </div>

    <jsp:include page="/components/order_confirm_modal.jsp" />
    <jsp:include page="/components/product_option_modal.jsp" />
</main>
<jsp:include page="/footer.jsp" />
