<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<div id="inventoryAlertModal" class="modal-overlay d-none"
     data-type-low-stock="<fmt:message key='alert.type.low_stock' />"
     data-type-out-of-stock="<fmt:message key='alert.type.out_of_stock' />"
     data-type-expiring-soon="<fmt:message key='alert.type.expiring_soon' />"
     data-type-expired="<fmt:message key='alert.type.expired' />"
     data-btn-confirm="<fmt:message key='alert.btn.confirm' />"
     data-btn-confirmed="<fmt:message key='alert.btn.confirmed' />">
    <div class="modal-card modal-card-lg">
        <div class="modal-header">
            <h3 class="m-0">
                <fmt:message key="alert.modal.title" />
            </h3>
            <button type="button" class="btn-modal-close" data-close-modal="inventoryAlertModal">✖</button>
        </div>
        <div class="modal-body">
            <div id="alertEmptyState" class="empty-cart-container d-none">
                <h3 class="msg-info"><fmt:message key="alert.empty" /></h3>
            </div>
            <div id="alertTableContainer" class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th><fmt:message key="alert.table.ingredient" /></th>
                            <th><fmt:message key="alert.table.type" /></th>
                            <th><fmt:message key="alert.table.current_qty" /></th>
                            <th><fmt:message key="alert.table.threshold" /></th>
                            <th><fmt:message key="alert.table.time" /></th>
                            <th><fmt:message key="alert.table.status" /></th>
                            <th><fmt:message key="alert.table.action" /></th>
                        </tr>
                    </thead>
                    <tbody id="inventoryAlertTableBody">
                        <!-- Rendered by JavaScript dynamically -->
                    </tbody>
                </table>
            </div>
        </div>
        <div class="modal-actions">
            <button type="button" class="btn-order-cancel" data-close-modal="inventoryAlertModal">
                <fmt:message key="alert.btn.close" />
            </button>
        </div>
    </div>
</div>
