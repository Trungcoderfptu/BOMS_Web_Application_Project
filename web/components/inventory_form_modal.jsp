<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<%-- MODAL 1: THÊM / CHỈNH SỬA THÔNG TIN NGUYÊN LIỆU --%>
<div id="inventoryModal" class="modal-overlay d-none"
     data-title-add="<fmt:message key="inventory.modal.title.add" />"
     data-title-edit="<fmt:message key="inventory.modal.title.edit" />"
     onclick="if(event.target === this) closeInventoryModal();">
    <div class="modal-card">
        <div class="modal-header">
            <h3 id="inventoryModalTitle" class="m-0"><fmt:message key="inventory.modal.title.add" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeInventoryModal()">✖</button>
        </div>

        <form id="inventoryForm" action="${pageContext.request.contextPath}/InventoryManagementController" method="POST" class="m-0">
            <div class="modal-body">
                <input type="hidden" id="inventoryModalAction" name="action" value="create" />
                <input type="hidden" id="inventoryModalId" name="ingredientId" value="" />

                <div class="form-group mb-15">
                    <label for="ingredientNameInput" class="form-label font-bold">
                        <fmt:message key="inventory.modal.label.name" /> <span class="text-danger">*</span>
                    </label>
                    <input type="text" id="ingredientNameInput" name="ingredientName" required
                           class="filter-input w-100" />
                </div>

                <div class="form-group mb-15">
                    <label for="ingredientCategorySelect" class="form-label font-bold">
                        <fmt:message key="inventory.modal.label.category" /> <span class="text-danger">*</span>
                    </label>
                    <select id="ingredientCategorySelect" name="categoryId" required class="filter-input w-100">
                        <c:forEach var="cat" items="${CATEGORY_LIST}">
                            <option value="${cat.categoryId}">${cat.categoryName}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="d-flex gap-10 mb-15">
                    <div class="form-group flex-grow-1">
                        <label for="ingredientUnitInput" class="form-label font-bold">
                            <fmt:message key="inventory.modal.label.unit" /> <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="ingredientUnitInput" name="unit" required
                               placeholder="kg, lít, hộp..." class="filter-input w-100" />
                    </div>
                    <div class="form-group flex-grow-1">
                        <label for="ingredientQtyInput" class="form-label font-bold">
                            <fmt:message key="inventory.modal.label.quantity" /> <span class="text-danger">*</span>
                        </label>
                        <input type="number" id="ingredientQtyInput" name="quantity" min="0" step="0.1" required
                               class="filter-input w-100" />
                    </div>
                </div>

                <div class="d-flex gap-10 mb-15">
                    <div class="form-group flex-grow-1">
                        <label for="ingredientPriceInput" class="form-label font-bold">
                            <fmt:message key="inventory.modal.label.price" /> <span class="text-danger">*</span>
                        </label>
                        <input type="number" id="ingredientPriceInput" name="price" min="0" step="1000" required
                               class="filter-input w-100" />
                    </div>
                    <div class="form-group flex-grow-1">
                        <label for="ingredientThresholdInput" class="form-label font-bold">
                            <fmt:message key="inventory.modal.label.threshold" /> <span class="text-danger">*</span>
                        </label>
                        <input type="number" id="ingredientThresholdInput" name="warningThreshold" min="0" step="0.1" required
                               class="filter-input w-100" />
                    </div>
                </div>

                <div class="form-group mb-15">
                    <label for="ingredientLocationInput" class="form-label font-bold">
                        <fmt:message key="inventory.modal.label.location" />
                    </label>
                    <input type="text" id="ingredientLocationInput" name="storageLocation"
                           placeholder="Tủ lạnh 1, Kệ A..." class="filter-input w-100" />
                </div>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-order-cancel" onclick="closeInventoryModal()">
                    <fmt:message key="inventory.modal.btn.close" />
                </button>
                <button type="submit" class="btn-login">
                    <fmt:message key="inventory.modal.btn.save" />
                </button>
            </div>
        </form>
    </div>
</div>

