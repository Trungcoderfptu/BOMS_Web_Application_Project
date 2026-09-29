<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />

<main class="main-content d-flex gap-20">

    <div class="settings-sidebar">
        <h3><fmt:message key="settings.sidebar.title" /></h3>
        <ul class="no-bullet">
            <li><button id="btnMenuProfile" class="btn-menu-tab"><fmt:message key="settings.tab.general" /></button></li>
            <li><button id="btnMenuPassword" class="btn-menu-tab"><fmt:message key="settings.tab.password" /></button></li>
        </ul>
    </div>

    <div class="flex-grow-1">

        <p class="msg-error">${ERROR_MESSAGE}</p>
        <p class="msg-success">${SUCCESS_MESSAGE}</p>

        <div id="tabProfile" class="tabContent" style="display: ${empty ACTIVE_TAB || ACTIVE_TAB == 'updateProfile' ? 'block' : 'none'};">
            <h2><fmt:message key="settings.profile.heading" /></h2>
            <form action="SettingsController" method="POST">
                <input type="hidden" name="action" value="updateProfile" />

                <label><fmt:message key="settings.label.email" /></label><br>
                <input type="text" name="txtEmail" value="${USER_PROFILE.email}" /><br><br>

                <label><fmt:message key="settings.label.phone" /></label><br>
                <input type="text" name="txtPhone" value="${USER_PROFILE.phone}" /><br><br>

                <label><fmt:message key="settings.label.address" /></label><br>
                <input type="text" name="txtAddress" value="${USER_PROFILE.address}" /><br><br>

                <input type="submit" value="<fmt:message key='settings.btn.save' />" />
            </form>
        </div>

        <div id="tabPassword" class="tabContent" style="display: ${ACTIVE_TAB == 'changePassword' ? 'block' : 'none'};">
            <h2><fmt:message key="settings.password.heading" /></h2>
            <form action="SettingsController" method="POST">
                <input type="hidden" name="action" value="changePassword" />

                <label><fmt:message key="settings.label.oldpass" /></label><br>
                <input type="password" name="txtOldPassword" required /><br><br>

                <label><fmt:message key="settings.label.newpass" /></label><br>
                <input type="password" name="txtNewPassword" required /><br><br>

                <label><fmt:message key="settings.label.confirmpass" /></label><br>
                <input type="password" name="txtConfirmPassword" required /><br><br>

                <input type="submit" value="<fmt:message key='settings.btn.confirm' />" />
            </form>
        </div>

    </div>
</main>

<jsp:include page="footer.jsp" />