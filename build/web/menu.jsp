<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><fmt:message key="menu.title" /></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
    </head>
    <body class="main-content">
        <c:choose>
            <c:when test="${not empty CURRENT_TABLE}">
                <div class="flex-between" style="padding-bottom: 20px; border-bottom: 1px solid var(--border-light); margin-bottom: 20px;">
                    <h2 class="m-0"><fmt:message key="menu.heading.customer" />: ${CURRENT_TABLE}</h2>
                    <div class="flex-align-center">
                        <jsp:include page="components/language_switcher.jsp" />
                        <jsp:include page="components/theme_toggle.jsp" />
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${not empty sessionScope.USER_SESSION}">
                        <jsp:include page="header.jsp" />
                        <h2><fmt:message key="menu.heading.counter" /> (<fmt:message key="menu.cashier" />: ${sessionScope.USER_SESSION.fullName})</h2>
                    </c:when>

                    <c:otherwise>
                        <div style="text-align: center; margin-top: 100px;">
                            <h2 class="msg-error"><fmt:message key="menu.access.denied" /></h2>
                            <p><fmt:message key="menu.access.scan_qr" /> 
                                <a href="${pageContext.request.contextPath}/login.jsp" class="text-link-success"><fmt:message key="menu.access.login" /></a>
                                <fmt:message key="menu.access.as_staff" />.
                            </p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty ERROR_MESSAGE}">
            <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
        </c:if>
        <c:if test="${not empty SUCCESS_MESSAGE}">
            <p class="msg-success text-center">
                <fmt:message key="${SUCCESS_MESSAGE}" />
                <c:if test="${not empty LAST_ORDER_ID}">
                    <strong> (#${LAST_ORDER_ID})</strong>
                </c:if>
            </p>
        </c:if>
        <c:set var="activeTab" value="${not empty ACTIVE_TAB ? ACTIVE_TAB : 'menu'}" />
        <c:if test="${not empty CURRENT_TABLE or not empty sessionScope.USER_SESSION}">
            <div class="tab-nav d-flex mt-15 mb-20">
                <button class="tab-btn ${activeTab == 'cart' ? '' : 'active'}" onclick="openTab(event, 'tab-menu')"><fmt:message key="menu.tab.menu" /></button>
                <button class="tab-btn ${activeTab == 'cart' ? 'active' : ''}" onclick="openTab(event, 'tab-cart')">
                    <fmt:message key="menu.tab.cart" /> 
                    <c:if test="${totalCartQuantity > 0}">
                        <span class="msg-error tab-badge">${totalCartQuantity}</span>
                    </c:if>
                </button>
            </div>
        </c:if>
        <div id="tab-menu" class="tab-content ${activeTab == 'cart' ? 'd-none' : 'active'}">
            <c:if test="${not empty CURRENT_TABLE or not empty sessionScope.USER_SESSION}">
                <c:if test="${not empty MENU_LIST}">
                    <div class="menu-grid">
                        <c:forEach var="item" items="${MENU_LIST}">
                            <div class="product-card ${item.available ? '' : 'out-of-stock'}">
                                <div class="product-img-placeholder">
                                    <fmt:message key="menu.product.image" />: ${item.imageUrl}
                                </div>

                                <h3>${item.productName}</h3>
                                <div class="product-price"><fmt:formatNumber value="${item.price}" pattern="#,###" /> ₫</div>

                                <c:choose>
                                    <c:when test="${item.available}">
                                        <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                            <input type="hidden" name="action" value="add" />
                                            <input type="hidden" name="productId" value="${item.productId}" />
                                            <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                            <button type="submit" class="btn-add-cart"><fmt:message key="menu.btn.add_to_cart" /></button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn-add-cart btn-disabled" disabled><fmt:message key="menu.btn.out_of_stock" /></button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </c:if>
        </div>
        <div id="tab-cart" class="tab-content ${activeTab == 'cart' ? 'active' : 'd-none'}">
            <c:choose>
                <c:when test="${empty sessionScope.CURRENT_CART or empty sessionScope.CURRENT_CART.orderDetails}">
                    <div class="empty-cart-container">
                        <h3><fmt:message key="menu.cart.empty" /></h3>
                        <button class="btn-login mt-15" onclick="openTab(event, 'tab-menu')"><fmt:message key="menu.cart.back_to_menu" /></button>
                    </div>
                </c:when>
                <c:otherwise>
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th><fmt:message key="menu.cart.table.product" /></th>
                                <th><fmt:message key="menu.cart.table.price" /></th>
                                <th><fmt:message key="menu.cart.table.quantity" /></th>
                                <th><fmt:message key="menu.cart.table.total" /></th>
                                <th><fmt:message key="menu.cart.table.action" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cartItem" items="${sessionScope.CURRENT_CART.orderDetails}">
                                <tr>
                                    <td><strong>${cartItem.product.productName}</strong></td>
                                    <td><fmt:formatNumber value="${cartItem.unitPrice}" pattern="#,###" /> ₫</td>
                                    <td>
                                        <div class="cart-qty-control">
                                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                                <input type="hidden" name="action" value="update" />
                                                <input type="hidden" name="productId" value="${cartItem.productId}" />
                                                <input type="hidden" name="quantity" value="${cartItem.quantity - 1}" />
                                                <c:if test="${not empty CURRENT_TABLE}">
                                                    <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                                </c:if>
                                                <button type="submit" class="btn-qty">-</button>
                                            </form>
                                            <span>${cartItem.quantity}</span>
                                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                                <input type="hidden" name="action" value="update" />
                                                <input type="hidden" name="productId" value="${cartItem.productId}" />
                                                <input type="hidden" name="quantity" value="${cartItem.quantity + 1}" />
                                                <c:if test="${not empty CURRENT_TABLE}">
                                                    <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                                </c:if>
                                                <button type="submit" class="btn-qty">+</button>
                                            </form>
                                        </div>
                                    </td>
                                    <td><fmt:formatNumber value="${cartItem.unitPrice * cartItem.quantity}" pattern="#,###" /> ₫</td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                            <input type="hidden" name="action" value="remove" />
                                            <input type="hidden" name="productId" value="${cartItem.productId}" />
                                            <c:if test="${not empty CURRENT_TABLE}">
                                                <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                            </c:if>
                                            <button type="submit" class="btn-cart-remove"><fmt:message key="menu.cart.btn.remove" /></button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <div class="flex-between mt-15 cart-summary">
                        <h3 class="m-0"><fmt:message key="menu.cart.total_amount" />: <fmt:formatNumber value="${sessionScope.CURRENT_CART.totalAmount}" pattern="#,###" /> ₫</h3>
                        <div class="cart-actions-group">
                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                <input type="hidden" name="action" value="clear" />
                                <c:if test="${not empty CURRENT_TABLE}">
                                    <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                </c:if>
                                <button type="submit" class="btn-cart-clear"><fmt:message key="menu.cart.btn.clear" /></button>
                            </form>
                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                <input type="hidden" name="action" value="checkout" />
                                <c:if test="${not empty CURRENT_TABLE}">
                                    <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                </c:if>
                                <button type="submit" class="btn-register-action m-0 btn-checkout"><fmt:message key="menu.btn.checkout" /></button>
                            </form>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <jsp:include page="components/floating_cart.jsp" />
        <script src="${pageContext.request.contextPath}/js/main.js"></script>
    </body>
</html>