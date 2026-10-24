<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
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
            <c:remove var="ERROR_MESSAGE" scope="session" />
            <c:remove var="SUCCESS_MESSAGE" scope="session" />

            <div class="tab-nav d-flex">
                <button class="tab-btn ${empty param.tab || param.tab == 'tab-users' ? 'active' : ''}" data-tab="tab-users"><fmt:message key="admin.dashboard.tab.users" /></button>
                <button class="tab-btn ${param.tab == 'tab-keys' ? 'active' : ''}" data-tab="tab-keys"><fmt:message key="admin.dashboard.tab.keys" /></button>
                <button class="tab-btn ${param.tab == 'tab-passwords' ? 'active' : ''}" data-tab="tab-passwords"><fmt:message key="admin.dashboard.tab.passwords" /></button>
            </div>

            <div id="tab-users" class="tab-content ${empty param.tab || param.tab == 'tab-users' ? 'active' : 'd-none'}">
                <h3><fmt:message key="admin.dashboard.tab1.title" /></h3>
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th><fmt:message key="admin.dashboard.table.id" /></th>
                            <th><fmt:message key="admin.dashboard.table.username" /></th>
                            <th><fmt:message key="admin.dashboard.table.fullname" /></th>
                            <th><fmt:message key="admin.dashboard.table.role" /></th>
                            <th><fmt:message key="admin.dashboard.table.hiredate" /></th>
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
                                        <td><c:out value="${user.username}" /></td>
                                        <td><c:out value="${user.fullName}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${user.username == sessionScope.USER_SESSION.username || user.role == 'Admin'}">
                                                    <span class="role-badge ${user.role == 'Admin' ? 'role-admin' : (user.role == 'Manager' ? 'role-manager' : (user.role == 'Shipper' ? 'role-shipper' : 'role-staff'))}">
                                                        ${user.role}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/AdminDashboardController" method="POST" class="d-flex gap-5 flex-align-center m-0">
                                                        <input type="hidden" name="action" value="changeRole" />
                                                        <input type="hidden" name="targetUserId" value="${user.userID}" />
                                                        <select name="newRole" class="select-role-inline">
                                                            <option value="Manager" ${user.role == 'Manager' ? 'selected' : ''}>Manager</option>
                                                            <option value="Staff" ${user.role == 'Staff' ? 'selected' : ''}>Staff</option>
                                                            <option value="Shipper" ${user.role == 'Shipper' ? 'selected' : ''}>Shipper</option>
                                                        </select>
                                                        <button type="submit" class="btn-login btn-sm" title="<fmt:message key='admin.dashboard.btn.save_role' />">
                                                            <fmt:message key="admin.dashboard.btn.save_role" />
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty user.hireDate}">
                                                    <fmt:formatDate value="${user.hireDate}" pattern="dd/MM/yyyy" />
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="${user.active ? 'msg-success' : 'msg-error'}">
                                                <fmt:message key="${user.active ? 'admin.dashboard.status.active' : 'admin.dashboard.status.locked'}" />
                                            </span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${user.username == sessionScope.USER_SESSION.username}">
                                                    <span class="msg-error"><fmt:message key="admin.dashboard.status.current_user" /></span>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/AdminDashboardController" method="POST" class="m-0">
                                                        <input type="hidden" name="action" value="toggleUser" />
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
                                    <td colspan="7" class="text-center"><fmt:message key="admin.dashboard.empty.users" /></td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <div id="tab-keys" class="tab-content ${param.tab == 'tab-keys' ? 'active' : 'd-none'}">
                <div class="flex-between">
                    <h3><fmt:message key="admin.dashboard.tab2.title" /></h3>

                    <form action="${pageContext.request.contextPath}/AdminDashboardController" method="POST" class="d-flex gap-10 flex-align-center m-0">
                        <input type="hidden" name="action" value="generateKey" />
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
                            <th><fmt:message key="admin.dashboard.table.action" /></th>
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
                                            <c:choose>
                                                <c:when test="${!key.active}">
                                                    <span class="msg-error">
                                                        <fmt:message key="admin.dashboard.status.locked_key" />
                                                    </span>
                                                </c:when>
                                                <c:when test="${key.userId != null}">
                                                    <span class="msg-warning">
                                                        <fmt:message key="admin.dashboard.status.used_key" />
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="msg-success">
                                                        <fmt:message key="admin.dashboard.status.unused_key" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><fmt:formatDate value="${key.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${key.active and empty key.userId}">
                                                    <form action="${pageContext.request.contextPath}/AdminDashboardController" method="POST" class="m-0">
                                                        <input type="hidden" name="action" value="revokeKey" />
                                                        <input type="hidden" name="keyId" value="${key.keyId}" />
                                                        <button type="submit" class="btn-reject btn-sm" data-confirm="<fmt:message key="admin.dashboard.confirm.revoke_key" />">
                                                            <fmt:message key="admin.dashboard.btn.revoke_key" />
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    -
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center"><fmt:message key="admin.dashboard.empty.keys" /></td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <div id="tab-passwords" class="tab-content ${param.tab == 'tab-passwords' ? 'active' : 'd-none'}">
                <h3><fmt:message key="admin.dashboard.tab3.title" /></h3>
                <p class="text-muted"><fmt:message key="admin.dashboard.tab3.desc" /></p>
                <div class="admin-password-box mt-15">
                    <form action="${pageContext.request.contextPath}/AdminDashboardController" method="POST" class="m-0">
                        <input type="hidden" name="action" value="resetPassword" />

                        <div class="form-group mb-15">
                            <label for="targetUserId" class="d-block font-weight-bold mb-5">
                                <fmt:message key="admin.dashboard.label.select_user" />:
                            </label>
                            <select name="targetUserId" id="targetUserId" class="register-select w-100 mt-0" required>
                                <option value=""><fmt:message key="admin.dashboard.select.choose_user" /></option>
                                <c:forEach var="user" items="${USER_LIST}">
                                    <c:if test="${user.username != sessionScope.USER_SESSION.username}">
                                        <option value="${user.userID}">
                                            <c:out value="${user.fullName}" /> (@<c:out value="${user.username}" />) - [${user.role}]
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group mb-15">
                            <label for="newPassword" class="d-block font-weight-bold mb-5">
                                <fmt:message key="admin.dashboard.label.new_password" />:
                            </label>
                            <input type="password" name="newPassword" id="newPassword" class="register-input w-100 mt-0" required minlength="3" placeholder="<fmt:message key='admin.dashboard.placeholder.new_password' />" />
                        </div>

                        <div class="form-group mb-20">
                            <label for="confirmPassword" class="d-block font-weight-bold mb-5">
                                <fmt:message key="admin.dashboard.label.confirm_password" />:
                            </label>
                            <input type="password" name="confirmPassword" id="confirmPassword" class="register-input w-100 mt-0" required minlength="3" placeholder="<fmt:message key='admin.dashboard.placeholder.confirm_password' />" />
                        </div>

                        <input type="submit" class="btn-register-action mt-0" value="<fmt:message key='admin.dashboard.btn.reset_password' />" />
                    </form>
                </div>
            </div>
        </div>
        <script src="${pageContext.request.contextPath}/js/main.js"></script>
    </body>
</html>
