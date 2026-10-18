<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <div class="unauthorized-message">
        <h2><fmt:message key="security.warning" /></h2>
        
        <p>
            <c:choose>
                <c:when test="${not empty ERROR_MESSAGE}">
                    <fmt:message key="${ERROR_MESSAGE}" />
                </c:when>
                <c:otherwise>
                    <fmt:message key="error.unauthorized" />
                </c:otherwise>
            </c:choose>
        </p>
        
        <button onclick="history.back()" class="btn-back">
            <fmt:message key="btn.back" />
        </button>
    </div>
</main>

<jsp:include page="footer.jsp" />