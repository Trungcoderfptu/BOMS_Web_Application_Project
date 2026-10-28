<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<div id="notificationDropdown" class="notification-dropdown d-none">
    <div class="notification-dropdown-header flex-between align-center">
        <h4 class="font-sm font-bold m-0">
            <fmt:message key="notification.title" />
        </h4>
        <button type="button" class="btn-link font-xs text-primary border-0 bg-transparent cursor-pointer" data-action="mark-all-notifications">
            <fmt:message key="notification.btn.mark_all_read" />
        </button>
    </div>
    <div id="notificationListContainer" class="notification-list-container">
        <div id="notificationLoading" class="text-center p-15 text-muted font-xs">
            <fmt:message key="notification.loading" />
        </div>
        <div id="notificationEmptyState" class="text-center p-20 text-muted font-xs d-none">
            <fmt:message key="notification.empty" />
        </div>
        <ul id="notificationItemsList" class="notification-items-list no-bullet m-0 p-0"></ul>
    </div>
</div>
