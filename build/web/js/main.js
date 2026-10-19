function initMainApp() {
    if (window.__bomsMainInitialized) {
        return;
    }
    window.__bomsMainInitialized = true;

    const btnHamburger = document.getElementById('btnHamburger');
    const btnDongMenuDoc = document.getElementById('btnDongMenuDoc');
    const menuDoc = document.getElementById('menuDoc');

    if (btnHamburger) {
        btnHamburger.addEventListener('click', function () {
            menuDoc.style.display = 'block';
        });
    }

    if (btnDongMenuDoc) {
        btnDongMenuDoc.addEventListener('click', function () {
            menuDoc.style.display = 'none';
        });
    }

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
            btnDarkMode.innerHTML = '\u2600\uFE0F';
        } else {
            btnDarkMode.innerHTML = '\uD83C\uDF19';
        }

        btnDarkMode.addEventListener('click', () => {
            body.classList.toggle('dark-theme');
            if (body.classList.contains('dark-theme')) {
                localStorage.setItem('theme', 'dark');
                btnDarkMode.innerHTML = '\u2600\uFE0F';
            } else {
                localStorage.setItem('theme', 'light');
                btnDarkMode.innerHTML = '\uD83C\uDF19';
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

    initCancelCountdowns();
    initInventoryAlertPolling();
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

    if (evt && evt.currentTarget && evt.currentTarget.classList.contains("tab-btn")) {
        evt.currentTarget.classList.add("active");
    } else {
        let targetBtn = document.querySelector(`.tab-btn[onclick*="${tabId}"]`);
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
function openStaffCancelModal(orderId) {
    const modal = document.getElementById('staffCancelModal');
    const orderIdInput = document.getElementById('staffCancelModalOrderId');
    const orderIdText = document.getElementById('staffCancelModalOrderIdText');
    if (modal) {
        if (orderIdInput)
            orderIdInput.value = orderId;
        if (orderIdText)
            orderIdText.textContent = '#' + orderId;
        modal.classList.remove('d-none');
    }
}

function closeStaffCancelModal() {
    const modal = document.getElementById('staffCancelModal');
    if (modal) {
        modal.classList.add('d-none');
    }
}

function handleStaffStatusSubmit(event, form, orderId) {
    const select = form.querySelector("select[name='newStatus']");
    if (select && select.value === 'Cancelled') {
        event.preventDefault();
        openStaffCancelModal(orderId);
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
        if (previewImg) previewImg.src = cleanImageUrl;
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

// Listen for Escape key to close modals
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeProductModal();
        closeInventoryModal();
        closeThresholdModal();
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
            : '<span class="badge-status-inactive">\u26A0\uFE0F Active</span>';

        const actionHtml = isConfirmed
            ? '<span class="text-muted">\u2714\uFE0F ' + escapeHtml(lblBtnConfirmed) + '</span>'
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
