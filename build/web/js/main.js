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
    const btnMenuLanguage = document.getElementById('btnMenuLanguage');

    const tabProfile = document.getElementById('tabProfile');
    const tabPassword = document.getElementById('tabPassword');
    const tabLanguage = document.getElementById('tabLanguage');

    function showTab(activeTab) {
        // Gắn class d-none để ẩn tất cả các tab
        if (tabProfile)
            tabProfile.classList.add('d-none');
        if (tabPassword)
            tabPassword.classList.add('d-none');
        if (tabLanguage)
            tabLanguage.classList.add('d-none');

        // Gỡ class d-none ra khỏi tab đang được click để hiện lên
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

    if (btnMenuLanguage) {
        btnMenuLanguage.addEventListener('click', function () {
            showTab(tabLanguage);
        });
    }
    // ==================== XỬ LÝ DARK MODE TOÀN CỤC ====================
    (function () {
        // 1. Dùng JS tự động tạo nút nổi (Floating Button)
        const btnDarkMode = document.createElement('button');
        btnDarkMode.id = 'btnDarkModeGlobal';
        btnDarkMode.title = 'Giao diện Tối/Sáng';

        // Style cho nút nổi luôn nằm ở góc dưới bên phải màn hình
        btnDarkMode.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 45px;
        height: 45px;
        border-radius: 50%;
        border: 2px solid var(--border-main);
        background-color: var(--bg-tertiary);
        color: var(--text-primary);
        font-size: 20px;
        cursor: pointer;
        z-index: 9999;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        display: flex;
        align-items: center;
        justify-content: center;
    `;

        // Tiêm nút vào thẻ body
        document.body.appendChild(btnDarkMode);

        const body = document.body;

        // 2. Khởi tạo trạng thái ban đầu từ localStorage
        if (localStorage.getItem('theme') === 'dark') {
            body.classList.add('dark-theme');
            btnDarkMode.innerHTML = '☀️';
        } else {
            btnDarkMode.innerHTML = '🌙';
        }

        // 3. Xử lý sự kiện click
        btnDarkMode.addEventListener('click', () => {
            body.classList.toggle('dark-theme');
            if (body.classList.contains('dark-theme')) {
                localStorage.setItem('theme', 'dark');
                btnDarkMode.innerHTML = '☀️';
            } else {
                localStorage.setItem('theme', 'light');
                btnDarkMode.innerHTML = '🌙';
            }
        });
    })();
});