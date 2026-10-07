<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Admin Dashboard - BOMS</title>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/global.css">
    </head>
    <body class="main-content">
        <jsp:include page="header.jsp" />
        <div class="dashboard-wrapper">
            <h2><fmt:message key="admin.dashbard.titile" /></h2>
            <c:if test="${not empty ERROR_MESSAGE}">
                <p class="msg-error"><fmt:message key="${ERROR_MESSAGE}" /></p>
            </c:if>
            <c:if test="${not empty SUCCESS_MESSAGE}">
                <p class="msg-success"><fmt:message key="${SUCCESS_MESSAGE}" /></p>
            </c:if>
            <div class="tab-nav d-flex">
                <button class="tab-btn ${param.tab != 'tab-keys' ? 'active' : ''}" onclick="openTab(event, 'tab-users')"><fmt:message key="admin.dashboard.tab.users" /></button>
                <button class="tab-btn ${param.tab == 'tab-keys' ? 'active' : ''}" onclick="openTab(event, 'tab-keys')"><fmt:message key="admin.dashboard.tab.keys" /></button>
            </div>
            <div id="tab-users" class="tab-content ${param.tab != 'tab-keys' ? 'active' : 'd-none'}">
                <h3><fmt:message key="admin.dashboard.tab1.title" /></h3>
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th><fmt:message key="admin.dashboard.table.id" /></th>
                            <th><fmt:message key="admin.dashboard.table.username" /></th>
                            <th><fmt:message key="admin.dashboard.table.fullname" /></th>
                            <th><fmt:message key="admin.dashboard.table.role" /></th>
                            <th><fmt:message key="admin.dashboard.table.status" /></th>
                            <th><fmt:message key="admin.dashboard.table.action" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty USER_LIST}">
                                <c:forEach var="user" items="${USER_LIST}">
                                    <tr>
                                        <td>${user.userID}</td>
                                        <td>${user.username}</td>
                                        <td>${user.fullName}</td>
                                        <td>${user.role}</td>
                                        <td>
                                            <span class="${user.active ? 'msg-success' : 'msg-error'}">
                                                <fmt:message key="${user.active ? 'admin.dashboard.status.active' : 'admin.dashboard.status.locked'}" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${user.username == USER_SESSION.username}">
                                                    <span class="msg-error"><fmt:message key="admin.dashboard.status.current_user" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/ToggleUserController" method="POST" class="m-0">
                                                        <input type="hidden" name="userId" value="${user.userID}" />
                                                        <button type="submit" class="btn-login btn-sm">
                                                            <fmt:message key="admin.dashboard.btn.toggle" />
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center"><fmt:message key="admin.dashboard.empty.users" /></td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
            <div id="tab-keys" class="tab-content ${param.tab == 'tab-keys' ? 'active' : 'd-none'}">
                <div class="flex-between">
                    <h3><fmt:message key="admin.dashboard.tab2.title" /></h3>

                    <form action="${pageContext.request.contextPath}/GenerateKeyController" method="POST" class="d-flex gap-10 flex-align-center m-0">
                        <label for="ddlRole"><fmt:message key="admin.dashboard.label.grant_role" /></label>
                        <select name="ddlRole" id="ddlRole" class="register-select w-auto mt-0">
                            <option value="Manager">Manager</option>
                            <option value="Staff">Staff</option>
                            <option value="Shipper">Shipper</option>
                        </select>
                        <input type="submit" class="btn-register-action mt-0" value="<fmt:message key='admin.dashboard.btn.create_key' />" />
                    </form>
                </div>

                <table class="admin-table mt-15">
                    <thead>
                        <tr>
                            <th><fmt:message key="admin.dashboard.table.key" /></th>
                            <th><fmt:message key="admin.dashboard.table.grant_role" /></th>
                            <th><fmt:message key="admin.dashboard.table.user_id" /></th>
                            <th><fmt:message key="admin.dashboard.table.status" /></th>
                            <th><fmt:message key="admin.dashboard.table.created_date" /></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty KEY_LIST}">
                                <c:forEach var="key" items="${KEY_LIST}">
                                    <tr>
                                        <td><strong>${key.securityKey}</strong></td>
                                        <td>${key.role}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${key.userId != null}">${key.userId}</c:when>
                                                <c:otherwise><fmt:message key="admin.dashboard.status.unused" /></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="${key.active ? 'msg-success' : 'msg-error'}">
                                                <fmt:message key="${key.active ? 'admin.dashboard.status.valid' : 'admin.dashboard.status.invalid'}" />
                                            </span>
                                        </td>
                                        <td><fmt:formatDate value="${key.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="5" class="text-center"><fmt:message key="admin.dashboard.empty.keys" /></td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
        <script src="${pageContext.request.contextPath}/js/main.js"></script>
    </body>
</html>