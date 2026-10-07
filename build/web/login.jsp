<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="login.title" /></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
        <jsp:include page="header.jsp" />

    </head>
    <body class="main-content">

        <h2><fmt:message key="login.heading" /></h2>
        <c:choose>
            <c:when test="${ERROR_MESSAGE == 'E0016'}">
                <div id="bannedPopup" class="popup-overlay">
                    <div class="popup-content">
                        <h3 class="popup-title"><fmt:message key="login.banned.title" /></h3>
                        <p><fmt:message key="login.banned.message" /></p>
                        <button onclick="document.getElementById('bannedPopup').style.display = 'none'" class="btn-register-action" style="margin-top: 15px;"><fmt:message key="header.sidebar.close" /></button>
                    </div>
                </div>
            </c:when>
            <c:when test="${not empty ERROR_MESSAGE}">
                <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
            </c:when>
        </c:choose>
        <form action="LoginController" method="POST">

            <label><fmt:message key="login.label.username" /></label><br>
            <input type="text" name="txtUsername" value="${param.txtUsername}" required /><br><br>

            <label><fmt:message key="login.label.password" /></label><br>
            <input type="password" name="txtPassword" required /><br><br>

            <label><fmt:message key="login.label.role" /></label><br>
            <select name="ddlRole">
                <option value="Admin" ${param.ddlRole == 'Admin' ? 'selected' : ''}>Admin</option>
                <option value="Manager" ${param.ddlRole == 'Manager' ? 'selected' : ''}>Manager</option>
                <option value="Staff" ${param.ddlRole == 'Staff' ? 'selected' : ''}>Staff</option>
                <option value="Shipper" ${param.ddlRole == 'Shipper' ? 'selected' : ''}>Shipper</option>
            </select><br><br>

            <input type="submit" value="<fmt:message key='login.btn.submit' />" />

        </form>

        <br>
        <a href="register.jsp"><fmt:message key="login.link.register" /></a>
        <script src="${pageContext.request.contextPath}/js/main.js"></script>
    </body>
</html>