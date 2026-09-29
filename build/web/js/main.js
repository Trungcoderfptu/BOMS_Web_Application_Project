document.addEventListener("DOMContentLoaded", function () {
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

    // ================= XỬ LÝ TAB TRANG CÀI ĐẶT =================
    const btnMenuProfile = document.getElementById('btnMenuProfile');
    const btnMenuPassword = document.getElementById('btnMenuPassword');
    const tabProfile = document.getElementById('tabProfile');
    const tabPassword = document.getElementById('tabPassword');

    // Hàm dùng chung để chuyển tab
    function switchTab(activeTab, hiddenTab) {
        if (activeTab && hiddenTab) {
            activeTab.style.display = 'block';
            hiddenTab.style.display = 'none';
        }
    }

    if (btnMenuProfile) {
        btnMenuProfile.addEventListener('click', function () {
            switchTab(tabProfile, tabPassword);
        });
    }

    if (btnMenuPassword) {
        btnMenuPassword.addEventListener('click', function () {
            switchTab(tabPassword, tabProfile);
        });
    }
});