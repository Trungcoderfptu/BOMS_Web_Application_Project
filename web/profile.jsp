<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />
<main class="main-content d-flex gap-30">
    <div class="profile-avatar-col">
        <h3><fmt:message key="profile.avatar.title" /></h3>
        <div class="profile-avatar-circle">
            ${USER_PROFILE.username.substring(0,2).toUpperCase()}
        </div>
        <br>
        <form action="UploadAvatarController" method="POST" enctype="multipart/form-data">
            <input type="file" name="fileAvatar" accept="image/png, image/jpeg" class="input-full-width" required>
            <input type="submit" value="<fmt:message key='profile.avatar.btn' />">
        </form>
    </div>

    <div class="flex-grow-1 profile-detail-col">
        <h2><fmt:message key="profile.info.title" /></h2>
        <table class="profile-table">
            <tr>
                <td><strong><fmt:message key="profile.label.username" /></strong></td>
                <td>${USER_PROFILE.username}</td>
            </tr>
            <tr>
                <td><strong><fmt:message key="profile.label.fullname" /></strong></td>
                <td>${USER_PROFILE.fullName}</td>
            </tr>
            <tr>
                <td><strong><fmt:message key="profile.label.role" /></strong></td>
                <td>${USER_PROFILE.role}</td>
            </tr>
            <tr>
                <td><strong><fmt:message key="profile.label.email" /></strong></td>
                <td>${USER_PROFILE.email}</td>
            </tr>
            <tr>
                <td><strong><fmt:message key="profile.label.phone" /></strong></td>
                <td>${USER_PROFILE.phone}</td>
            </tr>
            <tr>
                <td><strong><fmt:message key="profile.label.hiredate" /></strong></td>
                <td>${USER_PROFILE.hireDate}</td>
            </tr>
            <tr>
                <td><strong><fmt:message key="profile.label.address" /></strong></td>
                <td>${USER_PROFILE.address}</td>
            </tr>
        </table>
    </div>

</main>

<jsp:include page="footer.jsp" />