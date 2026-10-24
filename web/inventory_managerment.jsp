<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <div class="flex-between mt-15 mb-20">
        <h2 class="m-0"><fmt:message key="inventory.heading" /></h2>
        <c:if test="${IS_MANAGER}">
            <div class="d-flex gap-10">
                <button type="button" class="btn-login btn-secondary" data-action="open-inventory-category">
                    📁 <fmt:message key="inventory.category.btn.manage" />
                </button>
                <button type="button" class="btn-login" data-action="open-create-ingredient">
                    + <fmt:message key="inventory.btn.add" />
                </button>
            </div>
        </c:if>
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

    <%-- Thanh Tìm Kiếm Nguyên Liệu --%>
    <form action="${pageContext.request.contextPath}/InventoryManagementController" method="GET" class="order-filter-panel mb-20">
        <div class="filter-item flex-grow-1">
            <label for="searchIngredient"><fmt:message key="inventory.table.name" />:</label>
            <input type="text" id="searchIngredient" name="search" value="${fn:escapeXml(SEARCH_TERM)}"
                   placeholder="<fmt:message key="inventory.search.placeholder" />"
                   class="filter-input flex-grow-1" />
        </div>
        <button type="submit" class="btn-login"><fmt:message key="inventory.btn.search" /></button>
        <c:if test="${not empty SEARCH_TERM}">
            <a href="${pageContext.request.contextPath}/InventoryManagementController" class="btn-modal-cancel">
                <fmt:message key="inventory.btn.clear_search" />
            </a>
        </c:if>
    </form>

    <%-- Bảng Danh Sách Nguyên Liệu Kho --%>
    <c:choose>
        <c:when test="${empty INGREDIENT_LIST}">
            <div class="empty-cart-container">
                <h3 class="msg-info"><fmt:message key="inventory.empty" /></h3>
            </div>
        </c:when>
        <c:otherwise>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th><fmt:message key="inventory.table.id" /></th>
                        <th><fmt:message key="inventory.table.name" /></th>
                        <th><fmt:message key="inventory.table.category" /></th>
                        <th><fmt:message key="inventory.table.quantity" /></th>
                        <th><fmt:message key="inventory.table.unit" /></th>
                        <th><fmt:message key="inventory.table.price" /></th>
                        <th><fmt:message key="inventory.table.location" /></th>
                        <th><fmt:message key="inventory.table.threshold" /></th>
                        <th><fmt:message key="inventory.table.status" /></th>
                        <c:if test="${IS_MANAGER}">
                            <th><fmt:message key="inventory.table.action" /></th>
                        </c:if>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${INGREDIENT_LIST}">
                        <tr>
                            <td><strong>#${item.ingredientId}</strong></td>
                            <td><strong><c:out value="${item.ingredientName}" /></strong></td>
                            <td><span class="badge-category"><c:out value="${item.categoryName}" /></span></td>
                            <td class="font-bold">
                                <fmt:formatNumber value="${item.quantity}" maxFractionDigits="2" />
                            </td>
                            <td><c:out value="${item.unit}" /></td>
                            <td class="font-bold text-nowrap">
                                <fmt:formatNumber value="${item.price}" pattern="#,###" /> ₫
                            </td>
                            <td><c:out value="${item.storageLocation}" /></td>
                            <td>
                                <fmt:formatNumber value="${item.warningThreshold}" maxFractionDigits="2" /> <c:out value="${item.unit}" />
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.stockStatus == 'OUT_OF_STOCK'}">
                                        <span class="badge-status-inactive">
                                            <fmt:message key="inventory.status.out_of_stock" />
                                        </span>
                                    </c:when>
                                    <c:when test="${item.stockStatus == 'LOW_STOCK'}">
                                        <span class="badge-stock-out">
                                            <fmt:message key="inventory.status.low_stock" />
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-stock-ok">
                                            <fmt:message key="inventory.status.in_stock" />
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <c:if test="${IS_MANAGER}">
                                <td>
                                    <div class="menu-action-buttons">
                                        <button type="button" class="btn-action-edit"
                                                data-action="edit-ingredient"
                                                data-id="${item.ingredientId}"
                                                data-cat-id="${item.categoryId}"
                                                data-name="${fn:escapeXml(item.ingredientName)}"
                                                data-unit="${fn:escapeXml(item.unit)}"
                                                data-qty="${item.quantity}"
                                                data-price="${item.price}"
                                                data-loc="${fn:escapeXml(item.storageLocation)}"
                                                data-threshold="${item.warningThreshold}">
                                            <fmt:message key="inventory.btn.edit" />
                                        </button>
                                        <button type="button" class="btn-action-toggle-resume"
                                                data-action="open-threshold"
                                                data-id="${item.ingredientId}"
                                                data-name="${fn:escapeXml(item.ingredientName)}"
                                                data-threshold="${item.warningThreshold}">
                                            <fmt:message key="inventory.btn.update_threshold" />
                                        </button>
                                        <form action="${pageContext.request.contextPath}/InventoryManagementController" method="POST" class="m-0 d-inline"
                                              data-confirm="<fmt:message key="inventory.confirm.delete" />">
                                            <input type="hidden" name="action" value="delete" />
                                            <input type="hidden" name="ingredientId" value="${item.ingredientId}" />
                                            <button type="submit" class="btn-action-toggle-stop">
                                                <fmt:message key="inventory.btn.delete" />
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <jsp:include page="components/inventory_form_modal.jsp" />
</main>

<jsp:include page="footer.jsp" />
