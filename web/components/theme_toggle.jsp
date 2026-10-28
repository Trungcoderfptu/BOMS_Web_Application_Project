<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setLocale value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setBundle basename="resources.messages" />
<button id="btnDarkModeGlobal" type="button" title="<fmt:message key='theme.toggle.title' />" aria-label="<fmt:message key='theme.toggle.title' />"><i class="fi fi-rr-moon" aria-hidden="true"></i></button>
