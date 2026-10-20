<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setBundle basename="resources.messages" />

<div id="shipperCompleteModal" class="modal-overlay d-none">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0 text-success"><fmt:message key="shipper.modal.complete.title" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeShipperCompleteModal()">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/ShipperController" method="POST" enctype="multipart/form-data" class="m-0">
            <input type="hidden" name="action" value="completeDelivery" />
            <input type="hidden" id="completeModalTaskId" name="txtTaskId" value="" />
            
            <div class="modal-body">
                <p class="mb-15">
                    <strong><fmt:message key="shipper.table.ordercode" />:</strong> <span id="completeModalOrderCodeText" class="badge-table"></span>
                </p>
                <div class="form-group mb-15">
                    <label for="completeProofImage" class="font-bold d-block mb-5">
                        <fmt:message key="shipper.modal.complete.proof_label" />
                    </label>
                    <input type="file" id="completeProofImage" name="proofImage" accept="image/jpeg,image/png,image/webp" class="form-control" />
                    <small class="text-muted d-block mt-5"><fmt:message key="shipper.modal.complete.proof_hint" /></small>
                </div>
            </div>
            
            <div class="modal-actions">
                <button type="button" class="btn-modal-back" onclick="closeShipperCompleteModal()">
                    <fmt:message key="btn.back" />
                </button>
                <button type="submit" class="btn btn-accept">
                    <fmt:message key="shipper.modal.complete.btn_confirm" />
                </button>
            </div>
        </form>
    </div>
</div>
