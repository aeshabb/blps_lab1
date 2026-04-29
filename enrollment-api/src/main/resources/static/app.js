document.addEventListener('DOMContentLoaded', () => {
    let programsData = [];
    let currentAppId = null;

    const queryParams = new URLSearchParams(window.location.search);

    const authSection = document.getElementById('auth-section');
    const mainContent = document.getElementById('main-content');
    const userInfoContainer = document.getElementById('user-info-container');
    const userInfoBox = document.getElementById('user-info');
    const logoutBtn = document.getElementById('logoutBtn');
    const loginForm = document.getElementById('login-form');
    const loginError = document.getElementById('loginError');

    const adminSection = document.getElementById('admin-section');
    const tutorSection = document.getElementById('tutor-section');
    const step1 = document.getElementById('step-1');
    const step2 = document.getElementById('step-2');

    const addProgramForm = document.getElementById('add-program-form');
    const tutorAppIdInput = document.getElementById('tutorAppId');
    const loadAppBtn = document.getElementById('loadAppBtn');
    const approveAppBtn = document.getElementById('approveAppBtn');
    const tutorAppDetails = document.getElementById('tutorAppDetails');
    const tutorResultBox = document.getElementById('tutorResultBox');

    const programSelect = document.getElementById('programSelect');
    const tariffSelect = document.getElementById('tariffSelect');
    const tariffWrapper = document.getElementById('tariffWrapper');
    const form = document.getElementById('application-form');
    const paymentLinkBtn = document.getElementById('paymentLinkBtn');

    function checkAuth() {
        return fetch('/api/auth/me')
            .then(res => {
                if (!res.ok) throw new Error("Not auth");
                return res.json();
            })
            .then(authorities => {
                const validAuths = authorities.filter(a => a !== 'ROLE_ANONYMOUS');
                if (validAuths.length > 0) {
                    showMainView(validAuths);
                    loadPrograms();
                    return validAuths;
                } else {
                    showLoginView();
                    return [];
                }
            })
            .catch(() => {
                showLoginView();
                return [];
            });
    }

    function showMainView(authorities) {
        authSection.style.display = 'none';
        mainContent.style.display = 'block';
        userInfoContainer.style.display = 'block';
        userInfoBox.innerText = 'Права: ' + authorities.join(', ');

        adminSection.style.display = authorities.includes('WRITE_COURSES') ? 'block' : 'none';
        tutorSection.style.display = authorities.includes('MANAGE_APPLICATIONS') ? 'block' : 'none';
        step1.style.display = authorities.includes('APPLY_COURSE') ? 'block' : 'none';

        if (authorities.includes('MANAGE_APPLICATIONS')) {
            loadApplications();
        }
    }

    function showLoginView() {
        authSection.style.display = 'block';
        mainContent.style.display = 'none';
        userInfoContainer.style.display = 'none';
    }

    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const username = document.getElementById('loginUsername').value;
            const pass = document.getElementById('loginPassword').value;
            const btn = loginForm.querySelector('button[type="submit"]');
            
            btn.disabled = true;
            btn.innerText = 'Вход...';
            loginError.classList.add('d-none');

            const formData = new URLSearchParams();
            formData.append('username', username);
            formData.append('password', pass);

            fetch('/api/auth/login', { 
                method: 'POST',
                body: formData
            })
            .then(res => {
                if (res.ok) {
                    window.location.href = '/';
                } else {
                    throw new Error("Invalid credentials");
                }
            })
            .catch(err => {
                loginError.classList.remove('d-none');
                btn.disabled = false;
                btn.innerText = 'Войти';
            });
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            fetch('/api/auth/logout', { method: 'POST' })
            .finally(() => {
                window.location.href = '/';
            });
        });
    }

    function loadPrograms() {
        fetch('/api/programs')
            .then(res => res.json())
            .then(data => {
                programsData = data;
                if (programSelect) {
                    programSelect.innerHTML = '<option value="">Выберите программу...</option>';
                    data.forEach(prog => {
                        programSelect.innerHTML += `<option value="${prog.id}">${prog.title}</option>`;
                    });
                    tariffWrapper.style.display = 'block';
                }

                const adminList = document.getElementById('adminProgramsList');
                if (adminList) {
                    adminList.innerHTML = '';
                    data.forEach(prog => {
                        adminList.innerHTML += `<li class="list-group-item d-flex justify-content-between align-items-center">
                            <div><strong>${prog.title}</strong><br><small class="text-muted text-break">${prog.description || 'Нет описания'}</small></div>
                            <span class="badge bg-primary rounded-pill flex-shrink-0 ms-3">ID: ${prog.id}</span>
                        </li>`;
                    });
                }
            })
            .catch(err => console.error("Ошибка загрузки программ: " + err));
    }

    if (programSelect) {
        programSelect.addEventListener('change', (e) => {
            const progId = parseInt(e.target.value);
            const prog = programsData.find(p => p.id === progId);
            if (prog) {
                tariffSelect.innerHTML = '<option value="">Выберите тариф...</option>';
                prog.tariffs.forEach(t => {
                    tariffSelect.innerHTML += `<option value="${t.id}">${t.name} — ${t.price} ₽</option>`;
                });
            }
        });
    }

    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            
            const progId = parseInt(programSelect.value);
            const tarId = parseInt(tariffSelect.value);
            const prog = programsData.find(p => p.id === progId);
            const tariff = prog.tariffs.find(t => t.id === tarId);

            const payload = {
                programId: progId,
                tariffId: tarId,
                userName: document.getElementById('userName').value,
                userEmail: document.getElementById('userEmail').value
            };

            fetch('/api/applications', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => {
                if (!res.ok) throw new Error("Status " + res.status);
                return res.json();
            })
            .then(data => {
                currentAppId = data.id;
                step1.classList.remove('active');
                step2.classList.add('active');
                
                document.getElementById('displayAppId').innerText = `#${data.id}`;
                document.getElementById('displayStatus').innerText = data.status;
                document.getElementById('displayAmount').innerText = tariff.price;
                paymentLinkBtn.href = normalizePaymentLink(data.paymentLink);
            })
            .catch(err => alert("Ошибка создания заявки: " + err.message));
        });
    }

    if (addProgramForm) {
        addProgramForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const btn = e.target.querySelector('button');
            btn.disabled = true;
            
            const title = document.getElementById('newProgramTitle').value;
            const desc = document.getElementById('newProgramDesc').value;
            const courseId = document.getElementById('newProgramCourseId').value;
            
            fetch('/api/programs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: null, title: title, description: desc, openEdxCourseId: courseId, tariffs: [] })
            })
            .then(res => {
                if(!res.ok) throw new Error(res.status);
                return res.json();
            })
            .then(data => {
                const resBox = document.getElementById('addProgramResult');
                resBox.classList.remove('d-none', 'alert-danger');
                resBox.classList.add('alert-success');
                resBox.innerText = `Успех! Программа ${data.title} добавлена!`;
                loadPrograms();
            })
            .catch(err => {
                const resBox = document.getElementById('addProgramResult');
                resBox.classList.remove('d-none', 'alert-success');
                resBox.classList.add('alert-danger');
                resBox.innerText = `Ошибка: ` + err.message;
            })
            .finally(() => btn.disabled = false);
        });
    }

    let currentTutorAppId = null;

    const tutorAppsList = document.getElementById('tutorAppsList');
    const refreshAppsBtn = document.getElementById('refreshAppsBtn');

    function loadApplications() {
        if (!tutorAppsList) return;
        fetch('/api/applications')
            .then(res => {
                if(!res.ok) throw new Error(res.status);
                return res.json();
            })
            .then(data => {
                tutorAppsList.innerHTML = '';
                if(data.length === 0) {
                    tutorAppsList.innerHTML = '<li class="list-group-item text-muted">Заявок пока нет</li>';
                }
                data.forEach(app => {
                    let badgeClass = 'bg-secondary';
                    if (app.status === 'ENROLLED') badgeClass = 'bg-success';
                    if (app.status === 'PAYMENT_SUCCESS') badgeClass = 'bg-primary';
                    if (app.status === 'PAYMENT_FAILED') badgeClass = 'bg-danger';
                    
                    tutorAppsList.innerHTML += `<li class="list-group-item d-flex justify-content-between align-items-center">
                        Заявка #${app.id}
                        <span class="badge ${badgeClass} rounded-pill">${app.status}</span>
                    </li>`;
                });
            })
            .catch(err => console.error('Ошибка загрузки заявок:', err));
    }

    if (refreshAppsBtn) {
        refreshAppsBtn.addEventListener('click', loadApplications);
    }

    if (loadAppBtn) {
        loadAppBtn.addEventListener('click', () => {
            const id = tutorAppIdInput.value;
            if(!id) return;

            fetch(`/api/applications/${id}`)
            .then(res => {
                if(!res.ok) throw new Error(res.status);
                return res.json();
            })
            .then(data => {
                currentTutorAppId = data.id;
                tutorAppDetails.classList.remove('d-none', 'alert-danger');
                tutorAppDetails.classList.add('alert-info');
                tutorAppDetails.innerHTML = `<strong>Заявка #${data.id}:</strong> Статус - ${data.status}`;
                
                if (data.status !== 'ENROLLED') {
                    approveAppBtn.classList.remove('d-none');
                } else {
                    approveAppBtn.classList.add('d-none');
                }
                tutorResultBox.classList.add('d-none');
            })
            .catch(err => {
                tutorAppDetails.classList.add('d-none');
                approveAppBtn.classList.add('d-none');
                tutorResultBox.classList.remove('d-none', 'alert-success');
                tutorResultBox.classList.add('alert-danger');
                tutorResultBox.innerText = `Ошибка загрузки заявки: ` + err.message;
            });
        });
    }

    if (approveAppBtn) {
        approveAppBtn.addEventListener('click', () => {
            if(!currentTutorAppId) return;

            fetch(`/api/applications/${currentTutorAppId}/approve`, {
                method: 'POST'
            })
            .then(res => {
                if(!res.ok) throw new Error(res.status);
                return res.json();
            })
            .then(data => {
                tutorResultBox.classList.remove('d-none', 'alert-danger');
                tutorResultBox.classList.add('alert-success');
                tutorResultBox.innerText = `Заявка #${data.id} успешно одобрена!`;
                approveAppBtn.classList.add('d-none');
                tutorAppDetails.innerHTML = `<strong>Заявка #${data.id}:</strong> Статус - ${data.status}`;
            })
            .catch(err => {
                tutorResultBox.classList.remove('d-none', 'alert-success');
                tutorResultBox.classList.add('alert-danger');
                tutorResultBox.innerText = `Ошибка одобрения: ` + err.message;
            });
        });
    }

    function normalizePaymentLink(link) {
        if (!link) return '#';
        if (link.startsWith('http://') || link.startsWith('https://')) return link;
        return `${window.location.origin}${link}`;
    }

    function startStatusPolling() {
        const interval = setInterval(() => {
            if (!currentAppId) {
                clearInterval(interval);
                return;
            }

            fetch(`/api/applications/${currentAppId}`)
                .then(res => res.json())
                .then(data => {
                    const status = data.status;
                    document.getElementById('displayStatus').innerText = status;
                    
                    if (status === 'ENROLLED' || status === 'PAYMENT_SUCCESS') {
                        clearInterval(interval);
                        document.getElementById('displayStatus').className = 'text-success fw-bold';
                        document.getElementById('paymentLinkBtn').style.display = 'none';
                    } else if (status === 'PAYMENT_FAILED') {
                        clearInterval(interval);
                        document.getElementById('displayStatus').className = 'text-danger fw-bold';
                        document.getElementById('paymentLinkBtn').style.display = 'none';
                    }
                })
                .catch(err => console.error("Ошибка поллинга", err));

        }, 2000);
    }

    function restoreFromReturn() {
        const returnedAppId = parseInt(queryParams.get('applicationId'));
        if (!returnedAppId) return;

        currentAppId = returnedAppId;
        step1.classList.remove('active');
        step2.classList.add('active');
        document.getElementById('displayAppId').innerText = `#${currentAppId}`;

        fetch(`/api/applications/${currentAppId}`)
            .then(res => res.json())
            .then(data => {
                document.getElementById('displayStatus').innerText = data.status;
                if (data.status === 'PAYMENT_SUCCESS' || data.status === 'ENROLLED') {
                    document.getElementById('displayStatus').className = 'text-success fw-bold';
                    document.getElementById('paymentLinkBtn').style.display = 'none';
                } else if (data.status === 'PAYMENT_FAILED') {
                    document.getElementById('displayStatus').className = 'text-danger fw-bold';
                    document.getElementById('paymentLinkBtn').style.display = 'none';    
                } else {
                    document.getElementById('paymentLinkBtn').href = normalizePaymentLink(data.paymentLink);
                    startStatusPolling();
                }
            })
            .catch(e => console.error('Ошибка получения возвращенной заявки', e));
    }

    checkAuth().then(authorities => {
        if (authorities && authorities.includes('APPLY_COURSE')) {
            restoreFromReturn();
        }
    });
});
