<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<fmt:setLocale value="${not empty cookie.LANG.value ? cookie.LANG.value : 'vi'}" />
<fmt:setBundle basename="resources.messages" />

<div id="productModal" class="modal-overlay d-none"
     data-title-add="<fmt:message key="menu.modal.title.add" />"
     data-title-edit="<fmt:message key="menu.modal.title.edit" />"
     data-error-size="<fmt:message key="E1013" />"
     data-error-recipe="<fmt:message key="E0212" />"
>
    <div class="modal-card modal-card-md">
        <div class="modal-header">
            <h3 id="productModalTitle" class="m-0"><fmt:message key="menu.modal.title.add" /></h3>
            <button type="button" class="btn-modal-close" data-close-modal="productModal">✖</button>
        </div>

        <form id="productForm" action="${pageContext.request.contextPath}/MenuManagementController" method="POST" enctype="multipart/form-data" class="m-0">
            <div class="modal-body">
                <input type="hidden" id="productModalAction" name="action" value="createProduct" />
                <input type="hidden" id="productModalId" name="productId" value="" />
                <input type="hidden" name="filterCategory" value="${CURRENT_FILTER_CATEGORY}" />

                <div class="form-group mb-15">
                    <label for="productNameInput" class="form-label font-bold">
                        <fmt:message key="menu.modal.label.name" /> <span class="text-danger">*</span>
                    </label>
                    <input type="text" id="productNameInput" name="productName" required
                           class="filter-input w-100"
                           placeholder="<fmt:message key="menu.modal.placeholder.name" />" />
                </div>

                <div class="form-group mb-15">
                    <label for="productCategorySelect" class="form-label font-bold">
                        <fmt:message key="menu.modal.label.category" /> <span class="text-danger">*</span>
                    </label>
                    <select id="productCategorySelect" name="categoryId" required class="filter-input w-100">
                        <option value=""><fmt:message key="menu.mgmt.filter.category" /></option>
                        <c:forEach var="cat" items="${CATEGORY_LIST}">
                            <option value="${cat.categoryId}"><c:out value="${cat.categoryName}" /></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group mb-15">
                    <label for="productPriceInput" class="form-label font-bold">
                        <fmt:message key="menu.modal.label.price" /> <span class="text-danger">*</span>
                    </label>
                    <input type="number" id="productPriceInput" name="price" min="1000" step="1000" required
                           class="filter-input w-100"
                           placeholder="<fmt:message key="menu.modal.placeholder.price" />" />
                </div>

                <div class="form-group mb-15">
                    <label class="form-label font-bold mb-5"><fmt:message key="menu.mgmt.table.image" /></label>
                    <div class="image-tab-bar mb-10">
                        <button type="button" id="tabBtnUpload" class="btn-image-tab active" data-action="switch-image-tab" data-image-tab="upload">
                            📁 <fmt:message key="menu.modal.tab.upload_file" />
                        </button>
                        <button type="button" id="tabBtnUrl" class="btn-image-tab" data-action="switch-image-tab" data-image-tab="url">
                            🔗 <fmt:message key="menu.modal.tab.url_link" />
                        </button>
                    </div>

                    <!-- Tab 1: Upload File -->
                    <div id="imageUploadPanel" class="image-panel">
                        <input type="file" id="productImageFileInput" name="productImageFile"
                               accept=".jpg,.jpeg,.png,.webp" class="filter-input w-100"
                               data-action="preview-uploaded-image" />
                        <div class="form-hint-text mt-5"><fmt:message key="menu.modal.upload_hint" /></div>
                    </div>

                    <!-- Tab 2: URL Link -->
                    <div id="imageUrlPanel" class="image-panel d-none">
                        <input type="text" id="productImageInput" name="imageUrl"
                               maxlength="255"
                               class="filter-input w-100"
                               placeholder="<fmt:message key="menu.modal.placeholder.image" />"
                               data-action="preview-url-image" />
                        <div class="form-hint-text mt-5"><fmt:message key="menu.modal.url_hint" /></div>
                    </div>

                    <!-- Khung xem truoc anh -->
                    <div id="imagePreviewBox" class="image-preview-container d-none mt-10">
                        <div class="preview-title"><fmt:message key="menu.modal.preview.title" /></div>
                        <img id="imagePreviewImg" src="" alt="Preview" class="preview-thumb" />
                    </div>
                </div>

                <!-- Recipe Formulation Section -->
                <div class="form-group mb-15">
                    <div class="flex-between flex-align-center mb-8">
                        <label class="form-label font-bold m-0">
                            <fmt:message key="menu.modal.label.recipe" />
                        </label>
                        <button type="button" class="btn-action-add-recipe" data-action="add-recipe-row">
                            <fmt:message key="menu.modal.btn.add_ingredient" />
                        </button>
                    </div>

                    <!-- Hidden template select for ingredients -->
                    <select id="ingredientMasterTemplate" class="d-none">
                        <option value="" data-unit="">-- <fmt:message key="menu.modal.recipe.select_placeholder" /> --</option>
                        <c:forEach var="ing" items="${INGREDIENT_LIST}">
                            <option value="${ing.ingredientId}" data-unit="${fn:escapeXml(ing.unit)}">${fn:escapeXml(ing.ingredientName)} (${fn:escapeXml(ing.unit)})</option>
                        </c:forEach>
                    </select>

                    <div id="recipeContainer" class="recipe-container">
                        <div id="recipeEmptyHint" class="recipe-empty-hint">
                            <fmt:message key="menu.modal.recipe.empty_hint" />
                        </div>
                        <div id="recipeList" class="recipe-list"></div>
                    </div>
                </div>

                <div id="productStatusGroup" class="form-group mb-15 d-none">
                    <label class="form-label font-bold"><fmt:message key="menu.modal.label.status" /></label>
                    <div class="flex-align-center gap-10 mt-5">
                        <input type="checkbox" id="productAvailableCheckbox" name="isAvailable" value="true" checked />
                        <label for="productAvailableCheckbox" id="productAvailableLabel">
                            <fmt:message key="menu.mgmt.status.active" />
                        </label>
                    </div>
                </div>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-order-cancel" data-close-modal="productModal">
                    <fmt:message key="menu.modal.btn.cancel" />
                </button>
                <button type="submit" id="productModalSubmitBtn" class="btn-login">
                    <fmt:message key="menu.modal.btn.save" />
                </button>
            </div>
        </form>
    </div>
