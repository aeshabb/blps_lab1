document.addEventListener('DOMContentLoaded', () => {
    let programsData = [];
    let currentAppId = null;
    const queryParams = new URLSearchParams(window.location.search);

    const programSelect = document.getElementById('programSelect');
    const tariffSelect = document.getElementById('tariffSelect');
    const tariffWrapper = document.getElementById('tariffWrapper');
    const form = document.querySelector('form');
    const paymentLinkBtn = document.getElementById('paymentLinkBtn');
    const openedxProgramSelect = document.getElementById('openedxProgramSelect');
    const openedxForm = document.getElementById('openedx-form');
    const openedxEmailInput = document.getElementById('openedxEmail');
    const openedxResult = document.getElementById('openedxResult');
    const openedxSubmitBtn = document.getElementById('openedxSubmitBtn');

    fetch('/api/programs')
        .then(response => response.json())
        .then(data => {
            programsData = data;
            programSelect.innerHTML = '<option value="">Выберите программу...</option>';
            openedxProgramSelect.innerHTML = '<option value="">Выберите программу...</option>';
            data.forEach(prog => {
                programSelect.innerHTML += `<option value="${prog.id}">${prog.title}</option>`;
                openedxProgramSelect.innerHTML += `<option value="${prog.id}">${prog.title} (${prog.openEdxCourseId})</option>`;
            });
            tariffWrapper.style.display = 'block';
        })
        .catch(err => alert("Ошибка загрузки программ: " + err));

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
        .then(res => res.json())
        .then(data => {
            currentAppId = data.id;
            
            document.getElementById('step-1').classList.remove('active');
            document.getElementById('step-2').classList.add('active');
            
            document.getElementById('displayAppId').innerText = `#${data.id}`;
            document.getElementById('displayStatus').innerText = data.status;
            document.getElementById('displayAmount').innerText = tariff.price;
            paymentLinkBtn.href = normalizePaymentLink(data.paymentLink);
            
            startStatusPolling();
        })
        .catch(err => alert("Ошибка создания заявки: " + err));
    });

    openedxForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const email = openedxEmailInput.value.trim();
        const programId = parseInt(openedxProgramSelect.value);
        const program = programsData.find(p => p.id === programId);

        if (!program || !program.openEdxCourseId) {
            showOpenEdxResult('Выберите программу с корректным courseId.', false);
            return;
        }

        openedxSubmitBtn.disabled = true;
        openedxSubmitBtn.innerText = 'Отправка запроса...';

        fetch('/api/openedx/register-enroll', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email,
                courseId: program.openEdxCourseId
            })
        })
        .then(async (res) => {
            const payload = await res.json();
            if (!res.ok || !payload.success) {
                throw new Error(payload.message || 'Open edX request failed');
            }
            showOpenEdxResult(`Успех: ${payload.message}`, true);
        })
        .catch((err) => {
            showOpenEdxResult(`Ошибка: ${err.message}`, false);
        })
        .finally(() => {
            openedxSubmitBtn.disabled = false;
            openedxSubmitBtn.innerText = 'Зарегистрировать и зачислить';
        });
    });

    function showOpenEdxResult(message, success) {
        openedxResult.classList.remove('d-none', 'alert-success', 'alert-danger');
        openedxResult.classList.add(success ? 'alert-success' : 'alert-danger');
        openedxResult.innerText = message;
    }

    function normalizePaymentLink(link) {
        if (!link) {
            return '#';
        }
        if (link.startsWith('http://') || link.startsWith('https://')) {
            return link;
        }
        return `${window.location.origin}${link}`;
    }

    function restoreFromReturn() {
        const returnedAppId = parseInt(queryParams.get('applicationId'));
        if (!returnedAppId) {
            return;
        }

        currentAppId = returnedAppId;
        document.getElementById('step-1').classList.remove('active');
        document.getElementById('step-3').classList.remove('active');
        document.getElementById('step-4').classList.remove('active');
        document.getElementById('step-2').classList.add('active');
        document.getElementById('displayAppId').innerText = `#${currentAppId}`;

        startStatusPolling();
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
                        document.getElementById('step-2').classList.remove('active');
                        document.getElementById('step-3').classList.add('active');
                        document.getElementById('finalStatus').innerText = status;
                    } else if (status === 'PAYMENT_FAILED') {
                        clearInterval(interval);
                        document.getElementById('step-2').classList.remove('active');
                        document.getElementById('step-4').classList.add('active');
                        document.getElementById('failStatus').innerText = status;
                    }
                })
                .catch(err => console.error("Ошибка поллинга", err));

        }, 2000);
    }

    restoreFromReturn();
});