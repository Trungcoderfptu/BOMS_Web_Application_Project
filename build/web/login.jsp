<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="resources.messages" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="login.title" /></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
    </head>
    <body class="main-content">
        <h2><fmt:message key="login.heading" /></h2>

        <p class="msg-error">${ERROR_MESSAGE}</p>

        <form action="LoginController" method="POST">

            <label><fmt:message key="login.label.username" /></label><br>
            <input type="text" name="txtUsername" required /><br><br>

            <label><fmt:message key="login.label.password" /></label><br>
            <input type="password" name="txtPassword" required /><br><br>

            <label><fmt:message key="login.label.role" /></label><br>
            <select name="ddlRole">
                <option value="Admin">Admin</option>
                <option value="Manager">Manager</option>
                <option value="Staff">Staff</option>
                <option value="Shipper">Shipper</option>
            </select><br><br>

            <input type="submit" value="<fmt:message key='login.btn.submit' />" />

        </form>

        <br>
        <a href="register.jsp"><fmt:message key="login.link.register" /></a>

    </body>
</html>