<%-- MODAL 2: CẬP NHẬT NHANH NGƯỠNG CẢNH BÁO TỒN KHO --%>
<div id="thresholdModal" class="modal-overlay d-none"
     onclick="if(event.target === this) closeThresholdModal();">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="m-0"><fmt:message key="inventory.modal.title.threshold" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeThresholdModal()">✖</button>
        </div>

        <form id="thresholdForm" action="${pageContext.request.contextPath}/InventoryManagementController" method="POST" class="m-0">
            <div class="modal-body">
                <input type="hidden" name="action" value="updateThreshold" />
                <input type="hidden" id="thresholdIngredientId" name="ingredientId" value="" />

                <div class="mb-15">
                    <p class="m-0 font-bold"><fmt:message key="inventory.table.name" />: <span id="thresholdIngredientName"></span></p>
                </div>

                <div class="form-group mb-15">
                    <label for="thresholdValueInput" class="form-label font-bold">
                        <fmt:message key="inventory.modal.label.threshold" /> <span class="text-danger">*</span>
                    </label>
                    <input type="number" id="thresholdValueInput" name="threshold" min="0" step="0.1" required
                           class="filter-input w-100" />
                </div>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-order-cancel" onclick="closeThresholdModal()">
                    <fmt:message key="inventory.modal.btn.close" />
                </button>
                <button type="submit" class="btn-login">
                    <fmt:message key="inventory.modal.btn.save" />
                </button>
            </div>
        </form>
    </div>
</div>

<%-- MODAL 3: QUẢN LÝ DANH MỤC KHO NGUYÊN LIỆU --%>
<div id="inventoryCategoryModal" class="modal-overlay d-none" onclick="if(event.target === this) closeInventoryCategoryModal();">
    <div class="modal-card modal-card-md">
        <div class="modal-header">
            <h3 class="m-0"><fmt:message key="inventory.category.modal.title" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeInventoryCategoryModal()">✕</button>
        </div>

        <div class="modal-body">
            <%-- Form thêm danh mục nguyên liệu mới --%>
            <form action="${pageContext.request.contextPath}/InventoryManagementController" method="POST" class="mb-20 p-15 card bg-light">
                <input type="hidden" name="action" value="createCategory" />
                <label for="newInventoryCategoryName" class="form-label font-bold mb-5 d-block">
                    <fmt:message key="inventory.category.label.name" /> <span class="text-danger">*</span>
                </label>
                <div class="d-flex gap-10">
                    <input type="text" id="newInventoryCategoryName" name="categoryName" required maxlength="100"
                           class="filter-input flex-grow-1"
                           placeholder="<fmt:message key="inventory.category.placeholder.name" />" />
                    <button type="submit" class="btn-login btn-sm">
                        + <fmt:message key="inventory.category.btn.create" />
                    </button>
                </div>
            </form>

            <%-- Bảng danh sách danh mục nguyên liệu hiện có --%>
            <h4 class="mb-10"><fmt:message key="inventory.category.table.title" /></h4>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th class="w-60">ID</th>
                        <th><fmt:message key="inventory.category.table.name" /></th>
                        <th class="w-140 text-center"><fmt:message key="inventory.category.table.status" /></th>
                        <th class="w-100 text-center"><fmt:message key="inventory.category.table.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty CATEGORY_LIST}">
                            <c:forEach var="cat" items="${CATEGORY_LIST}">
                                <tr>
                                    <td>${cat.categoryId}</td>
                                    <td><strong>${cat.categoryName}</strong></td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${cat.inUse}">
                                                <span class="role-badge role-manager">
                                                    <fmt:message key="inventory.category.status.in_use" />
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="role-badge role-staff">
                                                    <fmt:message key="inventory.category.status.empty" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${not cat.inUse}">
                                                <form action="${pageContext.request.contextPath}/InventoryManagementController" method="POST" class="m-0 d-inline">
                                                    <input type="hidden" name="action" value="deleteCategory" />
                                                    <input type="hidden" name="categoryId" value="${cat.categoryId}" />
                                                    <button type="submit" class="btn-reject btn-sm"
                                                            onclick="return confirm('<fmt:message key="inventory.category.confirm.delete" />')">
                                                        <fmt:message key="inventory.category.btn.delete" />
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">-</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="4" class="text-center"><fmt:message key="inventory.category.empty" /></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <div class="modal-actions">
            <button type="button" class="btn-order-cancel" onclick="closeInventoryCategoryModal()">
                <fmt:message key="inventory.modal.btn.close" />
            </button>
        </div>
    </div>
</div>
