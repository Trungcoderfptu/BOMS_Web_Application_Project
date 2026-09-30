<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />

<div class="lang-switcher-wrapper">
    <button id="btnLangToggle" class="lang-btn">
        ${currentLang == 'en' ? '🇬🇧 EN' : '🇻🇳 VN'}
    </button>
    <ul id="langDropdown" class="lang-dropdown-menu d-none">
        <li><a href="${pageContext.request.contextPath}/LanguageController?lang=vi">🇻🇳 Tiếng Việt</a></li>
        <li><a href="${pageContext.request.contextPath}/LanguageController?lang=en">EN English</a></li>
    </ul>
</div>