<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />

<footer class="app-footer">
    <%-- Ghi công bộ icon theo giấy phép miễn phí của Flaticon --%>
    <p><fmt:message key="footer.copyright" /> &middot; <fmt:message key="footer.icons.credit" /> <a href="https://www.flaticon.com/uicons" target="_blank" rel="noopener">Flaticon</a></p>
</footer>

<script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
