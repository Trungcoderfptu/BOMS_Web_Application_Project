<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<div id="staffCancelModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0 msg-error"><fmt:message key="order.cancel.modal.title" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeStaffCancelModal()">&times;</button>
        </div>
        <div class="modal-body">
            <p class="mb-15 font-bold">
                <fmt:message key="order.cancel.modal.warning" />
            </p>
            <p class="modal-table-info">
                <strong><fmt:message key="order.management.table.id" />:</strong> <span id="staffCancelModalOrderIdText" class="badge-table"></span>
            </p>
        </div>
        <div class="modal-actions">
            <button type="button" class="btn-modal-back" onclick="closeStaffCancelModal()">
                <fmt:message key="order.cancel.modal.btn_back" />
            </button>
            <form id="staffCancelConfirmForm" action="${pageContext.request.contextPath}/OrderManagementController" method="POST" class="m-0">
                <input type="hidden" name="action" value="updateStatus" />
                <input type="hidden" id="staffCancelModalOrderId" name="orderId" value="" />
                <input type="hidden" name="newStatus" value="Cancelled" />
                <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                <input type="hidden" name="filterStatus" value="${CURRENT_FILTER_STATUS}" />
                <button type="submit" class="btn-order-cancel">
                    <fmt:message key="order.cancel.modal.btn_confirm" />
                </button>
            </form>
        </div>
    </div>
</div>
