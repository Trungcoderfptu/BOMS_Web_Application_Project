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

    // ================= XỬ LÝ TAB TRANG CÀI ĐẶT =================
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

    // ==================== XỬ LÝ DARK MODE TOÀN CỤC ====================
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

    // ================= XỬ LÝ DROPDOWN NGÔN NGỮ =================
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
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initMainApp);
} else {
    initMainApp();
}
// Hàm chuyển Tab cho các màn hình Dashboard
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
            floatingCart.style.display = 'none'; // Ẩn khi vào tab giỏ hàng
        } else {
            floatingCart.style.display = 'flex'; // Hiện lại khi sang tab khác
        }
    }
}