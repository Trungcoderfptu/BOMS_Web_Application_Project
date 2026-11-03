function initMainApp() {
    if (window.__bomsMainInitialized) {
        return;
    }
    window.__bomsMainInitialized = true;

    const btnHamburger = document.getElementById('btnHamburger');
    const btnDongMenuDoc = document.getElementById('btnDongMenuDoc');
    const menuDoc = document.getElementById('menuDoc');

    // Menu trái dạng ngăn kéo trên điện thoại/máy tính bảng: bật class menu-open trên body (CSS lo hiệu ứng trượt + lớp phủ)
    const menuDocBackdrop = document.getElementById('menuDocBackdrop');

    function setMenuDocOpen(isOpen) {
        document.body.classList.toggle('menu-open', isOpen);
        if (btnHamburger) {
            btnHamburger.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        }
    }

    if (btnHamburger && menuDoc) {
        btnHamburger.addEventListener('click', function () {
            setMenuDocOpen(!document.body.classList.contains('menu-open'));
        });
    }

    if (btnDongMenuDoc) {
        btnDongMenuDoc.addEventListener('click', function () {
            setMenuDocOpen(false);
        });
    }

    if (menuDocBackdrop) {
        menuDocBackdrop.addEventListener('click', function () {
            setMenuDocOpen(false);
        });
    }

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && document.body.classList.contains('menu-open')) {
            setMenuDocOpen(false);
        }
    });

    const btnAvatar = document.getElementById('btnAvatar');
    const menuTaiKhoan = document.getElementById('menuTaiKhoan');

    if (btnAvatar) {
        btnAvatar.addEventListener('click', function () {
            if (menuTaiKhoan.style.display === 'none' || menuTaiKhoan.style.display === '') {
                menuTaiKhoan.style.display = 'block';
            } else {
                menuTaiKhoan.style.display = 'none';
            }
        });
    }

    // ================= SETTINGS TAB MANAGEMENT =================
    const btnMenuProfile = document.getElementById('btnMenuProfile');
    const btnMenuPassword = document.getElementById('btnMenuPassword');

    const tabProfile = document.getElementById('tabProfile');
    const tabPassword = document.getElementById('tabPassword');

    function showTab(activeTab) {
        if (tabProfile)
            tabProfile.classList.add('d-none');
        if (tabPassword)
            tabPassword.classList.add('d-none');

        if (activeTab)
            activeTab.classList.remove('d-none');
    }

    if (btnMenuProfile) {
        btnMenuProfile.addEventListener('click', function () {
            showTab(tabProfile);
        });
    }

    if (btnMenuPassword) {
        btnMenuPassword.addEventListener('click', function () {
            showTab(tabPassword);
        });
    }

    // ==================== GLOBAL DARK MODE ====================
    const btnDarkMode = document.getElementById('btnDarkModeGlobal');

    if (btnDarkMode) {
        const body = document.body;

        if (localStorage.getItem('theme') === 'dark') {
            body.classList.add('dark-theme');
            btnDarkMode.innerHTML = '<i class="fi fi-rr-sun" aria-hidden="true"></i>';
        } else {
            btnDarkMode.innerHTML = '<i class="fi fi-rr-moon" aria-hidden="true"></i>';
        }

        btnDarkMode.addEventListener('click', () => {
            body.classList.toggle('dark-theme');
            if (body.classList.contains('dark-theme')) {
                localStorage.setItem('theme', 'dark');
                btnDarkMode.innerHTML = '<i class="fi fi-rr-sun" aria-hidden="true"></i>';
            } else {
                localStorage.setItem('theme', 'light');
                btnDarkMode.innerHTML = '<i class="fi fi-rr-moon" aria-hidden="true"></i>';
            }
        });
    }

    // ================= LANGUAGE DROPDOWN =================
    const btnLangToggle = document.getElementById('btnLangToggle');
    const langDropdown = document.getElementById('langDropdown');

    if (btnLangToggle && langDropdown) {
        btnLangToggle.addEventListener('click', function (e) {
            e.stopPropagation();
            langDropdown.classList.toggle('d-none');
        });

        document.addEventListener('click', function (e) {
            if (!btnLangToggle.contains(e.target) && !langDropdown.contains(e.target)) {
                langDropdown.classList.add('d-none');
            }
        });
    }

    // Modal click outside handling
    const staffModal = document.getElementById('staffCancelModal');
    if (staffModal) {
        staffModal.addEventListener('click', function (e) {
            if (e.target === staffModal) {
                closeStaffCancelModal();
            }
        });
    }

    const orderModal = document.getElementById('orderConfirmModal');
    if (orderModal) {
        orderModal.addEventListener('click', function (e) {
            if (e.target === orderModal) {
                closeOrderConfirmModal();
            }
        });
    }

    const alertModal = document.getElementById('inventoryAlertModal');
    if (alertModal) {
        alertModal.addEventListener('click', function (e) {
            if (e.target === alertModal) {
                closeInventoryAlertModal();
            }
        });
    }

    const revertModal = document.getElementById('revertDeliveryModal');
    if (revertModal) {
        revertModal.addEventListener('click', function (e) {
            if (e.target === revertModal) {
                closeRevertDeliveryModal();
            }
        });
    }

    const deliveryModal = document.getElementById('deliveryInfoModal');
    if (deliveryModal) {
        deliveryModal.addEventListener('click', function (e) {
            if (e.target === deliveryModal) {
                closeDeliveryInfoModal();
            }
        });
    }

    const shipperCompModal = document.getElementById('shipperCompleteModal');
    if (shipperCompModal) {
        shipperCompModal.addEventListener('click', function (e) {
            if (e.target === shipperCompModal) {
                closeShipperCompleteModal();
            }
        });
    }

    const shipperFlModal = document.getElementById('shipperFailModal');
    if (shipperFlModal) {
        shipperFlModal.addEventListener('click', function (e) {
            if (e.target === shipperFlModal) {
                closeShipperFailModal();
            }
        });
    }

    const broadcastModal = document.getElementById('broadcastModal');
    if (broadcastModal) {
        broadcastModal.addEventListener('click', function (e) {
            if (e.target === broadcastModal) {
                closeBroadcastModal();
            }
        });
    }

    const notifDropdown = document.getElementById('notificationDropdown');
    const notifBell = document.getElementById('btnNotificationBell');
    if (notifDropdown && notifBell) {
        document.addEventListener('click', function (e) {
            if (!notifDropdown.contains(e.target) && !notifBell.contains(e.target)) {
                notifDropdown.classList.add('d-none');
            }
        });
    }

    initCancelCountdowns();

    // ================= ATTENDANCE SERVER CLOCK =================
    const clockEl = document.getElementById('serverClockDisplay');
    if (clockEl) {
        const timeStr = clockEl.getAttribute('data-server-time');
        if (timeStr) {
            const parts = timeStr.split(':');
            if (parts.length === 3) {
                let hours = parseInt(parts[0], 10);
                let minutes = parseInt(parts[1], 10);
                let seconds = parseInt(parts[2], 10);

                setInterval(function () {
                    seconds++;
                    if (seconds >= 60) {
                        seconds = 0;
                        minutes++;
                        if (minutes >= 60) {
                            minutes = 0;
                            hours = (hours + 1) % 24;
                        }
                    }
                    const pad = function (n) { return n < 10 ? '0' + n : n; };
                    clockEl.textContent = pad(hours) + ':' + pad(minutes) + ':' + pad(seconds);
                }, 1000);
            }
        }
    }
    initInventoryAlertPolling();
    initNotificationPolling();
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initMainApp);
} else {
    initMainApp();
}

// ===== UC-07: Kiểm tra file bằng chứng ngay khi chọn (server vẫn kiểm tra lại lần nữa) =====
function initEvidenceFileCheck() {
    const input = document.getElementById('evidenceFiles');
    if (!input || input.dataset.checkReady) {
        return;
    }
    input.dataset.checkReady = 'true';
    const allowedTypes = (input.getAttribute('accept') || '').split(',').map(t => t.trim().replace('.', '').toLowerCase());

    input.addEventListener('change', function () {
        input.setCustomValidity(getEvidenceError(input, allowedTypes));
        input.classList.toggle('input-error', input.validationMessage !== '');
        input.reportValidity();
    });
}

