<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<div id="revertDeliveryModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0 msg-error">⚠️ <fmt:message key="order.revert.modal.title" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeRevertDeliveryModal()">&times;</button>
        </div>
        <div class="modal-body">
            <p class="mb-15 font-bold text-danger">
                <fmt:message key="order.revert.modal.warning" />
            </p>
            <p class="modal-table-info">
                <strong><fmt:message key="order.management.table.id" />:</strong> <span id="revertDeliveryModalOrderIdText" class="badge-table"></span>
            </p>
        </div>
        <div class="modal-actions">
            <button type="button" class="btn-modal-back" onclick="closeRevertDeliveryModal()">
                <fmt:message key="order.revert.modal.btn.cancel" />
            </button>
            <button type="button" class="btn-order-cancel" onclick="confirmRevertDeliverySubmit()">
                <fmt:message key="order.revert.modal.btn.confirm" />
            </button>
        </div>
    </div>
</div>
