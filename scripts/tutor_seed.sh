#!/usr/bin/env bash
set -euo pipefail

# Required env vars
: "${TUTOR_COURSE_ID:=course-v1:edX+DemoX+Demo_Course}"
: "${TUTOR_ADMIN_USERNAME:=admin}"
: "${TUTOR_ADMIN_EMAIL:=admin@example.com}"
: "${TUTOR_ADMIN_PASSWORD:=AdminPass_2026}"
: "${TUTOR_INSTRUCTOR_USERNAME:=instructor}"
: "${TUTOR_INSTRUCTOR_EMAIL:=instructor@example.com}"
: "${TUTOR_INSTRUCTOR_PASSWORD:=InstructorPass_2026}"

echo "[1/4] Creating admin and instructor users"
tutor local run lms ./manage.py lms shell <<'PY'
import os
from django.contrib.auth import get_user_model

User = get_user_model()

admin_username = os.environ.get("TUTOR_ADMIN_USERNAME", "admin")
admin_email = os.environ.get("TUTOR_ADMIN_EMAIL", "admin@example.com")
admin_password = os.environ.get("TUTOR_ADMIN_PASSWORD", "AdminPass_2026")

instructor_username = os.environ.get("TUTOR_INSTRUCTOR_USERNAME", "instructor")
instructor_email = os.environ.get("TUTOR_INSTRUCTOR_EMAIL", "instructor@example.com")
instructor_password = os.environ.get("TUTOR_INSTRUCTOR_PASSWORD", "InstructorPass_2026")

admin, _ = User.objects.get_or_create(username=admin_username, defaults={"email": admin_email, "is_staff": True, "is_superuser": True})
admin.email = admin_email
admin.is_staff = True
admin.is_superuser = True
admin.set_password(admin_password)
admin.save()

instructor, _ = User.objects.get_or_create(username=instructor_username, defaults={"email": instructor_email, "is_staff": True})
instructor.email = instructor_email
instructor.is_staff = True
instructor.set_password(instructor_password)
instructor.save()

print(f"Users ready: {admin_username}, {instructor_username}")
PY

echo "[2/4] Creating enrollment modes and assigning roles for course: $TUTOR_COURSE_ID"
tutor local run lms ./manage.py lms shell <<'PY'
import os
from decimal import Decimal
from django.contrib.auth import get_user_model
from opaque_keys.edx.keys import CourseKey
from common.djangoapps.student.roles import CourseInstructorRole, CourseStaffRole
from common.djangoapps.course_modes.models import CourseMode

course_id = os.environ.get("TUTOR_COURSE_ID", "course-v1:edX+DemoX+Demo_Course")
instructor_username = os.environ.get("TUTOR_INSTRUCTOR_USERNAME", "instructor")

course_key = CourseKey.from_string(course_id)
User = get_user_model()

instructor = User.objects.get(username=instructor_username)
CourseInstructorRole(course_key).add_users(instructor)
CourseStaffRole(course_key).add_users(instructor)

modes = [
    ("audit", "Audit", Decimal("0")),
    ("verified", "Verified", Decimal("10000")),
    ("professional", "Professional", Decimal("50000")),
]

for slug, display_name, min_price in modes:
    CourseMode.objects.update_or_create(
        course_id=course_key,
        mode_slug=slug,
        defaults={
            "mode_display_name": display_name,
            "currency": "RUB",
            "min_price": min_price,
        },
    )

print(f"Configured roles and modes for {course_id}")
PY

echo "[3/4] Creating OAuth2 application for API access"
tutor local run lms ./manage.py lms shell <<'PY'
import secrets
import os
from django.contrib.auth import get_user_model
from oauth2_provider.models import Application

admin_username = os.environ.get("TUTOR_ADMIN_USERNAME", "admin")
User = get_user_model()
admin = User.objects.get(username=admin_username)

app, _ = Application.objects.get_or_create(
    name="blps-lab-app",
    user=admin,
    client_type=Application.CLIENT_CONFIDENTIAL,
    authorization_grant_type=Application.GRANT_PASSWORD,
)

if not app.client_secret:
    app.client_secret = secrets.token_urlsafe(32)
    app.save(update_fields=["client_secret"])

print("CLIENT_ID=" + app.client_id)
print("CLIENT_SECRET=" + app.client_secret)
PY

echo "[4/4] Done"
echo "Use printed CLIENT_ID/CLIENT_SECRET to request token from /oauth2/access_token"
