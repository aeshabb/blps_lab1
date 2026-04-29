import sys

with open('src/main/resources/static/app.js', 'r') as f:
    content = f.read()

# Fix checkAuth()
content = content.replace(
    'if (authorities && authorities.length > 0) {',
    "const validAuths = authorities.filter(a => a !== 'ROLE_ANONYMOUS');\n                if (validAuths.length > 0) {"
)

# And fix showMainView
content = content.replace(
    'function showMainView(authorities) {',
    '''function showMainView(authorities) {
        authorities = authorities.filter(a => a !== 'ROLE_ANONYMOUS');'''
)

with open('src/main/resources/static/app.js', 'w') as f:
    f.write(content)