</div>

<%-- MODAL 2: QUẢN LÝ DANH MỤC THỰC ĐƠN --%>
<div id="menuCategoryModal" class="modal-overlay d-none">
    <div class="modal-card modal-card-md">
        <div class="modal-header">
            <h3 class="m-0"><fmt:message key="menu.category.modal.title" /></h3>
            <button type="button" class="btn-modal-close" data-close-modal="menuCategoryModal">✕</button>
        </div>

        <div class="modal-body">
            <%-- Form thêm danh mục mới --%>
            <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="mb-20 p-15 card bg-light">
                <input type="hidden" name="action" value="createCategory" />
                <label for="newMenuCategoryName" class="form-label font-bold mb-5 d-block">
                    <fmt:message key="menu.category.label.name" /> <span class="text-danger">*</span>
                </label>
                <div class="d-flex gap-10">
                    <input type="text" id="newMenuCategoryName" name="categoryName" required maxlength="100"
                           class="filter-input flex-grow-1"
                           placeholder="<fmt:message key="menu.category.placeholder.name" />" />
                    <button type="submit" class="btn-login btn-sm">
                        + <fmt:message key="menu.category.btn.create" />
                    </button>
                </div>
            </form>

            <%-- Bảng danh sách danh mục hiện có --%>
            <h4 class="mb-10"><fmt:message key="menu.category.table.title" /></h4>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th class="w-60">ID</th>
                        <th><fmt:message key="menu.category.table.name" /></th>
                        <th class="w-120 text-center"><fmt:message key="menu.category.table.status" /></th>
                        <th class="w-100 text-center"><fmt:message key="menu.category.table.action" /></th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty CATEGORY_LIST}">
                            <c:forEach var="cat" items="${CATEGORY_LIST}">
                                <tr>
                                    <td>${cat.categoryId}</td>
                                    <td><strong><c:out value="${cat.categoryName}" /></strong></td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${cat.inUse}">
                                                <span class="role-badge role-manager">
                                                    <fmt:message key="menu.category.status.in_use" />
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="role-badge role-staff">
                                                    <fmt:message key="menu.category.status.empty" />
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${not cat.inUse}">
                                                <form action="${pageContext.request.contextPath}/MenuManagementController" method="POST" class="m-0 d-inline">
                                                    <input type="hidden" name="action" value="deleteCategory" />
                                                    <input type="hidden" name="categoryId" value="${cat.categoryId}" />
                                                    <button type="submit" class="btn-reject btn-sm"
                                                            data-confirm="<fmt:message key="menu.category.confirm.delete" />">
                                                        <fmt:message key="menu.category.btn.delete" />
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
                                <td colspan="4" class="text-center"><fmt:message key="menu.category.empty" /></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <div class="modal-actions">
            <button type="button" class="btn-order-cancel" data-close-modal="menuCategoryModal">
                <fmt:message key="menu.modal.btn.cancel" />
            </button>
        </div>
    </div>
</div>
