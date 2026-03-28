#!/bin/bash

# Создание курсов в Tutor

echo "Creating courses in Tutor..."

tutor local run lms python manage.py shell << 'EOF'
from opaque_keys.edx.keys import CourseKey
from xmodule.modulestore.django import modulestore
from xmodule.courseware.models import CourseDynamics, CourseEnrollmentAllowed
from django.contrib.auth.models import User
from course_modes.models import CourseMode

courses = [
    {
        'org': 'SKB',
        'course': 'JV101',
        'run': '2026',
        'name': 'Java Verification 101',
        'modes': ['audit', 'verified', 'professional']
    },
    {
        'org': 'SKB',
        'course': 'WEB201',
        'run': '2026',
        'name': 'Web Development 201',
        'modes': ['audit', 'verified']
    }
]

for course_data in courses:
    course_id = f"course-v1:{course_data['org']}+{course_data['course']}+{course_data['run']}"
    print(f"\n{'='*60}")
    print(f"Creating course: {course_id}")
    print(f"Name: {course_data['name']}")
    print(f"{'='*60}")
    
    try:
        course_key = CourseKey.from_string(course_id)
        
        # Проверить, существует ли курс
        try:
            store = modulestore()
            existing = store.get_course(course_key)
            print(f"✓ Course already exists: {existing.display_name}")
        except:
            print("Course does not exist yet (will be created via Studio)")
        
        # Создать режимы обучения
        modes_prices = {
            'audit': 0,
            'verified': 49.00,
            'professional': 99.00
        }
        
        for mode in course_data['modes']:
            try:
                mode_obj, created = CourseMode.objects.get_or_create(
                    course_id=course_key,
                    mode_slug=mode,
                    defaults={
                        'mode_display_name': mode.capitalize(),
                        'min_price': modes_prices.get(mode, 0),
                        'suggested_prices': '',
                        'currency': 'rub',
                    }
                )
                action = "Created" if created else "Already exists"
                print(f"  ✓ {action} mode: {mode} (price: {modes_prices.get(mode, 0)} RUB)")
            except Exception as e:
                print(f"  ! Error with mode {mode}: {e}")
    
    except Exception as e:
        print(f"✗ Error creating course {course_id}: {e}")

print("\n" + "="*60)
print("Course creation completed!")
print("="*60)
print("\nCourse IDs for database:")
for course_data in courses:
    course_id = f"course-v1:{course_data['org']}+{course_data['course']}+{course_data['run']}"
    print(f"  {course_id}")
EOF

echo ""
echo "Done! Update data.sql with these course IDs."
