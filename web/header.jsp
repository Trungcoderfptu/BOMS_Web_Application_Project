<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title><fmt:message key="header.title" /></title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
    </head>
    <body>
        <header class="app-header">
            <div class="flex-between">
                <div>
                    <button id="btnHamburger">☰</button>
                </div>
                <nav>
                    <a href="#"><fmt:message key="header.nav.overview" /></a> |
                    <a href="#"><fmt:message key="header.nav.orders" /></a> |
                    <a href="#"><fmt:message key="header.nav.inventory" /></a>
                </nav>
                <div class="flex-align-center">
                    <div>
                        <input type="text" placeholder="<fmt:message key='header.search.placeholder' />">
                        <button><fmt:message key="header.search.button" /></button>
                    </div>
                    <div class="dropdown-wrapper">
                        <c:choose>
                            <c:when test="${empty sessionScope.USER_SESSION}">
                                <a href="${pageContext.request.contextPath}/login.jsp" class="btn-login">
                                    <fmt:message key="header.btn.login" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <button id="btnAvatar">
                                    ${sessionScope.USER_SESSION.username.substring(0,2).toUpperCase()}
                                </button>
                                <div id="menuTaiKhoan">
                                    <strong>${sessionScope.USER_SESSION.fullName}</strong>
                                    <hr>
                                    <a href="${pageContext.request.contextPath}/ProfileController"><fmt:message key="header.menu.profile" /></a><br><br>
                                    <a href="${pageContext.request.contextPath}/SettingsController"><fmt:message key="header.menu.settings" /></a><br><br>
                                    <a href="${pageContext.request.contextPath}/LogoutController" class="text-danger"><fmt:message key="header.menu.logout" /></a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </header>

        <aside id="menuDoc">
            <button id="btnDongMenuDoc">✖ <fmt:message key="header.sidebar.close" /></button>
            <ul class="no-bullet">
                <li><a href="#"><fmt:message key="header.sidebar.deshbard" /></a></li>
                <li><a href="#"><fmt:message key="header.sidebar.hr" /></a></li>
                <li><a href="#"><fmt:message key="header.sidebar.shipper" /></a></li>
                <li><a href="#"><fmt:message key="header.sidebar.reports" /></a></li>
                <li><a href="#"><fmt:message key="header.sidebar.order" /></a></li>
            </ul>
        </aside>