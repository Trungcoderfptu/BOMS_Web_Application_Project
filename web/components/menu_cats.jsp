<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<%-- Thanh lọc nhóm món + "Bán chạy" (main.js filterMenuByCategory). Dùng chung: màn khách QR và POS quầy (param.layout=pos -> xếp dọc) --%>
<div class="menu-cats ${param.layout == 'pos' ? 'pos-cats' : ''}">
    <button type="button" class="menu-cat active" data-menu-filter="all"><fmt:message key="menu.filter.all" /></button>
    <c:if test="${not empty BEST_SELLER_IDS}">
        <button type="button" class="menu-cat" data-menu-filter="best"><fmt:message key="menu.filter.best_seller" /></button>
    </c:if>
    <c:forEach var="cat" items="${MENU_CATEGORIES}">
        <c:if test="${cat.inUse}">
            <button type="button" class="menu-cat" data-menu-filter="${cat.categoryId}"><c:out value="${cat.categoryName}" /></button>
        </c:if>
    </c:forEach>
</div>
