<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="register.banner" /></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
    </head>
    <body class="main-content">
        <div class="register-wrapper">
            <h2 class="register-title"><fmt:message key="register.banner" /></h2>

            <p class="msg-error">${ERROR_MESSAGE}</p>
            <p class="msg-success">${SUCCESS_MESSAGE}</p>

            <form id="frmRegister" class="register-layout" action="RegisterController" method="POST">

                <label for="txtRegUser"><fmt:message key="register.label.username" /></label><br>
                <input type="text" id="txtRegUser" name="txtUsername" value="${param.txtUsername}" required /><br><br>

                <label for="txtRegPass"><fmt:message key="register.label.password" /></label><br>
                <input type="password" id="txtRegPass" name="txtPassword" required /><br><br>

                <label for="txtRegConfirm"><fmt:message key="register.label.passwordConfilm" /></label><br>
                <input type="password" id="txtRegConfirm" name="txtConfirmPassword" required /><br><br>

                <label for="txtRegEmail"><fmt:message key="register.label.email" /></label><br>
                <input type="email" id="txtRegEmail" name="txtEmail" value="${param.txtEmail}" required /><br><br>

                <label for="txtRegPhone"><fmt:message key="register.label.phone" /></label><br>
                <input type="text" id="txtRegPhone" name="txtPhone" value="${param.txtPhone}" required /><br><br>

                <label for="txtRegAddress"><fmt:message key="register.label.address" /></label><br>
                <input type="text" id="txtRegAddress" name="txtAddress" value="${param.txtAddress}" required /><br><br>

                <label for="ddlRegRole"><fmt:message key="register.label.roleSelect" /></label><br>
                <select id="ddlRegRole" name="ddlRole" class="register-select">
                    <option value="Admin" ${param.ddlRole == 'Admin' ? 'selected' : ''}>Admin</option>
                    <option value="Manager" ${param.ddlRole == 'Manager' ? 'selected' : ''}>Manager</option>
                    <option value="Staff" ${param.ddlRole == 'Staff' ? 'selected' : ''}>Staff</option>
                    <option value="Shipper" ${param.ddlRole == 'Shipper' ? 'selected' : ''}>Shipper</option>
                </select><br><br>

                <label for="txtRegCode"><fmt:message key="register.label.securityId" /></label><br>
                <input type="text" id="txtRegCode" name="txtSecurityId" value="${param.txtSecurityId}" placeholder="<fmt:message key='register.label.securityIdInput' />" required /><br><br>

                <input type="submit" id="btnSubmitRegister" class="btn-register-action" value="<fmt:message key='register.btn.submit' />" />

            </form>

            <br>
            <a href="login.jsp" class="register-nav-link"><fmt:message key="register.link.register" /></a>
        </div>
        <script src="${pageContext.request.contextPath}/js/main.js"></script>
    </body>
</html>