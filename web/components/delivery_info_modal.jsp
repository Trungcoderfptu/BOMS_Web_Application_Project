<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<div id="deliveryInfoModal" class="modal-overlay d-none" onclick="if(event.target === this) closeDeliveryInfoModal();">
    <div class="modal-card modal-card-md">
        <div class="modal-header">
            <h3 class="m-0 msg-info"><fmt:message key="modal.delivery.title" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeDeliveryInfoModal()">&times;</button>
        </div>
        <form id="deliveryInfoForm" action="${pageContext.request.contextPath}/OrderManagementController" method="POST" class="m-0">
            <div class="modal-body">
                <input type="hidden" name="action" value="convertToDelivery" />
                <input type="hidden" id="deliveryModalOrderId" name="orderId" value="" />
                <input type="hidden" name="filterDate" value="${CURRENT_FILTER_DATE}" />
                <input type="hidden" name="filterStatus" value="${CURRENT_FILTER_STATUS}" />
                <input type="hidden" name="activeTab" value="tab-delivery-orders" />

                <div class="mb-15">
                    <p class="modal-table-info m-0">
                        <strong><fmt:message key="order.management.table.id" />:</strong> 
                        <span id="deliveryModalOrderIdText" class="badge-table"></span>
                    </p>
                </div>

                <div class="form-group mb-15">
                    <label for="deliveryCustomerName" class="form-label font-bold">
                        <fmt:message key="modal.delivery.customer_name" /> <span class="text-danger">*</span>
                    </label>
                    <input type="text" id="deliveryCustomerName" name="customerName" required maxlength="100"
                           class="filter-input w-100" placeholder="<fmt:message key="modal.delivery.customer_name.placeholder" />" />
                </div>

                <div class="form-group mb-15">
                    <label for="deliveryCustomerPhone" class="form-label font-bold">
                        <fmt:message key="modal.delivery.customer_phone" /> <span class="text-danger">*</span>
                    </label>
                    <input type="tel" id="deliveryCustomerPhone" name="customerPhone" required pattern="^0[0-9]{9,10}$" maxlength="20"
                           class="filter-input w-100" placeholder="<fmt:message key="modal.delivery.customer_phone.placeholder" />" />
                </div>

                <div class="form-group mb-15">
                    <label for="deliveryAddress" class="form-label font-bold">
                        <fmt:message key="modal.delivery.address" /> <span class="text-danger">*</span>
                    </label>
                    <input type="text" id="deliveryAddress" name="deliveryAddress" required maxlength="255"
                           class="filter-input w-100" placeholder="<fmt:message key="modal.delivery.address.placeholder" />" />
                </div>

                <div class="form-group mb-15">
                    <label for="deliveryAreaId" class="form-label font-bold">
                        <fmt:message key="modal.delivery.area" /> <span class="text-danger">*</span>
                    </label>
                    <select id="deliveryAreaId" name="areaId" required class="filter-input w-100">
                        <option value=""><fmt:message key="modal.delivery.area.select" /></option>
                        <c:forEach var="area" items="${DELIVERY_AREAS}">
                            <option value="${area.areaId}">${area.areaName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-modal-back" onclick="closeDeliveryInfoModal()">
                    <fmt:message key="modal.delivery.btn.cancel" />
                </button>
                <button type="submit" class="btn-login">
                    <fmt:message key="modal.delivery.btn.submit" />
                </button>
            </div>
        </form>
    </div>
</div>
