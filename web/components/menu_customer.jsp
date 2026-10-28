<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="bx" tagdir="/WEB-INF/tags"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><fmt:message key="menu.title" /></title>
        <link rel="icon" href="${pageContext.request.contextPath}/images/brand/bingxue-favicon-32.png">
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700&amp;display=swap">
        <link rel="stylesheet" href="https://cdn-uicons.flaticon.com/4.0.0/uicons-regular-rounded/css/uicons-regular-rounded.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/layout.css">
    </head>
    <%-- Màn khách quét QR (bố cục G trên điện thoại: tab xuống đáy, lưới 2 cột). data-open-checkout: sau "Thanh toán ngay" main.js tự mở hộp xác nhận đơn --%>
    <body class="main-content qr-page" data-open-checkout="${OPEN_CHECKOUT ? 'true' : 'false'}">
        <c:choose>
            <c:when test="${not empty CURRENT_TABLE}">
                <div class="flex-between menu-header-bar">
                    <div class="qr-brand"><span class="brand-mark"><img src="${pageContext.request.contextPath}/images/brand/bingxue-logo.png" alt="" /></span><h2 class="m-0"><fmt:message key="menu.heading.customer" />: <c:out value="${CURRENT_TABLE}" /></h2></div>
                    <div class="flex-align-center">
                        <jsp:include page="/components/language_switcher.jsp" />
                        <jsp:include page="/components/theme_toggle.jsp" />
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="menu-empty-container">
                    <h2 class="msg-error"><fmt:message key="menu.access.denied" /></h2>
                    <p><fmt:message key="menu.access.scan_qr" /> 
                        <a href="${pageContext.request.contextPath}/login.jsp" class="text-link-success"><fmt:message key="menu.access.login" /></a>
                        <fmt:message key="menu.access.as_staff" />.
                    </p>
                </div>
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
                <button type="button" class="tab-btn ${(activeTab == 'cart' or activeTab == 'orders') ? '' : 'active'}" data-tab="tab-menu"><fmt:message key="menu.tab.menu" /></button>
                <button type="button" class="tab-btn ${activeTab == 'cart' ? 'active' : ''}" data-tab="tab-cart">
                    <fmt:message key="menu.tab.cart" /> 
                    <c:if test="${totalCartQuantity > 0}">
                        <span class="msg-error tab-badge">${totalCartQuantity}</span>
                    </c:if>
                </button>
                <c:if test="${not empty CURRENT_TABLE}">
                    <button type="button" class="tab-btn ${activeTab == 'orders' ? 'active' : ''}" data-tab="tab-orders">
                        <fmt:message key="menu.tab.orders" />
                        <c:if test="${not empty TABLE_ORDERS and fn:length(TABLE_ORDERS) > 0}">
                            <span class="msg-success tab-badge">${fn:length(TABLE_ORDERS)}</span>
                        </c:if>
                    </button>
                </c:if>
            </div>
        </c:if>
        <div id="tab-menu" class="tab-content ${(activeTab == 'cart' or activeTab == 'orders') ? 'd-none' : 'active'}">
            <c:if test="${not empty CURRENT_TABLE or not empty sessionScope.USER_SESSION}">
                <%-- Lọc món theo nhóm + nhóm "Bán chạy" (main.js: filterMenuByCategory) --%>
                <jsp:include page="/components/menu_cats.jsp" />
                <jsp:include page="/components/menu_grid.jsp" />
            </c:if>
        </div>
        <div id="tab-cart" class="tab-content ${activeTab == 'cart' ? 'active' : 'd-none'}">
            <c:choose>
                <c:when test="${empty sessionScope.CURRENT_CART or empty sessionScope.CURRENT_CART.orderDetails}">
                    <div class="empty-cart-container">
                        <h3><fmt:message key="menu.cart.empty" /></h3>
                        <button type="button" class="btn-login mt-15" data-tab="tab-menu"><fmt:message key="menu.cart.back_to_menu" /></button>
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
                                    <td>
                                        <strong><c:out value="${cartItem.product.productName}" /></strong>
                                        <bx:itemOptions sizeName="${cartItem.sizeName}" iceLevel="${cartItem.iceLevel}" sugarLevel="${cartItem.sugarLevel}" toppings="${cartItem.toppings}" note="${cartItem.note}" />
                                    </td>
                                    <td><fmt:formatNumber value="${cartItem.unitPrice}" pattern="#,###" /> ₫</td>
                                    <td>
                                        <div class="cart-qty-control">
                                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                                <input type="hidden" name="action" value="update" />
                                                <input type="hidden" name="lineKey" value="${fn:escapeXml(cartItem.lineKey)}" />
                                                <input type="hidden" name="quantity" value="${cartItem.quantity - 1}" />
                                                <c:if test="${not empty CURRENT_TABLE}">
                                                    <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                                </c:if>
                                                <button type="submit" class="btn-qty">-</button>
                                            </form>
                                            <span>${cartItem.quantity}</span>
                                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0">
                                                <input type="hidden" name="action" value="update" />
                                                <input type="hidden" name="lineKey" value="${fn:escapeXml(cartItem.lineKey)}" />
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
                                            <input type="hidden" name="lineKey" value="${fn:escapeXml(cartItem.lineKey)}" />
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
                            <button type="button" class="btn-register-action m-0 btn-checkout" id="btnOpenCheckoutModal"><fmt:message key="menu.btn.checkout" /></button>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${not empty CURRENT_TABLE}">
            <div id="tab-orders" class="tab-content ${activeTab == 'orders' ? 'active' : 'd-none'}">
                <c:choose>
                    <c:when test="${empty TABLE_ORDERS}">
                        <div class="empty-cart-container">
                            <h3><fmt:message key="menu.orders.empty" /></h3>
                            <button type="button" class="btn-login mt-15" data-tab="tab-menu"><fmt:message key="menu.cart.back_to_menu" /></button>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="orders-list-container">
                            <c:forEach var="tableOrder" items="${TABLE_ORDERS}">
                                <div class="order-summary-card mb-15">
                                    <div class="flex-between order-header-row">
                                        <div>
                                            <strong>#${tableOrder.orderId}</strong>
                                            <span class="order-time-text"> - ${tableOrder.createdAt}</span>
                                        </div>
                                        <div>
                                            <span class="order-badge order-badge-${fn:toLowerCase(tableOrder.status)}">
                                                <c:choose>
                                                    <c:when test="${tableOrder.status == 'Accepted'}">
                                                        <fmt:message key="menu.orders.status.accepted" />
                                                    </c:when>
                                                    <c:when test="${tableOrder.status == 'Prepared' or tableOrder.status == 'Delivering'}">
                                                        <fmt:message key="menu.orders.status.cooking" />
                                                    </c:when>
                                                    <c:when test="${tableOrder.status == 'Delivered'}">
                                                        <fmt:message key="menu.orders.status.delivered" />
                                                    </c:when>
                                                    <c:when test="${tableOrder.status == 'Cancelled'}">
                                                        <fmt:message key="menu.orders.status.cancelled" />
                                                    </c:when>
                                                    <c:otherwise>${tableOrder.status}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>

                                    <table class="table-items-breakdown mt-10">
                                        <c:forEach var="item" items="${tableOrder.details}">
                                            <tr>
                                                <td>
                                                    <c:out value="${item.productName}" />
                                                    <bx:itemOptions sizeName="${item.sizeName}" iceLevel="${item.iceLevel}" sugarLevel="${item.sugarLevel}" toppings="${item.toppings}" note="${item.note}" />
                                                </td>
                                                <td class="text-center">x${item.quantity}</td>
                                                <td class="text-right"><fmt:formatNumber value="${item.unitPrice * item.quantity}" pattern="#,###" /> ₫</td>
                                            </tr>
                                        </c:forEach>
                                    </table>

                                    <div class="flex-between mt-10 order-footer-row">
                                        <h4 class="m-0"><fmt:message key="menu.cart.total_amount" />: <fmt:formatNumber value="${tableOrder.totalAmount}" pattern="#,###" /> ₫</h4>
                                        <c:if test="${tableOrder.canCancelOrder}">
                                            <form action="${pageContext.request.contextPath}/CartController" method="POST" class="m-0 form-cancel-table-order" data-confirm-msg="<fmt:message key="menu.orders.confirm_cancel" />">
                                                <input type="hidden" name="action" value="cancelTableOrder" />
                                                <input type="hidden" name="orderId" value="${tableOrder.orderId}" />
                                                <input type="hidden" name="tableId" value="${CURRENT_TABLE}" />
                                                <button type="submit" class="btn-order-cancel"><fmt:message key="menu.orders.btn.cancel" /></button>
                                            </form>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
        <jsp:include page="/components/floating_cart.jsp" />
        <jsp:include page="/components/order_confirm_modal.jsp" />
        <jsp:include page="/components/product_option_modal.jsp" />
        <p class="qr-credit"><fmt:message key="footer.icons.credit" /> <a href="https://www.flaticon.com/uicons" target="_blank" rel="noopener">Flaticon</a></p>
        <script src="${pageContext.request.contextPath}/js/main.js"></script>
    </body>
</html>