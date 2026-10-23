<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<jsp:include page="header.jsp" />
<main class="main-content">
    <c:if test="${not empty SUCCESS_MESSAGE}">
        <div class="alert alert-success">
            <fmt:message key="${SUCCESS_MESSAGE}" />
        </div>
        <c:remove var="SUCCESS_MESSAGE" scope="session" />
    </c:if>
    <c:if test="${not empty ERROR_MESSAGE}">
        <div class="alert alert-danger">
            <fmt:message key="${ERROR_MESSAGE}" />
        </div>
        <c:remove var="ERROR_MESSAGE" scope="session" />
    </c:if>

    <div class="d-flex gap-30">
        <div class="profile-avatar-col">
            <h3><fmt:message key="profile.avatar.title" /></h3>
            <c:choose>
                <c:when test="${not empty USER_PROFILE.avatar}">
                    <div class="profile-avatar-wrapper">
                        <img src="${pageContext.request.contextPath}/${USER_PROFILE.avatar}" alt="Avatar" class="profile-avatar-img" />
                    </div>
                    <form action="${pageContext.request.contextPath}/ProfileController" method="POST" class="mt-10" onsubmit="return confirm('<fmt:message key="profile.avatar.confirm_delete" />');">
                        <input type="hidden" name="action" value="deleteAvatar" />
                        <button type="submit" class="btn btn-secondary btn-delete-avatar"><fmt:message key="profile.avatar.delete" /></button>
                    </form>
                </c:when>
                <c:otherwise>
                    <div class="profile-avatar-circle">
                        ${USER_PROFILE.username.substring(0,2).toUpperCase()}
                    </div>
                </c:otherwise>
            </c:choose>
            <br>
            <form action="${pageContext.request.contextPath}/ProfileController" method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="uploadAvatar" />
                <input type="file" name="fileAvatar" accept="image/png, image/jpeg, image/webp" class="input-full-width" required>
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
    </div>
</main>

<jsp:include page="footer.jsp" />