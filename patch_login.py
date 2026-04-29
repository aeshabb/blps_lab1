with open("src/main/resources/static/app.js", "r") as f:
    content = f.read()

old_login_block = """    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const pass = document.getElementById('loginPassword').value;

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
                    return fetch('/api/auth/me').then(r => r.json());
                }
                throw new Error("Invalid credentials");
            })
            .then(auths => {
                showMainView(auths);
                loadPrograms();
            })
            .catch(err => {
                loginError.classList.remove('d-none');
            });
    });"""

new_login_block = """    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('loginUsername').value;
        const pass = document.getElementById('loginPassword').value;
        const btn = loginForm.querySelector('button[type="submit"]');
        const originalText = btn.innerText;
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
                    return fetch('/api/auth/me').then(r => {
                        if (!r.ok) throw new Error("ME request failed");
                        return r.json();
                    });
                } else {
                    return res.text().then(t => { throw new Error('Auth failed: ' + res.status + ' ' + t); });
                }
            })
            .then(auths => {
                // If it returned empty array, it means spring didn't log them in (e.g., sessions disabled?)
                if(auths.length === 0 || (auths.length === 1 && auths[0] === 'ROLE_ANONYMOUS')) {
                    throw new Error("STILL_ANONYMOUS");
                }
                showMainView(auths);
                loadPrograms();
                btn.disabled = false;
                btn.innerText = originalText;
            })
            .catch(err => {
                console.error("Login catch:", err);
                loginError.innerText = "Ошибка входа. " + err.message;
                loginError.classList.remove('d-none');
                btn.disabled = false;
                btn.innerText = originalText;
            });
    });"""

if old_login_block in content:
    content = content.replace(old_login_block, new_login_block)
    with open("src/main/resources/static/app.js", "w") as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Block not found!")
