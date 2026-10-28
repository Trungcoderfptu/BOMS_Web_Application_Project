<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<%-- Quản lý size ly & topping dùng chung. Manager/Admin: thêm, sửa giá, bật/tắt. Staff: chỉ xem. --%>
<div id="menuOptionModal" class="modal-overlay d-none">
    <div class="modal-card modal-card-md">
        <div class="modal-header">
            <h3 class="m-0"><fmt:message key="options.modal.title" /></h3>
            <button type="button" class="btn-modal-close" data-close-modal="menuOptionModal"><i class="fi fi-rr-cross-small" aria-hidden="true"></i></button>
        </div>
        <div class="modal-body">
            <c:if test="${not CAN_MANAGE_OPTIONS}">
                <p class="msg-warning"><fmt:message key="options.readonly_note" /></p>
            </c:if>

            <%-- ===== SIZE LY ===== --%>
            <h4 class="option-admin-heading"><fmt:message key="options.size.heading" /></h4>
            <c:if test="${CAN_MANAGE_OPTIONS}">
                <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="option-admin-row option-admin-new">
                    <input type="hidden" name="action" value="createSize" />
                    <input type="text" name="sizeName" required maxlength="50" class="filter-input" placeholder="<fmt:message key='options.label.name' />" />
                    <input type="number" name="extraPrice" min="0" step="1000" value="0" class="filter-input" title="<fmt:message key='options.label.extra_price' />" />
                    <input type="number" name="sortOrder" value="0" class="filter-input option-admin-sort" title="<fmt:message key='options.label.sort' />" />
                    <button type="submit" class="btn-status-save"><fmt:message key="options.btn.add" /></button>
                </form>
            </c:if>
            <c:choose>
                <c:when test="${empty SIZE_LIST}">
                    <p class="option-admin-empty"><fmt:message key="options.empty" /></p>
                </c:when>
                <c:otherwise>
                    <div class="option-admin-list">
                        <div class="option-admin-row option-admin-head">
                            <span><fmt:message key="options.label.name" /></span>
                            <span><fmt:message key="options.label.extra_price" /></span>
                            <span><fmt:message key="options.label.sort" /></span>
                            <span><fmt:message key="options.label.status" /></span>
                        </div>
                        <c:forEach var="s" items="${SIZE_LIST}">
                            <div class="option-admin-row ${s.active ? '' : 'is-inactive'}">
                                <c:choose>
                                    <c:when test="${CAN_MANAGE_OPTIONS}">
                                        <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="option-admin-edit">
                                            <input type="hidden" name="action" value="updateSize" />
                                            <input type="hidden" name="sizeId" value="${s.sizeId}" />
                                            <input type="text" name="sizeName" required maxlength="50" class="filter-input" value="<c:out value='${s.sizeName}' />" />
                                            <input type="number" name="extraPrice" min="0" step="1000" class="filter-input" value="<fmt:formatNumber value='${s.extraPrice}' pattern='0' />" />
                                            <input type="number" name="sortOrder" class="filter-input option-admin-sort" value="${s.sortOrder}" />
                                            <button type="submit" class="btn-status-save"><fmt:message key="options.btn.save" /></button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="m-0">
                                            <input type="hidden" name="action" value="toggleSize" />
                                            <input type="hidden" name="sizeId" value="${s.sizeId}" />
                                            <button type="submit" class="btn-option-toggle">
                                                <fmt:message key="${s.active ? 'options.btn.toggle_off' : 'options.btn.toggle_on'}" />
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span><c:out value="${s.sizeName}" /></span>
                                        <span>+<fmt:formatNumber value="${s.extraPrice}" pattern="#,##0" /> ₫</span>
                                        <span>${s.sortOrder}</span>
                                        <span><fmt:message key="${s.active ? 'options.status.active' : 'options.status.inactive'}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

            <%-- ===== TOPPING ===== --%>
            <h4 class="option-admin-heading"><fmt:message key="options.topping.heading" /></h4>
            <c:if test="${CAN_MANAGE_OPTIONS}">
                <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="option-admin-row option-admin-new">
                    <input type="hidden" name="action" value="createTopping" />
                    <input type="text" name="toppingName" required maxlength="50" class="filter-input" placeholder="<fmt:message key='options.label.name' />" />
                    <input type="number" name="toppingPrice" min="0" step="1000" value="0" class="filter-input" title="<fmt:message key='options.label.price' />" />
                    <button type="submit" class="btn-status-save"><fmt:message key="options.btn.add" /></button>
                </form>
            </c:if>
            <c:choose>
                <c:when test="${empty TOPPING_LIST}">
                    <p class="option-admin-empty"><fmt:message key="options.empty" /></p>
                </c:when>
                <c:otherwise>
                    <div class="option-admin-list">
                        <c:forEach var="t" items="${TOPPING_LIST}">
                            <div class="option-admin-row ${t.active ? '' : 'is-inactive'}">
                                <c:choose>
                                    <c:when test="${CAN_MANAGE_OPTIONS}">
                                        <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="option-admin-edit">
                                            <input type="hidden" name="action" value="updateTopping" />
                                            <input type="hidden" name="toppingId" value="${t.toppingId}" />
                                            <input type="text" name="toppingName" required maxlength="50" class="filter-input" value="<c:out value='${t.toppingName}' />" />
                                            <input type="number" name="toppingPrice" min="0" step="1000" class="filter-input" value="<fmt:formatNumber value='${t.price}' pattern='0' />" />
                                            <button type="submit" class="btn-status-save"><fmt:message key="options.btn.save" /></button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="m-0">
                                            <input type="hidden" name="action" value="toggleTopping" />
                                            <input type="hidden" name="toppingId" value="${t.toppingId}" />
                                            <button type="submit" class="btn-option-toggle">
                                                <fmt:message key="${t.active ? 'options.btn.toggle_off' : 'options.btn.toggle_on'}" />
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span><c:out value="${t.toppingName}" /></span>
                                        <span>+<fmt:formatNumber value="${t.price}" pattern="#,##0" /> ₫</span>
                                        <span><fmt:message key="${t.active ? 'options.status.active' : 'options.status.inactive'}" /></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