function getEvidenceError(input, allowedTypes) {
    const maxFiles = parseInt(input.dataset.maxFiles, 10);
    const maxSize = parseInt(input.dataset.maxSize, 10);
    if (input.files.length > maxFiles) {
        return input.dataset.msgFiles;
    }
    for (const file of input.files) {
        const ext = file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : '';
        if (!allowedTypes.includes(ext)) {
            return input.dataset.msgType;
        }
        if (file.size <= 0 || file.size > maxSize) {
            return input.dataset.msgSize;
        }
    }
    return '';
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initEvidenceFileCheck);
} else {
    initEvidenceFileCheck();
}
// Tab switching for Dashboard screens
function openTab(evt, tabId) {
    let tabcontent = document.getElementsByClassName("tab-content");
    for (let i = 0; i < tabcontent.length; i++) {
        tabcontent[i].classList.add("d-none");
        tabcontent[i].classList.remove("active");
    }

    let tablinks = document.getElementsByClassName("tab-btn");
    for (let i = 0; i < tablinks.length; i++) {
        tablinks[i].classList.remove("active");
    }

    document.getElementById(tabId).classList.remove("d-none");
    document.getElementById(tabId).classList.add("active");

    let clickedTabBtn = (evt && evt.target) ? evt.target.closest('.tab-btn') : null;
    if (clickedTabBtn) {
        clickedTabBtn.classList.add("active");
    } else {
        let targetBtn = document.querySelector(`.tab-btn[data-tab="${tabId}"]`) || document.querySelector(`.tab-btn[onclick*="${tabId}"]`);
        if (targetBtn)
            targetBtn.classList.add("active");
    }

    let floatingCart = document.querySelector(".floating-cart");
    if (floatingCart) {
        if (tabId === 'tab-cart') {
            floatingCart.style.display = 'none'; // Hide floating cart on cart tab
        } else {
            floatingCart.style.display = 'flex'; // Show floating cart on other tabs
        }
    }
}

// Modal order confirmation handlers
function openOrderConfirmModal() {
    const modal = document.getElementById('orderConfirmModal');
    if (modal) {
        modal.classList.remove('d-none');
    }
}

