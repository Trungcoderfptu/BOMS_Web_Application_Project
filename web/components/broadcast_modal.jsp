<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<%-- ================= MODAL PHÁT LOA TOÀN CỤC (Task 11.4) ================= --%>
<div id="broadcastModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="modal-title font-md font-bold m-0">
                <fmt:message key="broadcast.modal.title" />
            </h3>
            <button type="button" class="btn-close-modal" data-close-modal="broadcastModal">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/NotificationController" method="POST">
            <input type="hidden" name="action" value="broadcast" />

            <div class="modal-body p-20">
                <p class="font-xs text-muted mb-15">
                    <fmt:message key="broadcast.modal.desc" />
                </p>
                <div class="form-group mb-15">
                    <label for="broadcastMessageInput" class="form-label font-bold mb-5 d-block">
                        <fmt:message key="broadcast.label.message" /> <span class="text-danger">*</span>
                    </label>
                    <textarea id="broadcastMessageInput" name="broadcastMessage" class="form-input form-textarea w-100" rows="4" maxlength="500" required placeholder="<fmt:message key="broadcast.placeholder.message" />"></textarea>
                    <small class="text-muted font-xs d-block text-right mt-5">500 <fmt:message key="broadcast.label.max_chars" /></small>
                </div>
            </div>

            <div class="modal-footer flex-end gap-10">
                <button type="button" class="btn-secondary" data-close-modal="broadcastModal">
                    <fmt:message key="broadcast.btn.cancel" />
                </button>
                <button type="submit" class="btn-primary">
                    <i class="fi fi-rr-paper-plane" aria-hidden="true"></i> <fmt:message key="broadcast.btn.send" />
                </button>
            </div>
        </form>
    </div>
</div>
