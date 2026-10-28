<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:set var="optLocale" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${optLocale}" />
<fmt:setBundle basename="resources.messages" />
<%-- Popup chi tiết món kiểu menu online: ảnh lớn, mô tả, video, size (bắt buộc), mức đá, mức đường,
     topping (chọn nhiều), ghi chú, số lượng. Dữ liệu từng món lấy từ data-* của .product-card (main.js điền vào).
     Bố cục: ảnh đứng yên phía trên, chỉ .option-scroll cuộn, chân popup (số lượng + nút) luôn ở đáy. --%>
<div id="productOptionModal" class="modal-overlay d-none">
    <div class="modal-card option-modal">
        <button type="button" class="btn-modal-close option-modal-close" data-close-modal="productOptionModal"
                aria-label="<fmt:message key='option.close' />">&times;</button>

        <div class="option-media" data-action="toggle-option-zoom">
            <img id="optModalImg" class="option-img d-none" src="" alt="" />
            <span id="optModalLetter" class="option-img-letter d-none"></span>
            <span class="option-zoom-hint"><fmt:message key="option.zoom_hint" /></span>
        </div>

        <form id="productOptionForm" action="${pageContext.request.contextPath}/CartController" method="POST"
              class="option-form" data-locale="${fn:escapeXml(optLocale)}">
            <input type="hidden" name="action" value="add" />
            <input type="hidden" id="optModalProductId" name="productId" value="" />
            <input type="hidden" id="optModalBuyNow" name="buyNow" value="false" />
            <c:if test="${not empty CURRENT_TABLE}">
                <input type="hidden" name="tableId" value="${fn:escapeXml(CURRENT_TABLE)}" />
            </c:if>

            <div class="option-scroll">
                <div class="option-title-row">
                    <h3 id="optModalName" class="option-name"></h3>
                    <a id="optModalVideo" class="option-video d-none" href="#" target="_blank" rel="noopener noreferrer">
                        <fmt:message key="option.video" />
                    </a>
                </div>
                <p id="optModalDesc" class="option-desc d-none"></p>

                <%-- Phần tuỳ chọn: ẩn với món không có tuỳ chọn (HasOptions = 0) --%>
                <div id="optModalChoices">
                    <c:if test="${not empty MENU_SIZES}">
                        <fieldset class="option-group option-box">
                            <legend><fmt:message key="option.size" /> <span class="option-required"><fmt:message key="option.required" /></span></legend>
                            <c:forEach var="size" items="${MENU_SIZES}" varStatus="st">
                                <label class="option-choice">
                                    <input type="radio" name="sizeId" value="${size.sizeId}" data-extra="${size.extraPrice}" ${st.first ? 'checked' : ''} />
                                    <span class="option-choice-name"><c:out value="${size.sizeName}" /></span>
                                    <span class="option-choice-price">+<fmt:formatNumber value="${size.extraPrice}" pattern="#,##0" /> ₫</span>
                                </label>
                            </c:forEach>
                        </fieldset>
                    </c:if>

                    <div class="option-pair">
                        <fieldset class="option-group option-box">
                            <legend><fmt:message key="option.ice" /></legend>
                            <c:forEach var="lv" items="${ICE_LEVELS}" varStatus="st">
                                <label class="option-choice">
                                    <input type="radio" name="iceLevel" value="${lv}" ${st.first ? 'checked' : ''} />
                                    <span class="option-choice-name"><fmt:message key="option.ice.${lv}" /></span>
                                </label>
                            </c:forEach>
                        </fieldset>

                        <fieldset class="option-group option-box">
                            <legend><fmt:message key="option.sugar" /></legend>
                            <c:forEach var="lv" items="${SUGAR_LEVELS}" varStatus="st">
                                <label class="option-choice">
                                    <input type="radio" name="sugarLevel" value="${lv}" ${st.first ? 'checked' : ''} />
                                    <span class="option-choice-name"><fmt:message key="option.sugar.${lv}" /></span>
                                </label>
                            </c:forEach>
                        </fieldset>
                    </div>

                    <c:if test="${not empty MENU_TOPPINGS}">
                        <fieldset class="option-group option-box">
                            <legend><fmt:message key="option.toppings" /></legend>
                            <c:forEach var="tp" items="${MENU_TOPPINGS}">
                                <label class="option-choice">
                                    <input type="checkbox" name="toppingId" value="${tp.toppingId}" data-extra="${tp.price}" />
                                    <span class="option-choice-name"><c:out value="${tp.toppingName}" /></span>
                                    <span class="option-choice-price">+<fmt:formatNumber value="${tp.price}" pattern="#,##0" /> ₫</span>
                                </label>
                            </c:forEach>
                        </fieldset>
                    </c:if>
                </div>

                <fieldset class="option-group">
                    <legend><label for="optModalNote"><fmt:message key="option.note" /></label></legend>
                    <textarea id="optModalNote" name="itemNote" rows="2" maxlength="200" class="filter-input option-note"
                              placeholder="<fmt:message key='option.note.placeholder' />"></textarea>
                </fieldset>
            </div>

            <div class="option-footer">
                <div class="option-qty" aria-label="<fmt:message key='option.quantity' />">
                    <button type="button" class="btn-qty" data-action="opt-qty" data-step="-1">-</button>
                    <input type="number" id="optModalQty" name="quantity" value="1" min="1" max="99" readonly />
                    <button type="button" class="btn-qty" data-action="opt-qty" data-step="1">+</button>
                </div>
                <button type="submit" class="btn-option-add"><fmt:message key="menu.btn.add_to_cart" /></button>
                <button type="button" class="btn-option-buy" data-action="opt-buy-now">
                    <fmt:message key="option.btn.buy_now" /> - <span id="optModalTotal">0</span> ₫
                </button>
            </div>
        </form>
    </div>
</div>
