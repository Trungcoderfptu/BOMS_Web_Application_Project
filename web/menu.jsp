<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- MenuController forward về đây. Nhân viên đăng nhập -> màn POS quầy (bố cục C, có khung app);
     khách quét QR / chưa đăng nhập -> màn khách (không có khung app). --%>
<c:choose>
    <c:when test="${not empty sessionScope.USER_SESSION}">
        <jsp:include page="/components/pos_counter.jsp" />
    </c:when>
    <c:otherwise>
        <jsp:include page="/components/menu_customer.jsp" />
    </c:otherwise>
</c:choose>
