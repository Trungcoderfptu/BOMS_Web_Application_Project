<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
<fmt:setLocale value="${currentLang}" />
<fmt:setBundle basename="resources.messages" />

<div id="shipperFailModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0 text-danger"><fmt:message key="shipper.modal.fail.title" /></h3>
            <button type="button" class="btn-modal-close btn-close-fail-modal">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/ShipperController" method="POST" class="m-0">
            <input type="hidden" name="action" value="failDelivery" />
            <input type="hidden" id="failModalTaskId" name="txtTaskId" value="" />
            
            <div class="modal-body">
                <p class="mb-15">
                    <strong><fmt:message key="shipper.table.ordercode" />:</strong> <span id="failModalOrderCodeText" class="badge-table"></span>
                </p>
                <div class="form-group mb-15">
                    <label for="failReason" class="font-bold d-block mb-5">
                        <fmt:message key="shipper.modal.fail.reason_label" /> <span class="text-danger">*</span>
                    </label>
                    <select id="presetFailReason" class="form-control mb-10">
                        <option value=""><fmt:message key="shipper.modal.fail.select_preset" /></option>
                        <option value="<fmt:message key="shipper.modal.fail.reason_1" />"><fmt:message key="shipper.modal.fail.reason_1" /></option>
                        <option value="<fmt:message key="shipper.modal.fail.reason_2" />"><fmt:message key="shipper.modal.fail.reason_2" /></option>
                        <option value="<fmt:message key="shipper.modal.fail.reason_3" />"><fmt:message key="shipper.modal.fail.reason_3" /></option>
                    </select>
                    <textarea id="failReason" name="failReason" rows="3" class="form-control" required placeholder="<fmt:message key="shipper.modal.fail.placeholder" />"></textarea>
                </div>
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn-modal-back btn-close-fail-modal">
                    <fmt:message key="btn.back" />
                </button>
                <button type="submit" class="btn btn-reject">
                    <fmt:message key="shipper.modal.fail.btn_confirm" />
                </button>
            </div>
        </form>
    </div>
</div>