function closeOrderConfirmModal() {
    const modal = document.getElementById('orderConfirmModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// Modal staff cancel order confirmation handlers
function openStaffCancelModal(orderId, activeTab) {
    const modal = document.getElementById('staffCancelModal');
    const orderIdInput = document.getElementById('staffCancelModalOrderId');
    const orderIdText = document.getElementById('staffCancelModalOrderIdText');
    const activeTabInput = document.getElementById('staffCancelModalActiveTab');
    if (modal) {
        if (orderIdInput)
            orderIdInput.value = orderId;
        if (orderIdText)
            orderIdText.textContent = '#' + orderId;
        if (activeTabInput && activeTab)
            activeTabInput.value = activeTab;
        modal.classList.remove('d-none');
    }
}

function closeStaffCancelModal() {
    const modal = document.getElementById('staffCancelModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

let __pendingRevertForm = null;

function openRevertDeliveryModal(form, orderId) {
    __pendingRevertForm = form;
    const modal = document.getElementById('revertDeliveryModal');
    const textEl = document.getElementById('revertDeliveryModalOrderIdText');
    if (modal) {
        if (textEl) {
            textEl.textContent = '#' + (orderId || '');
        }
        modal.classList.remove('d-none');
    }
}

function closeRevertDeliveryModal() {
    __pendingRevertForm = null;
    const modal = document.getElementById('revertDeliveryModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function confirmRevertDeliverySubmit() {
    if (__pendingRevertForm) {
        const formToSubmit = __pendingRevertForm;
        __pendingRevertForm = null;
        closeRevertDeliveryModal();
        formToSubmit.submit();
    }
}

function openDeliveryInfoModal(orderId, customerName, customerPhone, address, areaId) {
    const modal = document.getElementById('deliveryInfoModal');
    if (!modal) return;
    const orderIdInput = document.getElementById('deliveryModalOrderId');
    const orderIdText = document.getElementById('deliveryModalOrderIdText');
    const nameInput = document.getElementById('deliveryCustomerName');
    const phoneInput = document.getElementById('deliveryCustomerPhone');
    const addrInput = document.getElementById('deliveryAddress');
    const areaSelect = document.getElementById('deliveryAreaId');

    if (orderIdInput) orderIdInput.value = orderId || '';
    if (orderIdText) orderIdText.textContent = '#' + (orderId || '');
    if (nameInput) nameInput.value = customerName || '';
    if (phoneInput) phoneInput.value = customerPhone || '';
    if (addrInput) addrInput.value = address || '';
    if (areaSelect) areaSelect.value = areaId || '';

    modal.classList.remove('d-none');
}

function closeDeliveryInfoModal() {
    const modal = document.getElementById('deliveryInfoModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function openShipperCompleteModal(taskId, orderCode) {
    const modal = document.getElementById('shipperCompleteModal');
    const taskIdInput = document.getElementById('completeModalTaskId');
    const orderCodeText = document.getElementById('completeModalOrderCodeText');
    const fileInput = document.getElementById('completeProofImage');
    if (modal) {
        if (taskIdInput) taskIdInput.value = taskId;
        if (orderCodeText) orderCodeText.textContent = orderCode || ('#' + taskId);
        if (fileInput) fileInput.value = '';
        modal.classList.remove('d-none');
    }
}

function closeShipperCompleteModal() {
    const modal = document.getElementById('shipperCompleteModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function openShipperFailModal(taskId, orderCode) {
    const modal = document.getElementById('shipperFailModal');
    const taskIdInput = document.getElementById('failModalTaskId');
    const orderCodeText = document.getElementById('failModalOrderCodeText');
    const reasonText = document.getElementById('failReason');
    const presetSelect = document.getElementById('presetFailReason');
    if (modal) {
        if (taskIdInput) taskIdInput.value = taskId;
        if (orderCodeText) orderCodeText.textContent = orderCode || ('#' + taskId);
        if (reasonText) reasonText.value = '';
        if (presetSelect) presetSelect.value = '';
        modal.classList.remove('d-none');
    }
}

function closeShipperFailModal() {
    const modal = document.getElementById('shipperFailModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function handleStaffStatusSubmit(event, form, orderId) {
    // newStatus có thể là ô chọn (bảng cũ) hoặc ô ẩn của nút "bước tiếp theo" trên thẻ Kanban
    const select = form.querySelector("[name='newStatus']");
    if (!select) return true;
    const newStatus = select.value;
    const activeTabInput = form.querySelector("input[name='activeTab']");
    const activeTab = activeTabInput ? activeTabInput.value : '';
    const currentOrderType = form.getAttribute('data-order-type') || '';
    const currentStatus = form.getAttribute('data-current-status') || '';

    if (newStatus === 'Cancelled') {
        event.preventDefault();
        openStaffCancelModal(orderId, activeTab);
        return false;
    }

    if (newStatus === 'Waiting for dispatch') {
        const custName = form.getAttribute('data-customer-name') || '';
        const custPhone = form.getAttribute('data-customer-phone') || '';
        const addr = form.getAttribute('data-delivery-address') || '';
        const areaId = form.getAttribute('data-area-id') || '';

        if (currentOrderType !== 'Delivery' || !custName || !custPhone || !addr) {
            event.preventDefault();
            openDeliveryInfoModal(orderId, custName, custPhone, addr, areaId);
            return false;
        }
    }

    if (currentOrderType === 'Delivery' && (newStatus === 'Accepted' || newStatus === 'Prepared')) {
        if (currentStatus === 'Delivering' || currentStatus === 'Delivered' || currentStatus === 'Returned') {
            event.preventDefault();
            alert('Kh\u00F4ng th\u1EC3 chuy\u1EC3n tr\u1EA1ng th\u00E1i \u0111\u01A1n h\u00E0ng \u0111ang ho\u1EB7c \u0111\u00E3 giao v\u1EC1 tr\u1EA1ng th\u00E1i n\u00E0y!');
            return false;
        }
        event.preventDefault();
        openRevertDeliveryModal(form, orderId);
        return false;
    }

    return true;
}

// Real-time 5-minute countdown for canceled orders
function initCancelCountdowns() {
    const timers = document.querySelectorAll('.cancel-countdown-timer');
    if (!timers || timers.length === 0)
        return;

    timers.forEach(timer => {
        let seconds = parseInt(timer.getAttribute('data-seconds'), 10);
        const orderId = timer.getAttribute('data-order-id');
        const lockedText = timer.getAttribute('data-locked-text') || '\u0110\u00E3 kh\u00F3a';

        function updateDisplay() {
            if (seconds <= 0) {
                timer.textContent = '00:00';
                const wrapper = document.getElementById('cancelCountdownWrapper-' + orderId);
                if (wrapper) {
                    wrapper.innerHTML = '<span class="badge-locked">' + lockedText + '</span>';
                }
                const selectEl = document.querySelector('.status-select-' + orderId);
                if (selectEl)
                    selectEl.disabled = true;
                const btnSave = document.querySelector('.btn-status-save-' + orderId);
                if (btnSave)
                    btnSave.disabled = true;
                return false;
            }

            const m = Math.floor(seconds / 60);
            const s = seconds % 60;
            timer.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
            return true;
        }

        updateDisplay();
        const intervalId = setInterval(() => {
            seconds--;
            if (!updateDisplay()) {
                clearInterval(intervalId);
            }
        }, 1000);
    });
}

// ==================== MENU MANAGEMENT MODAL ====================
function switchImageTab(mode) {
    const btnUpload = document.getElementById('tabBtnUpload');
    const btnUrl = document.getElementById('tabBtnUrl');
    const panelUpload = document.getElementById('imageUploadPanel');
    const panelUrl = document.getElementById('imageUrlPanel');

    if (mode === 'upload') {
        if (btnUpload) btnUpload.classList.add('active');
        if (btnUrl) btnUrl.classList.remove('active');
        if (panelUpload) panelUpload.classList.remove('d-none');
        if (panelUrl) panelUrl.classList.add('d-none');
    } else {
        if (btnUrl) btnUrl.classList.add('active');
        if (btnUpload) btnUpload.classList.remove('active');
        if (panelUrl) panelUrl.classList.remove('d-none');
        if (panelUpload) panelUpload.classList.add('d-none');
    }
}

function previewUploadedImage(input) {
    const previewBox = document.getElementById('imagePreviewBox');
    const previewImg = document.getElementById('imagePreviewImg');
    const modal = document.getElementById('productModal');
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const maxBytes = 10 * 1024 * 1024; // 10 MB
        if (file.size > maxBytes) {
            const errorMsg = (modal && modal.getAttribute('data-error-size'))
                ? modal.getAttribute('data-error-size')
                : 'K\u00EDch th\u01B0\u1EDBc \u1EA3nh v\u01B0\u1EE3t qu\u00E1 dung l\u01B0\u1EE3ng cho ph\u00E9p (t\u1ED1i \u0111a 10MB)!';
            alert(errorMsg);
            input.value = '';
            if (previewBox) previewBox.classList.add('d-none');
            return;
        }
        const reader = new FileReader();
        reader.onload = function(e) {
            if (previewImg) previewImg.src = e.target.result;
            if (previewBox) previewBox.classList.remove('d-none');
        };
        reader.readAsDataURL(file);
    } else {
        if (previewBox) previewBox.classList.add('d-none');
    }
}

function previewUrlImage(url) {
    const previewBox = document.getElementById('imagePreviewBox');
    const previewImg = document.getElementById('imagePreviewImg');
    const urlInput = document.getElementById('productImageInput');
    if (url && url.trim().length > 0) {
        if (url.trim().length > 255) {
            if (urlInput) urlInput.classList.add('border-danger');
            if (previewBox) previewBox.classList.add('d-none');
            return;
        }
        if (urlInput) urlInput.classList.remove('border-danger');
        if (previewImg) previewImg.src = url.trim();
        if (previewBox) previewBox.classList.remove('d-none');
    } else {
        if (urlInput) urlInput.classList.remove('border-danger');
        if (previewBox) previewBox.classList.add('d-none');
    }
}

function resetImageModalState() {
    switchImageTab('upload');
    const fileInput = document.getElementById('productImageFileInput');
    const urlInput = document.getElementById('productImageInput');
    const previewBox = document.getElementById('imagePreviewBox');
    const previewImg = document.getElementById('imagePreviewImg');

    if (fileInput) fileInput.value = '';
    if (urlInput) {
        urlInput.value = '';
        urlInput.classList.remove('border-danger');
    }
    if (previewImg) previewImg.src = '';
    if (previewBox) previewBox.classList.add('d-none');
}

function resetRecipeRows() {
    const list = document.getElementById('recipeList');
    const hint = document.getElementById('recipeEmptyHint');
    if (list) list.innerHTML = '';
    if (hint) hint.classList.remove('d-none');
}

function addRecipeRow(selectedIngId, qtyValue) {
    const template = document.getElementById('ingredientMasterTemplate');
    const list = document.getElementById('recipeList');
    const hint = document.getElementById('recipeEmptyHint');
    if (!template || !list) return;

    if (hint) hint.classList.add('d-none');

    const row = document.createElement('div');
    row.className = 'recipe-row flex-align-center gap-10 mb-8';

    const colIng = document.createElement('div');
    colIng.className = 'recipe-col-ingredient flex-1';

    const select = document.createElement('select');
    select.name = 'recipeIngredientId';
    select.className = 'filter-input w-100';
    select.required = true;
    select.innerHTML = template.innerHTML;

    if (selectedIngId) {
        select.value = String(selectedIngId);
    }

    colIng.appendChild(select);

    const colQty = document.createElement('div');
    colQty.className = 'recipe-col-qty flex-align-center gap-5';

    const qtyInput = document.createElement('input');
    qtyInput.type = 'number';
    qtyInput.name = 'recipeQuantity';
    qtyInput.className = 'filter-input input-qty-sm text-right';
    qtyInput.step = 'any';
    qtyInput.min = '0.001';
    qtyInput.placeholder = '0.0';
    qtyInput.required = true;
    if (qtyValue !== undefined && qtyValue !== null && qtyValue !== '') {
        qtyInput.value = qtyValue;
    }

    const unitSpan = document.createElement('span');
    unitSpan.className = 'recipe-unit-label font-bold text-muted';
    unitSpan.textContent = '---';

    colQty.appendChild(qtyInput);
    colQty.appendChild(unitSpan);

    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.className = 'btn-remove-recipe';
    removeBtn.textContent = '\u2715';
    removeBtn.onclick = function() {
        removeRecipeRow(this);
    };

    row.appendChild(colIng);
    row.appendChild(colQty);
    row.appendChild(removeBtn);

    select.onchange = function() {
        onRecipeIngredientChange(this, unitSpan);
    };

    list.appendChild(row);
    onRecipeIngredientChange(select, unitSpan);
}

function onRecipeIngredientChange(selectEl, unitSpanEl) {
    if (!selectEl || !unitSpanEl) return;
    const selectedOption = selectEl.options[selectEl.selectedIndex];
    const unit = selectedOption ? selectedOption.getAttribute('data-unit') : '';
    unitSpanEl.textContent = unit || '---';
}

function removeRecipeRow(btnEl) {
    if (!btnEl) return;
    const row = btnEl.closest('.recipe-row');
    if (row && row.parentNode) {
        row.parentNode.removeChild(row);
    }
    const list = document.getElementById('recipeList');
    const hint = document.getElementById('recipeEmptyHint');
    if (list && list.children.length === 0 && hint) {
        hint.classList.remove('d-none');
    }
}

function openCreateProductModal() {
    const modal = document.getElementById('productModal');
    if (!modal) return;

    const titleEl = document.getElementById('productModalTitle');
    const actionEl = document.getElementById('productModalAction');
    const idEl = document.getElementById('productModalId');
    const nameEl = document.getElementById('productNameInput');
    const catEl = document.getElementById('productCategorySelect');
    const priceEl = document.getElementById('productPriceInput');
    const statusGroup = document.getElementById('productStatusGroup');

    if (titleEl) {
        titleEl.textContent = modal.getAttribute('data-title-add') || 'Th\u00EAm M\u00F3n M\u1EDBi V\u00E0o Th\u1EF1c \u0110\u01A1n';
    }
    if (actionEl) actionEl.value = 'createProduct';
    if (idEl) idEl.value = '';
    if (nameEl) nameEl.value = '';
    if (catEl) catEl.value = '';
    if (priceEl) priceEl.value = '';
    if (statusGroup) statusGroup.classList.add('d-none');

    resetImageModalState();
    resetRecipeRows();

    modal.classList.remove('d-none');
    if (nameEl) nameEl.focus();
}

function openEditProductModal(productId, productName, categoryId, price, imageUrl, isAvailable, recipeData) {
    const modal = document.getElementById('productModal');
    if (!modal) return;

    const titleEl = document.getElementById('productModalTitle');
    const actionEl = document.getElementById('productModalAction');
    const idEl = document.getElementById('productModalId');
    const nameEl = document.getElementById('productNameInput');
    const catEl = document.getElementById('productCategorySelect');
    const priceEl = document.getElementById('productPriceInput');
    const urlInput = document.getElementById('productImageInput');
    const fileInput = document.getElementById('productImageFileInput');
    const previewBox = document.getElementById('imagePreviewBox');
    const previewImg = document.getElementById('imagePreviewImg');
    const statusGroup = document.getElementById('productStatusGroup');
    const availableCb = document.getElementById('productAvailableCheckbox');

    if (titleEl) {
        titleEl.textContent = modal.getAttribute('data-title-edit') || 'Ch\u1EC9nh S\u1EEDa Th\u00F4ng Tin M\u00F3n \u0102n';
    }
    if (actionEl) actionEl.value = 'updateProduct';
    if (idEl) idEl.value = productId;
    if (nameEl) nameEl.value = productName || '';
    if (catEl) catEl.value = categoryId || '';
    if (priceEl) priceEl.value = Math.round(Number(price)) || '';

    if (fileInput) fileInput.value = '';
    const cleanImageUrl = (imageUrl && imageUrl !== 'null') ? imageUrl : '';
    if (urlInput) urlInput.value = cleanImageUrl;

    if (cleanImageUrl) {
        switchImageTab('url');
        if (previewImg) {
            if (cleanImageUrl.startsWith('http')) {
                previewImg.src = cleanImageUrl;
            } else {
                const basePath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
                previewImg.src = (basePath ? basePath + '/' : '/') + cleanImageUrl.replace(/^\//, '');
            }
        }
        if (previewBox) previewBox.classList.remove('d-none');
    } else {
        switchImageTab('upload');
        if (previewBox) previewBox.classList.add('d-none');
    }

    if (statusGroup) statusGroup.classList.remove('d-none');
    if (availableCb) availableCb.checked = (isAvailable === true || isAvailable === 'true');

    resetRecipeRows();
    if (recipeData && typeof recipeData === 'string' && recipeData.trim() !== '') {
        const pairs = recipeData.trim().split(',');
        for (let i = 0; i < pairs.length; i++) {
            const parts = pairs[i].split(':');
            if (parts.length === 2) {
                addRecipeRow(parts[0], parts[1]);
            }
        }
    }

    modal.classList.remove('d-none');
    if (nameEl) nameEl.focus();
}

function closeProductModal() {
    const modal = document.getElementById('productModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// ================= MENU CATEGORY MODAL =================
function openMenuCategoryModal() {
    const modal = document.getElementById('menuCategoryModal');
    if (modal) {
        modal.classList.remove('d-none');
        const input = document.getElementById('newMenuCategoryName');
        if (input) input.focus();
    }
}

function closeMenuCategoryModal() {
    const modal = document.getElementById('menuCategoryModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// ================= INVENTORY CATEGORY MODAL =================
function openInventoryCategoryModal() {
    const modal = document.getElementById('inventoryCategoryModal');
    if (modal) {
        modal.classList.remove('d-none');
        const input = document.getElementById('newInventoryCategoryName');
        if (input) input.focus();
    }
}

function closeInventoryCategoryModal() {
    const modal = document.getElementById('inventoryCategoryModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// Listen for Escape key to close modals
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        if (typeof closeProductModal === 'function') closeProductModal();
        if (typeof closeMenuCategoryModal === 'function') closeMenuCategoryModal();
        if (typeof closeInventoryCategoryModal === 'function') closeInventoryCategoryModal();
        if (typeof closeOtReasonModal === 'function') closeOtReasonModal();
        if (typeof closeInventoryModal === 'function') closeInventoryModal();
        if (typeof closeThresholdModal === 'function') closeThresholdModal();
        if (typeof closeStaffCancelModal === 'function') closeStaffCancelModal();
        if (typeof closeOrderConfirmModal === 'function') closeOrderConfirmModal();
        if (typeof closeInventoryAlertModal === 'function') closeInventoryAlertModal();
        if (typeof closeRevertDeliveryModal === 'function') closeRevertDeliveryModal();
        if (typeof closeDeliveryInfoModal === 'function') closeDeliveryInfoModal();
        if (typeof closeShipperCompleteModal === 'function') closeShipperCompleteModal();
        if (typeof closeShipperFailModal === 'function') closeShipperFailModal();
    }
});

// Auto open category modal if manageCategory query param is present (Option A)
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('manageCategory') === 'true') {
        if (document.getElementById('menuCategoryModal')) {
            openMenuCategoryModal();
        } else if (document.getElementById('inventoryCategoryModal')) {
            openInventoryCategoryModal();
        }
    }
});

// ================= INVENTORY MODAL HANDLERS =================
function openCreateIngredientModal() {
    const modal = document.getElementById('inventoryModal');
    if (!modal) return;

    const titleEl = document.getElementById('inventoryModalTitle');
    const actionEl = document.getElementById('inventoryModalAction');
    const idEl = document.getElementById('inventoryModalId');
    const nameEl = document.getElementById('ingredientNameInput');
    const catEl = document.getElementById('ingredientCategorySelect');
    const unitEl = document.getElementById('ingredientUnitInput');
    const qtyEl = document.getElementById('ingredientQtyInput');
    const priceEl = document.getElementById('ingredientPriceInput');
    const threshEl = document.getElementById('ingredientThresholdInput');
    const locEl = document.getElementById('ingredientLocationInput');

    if (titleEl) {
        titleEl.textContent = modal.getAttribute('data-title-add') || 'Thêm Nguyên Liệu Mới';
    }
    if (actionEl) actionEl.value = 'create';
    if (idEl) idEl.value = '';
    if (nameEl) nameEl.value = '';
    if (unitEl) unitEl.value = '';
    if (qtyEl) qtyEl.value = '0';
    if (priceEl) priceEl.value = '0';
    if (threshEl) threshEl.value = '5';
    if (locEl) locEl.value = '';

    modal.classList.remove('d-none');
    if (nameEl) nameEl.focus();
}

function openEditIngredientModal(id, categoryId, name, unit, quantity, price, location, threshold) {
    const modal = document.getElementById('inventoryModal');
    if (!modal) return;

    const titleEl = document.getElementById('inventoryModalTitle');
    const actionEl = document.getElementById('inventoryModalAction');
    const idEl = document.getElementById('inventoryModalId');
    const nameEl = document.getElementById('ingredientNameInput');
    const catEl = document.getElementById('ingredientCategorySelect');
    const unitEl = document.getElementById('ingredientUnitInput');
    const qtyEl = document.getElementById('ingredientQtyInput');
    const priceEl = document.getElementById('ingredientPriceInput');
    const threshEl = document.getElementById('ingredientThresholdInput');
    const locEl = document.getElementById('ingredientLocationInput');

    if (titleEl) {
        titleEl.textContent = modal.getAttribute('data-title-edit') || 'Chỉnh Sửa Nguyên Liệu';
    }
    if (actionEl) actionEl.value = 'update';
    if (idEl) idEl.value = id;
    if (nameEl) nameEl.value = name || '';
    if (catEl) catEl.value = categoryId || '';
    if (unitEl) unitEl.value = unit || '';
    if (qtyEl) qtyEl.value = quantity || '0';
    if (priceEl) priceEl.value = Math.round(Number(price)) || '0';
    if (threshEl) threshEl.value = threshold || '0';
    if (locEl) locEl.value = (location && location !== '-') ? location : '';

    modal.classList.remove('d-none');
    if (nameEl) nameEl.focus();
}

function closeInventoryModal() {
    const modal = document.getElementById('inventoryModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function openThresholdModal(id, name, currentThreshold) {
    const modal = document.getElementById('thresholdModal');
    if (!modal) return;

    const idEl = document.getElementById('thresholdIngredientId');
    const nameEl = document.getElementById('thresholdIngredientName');
    const valEl = document.getElementById('thresholdValueInput');

    if (idEl) idEl.value = id;
    if (nameEl) nameEl.textContent = name || '';
    if (valEl) valEl.value = currentThreshold || '0';

    modal.classList.remove('d-none');
    if (valEl) valEl.focus();
}

function closeThresholdModal() {
    const modal = document.getElementById('thresholdModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// ================= INVENTORY ALERTS (POLLING, AUDIO & MODAL) =================
let __lastAlertCount = 0;
let __latestInventoryAlerts = [];

function playNotificationSound() {
    try {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        const ctx = new AudioCtx();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(880, ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(440, ctx.currentTime + 0.25);
        gain.gain.setValueAtTime(0.15, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.25);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start();
        osc.stop(ctx.currentTime + 0.25);
    } catch (e) {
        // Silently catch audio policy restrictions
    }
}

function pollInventoryAlerts() {
    const badge = document.getElementById('inventoryAlertBadge');
    if (!badge) return;

    fetch('IngredientAlertController?action=poll', { cache: 'no-store' })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (!data || !data.success) return;
            const count = data.count || 0;
            __latestInventoryAlerts = data.alerts || [];

            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('d-none');
                badge.classList.add('alert-badge-pulse');
                if (count > __lastAlertCount) {
                    playNotificationSound();
                }
            } else {
                badge.textContent = '0';
                badge.classList.add('d-none');
                badge.classList.remove('alert-badge-pulse');
            }
            __lastAlertCount = count;

            const modal = document.getElementById('inventoryAlertModal');
            if (modal && !modal.classList.contains('d-none')) {
                renderInventoryAlertsTable(__latestInventoryAlerts);
            }
        })
        .catch(function (err) {
            // Ignore polling connection glitches silently
        });
}

function initInventoryAlertPolling() {
    const bell = document.getElementById('btnInventoryAlertBell');
    if (!bell) return;

    // Initial check on load
    pollInventoryAlerts();

    // Regular polling every 60 seconds
    setInterval(pollInventoryAlerts, 60000);
}

function openInventoryAlertModal() {
    const modal = document.getElementById('inventoryAlertModal');
    if (!modal) return;

    modal.classList.remove('d-none');
    renderInventoryAlertsTable(__latestInventoryAlerts);
    pollInventoryAlerts();
}

function closeInventoryAlertModal() {
    const modal = document.getElementById('inventoryAlertModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function renderInventoryAlertsTable(alerts) {
    const modal = document.getElementById('inventoryAlertModal');
    const tableBody = document.getElementById('inventoryAlertTableBody');
    const emptyState = document.getElementById('alertEmptyState');
    const tableContainer = document.getElementById('alertTableContainer');
    if (!modal || !tableBody) return;

    const lblLow = modal.getAttribute('data-type-low-stock') || 'Low Stock';
    const lblOut = modal.getAttribute('data-type-out-of-stock') || 'Out of Stock';
    const lblExpSoon = modal.getAttribute('data-type-expiring-soon') || 'Expiring Soon';
    const lblExp = modal.getAttribute('data-type-expired') || 'Expired';
    const lblBtnConfirm = modal.getAttribute('data-btn-confirm') || 'Confirm';
    const lblBtnConfirmed = modal.getAttribute('data-btn-confirmed') || 'Confirmed';

    tableBody.innerHTML = '';

    if (!alerts || alerts.length === 0) {
        if (emptyState) emptyState.classList.remove('d-none');
        if (tableContainer) tableContainer.classList.add('d-none');
        return;
    }

    if (emptyState) emptyState.classList.add('d-none');
    if (tableContainer) tableContainer.classList.remove('d-none');

    alerts.forEach(function (a) {
        const tr = document.createElement('tr');

        let typeBadgeHtml = '';
        if (a.alertType === 'OutOfStock') {
            typeBadgeHtml = '<span class="badge-status-inactive">' + escapeHtml(lblOut) + '</span>';
        } else if (a.alertType === 'Expired') {
            typeBadgeHtml = '<span class="badge-alert-expired">' + escapeHtml(lblExp) + '</span>';
        } else if (a.alertType === 'ExpiringSoon' || a.alertType === 'Expiring Soon') {
            typeBadgeHtml = '<span class="badge-stock-out">' + escapeHtml(lblExpSoon) + '</span>';
        } else {
            typeBadgeHtml = '<span class="badge-stock-out">' + escapeHtml(lblLow) + '</span>';
        }

        const isConfirmed = (a.status === 'Confirmed');
        const statusHtml = isConfirmed
            ? '<span class="badge-stock-ok">' + escapeHtml(lblBtnConfirmed) + (a.confirmedBy ? ' (' + escapeHtml(a.confirmedBy) + ')' : '') + '</span>'
            : '<span class="badge-status-inactive"><i class="fi fi-rr-triangle-warning" aria-hidden="true"></i> Active</span>';

        const actionHtml = isConfirmed
            ? '<span class="text-muted"><i class="fi fi-rr-check" aria-hidden="true"></i> ' + escapeHtml(lblBtnConfirmed) + '</span>'
            : '<button type="button" class="btn-action-edit" onclick="confirmInventoryAlert(' + a.alertId + ', this)">' + escapeHtml(lblBtnConfirm) + '</button>';

        tr.innerHTML = '<td><strong>' + escapeHtml(a.ingredientName || '-') + '</strong></td>'
            + '<td>' + typeBadgeHtml + '</td>'
            + '<td><span class="text-danger font-bold">' + (a.quantity != null ? a.quantity : 0) + '</span> ' + escapeHtml(a.unit || '') + '</td>'
            + '<td>' + (a.warningThreshold != null ? a.warningThreshold : 0) + ' ' + escapeHtml(a.unit || '') + '</td>'
            + '<td>' + escapeHtml(a.createdAt || '-') + '</td>'
            + '<td>' + statusHtml + '</td>'
            + '<td>' + actionHtml + '</td>';

        tableBody.appendChild(tr);
    });
}

function confirmInventoryAlert(alertId, btnElement) {
    if (!alertId) return;

    if (btnElement) {
        btnElement.disabled = true;
    }

    fetch('IngredientAlertController', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=confirm&alertId=' + encodeURIComponent(alertId)
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        if (data && data.success) {
            pollInventoryAlerts();
        } else if (btnElement) {
            btnElement.disabled = false;
        }
    })
    .catch(function (err) {
        if (btnElement) {
            btnElement.disabled = false;
        }
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, function (m) { return map[m]; });
}

// ================= HR & SHIFT MANAGEMENT MODAL HANDLERS =================
function openCreateShiftModal() {
    const modal = document.getElementById('shiftModal');
    const form = modal ? modal.querySelector('form') : null;
    if (form) {
        form.reset();
        const actionInput = document.getElementById('shiftModalAction');
        const idInput = document.getElementById('shiftModalId');
        if (actionInput) actionInput.value = 'createShift';
        if (idInput) idInput.value = '0';
    }
    if (modal) {
        modal.classList.remove('d-none');
    }
}

function openEditShiftModal(shiftId, shiftName, startTime, endTime) {
    const modal = document.getElementById('shiftModal');
    if (!modal) return;
    const actionInput = document.getElementById('shiftModalAction');
    const idInput = document.getElementById('shiftModalId');
    const nameInput = document.getElementById('shiftNameInput');
    const startInput = document.getElementById('startTimeInput');
    const endInput = document.getElementById('endTimeInput');

    if (actionInput) actionInput.value = 'updateShift';
    if (idInput) idInput.value = shiftId;
    if (nameInput) nameInput.value = shiftName || '';
    if (startInput) startInput.value = startTime || '';
    if (endInput) endInput.value = endTime || '';

    modal.classList.remove('d-none');
}

function closeShiftModal() {
    const modal = document.getElementById('shiftModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function openAssignShiftModal() {
    const modal = document.getElementById('assignShiftModal');
    if (modal) {
        modal.classList.remove('d-none');
        setAssignMode('single');
    }
}

function openQuickAssignModal(userId, fullName, date) {
    const modal = document.getElementById('assignShiftModal');
    if (modal) {
        setAssignMode('single');
        const userSelect = document.getElementById('assignUserIdSelect');
        if (userSelect && userId) {
            userSelect.value = userId;
        }
        const workDateInput = document.getElementById('assignWorkDateInput');
        if (workDateInput && date) {
            workDateInput.value = date;
        }
        modal.classList.remove('d-none');
    }
}

function closeAssignShiftModal() {
    const modal = document.getElementById('assignShiftModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function setAssignMode(mode) {
    const btnSingle = document.getElementById('btnModeSingle');
    const btnBulk = document.getElementById('btnModeBulk');
    const secSingle = document.getElementById('sectionSingleDate');
    const secBulk = document.getElementById('sectionBulkDate');
    const actionInput = document.getElementById('assignFormAction');
    const workDateInput = document.getElementById('assignWorkDateInput');
    const startDateInput = document.getElementById('bulkStartDateInput');
    const endDateInput = document.getElementById('bulkEndDateInput');

    if (!btnSingle || !btnBulk) return;

    if (mode === 'bulk') {
        btnSingle.classList.remove('active');
        btnBulk.classList.add('active');
        if (secSingle) secSingle.classList.add('d-none');
        if (secBulk) secBulk.classList.remove('d-none');
        if (actionInput) actionInput.value = 'bulkAssignShift';
        if (workDateInput) workDateInput.removeAttribute('required');
        if (startDateInput) startDateInput.setAttribute('required', 'required');
        if (endDateInput) endDateInput.setAttribute('required', 'required');
    } else {
        btnBulk.classList.remove('active');
        btnSingle.classList.add('active');
        if (secBulk) secBulk.classList.add('d-none');
        if (secSingle) secSingle.classList.remove('d-none');
        if (actionInput) actionInput.value = 'assignShift';
        if (workDateInput) workDateInput.setAttribute('required', 'required');
        if (startDateInput) startDateInput.removeAttribute('required');
        if (endDateInput) endDateInput.removeAttribute('required');
    }
}

function selectWeekdays(type) {
    const checkboxes = document.querySelectorAll('.day-cb');
    checkboxes.forEach(cb => {
        const val = parseInt(cb.value, 10);
        if (type === 'all') {
            cb.checked = true;
        } else if (type === 'workdays') {
            cb.checked = (val >= 1 && val <= 5);
        }
    });
}

function openOtReasonModal() {
    const modal = document.getElementById('otReasonModal');
    if (modal) {
        modal.classList.remove('d-none');
        const input = document.getElementById('otReasonInput');
        if (input) {
            input.value = '';
            input.focus();
        }
    }
}

function closeOtReasonModal() {
    const modal = document.getElementById('otReasonModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// ================= NOTIFICATION & BROADCAST (TASK 11) =================
let __latestNotifications = [];

function initNotificationPolling() {
    const notifBell = document.getElementById('btnNotificationBell');
    if (!notifBell) return;

    // Initial fetch
    pollNotifications();

    // Polling every 30 seconds
    setInterval(pollNotifications, 30000);
}

function pollNotifications() {
    const badge = document.getElementById('unreadNotifBadge');
    if (!badge) return;

    fetch('NotificationController?action=getUnread', { cache: 'no-store' })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (!data || !data.success) return;
            const count = data.unreadCount || 0;
            updateNotificationBadge(count);
        })
        .catch(function (err) {
            // Silently catch background polling errors
        });
}

function updateNotificationBadge(count) {
    const badge = document.getElementById('unreadNotifBadge');
    if (!badge) return;

    if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.classList.remove('d-none');
        badge.classList.add('alert-badge-pulse');
    } else {
        badge.textContent = '0';
        badge.classList.add('d-none');
        badge.classList.remove('alert-badge-pulse');
    }
}

function toggleNotificationDropdown(event) {
    if (event) {
        event.stopPropagation();
    }
    const dropdown = document.getElementById('notificationDropdown');
    if (!dropdown) return;

    const isHidden = dropdown.classList.contains('d-none');
    if (isHidden) {
        dropdown.classList.remove('d-none');
        loadRecentNotifications();
    } else {
        dropdown.classList.add('d-none');
    }
}

function loadRecentNotifications() {
    const loading = document.getElementById('notificationLoading');
    const emptyState = document.getElementById('notificationEmptyState');
    const list = document.getElementById('notificationItemsList');
    if (!list) return;

    if (loading) loading.classList.remove('d-none');
    if (emptyState) emptyState.classList.add('d-none');
    list.innerHTML = '';

    fetch('NotificationController?action=getRecent&limit=10', { cache: 'no-store' })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (loading) loading.classList.add('d-none');
            if (!data || !data.success) {
                if (emptyState) emptyState.classList.remove('d-none');
                return;
            }

            const notifs = data.notifications || [];
            __latestNotifications = notifs;
            updateNotificationBadge(data.unreadCount || 0);

            if (notifs.length === 0) {
                if (emptyState) emptyState.classList.remove('d-none');
                return;
            }

            if (emptyState) emptyState.classList.add('d-none');
            renderNotificationList(notifs);
        })
        .catch(function (err) {
            if (loading) loading.classList.add('d-none');
            if (emptyState) emptyState.classList.remove('d-none');
        });
}

function renderNotificationList(notifications) {
    const list = document.getElementById('notificationItemsList');
    if (!list) return;
    list.innerHTML = '';

    notifications.forEach(function (n) {
        const li = document.createElement('li');
        li.className = 'notification-item' + (n.isRead ? '' : ' unread');
        li.id = 'notifItem_' + n.notificationId;

        const icon = '<i class="fi fi-rr-' + escapeHtml(n.icon || 'bell') + '" aria-hidden="true"></i>';
        const markReadHtml = n.isRead ? '' : '<button type="button" class="btn-mark-read-item" onclick="event.stopPropagation(); markNotificationAsRead(' + n.notificationId + ')">&#10003;</button>';
        const unreadDot = n.isRead ? '' : '<span class="notification-unread-dot"></span>';

        li.innerHTML = '<div class="notification-icon-box">' + icon + '</div>'
            + '<div class="notification-content-box">'
            + '  <div class="notification-message">' + escapeHtml(n.message) + '</div>'
            + '  <div class="notification-meta">'
            + '    <span>' + escapeHtml(n.createdAt) + '</span>'
            + '    <div>' + markReadHtml + '</div>'
            + '  </div>'
            + '</div>'
            + unreadDot;

        if (!n.isRead) {
            li.addEventListener('click', function () {
                markNotificationAsRead(n.notificationId);
            });
        }

        list.appendChild(li);
    });
}

function markNotificationAsRead(notificationId) {
    if (!notificationId) return;

    fetch('NotificationController', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=markAsRead&notificationId=' + encodeURIComponent(notificationId)
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        if (data && data.success) {
            // 1. Cập nhật số lượng thông báo chưa đọc trên Badge chuông
            if (data.unreadCount !== undefined) {
                updateNotificationBadge(data.unreadCount);
            }

            // 2. Xóa thông báo khỏi Dropdown Header và hiển thị Empty State nếu danh sách trống
            const item = document.getElementById('notifItem_' + notificationId);
            if (item) {
                item.remove();
            }
            const list = document.getElementById('notificationItemsList');
            const emptyState = document.getElementById('notificationEmptyState');
            if (list && list.children.length === 0 && emptyState) {
                emptyState.classList.remove('d-none');
            }

            // 3. Đồng bộ giao diện bảng tin trên Dashboard Feed (index.jsp): gỡ class unread và dấu chấm đỏ
            const feedItem = document.getElementById('feedItem_' + notificationId);
            if (feedItem) {
                feedItem.classList.remove('unread');
                const feedDot = feedItem.querySelector('.notification-unread-dot');
                if (feedDot) feedDot.remove();
            }
        }
    })
    .catch(function (err) {
        // Silently ignore
    });
}

function markAllNotificationsAsRead() {
    fetch('NotificationController', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'action=markAllAsRead'
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        if (data && data.success) {
            // 1. Cập nhật Badge chuông về 0 (ẩn)
            updateNotificationBadge(0);

            // 2. Xóa toàn bộ danh sách trong Dropdown và hiển thị Empty State
            const list = document.getElementById('notificationItemsList');
            if (list) {
                list.innerHTML = '';
            }
            const emptyState = document.getElementById('notificationEmptyState');
            if (emptyState) {
                emptyState.classList.remove('d-none');
            }

            // 3. Đồng bộ toàn bộ các tin trên Dashboard Feed (index.jsp) sang trạng thái đã đọc
            const feedItems = document.querySelectorAll('.feed-item.unread');
            feedItems.forEach(function (el) {
                el.classList.remove('unread');
                const dot = el.querySelector('.notification-unread-dot');
                if (dot) dot.remove();
            });
        }
    })
    .catch(function (err) {
        // Silently ignore
    });
}

// ================= BROADCAST MODAL HANDLERS =================
function openBroadcastModal() {
    const modal = document.getElementById('broadcastModal');
    if (!modal) return;
    modal.classList.remove('d-none');
    const input = document.getElementById('broadcastMessageInput');
    if (input) {
        input.value = '';
        input.focus();
    }
}

function closeBroadcastModal() {
    const modal = document.getElementById('broadcastModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

// ================= BANNED ACCOUNT POPUP =================
function closeBannedPopup() {
    const popup = document.getElementById('bannedPopup');
    if (popup) {
        popup.style.display = 'none';
    }
}


document.addEventListener('DOMContentLoaded', function () {
    const btnCloseBanned = document.getElementById('btnCloseBannedPopup');
    if (btnCloseBanned) {
        btnCloseBanned.addEventListener('click', closeBannedPopup);
    }
});


// ================= SHIPPER & DELIVERY DELEGATED EVENT LISTENERS =================
document.addEventListener('click', function (e) {
    const btnComplete = e.target.closest('.btn-open-complete-modal');
    if (btnComplete) {
        openShipperCompleteModal(btnComplete.dataset.taskId, btnComplete.dataset.orderCode);
        return;
    }
    const btnFail = e.target.closest('.btn-open-fail-modal');
    if (btnFail) {
        openShipperFailModal(btnFail.dataset.taskId, btnFail.dataset.orderCode);
        return;
    }
    const btnCloseComplete = e.target.closest('.btn-close-complete-modal');
    if (btnCloseComplete) {
        closeShipperCompleteModal();
        return;
    }
    const btnCloseFail = e.target.closest('.btn-close-fail-modal');
    if (btnCloseFail) {
        closeShipperFailModal();
        return;
    }
    const btnDeliveryBack = e.target.closest('#btnDeliveryBack');
    if (btnDeliveryBack) {
        window.history.back();
        return;
    }
    const menuFilterBtn = e.target.closest('[data-menu-filter]');
    if (menuFilterBtn) {
        filterMenuByCategory(menuFilterBtn);
        return;
    }
    const pickIngredientBtn = e.target.closest('[data-action="pick-ingredient"]');
    if (pickIngredientBtn) {
        pickIngredient(pickIngredientBtn);
        return;
    }
    const orderTypeBtn = e.target.closest('[data-order-filter]');
    if (orderTypeBtn) {
        filterKanbanByType(orderTypeBtn);
        return;
    }
    const tabBtn = e.target.closest('[data-tab]');
    if (tabBtn) {
        const tabId = tabBtn.getAttribute('data-tab');
        if (tabId && typeof openTab === 'function') {
            openTab(e, tabId);
            return;
        }
    }
    const btnOpenCheckoutModal = e.target.closest('#btnOpenCheckoutModal');
    if (btnOpenCheckoutModal) {
        if (typeof openOrderConfirmModal === 'function') {
            openOrderConfirmModal();
        }
        return;
    }
    const btnCloseOrderConfirm = e.target.closest('.btn-close-order-confirm');
    if (btnCloseOrderConfirm) {
        if (typeof closeOrderConfirmModal === 'function') {
            closeOrderConfirmModal();
        }
        return;
    }
    const btnOpenStaffCancel = e.target.closest('.btn-open-staff-cancel');
    if (btnOpenStaffCancel) {
        const orderId = btnOpenStaffCancel.getAttribute('data-order-id');
        const tabKey = btnOpenStaffCancel.getAttribute('data-tab-key');
        if (typeof openStaffCancelModal === 'function') {
            openStaffCancelModal(orderId, tabKey);
        }
        return;
    }

    // ================= HR & SHIFT MANAGEMENT EVENT DELEGATION =================
    const btnDismissAlert = e.target.closest('[data-dismiss="alert"]');
    if (btnDismissAlert) {
        const alertBox = btnDismissAlert.closest('.alert-box');
        if (alertBox) alertBox.remove();
        return;
    }
    const hrTabBtn = e.target.closest('[data-hr-tab]');
    if (hrTabBtn) {
        const targetId = hrTabBtn.getAttribute('data-hr-tab');
        const parent = hrTabBtn.closest('.page-container') || document;
        parent.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        parent.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
            content.classList.add('d-none');
        });
        hrTabBtn.classList.add('active');
        const targetContent = document.getElementById(targetId);
        if (targetContent) {
            targetContent.classList.remove('d-none');
            targetContent.classList.add('active');
        }
        return;
    }
    const btnCreateShift = e.target.closest('#btnOpenCreateShift');
    if (btnCreateShift) {
        if (typeof openCreateShiftModal === 'function') {
            openCreateShiftModal();
        }
        return;
    }
    const btnEditShift = e.target.closest('[data-action="edit-shift"]');
    if (btnEditShift) {
        const shiftId = btnEditShift.getAttribute('data-shift-id');
        const shiftName = btnEditShift.getAttribute('data-shift-name');
        const startTime = btnEditShift.getAttribute('data-start-time');
        const endTime = btnEditShift.getAttribute('data-end-time');
        if (typeof openEditShiftModal === 'function') {
            openEditShiftModal(shiftId, shiftName, startTime, endTime);
        }
        return;
    }
    const btnOpenAssign = e.target.closest('.btn-open-assign-shift');
    if (btnOpenAssign) {
        if (typeof openAssignShiftModal === 'function') {
            openAssignShiftModal();
        }
        return;
    }
    const btnQuickAssign = e.target.closest('[data-action="quick-assign"]');
    if (btnQuickAssign) {
        const userId = btnQuickAssign.getAttribute('data-user-id');
        const fullName = btnQuickAssign.getAttribute('data-full-name');
        const workDate = btnQuickAssign.getAttribute('data-work-date');
        if (typeof openQuickAssignModal === 'function') {
            openQuickAssignModal(userId, fullName, workDate);
        }
        return;
    }
    const btnCloseModal = e.target.closest('[data-close-modal]');
    if (btnCloseModal) {
        const modalId = btnCloseModal.getAttribute('data-close-modal');
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('d-none');
        }
        return;
    }
    const elWithConfirm = e.target.closest('[data-confirm]');
    if (elWithConfirm && elWithConfirm.tagName !== 'FORM') {
        const confirmMsg = elWithConfirm.getAttribute('data-confirm') || 'Are you sure?';
        if (!confirm(confirmMsg)) {
            e.preventDefault();
            e.stopPropagation();
            return;
        }
    }

    if (e.target.classList && e.target.classList.contains('modal-overlay')) {
        e.target.classList.add('d-none');
        return;
    }

    // ================= ARCHITECTURAL PURGE & EVENT DELEGATION (TASK 27.1) =================
    const btnAction = e.target.closest('[data-action]');
    if (btnAction) {
        const action = btnAction.getAttribute('data-action');
        if (action === 'open-inventory-category') {
            if (typeof openInventoryCategoryModal === 'function') openInventoryCategoryModal();
            return;
        }
        if (action === 'open-create-ingredient') {
            if (typeof openCreateIngredientModal === 'function') openCreateIngredientModal();
            return;
        }
        if (action === 'edit-ingredient') {
            const id = btnAction.getAttribute('data-id');
            const catId = btnAction.getAttribute('data-cat-id');
            const name = btnAction.getAttribute('data-name');
            const unit = btnAction.getAttribute('data-unit');
            const qty = btnAction.getAttribute('data-qty');
            const price = btnAction.getAttribute('data-price');
            const loc = btnAction.getAttribute('data-loc');
            const threshold = btnAction.getAttribute('data-threshold');
            if (typeof openEditIngredientModal === 'function') {
                openEditIngredientModal(id, catId, name, unit, qty, price, loc, threshold);
            }
            return;
        }
        if (action === 'open-threshold') {
            const id = btnAction.getAttribute('data-id');
            const name = btnAction.getAttribute('data-name');
            const threshold = btnAction.getAttribute('data-threshold');
            if (typeof openThresholdModal === 'function') {
                openThresholdModal(id, name, threshold);
            }
            return;
        }
        if (action === 'open-broadcast') {
            if (typeof openBroadcastModal === 'function') openBroadcastModal();
            return;
        }
        if (action === 'open-inventory-alert') {
            if (typeof openInventoryAlertModal === 'function') openInventoryAlertModal();
            return;
        }
        if (action === 'toggle-notification-dropdown') {
            if (typeof toggleNotificationDropdown === 'function') toggleNotificationDropdown(e);
            return;
        }
        if (action === 'open-menu-category') {
            if (typeof openMenuCategoryModal === 'function') openMenuCategoryModal();
            return;
        }
        if (action === 'open-create-product') {
            if (typeof openCreateProductModal === 'function') openCreateProductModal();
            fillProductDisplayFields('', '', false, true);
            return;
        }
        if (action === 'edit-product') {
            const id = btnAction.getAttribute('data-id');
            const name = btnAction.getAttribute('data-name');
            const catId = btnAction.getAttribute('data-cat-id');
            const price = btnAction.getAttribute('data-price');
            const img = btnAction.getAttribute('data-img');
            const avail = btnAction.getAttribute('data-avail') === 'true';
            const recipe = btnAction.getAttribute('data-recipe');
            if (typeof openEditProductModal === 'function') {
                openEditProductModal(id, name, catId, price, img, avail, recipe);
            }
            fillProductDisplayFields(btnAction.getAttribute('data-desc'), btnAction.getAttribute('data-video'),
                    btnAction.getAttribute('data-featured') === 'true', btnAction.getAttribute('data-has-options') !== 'false');
            return;
        }
        if (action === 'open-menu-options') {
            const optModal = document.getElementById('menuOptionModal');
            if (optModal) optModal.classList.remove('d-none');
            return;
        }
        if (action === 'switch-image-tab') {
            const tab = btnAction.getAttribute('data-image-tab');
            if (typeof switchImageTab === 'function') {
                switchImageTab(tab);
            }
            return;
        }
        if (action === 'add-recipe-row') {
            if (typeof addRecipeRow === 'function') {
                addRecipeRow();
            }
            return;
        }
        if (action === 'confirm-revert-delivery') {
            if (typeof confirmRevertDeliverySubmit === 'function') {
                confirmRevertDeliverySubmit();
            }
            return;
        }
        if (action === 'mark-all-notifications') {
            if (typeof markAllNotificationsAsRead === 'function') {
                markAllNotificationsAsRead();
            }
            return;
        }
        if (action === 'mark-notification') {
            const notifId = btnAction.getAttribute('data-notif-id');
            if (typeof markNotificationAsRead === 'function') {
                markNotificationAsRead(notifId);
            }
            return;
        }
    }
    const btnAssignMode = e.target.closest('[data-assign-mode]');
    if (btnAssignMode) {
        const mode = btnAssignMode.getAttribute('data-assign-mode');
        if (typeof setAssignMode === 'function') {
            setAssignMode(mode);
        }
        return;
    }
    const btnQuickSelect = e.target.closest('[data-quick-select]');
    if (btnQuickSelect) {
        const type = btnQuickSelect.getAttribute('data-quick-select');
        if (typeof selectWeekdays === 'function') {
            selectWeekdays(type);
        }
        return;
    }
    const btnOpenOt = e.target.closest('#btnOpenOtModal');
    if (btnOpenOt) {
        if (typeof openOtReasonModal === 'function') {
            openOtReasonModal();
        }
        return;
    }

});

document.addEventListener('submit', function (e) {
    const formWithConfirm = e.target.closest('form[data-confirm]');
    if (formWithConfirm) {
        const confirmMsg = formWithConfirm.getAttribute('data-confirm') || 'Are you sure?';
        if (!confirm(confirmMsg)) {
            e.preventDefault();
            return;
        }
    }
    const formReject = e.target.closest('.form-reject-task');
    if (formReject) {
        const confirmMsg = formReject.dataset.confirmMsg || 'Are you sure?';
        if (!confirm(confirmMsg)) {
            e.preventDefault();
            return;
        }
    }
    const orderStatusForm = e.target.closest('.order-status-form');
    if (orderStatusForm) {
        const orderId = orderStatusForm.getAttribute('data-order-id');
        if (typeof handleStaffStatusSubmit === 'function') {
            const res = handleStaffStatusSubmit(e, orderStatusForm, orderId);
            if (res === false) {
                e.preventDefault();
                return;
            }
        }
    }
    const formCancelTable = e.target.closest('.form-cancel-table-order');
    if (formCancelTable) {
        const confirmMsg = formCancelTable.getAttribute('data-confirm-msg') || 'Are you sure?';
        if (!confirm(confirmMsg)) {
            e.preventDefault();
            return;
        }
    }
    const formRevoke = e.target.closest('.form-revoke-task');
    if (formRevoke) {
        const confirmMsg = formRevoke.getAttribute('data-confirm-msg') || 'Are you sure you want to revoke this order?';
        if (!confirm(confirmMsg)) {
            e.preventDefault();
            return;
        }
    }
});

document.addEventListener('change', function (e) {
    if (e.target && e.target.getAttribute('data-auto-submit') === 'true') {
        if (e.target.form) {
            e.target.form.submit();
        }
        return;
    }
    if (e.target && e.target.getAttribute('data-action') === 'preview-uploaded-image') {
        if (typeof previewUploadedImage === 'function') {
            previewUploadedImage(e.target);
        }
        return;
    }
    if (e.target && e.target.id === 'presetFailReason') {
        const val = e.target.value;
        if (val) {
            const reasonText = document.getElementById('failReason');
            if (reasonText) {
                reasonText.value = val;
            }
        }
    }
});

document.addEventListener('input', function (e) {
    if (e.target && e.target.getAttribute('data-action') === 'preview-url-image') {
        if (typeof previewUrlImage === 'function') {
            previewUrlImage(e.target.value);
        }
        return;
    }
});

// ===== Màn đặt món: lọc món theo nhóm (menu.jsp, thanh .menu-cats) =====
function filterMenuByCategory(selectedBtn) {
    const filter = selectedBtn.getAttribute('data-menu-filter');
    document.querySelectorAll('.menu-cats [data-menu-filter]').forEach(function (btn) {
        btn.classList.toggle('active', btn === selectedBtn);
    });
    document.querySelectorAll('.menu-grid .product-card').forEach(function (card) {
        let matched = filter === 'all' || card.getAttribute('data-category') === filter;
        if (filter === 'best') {
            matched = card.getAttribute('data-bestseller') === 'true';
        }
        card.classList.toggle('d-none', !matched);
    });
}

// ===== Màn đặt món: popup chi tiết món (size, đá, đường, topping, ghi chú, số lượng) =====
function openProductOptionModal(card) {
    const modal = document.getElementById('productOptionModal');
    const form = document.getElementById('productOptionForm');
    if (!modal || !form || !card) return;

    form.reset();
    document.getElementById('optModalProductId').value = card.getAttribute('data-product-id');
    document.getElementById('optModalBuyNow').value = 'false';
    document.getElementById('optModalQty').value = '1';
    modal.setAttribute('data-base-price', card.getAttribute('data-price') || '0');

    const name = card.getAttribute('data-name') || '';
    document.getElementById('optModalName').textContent = name;

    const img = document.getElementById('optModalImg');
    const letter = document.getElementById('optModalLetter');
    const imageSrc = card.getAttribute('data-image');
    if (imageSrc) {
        img.src = imageSrc;
        img.alt = name;
        img.classList.remove('d-none');
        letter.classList.add('d-none');
    } else {
        img.classList.add('d-none');
        letter.textContent = name.charAt(0);
        letter.classList.remove('d-none');
    }
    modal.querySelector('.option-media').classList.remove('is-zoomed');
    modal.querySelector('.option-scroll').scrollTop = 0;

    const desc = document.getElementById('optModalDesc');
    const descText = card.getAttribute('data-desc');
    desc.textContent = descText || '';
    desc.classList.toggle('d-none', !descText);

    const video = document.getElementById('optModalVideo');
    const videoUrl = card.getAttribute('data-video');
    const safeVideo = videoUrl && /^https?:\/\//i.test(videoUrl);
    video.href = safeVideo ? videoUrl : '#';
    video.classList.toggle('d-none', !safeVideo);

    // Món không có tuỳ chọn (ví dụ kem): ẩn và vô hiệu hoá phần size/đá/đường/topping
    const hasOptions = card.getAttribute('data-has-options') !== 'false';
    const choices = document.getElementById('optModalChoices');
    choices.classList.toggle('d-none', !hasOptions);
    choices.querySelectorAll('input').forEach(function (input) {
        input.disabled = !hasOptions;
    });

    updateProductOptionTotal();
    modal.classList.remove('d-none');
}

function updateProductOptionTotal() {
    const modal = document.getElementById('productOptionModal');
    const form = document.getElementById('productOptionForm');
    if (!modal || !form) return;
    let unit = Number(modal.getAttribute('data-base-price')) || 0;
    form.querySelectorAll('input[data-extra]:checked:not(:disabled)').forEach(function (input) {
        unit += Number(input.getAttribute('data-extra')) || 0;
    });
    const qty = Number(document.getElementById('optModalQty').value) || 1;
    // Định dạng số theo ngôn ngữ đang chọn để khớp với giá size/topping do fmt:formatNumber in ra
    const numberLocale = form.getAttribute('data-locale') === 'en' ? 'en-US' : 'vi-VN';
    document.getElementById('optModalTotal').textContent = Math.round(unit * qty).toLocaleString(numberLocale);
}

function changeProductOptionQty(step) {
    const qtyInput = document.getElementById('optModalQty');
    if (!qtyInput) return;
    const next = Math.min(99, Math.max(1, (Number(qtyInput.value) || 1) + step));
    qtyInput.value = String(next);
    updateProductOptionTotal();
}

// ===== Quản lý thực đơn: điền mô tả, video, nổi bật, có tuỳ chọn vào form món =====
function fillProductDisplayFields(description, videoUrl, isFeatured, hasOptions) {
    const descEl = document.getElementById('productDescInput');
    const videoEl = document.getElementById('productVideoInput');
    const featuredEl = document.getElementById('productFeaturedCheckbox');
    const optionsEl = document.getElementById('productHasOptionsCheckbox');
    if (descEl) descEl.value = (description && description !== 'null') ? description : '';
    if (videoEl) videoEl.value = (videoUrl && videoUrl !== 'null') ? videoUrl : '';
    if (featuredEl) featuredEl.checked = isFeatured === true;
    if (optionsEl) optionsEl.checked = hasOptions !== false;
}

document.addEventListener('click', function (e) {
    const openBtn = e.target.closest('[data-action="open-product-option"]');
    if (openBtn) {
        openProductOptionModal(openBtn.closest('.product-card'));
        return;
    }
    const zoomBox = e.target.closest('[data-action="toggle-option-zoom"]');
    if (zoomBox) {
        zoomBox.classList.toggle('is-zoomed');
        return;
    }
    const qtyBtn = e.target.closest('[data-action="opt-qty"]');
    if (qtyBtn) {
        changeProductOptionQty(Number(qtyBtn.getAttribute('data-step')) || 0);
        return;
    }
    const buyNowBtn = e.target.closest('[data-action="opt-buy-now"]');
    if (buyNowBtn) {
        const form = document.getElementById('productOptionForm');
        document.getElementById('optModalBuyNow').value = 'true';
        if (form.reportValidity()) {
            form.submit();
        }
    }
});

document.addEventListener('change', function (e) {
    if (e.target && e.target.closest && e.target.closest('#productOptionForm')) {
        updateProductOptionTotal();
    }
});

document.addEventListener('DOMContentLoaded', function () {
    // Vừa bấm "Thanh toán ngay" trong popup món: mở luôn hộp xác nhận đặt món
    const orderPage = document.querySelector('[data-open-checkout="true"]');
    if (orderPage && typeof openOrderConfirmModal === 'function') {
        openOrderConfirmModal();
    }
    // Quay lại trang Quản lý thực đơn sau khi thêm/sửa size, topping: mở lại popup quản lý
    if (new URLSearchParams(window.location.search).get('manageOptions') === 'true') {
        const optionModal = document.getElementById('menuOptionModal');
        if (optionModal) optionModal.classList.remove('d-none');
    }
});

// ===== Quản lý đơn (Kanban): lọc thẻ theo loại đơn và đếm lại số thẻ từng cột =====
function filterKanbanByType(selectedBtn) {
    const type = selectedBtn.getAttribute('data-order-filter');
    document.querySelectorAll('[data-order-filter]').forEach(function (btn) {
        btn.classList.toggle('active', btn === selectedBtn);
    });
    document.querySelectorAll('.kanban-col').forEach(function (col) {
        let visible = 0;
        col.querySelectorAll('.kanban-card').forEach(function (card) {
            const show = type === 'ALL' || card.getAttribute('data-order-type') === type;
            card.classList.toggle('d-none', !show);
            if (show) visible++;
        });
        const counter = col.querySelector('.kanban-count');
        if (counter) counter.textContent = String(visible);
    });
}

// ===== Kho (danh sách + chi tiết): chọn 1 dòng -> chép data-* sang khung chi tiết và các nút thao tác =====
function pickIngredient(row) {
    document.querySelectorAll('[data-action="pick-ingredient"]').forEach(function (r) {
        r.classList.toggle('active', r === row);
    });
    const get = function (name) { return row.getAttribute('data-' + name) || ''; };
    const setText = function (id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    };
    setText('invDName', get('name'));
    setText('invDCat', get('cat-name'));
    setText('invDQty', get('qty') + ' ' + get('unit'));
    setText('invDThreshold', get('threshold') + ' ' + get('unit'));
    setText('invDStatus', get('status-text'));
    setText('invDPrice', Number(get('price') || 0).toLocaleString('vi-VN') + ' \u20AB');
    setText('invDLoc', get('loc'));

    // Nút Sửa / Cập nhật ngưỡng dùng lại handler cũ (edit-ingredient, open-threshold) nên mang đúng data-* của dòng
    ['invDEdit', 'invDThresholdBtn'].forEach(function (id) {
        const btn = document.getElementById(id);
        if (!btn) return;
        ['id', 'cat-id', 'name', 'unit', 'qty', 'price', 'loc', 'threshold'].forEach(function (attr) {
            btn.setAttribute('data-' + attr, get(attr));
        });
    });
    const deleteId = document.getElementById('invDDeleteId');
    if (deleteId) deleteId.value = get('id');

    const empty = document.getElementById('invDetailEmpty');
    const body = document.getElementById('invDetailBody');
    if (empty) empty.classList.add('d-none');
    if (body) body.classList.remove('d-none');
}
