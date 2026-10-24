<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content">
    <h2><fmt:message key="login.heading" /></h2>

    <c:if test="${not empty SUCCESS_MESSAGE}">
        <p class="msg-success"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
        <c:remove var="SUCCESS_MESSAGE" scope="session" />
    </c:if>

    <c:choose>
        <c:when test="${ERROR_MESSAGE == 'E0016'}">
            <div id="bannedPopup" class="popup-overlay">
                <div class="popup-content">
                    <h3 class="popup-title"><fmt:message key="login.banned.title" /></h3>
                    <p><fmt:message key="login.banned.message" /></p>
                    <button type="button" id="btnCloseBannedPopup" class="btn-register-action btn-banned-close"><fmt:message key="header.sidebar.close" /></button>
                </div>
            </div>
            <c:remove var="ERROR_MESSAGE" scope="session" />
        </c:when>
        <c:when test="${not empty ERROR_MESSAGE}">
            <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
            <c:remove var="ERROR_MESSAGE" scope="session" />
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
</main>

<jsp:include page="footer.jsp" />
