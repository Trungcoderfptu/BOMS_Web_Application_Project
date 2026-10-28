<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<%-- Lưới món: thẻ món mang data-* cho popup chọn tùy chọn (product_option_modal.jsp). Dùng chung: màn khách QR và POS quầy --%>
<c:if test="${not empty MENU_LIST}">
    <div class="menu-grid">
        <c:forEach var="item" items="${MENU_LIST}">
            <c:set var="isBestSeller" value="false" />
            <c:forEach var="bestId" items="${BEST_SELLER_IDS}">
                <c:if test="${bestId == item.productId}"><c:set var="isBestSeller" value="true" /></c:if>
            </c:forEach>
            <c:set var="itemImageSrc" value="" />
            <c:if test="${not empty item.imageUrl}">
                <c:choose>
                    <c:when test="${fn:startsWith(item.imageUrl, 'http')}"><c:set var="itemImageSrc" value="${item.imageUrl}" /></c:when>
                    <c:otherwise><c:set var="itemImageSrc" value="${pageContext.request.contextPath}/${item.imageUrl}" /></c:otherwise>
                </c:choose>
            </c:if>
            <%-- data-* dùng cho popup chọn size / đá / đường / topping / ghi chú (product_option_modal.jsp) --%>
            <div class="product-card ${item.available ? '' : 'out-of-stock'}"
                 data-category="${item.categoryId}"
                 data-bestseller="${isBestSeller}"
                 data-product-id="${item.productId}"
                 data-name="${fn:escapeXml(item.productName)}"
                 data-price="${item.price}"
                 data-image="${fn:escapeXml(itemImageSrc)}"
                 data-desc="${fn:escapeXml(item.description)}"
                 data-video="${fn:escapeXml(item.videoUrl)}"
                 data-has-options="${item.hasOptions}">
                <div class="product-img-placeholder">
                    <c:if test="${item.featured}">
                        <span class="badge-featured"><fmt:message key="menu.badge.featured" /></span>
                    </c:if>
                    <c:if test="${not item.available}">
                        <span class="badge-out-of-stock"><fmt:message key="menu.badge.out_of_stock" /></span>
                    </c:if>
                    <c:choose>
                        <c:when test="${not empty item.imageUrl}">
                            <c:choose>
                                <c:when test="${fn:startsWith(item.imageUrl, 'http')}">
                                    <img src="${item.imageUrl}" alt="${fn:escapeXml(item.productName)}" class="product-card-img" />
                                </c:when>
                                <c:otherwise>
                                    <img src="${pageContext.request.contextPath}/${item.imageUrl}" alt="${fn:escapeXml(item.productName)}" class="product-card-img" />
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
                            <span class="placeholder-icon"><i class="fi fi-rr-mug-hot" aria-hidden="true"></i></span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <h3><c:out value="${item.productName}" /></h3>
                <div class="product-price"><fmt:formatNumber value="${item.price}" pattern="#,###" /> ₫</div>

                <c:choose>
                    <c:when test="${item.available}">
                        <%-- Mở popup chọn tùy chọn; form thêm giỏ nằm trong popup --%>
                        <button type="button" class="btn-add-cart" data-action="open-product-option"><fmt:message key="menu.btn.add_to_cart" /></button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="btn-add-cart btn-disabled" disabled><fmt:message key="menu.btn.out_of_stock" /></button>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:forEach>
    </div>
</c:if>
