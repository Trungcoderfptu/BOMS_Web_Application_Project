<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">

        <div class="flex-between mt-15 mb-20">
            <h2 class="m-0"><fmt:message key="order.management.heading" /></h2>
        </div>

        <c:if test="${not empty ERROR_MESSAGE}">
            <p class="msg-error text-center"><fmt:message key="${ERROR_MESSAGE}" /></p>
        </c:if>
        <c:if test="${not empty SUCCESS_MESSAGE}">
            <p class="msg-success text-center"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
        </c:if>

        <c:set var="curActiveTab" value="${not empty ACTIVE_TAB ? ACTIVE_TAB : (not empty param.activeTab ? param.activeTab : 'tab-all-orders')}" />

        <form action="${pageContext.request.contextPath}/OrderManagementController" method="GET" class="order-filter-panel">
            <input type="hidden" name="activeTab" value="${curActiveTab}" />
            <div class="filter-item">
                <label for="filterDate"><fmt:message key="order.management.filter.date" /></label>
                <input type="date" id="filterDate" name="filterDate" value="${CURRENT_FILTER_DATE}" class="filter-input" />
            </div>

            <div class="filter-item">
                <label for="filterStatus"><fmt:message key="order.management.filter.status" /></label>
                <select id="filterStatus" name="filterStatus" class="filter-input">
                    <option value="ALL" ${CURRENT_FILTER_STATUS == 'ALL' ? 'selected' : ''}><fmt:message key="order.management.filter.status.all" /></option>
                    <option value="Accepted" ${CURRENT_FILTER_STATUS == 'Accepted' ? 'selected' : ''}>Accepted</option>
                    <option value="Prepared" ${CURRENT_FILTER_STATUS == 'Prepared' ? 'selected' : ''}>Prepared</option>
                    <option value="Waiting for dispatch" ${CURRENT_FILTER_STATUS == 'Waiting for dispatch' ? 'selected' : ''}><fmt:message key="order.status.waiting_dispatch" /></option>
                    <option value="Delivering" ${CURRENT_FILTER_STATUS == 'Delivering' ? 'selected' : ''}>Delivering</option>
                    <option value="Delivered" ${CURRENT_FILTER_STATUS == 'Delivered' ? 'selected' : ''}>Delivered</option>
                    <option value="Paid" ${CURRENT_FILTER_STATUS == 'Paid' ? 'selected' : ''}>Paid</option>
                    <option value="Cancelled" ${CURRENT_FILTER_STATUS == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                </select>
            </div>

            <button type="submit" class="btn-login"><fmt:message key="order.management.filter.btn" /></button>
        </form>

        <div class="tab-nav d-flex mt-20 mb-20">
            <button type="button" class="tab-btn ${curActiveTab == 'tab-all-orders' ? 'active' : ''}" onclick="openTab(event, 'tab-all-orders')">
                <fmt:message key="order.management.tab.all" /> (${not empty ORDER_COUNTS.all ? ORDER_COUNTS.all : 0})
            </button>
            <button type="button" class="tab-btn ${curActiveTab == 'tab-counter-orders' ? 'active' : ''}" onclick="openTab(event, 'tab-counter-orders')">
                <fmt:message key="order.management.tab.counter" /> (${not empty ORDER_COUNTS.counter ? ORDER_COUNTS.counter : 0})
            </button>
            <button type="button" class="tab-btn ${curActiveTab == 'tab-table-orders' ? 'active' : ''}" onclick="openTab(event, 'tab-table-orders')">
                <fmt:message key="order.management.tab.table" /> (${not empty ORDER_COUNTS.table ? ORDER_COUNTS.table : 0})
            </button>
            <button type="button" class="tab-btn ${curActiveTab == 'tab-delivery-orders' ? 'active' : ''}" onclick="openTab(event, 'tab-delivery-orders')">
                <fmt:message key="order.management.tab.delivery" /> (${not empty ORDER_COUNTS.delivery ? ORDER_COUNTS.delivery : 0})
            </button>
        </div>

        <div id="tab-all-orders" class="tab-content ${curActiveTab == 'tab-all-orders' ? 'active' : 'd-none'}">
            <c:set var="targetOrderType" value="ALL" scope="request" />
            <c:set var="tabKey" value="tab-all-orders" scope="request" />
            <jsp:include page="components/standard_orders_table.jsp" />
        </div>

        <div id="tab-counter-orders" class="tab-content ${curActiveTab == 'tab-counter-orders' ? 'active' : 'd-none'}">
            <c:set var="targetOrderType" value="Counter" scope="request" />
            <c:set var="tabKey" value="tab-counter-orders" scope="request" />
            <jsp:include page="components/standard_orders_table.jsp" />
        </div>

        <div id="tab-table-orders" class="tab-content ${curActiveTab == 'tab-table-orders' ? 'active' : 'd-none'}">
            <c:set var="targetOrderType" value="Table" scope="request" />
            <c:set var="tabKey" value="tab-table-orders" scope="request" />
            <jsp:include page="components/standard_orders_table.jsp" />
        </div>

        <div id="tab-delivery-orders" class="tab-content ${curActiveTab == 'tab-delivery-orders' ? 'active' : 'd-none'}">
            <jsp:include page="components/delivery_orders_table.jsp" />
        </div>

        <jsp:include page="components/staff_cancel_modal.jsp" />
        <jsp:include page="components/delivery_info_modal.jsp" />
        <jsp:include page="components/revert_delivery_modal.jsp" />
        </main>
        <jsp:include page="footer.jsp" />
