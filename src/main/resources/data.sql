INSERT INTO programs (id, title, description, open_edx_course_id) VALUES 
(1, 'Профессия Java-разработчик', 'Курс по Java с нуля до PRO', 'course-v1:SKB+JV101+2026'),
(2, 'Профессия Python-разработчик', 'Курс по Python с нуля до PRO', 'course-v1:SKB+WEB201+2026')
ON CONFLICT DO NOTHING;

INSERT INTO tariffs (id, name, price, program_id) VALUES 
(1, 'Базовый', 50000.00, 1),
(2, 'Индивидуальный', 100000.00, 1),
(3, 'Базовый', 45000.00, 2)
ON CONFLICT DO NOTHING;
