<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <h1><fmt:message key="dashboard.welcome" /></h1>
    <p><fmt:message key="dashboard.desc" /></p>
</main>

<jsp:include page="footer.jsp" />