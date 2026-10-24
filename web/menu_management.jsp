<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <div class="flex-between mt-15 mb-20">
        <h2 class="m-0"><fmt:message key="menu.mgmt.heading" /></h2>
        <div class="d-flex gap-10">
            <button type="button" class="btn-login btn-secondary" data-action="open-menu-category">
                📁 <fmt:message key="menu.category.btn.manage" />
            </button>
            <button type="button" class="btn-login" data-action="open-create-product">
                + <fmt:message key="menu.mgmt.btn.add" />
            </button>
        </div>
    </div>

    <%-- Flash Message Thông Báo Một Lần (Chuẩn PRG) --%>
    <c:if test="${not empty sessionScope.ERROR_MESSAGE}">
        <p class="msg-error text-center"><fmt:message key="${sessionScope.ERROR_MESSAGE}" /></p>
        <c:remove var="ERROR_MESSAGE" scope="session" />
    </c:if>
    <c:if test="${not empty ERROR_MESSAGE}">
        <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
    </c:if>

    <c:if test="${not empty sessionScope.SUCCESS_MESSAGE}">
        <p class="msg-success text-center"><fmt:message key="${sessionScope.SUCCESS_MESSAGE}" /></p>
        <c:remove var="SUCCESS_MESSAGE" scope="session" />
    </c:if>
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success text-center"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
    </c:if>

    <%-- Thanh Bộ Lọc Danh Mục Thực Đơn --%>
    <form action="${pageContext.request.contextPath}/MenuManagementController" method="GET" class="order-filter-panel mb-20">
        <div class="filter-item">
            <label for="filterCategory"><fmt:message key="menu.mgmt.filter.category" /></label>
            <select id="filterCategory" name="filterCategory" class="filter-input" data-auto-submit="true">
                <option value="ALL" ${empty CURRENT_FILTER_CATEGORY or CURRENT_FILTER_CATEGORY == 'ALL' ? 'selected' : ''}>
                    <fmt:message key="menu.mgmt.filter.all" />
                </option>
                <c:forEach var="cat" items="${CATEGORY_LIST}">
                    <c:set var="catIdStr">${cat.categoryId}</c:set>
                    <option value="${cat.categoryId}" ${CURRENT_FILTER_CATEGORY == catIdStr ? 'selected' : ''}>
                        <c:out value="${cat.categoryName}" />
                    </option>
                </c:forEach>
            </select>
        </div>
        <button type="submit" class="btn-login"><fmt:message key="menu.mgmt.filter.btn" /></button>
    </form>

    <%-- Bảng Quản Lý Thực Đơn --%>
    <c:choose>
        <c:when test="${empty PRODUCT_LIST}">
            <div class="empty-cart-container">
                <h3 class="msg-info"><fmt:message key="menu.mgmt.empty" /></h3>
            </div>
        </c:when>
        <c:otherwise>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th><fmt:message key="menu.mgmt.table.id" /></th>
                        <th><fmt:message key="menu.mgmt.table.image" /></th>
                        <th><fmt:message key="menu.mgmt.table.name" /></th>
                        <th><fmt:message key="menu.mgmt.table.category" /></th>
                        <th><fmt:message key="menu.mgmt.table.price" /></th>
                        <th><fmt:message key="menu.mgmt.table.status" /></th>
                        <th><fmt:message key="menu.mgmt.table.stock" /></th>
                        <th><fmt:message key="menu.mgmt.table.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="product" items="${PRODUCT_LIST}">
                        <tr>
                            <td><strong>#${product.productId}</strong></td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${not empty product.imageUrl}">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(product.imageUrl, 'http')}">
                                                <img src="${product.imageUrl}" alt="${fn:escapeXml(product.productName)}" class="menu-thumb" />
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/${product.imageUrl}" alt="${fn:escapeXml(product.productName)}" class="menu-thumb" />
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="menu-thumb-placeholder">☕</div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <strong><c:out value="${product.productName}" /></strong>
                            </td>
                            <td>
                                <span class="badge-category"><c:out value="${product.categoryName}" /></span>
                            </td>
                            <td class="font-bold text-nowrap">
                                <fmt:formatNumber value="${product.price}" pattern="#,###" /> ₫
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${product.isAvailable}">
                                        <span class="badge-status-active"><fmt:message key="menu.mgmt.status.active" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-status-inactive"><fmt:message key="menu.mgmt.status.inactive" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${product.availableInStock}">
                                        <span class="badge-stock-ok"><fmt:message key="menu.mgmt.stock.sufficient" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-stock-out"><fmt:message key="menu.mgmt.stock.insufficient" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="menu-action-buttons">
                                    <button type="button" class="btn-action-edit"
                                            data-action="edit-product"
                                            data-id="${product.productId}"
                                            data-name="${fn:escapeXml(product.productName)}"
                                            data-cat-id="${product.categoryId}"
                                            data-price="${product.price}"
                                            data-img="${fn:escapeXml(product.imageUrl)}"
                                            data-avail="${product.isAvailable}"
                                            data-recipe="${fn:escapeXml(product.recipeData)}">
                                        <fmt:message key="menu.mgmt.btn.edit" />
                                    </button>

                                    <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="m-0 d-inline">
                                        <input type="hidden" name="action" value="toggleStatus" />
                                        <input type="hidden" name="productId" value="${product.productId}" />
                                        <input type="hidden" name="filterCategory" value="${CURRENT_FILTER_CATEGORY}" />
                                        <c:choose>
                                            <c:when test="${product.isAvailable}">
                                                <button type="submit" class="btn-action-toggle-stop">
                                                    <fmt:message key="menu.mgmt.btn.toggle_stop" />
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="submit" class="btn-action-toggle-resume">
                                                    <fmt:message key="menu.mgmt.btn.toggle_resume" />
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <jsp:include page="components/product_form_modal.jsp" />
</main>

<jsp:include page="footer.jsp" />

