document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const applicationId = parseInt(params.get('applicationId'));

    const appIdLabel = document.getElementById('appIdLabel');
    const backBtn = document.getElementById('backBtn');
    const payBtn = document.getElementById('payBtn');
    const cardMode = document.getElementById('cardMode');
    const resultBox = document.getElementById('resultBox');

    if (!applicationId) {
        showResult('Некорректная ссылка оплаты: отсутствует applicationId.', false);
        payBtn.disabled = true;
        backBtn.href = '/';
        return;
    }

    appIdLabel.innerText = `#${applicationId}`;
    backBtn.href = `/?applicationId=${applicationId}`;

    payBtn.addEventListener('click', async () => {
        payBtn.disabled = true;
        payBtn.innerText = 'Обработка...';

        try {
            const response = await fetch('/api/payments/webhook', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    applicationId,
                    status: cardMode.value
                })
            });

            if (!response.ok) {
                throw new Error(`Webhook returned ${response.status}`);
            }

            showResult('Платеж обработан. Возврат в приложение...', true);
            setTimeout(() => {
                window.location.href = `/?applicationId=${applicationId}`;
            }, 900);
        } catch (error) {
            showResult(`Ошибка оплаты: ${error.message}`, false);
            payBtn.disabled = false;
            payBtn.innerText = 'Оплатить';
        }
    });

    function showResult(message, success) {
        resultBox.classList.remove('d-none', 'alert-success', 'alert-danger');
        resultBox.classList.add(success ? 'alert-success' : 'alert-danger');
        resultBox.innerText = message;
    }
});
