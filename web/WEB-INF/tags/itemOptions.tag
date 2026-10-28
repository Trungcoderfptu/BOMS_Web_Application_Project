<%@tag description="Hiển thị tuỳ chọn của 1 dòng món: size, đá, đường, topping, ghi chú (dùng chung cho giỏ hàng, xác nhận đơn, bảng đơn)" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@attribute name="sizeName" required="false"%>
<%@attribute name="iceLevel" required="false"%>
<%@attribute name="sugarLevel" required="false"%>
<%@attribute name="toppings" required="false"%>
<%@attribute name="note" required="false"%>
<%-- Tag chạy trong ngữ cảnh riêng nên phải nạp lại ngôn ngữ (Locale 3 tầng: Session -> Cookie -> vi) --%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<c:if test="${not empty sizeName or not empty iceLevel or not empty sugarLevel or not empty toppings or not empty note}">
    <span class="item-options">
        <c:if test="${not empty sizeName}">
            <span class="item-opt"><fmt:message key="option.size_prefix" /> <c:out value="${sizeName}" /></span>
        </c:if>
        <c:if test="${not empty iceLevel}">
            <span class="item-opt"><fmt:message key="option.ice.${iceLevel}" /></span>
        </c:if>
        <c:if test="${not empty sugarLevel}">
            <span class="item-opt"><fmt:message key="option.sugar.${sugarLevel}" /></span>
        </c:if>
        <c:if test="${not empty toppings}">
            <span class="item-opt">+ <c:out value="${toppings}" /></span>
        </c:if>
        <c:if test="${not empty note}">
            <span class="item-opt item-note"><fmt:message key="option.note_prefix" />: <c:out value="${note}" /></span>
        </c:if>
    </span>
</c:if>
