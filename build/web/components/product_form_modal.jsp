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
     onclick="if(event.target === this) closeProductModal();">
    <div class="modal-card modal-card-md">
        <div class="modal-header">
            <h3 id="productModalTitle" class="m-0"><fmt:message key="menu.modal.title.add" /></h3>
            <button type="button" class="btn-modal-close" onclick="closeProductModal()">✖</button>
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
                            <option value="${cat.categoryId}">${cat.categoryName}</option>
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
                        <button type="button" id="tabBtnUpload" class="btn-image-tab active" onclick="switchImageTab('upload')">
                            📁 <fmt:message key="menu.modal.tab.upload_file" />
                        </button>
                        <button type="button" id="tabBtnUrl" class="btn-image-tab" onclick="switchImageTab('url')">
                            🔗 <fmt:message key="menu.modal.tab.url_link" />
                        </button>
                    </div>

                    <!-- Tab 1: Upload File -->
                    <div id="imageUploadPanel" class="image-panel">
                        <input type="file" id="productImageFileInput" name="productImageFile"
                               accept=".jpg,.jpeg,.png,.webp" class="filter-input w-100"
                               onchange="previewUploadedImage(this)" />
                        <div class="form-hint-text mt-5"><fmt:message key="menu.modal.upload_hint" /></div>
                    </div>

                    <!-- Tab 2: URL Link -->
                    <div id="imageUrlPanel" class="image-panel d-none">
                        <input type="text" id="productImageInput" name="imageUrl"
                               maxlength="255"
                               class="filter-input w-100"
                               placeholder="<fmt:message key="menu.modal.placeholder.image" />"
                               oninput="previewUrlImage(this.value)" />
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
                        <button type="button" class="btn-action-add-recipe" onclick="addRecipeRow()">
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
                <button type="button" class="btn-order-cancel" onclick="closeProductModal()">
                    <fmt:message key="menu.modal.btn.cancel" />
                </button>
                <button type="submit" id="productModalSubmitBtn" class="btn-login">
                    <fmt:message key="menu.modal.btn.save" />
                </button>
            </div>
        </form>
    </div>
</div>
