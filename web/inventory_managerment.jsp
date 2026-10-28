<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- Bố cục B: danh sách + chi tiết. Trái: tìm kiếm + danh sách nguyên liệu (bấm 1 dòng);
     phải: chi tiết + thao tác của dòng đang chọn (main.js pickIngredient chép data-* của dòng sang khung chi tiết) --%>
<main class="main-content">
    <div class="page-head">
        <h2><fmt:message key="inventory.heading" /></h2>
        <c:if test="${IS_MANAGER}">
            <div class="d-flex gap-10">
                <button type="button" class="btn-plain" data-action="open-inventory-category"><fmt:message key="inventory.category.btn.manage" /></button>
                <button type="button" class="btn-next" data-action="open-create-ingredient">+ <fmt:message key="inventory.btn.add" /></button>
            </div>
        </c:if>
    </div>

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

    <div class="md-layout">
        <section class="panel md-list">
            <form action="${ctx}/InventoryManagementController" method="GET" class="md-filter">
                <input type="text" id="searchIngredient" name="search" value="${fn:escapeXml(SEARCH_TERM)}" class="filter-input"
                       placeholder="<fmt:message key="inventory.search.placeholder" />" aria-label="<fmt:message key="inventory.table.name" />" />
                <button type="submit" class="btn-plain"><fmt:message key="inventory.btn.search" /></button>
                <c:if test="${not empty SEARCH_TERM}">
                    <a href="${ctx}/InventoryManagementController" class="btn-plain"><fmt:message key="inventory.btn.clear_search" /></a>
                </c:if>
            </form>

            <c:choose>
                <c:when test="${empty INGREDIENT_LIST}">
                    <p class="todo-empty"><fmt:message key="inventory.empty" /></p>
                </c:when>
                <c:otherwise>
                    <ul class="md-rows">
                        <c:forEach var="item" items="${INGREDIENT_LIST}">
                            <c:choose>
                                <c:when test="${item.stockStatus == 'OUT_OF_STOCK'}"><c:set var="stKey" value="inventory.status.out_of_stock" /><c:set var="stCls" value="badge-status-inactive" /></c:when>
                                <c:when test="${item.stockStatus == 'LOW_STOCK'}"><c:set var="stKey" value="inventory.status.low_stock" /><c:set var="stCls" value="badge-stock-out" /></c:when>
                                <c:otherwise><c:set var="stKey" value="inventory.status.in_stock" /><c:set var="stCls" value="badge-stock-ok" /></c:otherwise>
                            </c:choose>
                            <fmt:message key="${stKey}" var="stText" />
                            <li>
                                <button type="button" class="md-row" data-action="pick-ingredient"
                                        data-id="${item.ingredientId}"
                                        data-cat-id="${item.categoryId}"
                                        data-cat-name="${fn:escapeXml(item.categoryName)}"
                                        data-name="${fn:escapeXml(item.ingredientName)}"
                                        data-unit="${fn:escapeXml(item.unit)}"
                                        data-qty="${item.quantity}"
                                        data-price="${item.price}"
                                        data-loc="${fn:escapeXml(item.storageLocation)}"
                                        data-threshold="${item.warningThreshold}"
                                        data-status-text="${fn:escapeXml(stText)}">
                                    <span class="md-row-top">
                                        <strong><c:out value="${item.ingredientName}" /></strong>
                                        <span class="${stCls}">${stText}</span>
                                    </span>
                                    <span class="muted"><c:out value="${item.categoryName}" /> · <fmt:formatNumber value="${item.quantity}" maxFractionDigits="2" /> <c:out value="${item.unit}" /></span>
                                </button>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>

        <section id="invDetail" class="panel md-detail">
            <p id="invDetailEmpty" class="md-placeholder"><fmt:message key="md.pick.ingredient" /></p>
            <div id="invDetailBody" class="d-none">
                <h3 id="invDName" class="panel-title"></h3>
                <table class="admin-table report-info-table">
                    <tr><th><fmt:message key="inventory.table.category" /></th><td id="invDCat"></td></tr>
                    <tr><th><fmt:message key="inventory.table.quantity" /></th><td id="invDQty"></td></tr>
                    <tr><th><fmt:message key="inventory.table.threshold" /></th><td id="invDThreshold"></td></tr>
                    <tr><th><fmt:message key="inventory.table.status" /></th><td id="invDStatus"></td></tr>
                    <tr><th><fmt:message key="inventory.table.price" /></th><td id="invDPrice"></td></tr>
                    <tr><th><fmt:message key="inventory.table.location" /></th><td id="invDLoc"></td></tr>
                </table>
                <c:if test="${IS_MANAGER}">
                    <div class="kanban-actions mt-15">
                        <button type="button" id="invDEdit" class="btn-next" data-action="edit-ingredient"><fmt:message key="inventory.btn.edit" /></button>
                        <button type="button" id="invDThresholdBtn" class="btn-plain" data-action="open-threshold"><fmt:message key="inventory.btn.update_threshold" /></button>
                        <form action="${ctx}/InventoryManagementController" method="POST" class="m-0" data-confirm="<fmt:message key="inventory.confirm.delete" />">
                            <input type="hidden" name="action" value="delete" />
                            <input type="hidden" id="invDDeleteId" name="ingredientId" value="" />
                            <button type="submit" class="btn-plain"><fmt:message key="inventory.btn.delete" /></button>
                        </form>
                    </div>
                </c:if>
            </div>
        </section>
    </div>

    <jsp:include page="components/inventory_form_modal.jsp" />
</main>

<jsp:include page="footer.jsp" />
