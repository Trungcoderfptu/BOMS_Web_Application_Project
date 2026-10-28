<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<c:set var="totalCartQuantity" value="0" />
<c:if test="${not empty sessionScope.CURRENT_CART}">
    <c:forEach var="item" items="${sessionScope.CURRENT_CART.orderDetails}">
        <c:set var="totalCartQuantity" value="${totalCartQuantity + item.quantity}" />
    </c:forEach>
</c:if>

<c:if test="${totalCartQuantity > 0 or not empty CURRENT_TABLE}">
    <button type="button" id="btnFloatingCart" data-tab="tab-cart" class="floating-cart">
        <div class="cart-icon">
            <i class="fi fi-rr-shopping-cart" aria-hidden="true"></i>
            <c:if test="${totalCartQuantity > 0}">
                <span class="cart-badge">${totalCartQuantity}</span>
            </c:if>
        </div>
        <div class="cart-text">
            <fmt:message key="menu.cart.floating_text" />
        </div>
    </button>
</c:if>
