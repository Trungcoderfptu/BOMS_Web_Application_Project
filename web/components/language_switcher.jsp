<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />

<div class="lang-switcher-wrapper">
    <button id="btnLangToggle" type="button" class="lang-btn">
        <i class="fi fi-rr-globe" aria-hidden="true"></i> ${currentLang == 'en' ? 'EN' : (currentLang == 'ja' ? 'JA' : 'VI')}
    </button>
    <ul id="langDropdown" class="lang-dropdown-menu d-none">
        <li><a href="${pageContext.request.contextPath}/LanguageController?lang=vi">Tiếng Việt</a></li>
        <li><a href="${pageContext.request.contextPath}/LanguageController?lang=en">English</a></li>
        <li><a href="${pageContext.request.contextPath}/LanguageController?lang=ja">日本語</a></li>
    </ul>
</div>