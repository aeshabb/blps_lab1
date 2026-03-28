#!/bin/bash

# Script to create courses in Tutor/Open edX LMS

echo "Creating courses in Tutor LMS..."

# Create courses via Django shell
tutor local run lms python manage.py shell << 'EOF'
from opaque_keys.edx.keys import CourseKey
from xmodule.modulestore.django import modulestore
from xmodule.modulestore.split_mongo import SplitMongoModuleStore
from datetime import datetime

def create_course(org, number, run, display_name):
    """Create a course if it doesn't exist"""
    course_key = CourseKey.from_string(f'course-v1:{org}+{number}+{run}')
    store = modulestore()
    
    try:
        # Check if course already exists
        existing = store.get_course(course_key)
        print(f"✓ Course already exists: {course_key}")
        return str(course_key)
    except:
        # Create course structure
        try:
            print(f"Creating course: {course_key}")
            # Create via course factory
            from openedx.core.djangoapps.content_libraries_migration_status.models import ContentLibraryMigrationStatus
            from contentstore.models import CourseRerunState
            from xmodule.course_module import CourseModule
            
            course = CourseModule.create_modulestorewrapper(
                store,
                course_key,
                display_name=display_name,
                start=datetime(2026, 1, 1),
                end=datetime(2026, 12, 31),
            )
            store.update_item(course, None)
            print(f"✓ Created course: {course_key}")
            return str(course_key)
        except Exception as e:
            print(f"✗ Error creating course {course_key}: {e}")
            # Try alternative method
            try:
                course_module = store.create_course(
                    org=org,
                    course=number, 
                    run=run,
                    user_id=None,
                    fields=dict(display_name=display_name)
                )
                print(f"✓ Created course (alternative): {course_key}")
                return str(course_key)
            except Exception as e2:
                print(f"✗ Alternative method also failed: {e2}")
                return None

# Create courses
courses = [
    ("SKB", "JV101", "2026", "Java Verification 101"),
    ("SKB", "PY201", "2026", "Python Development 201"),
    ("SKB", "WEB301", "2026", "Web Technologies 301"),
]

print("\n" + "="*60)
print("CREATING COURSES IN TUTOR")
print("="*60 + "\n")

for org, number, run, name in courses:
    create_course(org, number, run, name)

print("\n" + "="*60)
print("Course creation completed")
print("="*60)

EOF

echo -e "\n✓ Script execution completed"
