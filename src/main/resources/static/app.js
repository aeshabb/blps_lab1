document.addEventListener('DOMContentLoaded', () => {
    let programsData = [];
    let currentAppId = null;

    const programSelect = document.getElementById('programSelect');
    const tariffSelect = document.getElementById('tariffSelect');
    const tariffWrapper = document.getElementById('tariffWrapper');
    const form = document.querySelector('form');

    fetch('/api/programs')
        .then(response => response.json())
        .then(data => {
            programsData = data;
            programSelect.innerHTML = '<option value="">Выберите программу...</option>';
            data.forEach(prog => {
                programSelect.innerHTML += `<option value="${prog.id}">${prog.title}</option>`;
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
            
            startStatusPolling();
        })
        .catch(err => alert("Ошибка создания заявки: " + err));
    });

    document.getElementById('payment-form').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentAppId) return;

        const selectedCard = document.querySelector('input[name="cardSelect"]:checked').value;
        const btn = document.getElementById('payBtn');
        btn.innerText = "Обработка транзакции...";
        btn.disabled = true;

        const webhookStatus = selectedCard === 'SUCCESS' ? 'SUCCESS' : 'FAILED';

        fetch('/api/payments/webhook', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                applicationId: currentAppId,
                status: webhookStatus
            })
        })
        .then(res => {
            if (res.ok) {
                btn.innerText = "Ответ от банка получен...";
            }
        })
        .catch(err => alert("Ошибка Webhook: " + err));
    });

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
});