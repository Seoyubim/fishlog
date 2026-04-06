const API_BASE = '/api/v1';

/* ── 인증 상태 관리 ─────────────────────────────── */
const Auth = {
    getToken: () => localStorage.getItem('fishlog_token'),
    getUser:  () => JSON.parse(localStorage.getItem('fishlog_user') || 'null'),
    save(token, userId, username) {
        localStorage.setItem('fishlog_token', token);
        localStorage.setItem('fishlog_user', JSON.stringify({ id: userId, username }));
    },
    clear() {
        localStorage.removeItem('fishlog_token');
        localStorage.removeItem('fishlog_user');
    },
    isLoggedIn: () => !!localStorage.getItem('fishlog_token'),
};

/* ── 공통 fetch (JWT 헤더 자동 주입) ─────────────── */
async function apiFetch(path, options = {}) {
    const token = Auth.getToken();
    const headers = { ...(options.headers || {}) };

    if (!(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    let res;
    try {
        res = await fetch(API_BASE + path, { ...options, headers });
    } catch (e) {
        throw new Error('서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.');
    }

    if (res.status === 401) {
        Auth.clear();
        window.location.href = '/login';
        return null;
    }

    return res;
}

/* ── 인증 필요 페이지 가드 ───────────────────────── */
function requireAuth() {
    if (!Auth.isLoggedIn()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

/* ── 로그아웃 ───────────────────────────────────── */
function logout() {
    if (!confirm('로그아웃 하시겠습니까?')) return;
    Auth.clear();
    window.location.href = '/login';
}

/* ── 유틸 ───────────────────────────────────────── */
function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#x27;');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${y}년 ${parseInt(m)}월 ${parseInt(d)}일`;
}

function showToast(msg, type = 'success') {
    const toast = document.createElement('div');
    const color = type === 'success' ? 'bg-teal-700' : 'bg-red-600';
    toast.className = `fixed top-4 left-1/2 -translate-x-1/2 z-[100] px-5 py-3 rounded-2xl text-white font-bold text-base shadow-xl ${color} transition-all`;
    toast.textContent = msg;
    document.body.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 400); }, 2500);
}
