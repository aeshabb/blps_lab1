import re

with open("src/main/resources/static/app.js", "r") as f:
    content = f.read()

content = content.replace("    showRegisterBtn.addEventListener('click', () => {\n        loginForm.classList.add('d-none');\n        registerForm.classList.remove('d-none');\n    });\n\n    showLoginBtn.addEventListener('click', () => {\n        registerForm.classList.add('d-none');\n        loginForm.classList.remove('d-none');\n    });",
"""    if (showRegisterBtn) {
        showRegisterBtn.addEventListener('click', () => {
            loginForm.classList.add('d-none');
            registerForm.classList.remove('d-none');
        });
    }

    if (showLoginBtn) {
        showLoginBtn.addEventListener('click', () => {
            registerForm.classList.add('d-none');
            loginForm.classList.remove('d-none');
        });
    }""")

with open("src/main/resources/static/app.js", "w") as f:
    f.write(content)
