INSERT INTO sys_role (id, role_code, role_name, description)
VALUES
  (1, 'USER', 'User', 'Regular platform user'),
  (2, 'ADMIN', 'Admin', 'Platform administrator');

INSERT INTO sys_user (id, username, nickname, password_hash, status)
VALUES
  (1, 'admin', 'Administrator', '$2a$10$BCbYbDRn7VrxyjFelpJq8OckoO1z2fB7PjWLGaKjrqKOtpWV1SB5y', 1);

INSERT INTO sys_user_role (id, user_id, role_id)
VALUES
  (1, 1, 2);

INSERT INTO question_tag (id, tag_name, description)
VALUES
  (1, 'Java', 'Java language fundamentals'),
  (2, 'Spring Boot', 'Spring Boot backend development'),
  (3, 'MySQL', 'MySQL database knowledge'),
  (4, 'Redis', 'Redis cache and data structure knowledge'),
  (5, 'Security', 'Authentication and authorization knowledge');

INSERT INTO question (id, title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status, create_time)
VALUES
  (1, 'String and StringBuilder differences', 'Which option describes StringBuilder correctly? A. Mutable B. Immutable C. Database tool D. Cache server', 'SINGLE_CHOICE', 'EASY', 'A', 'StringBuilder is mutable and suitable for repeated concatenation in a single thread.', 1, 'seed', 1, TIMESTAMP '2026-01-01 10:00:00'),
  (2, 'Spring Boot auto configuration', 'What is Spring Boot auto configuration and how can it be customized?', 'SHORT_ANSWER', 'MEDIUM', 'Auto configuration configures beans based on classpath, properties, and existing beans.', 'Auto configuration reduces boilerplate.', 1, 'seed', 1, TIMESTAMP '2026-01-02 10:00:00'),
  (3, 'MySQL index usage', 'Which situations may prevent MySQL from using an index effectively? A. Function on indexed column B. Leading wildcard LIKE C. Implicit type conversion D. Exact primary key lookup', 'MULTIPLE_CHOICE', 'MEDIUM', 'A,B,C', 'Functions, leading wildcard LIKE, and implicit type conversion may reduce index effectiveness.', 1, 'seed', 1, TIMESTAMP '2026-01-03 10:00:00'),
  (4, 'Redis cache penetration', 'How can a backend service reduce Redis cache penetration?', 'SHORT_ANSWER', 'MEDIUM', 'Cache empty values, use Bloom filters, validate parameters, and rate limit.', 'Cache penetration targets nonexistent keys.', 1, 'seed', 1, TIMESTAMP '2026-01-04 10:00:00'),
  (5, 'JWT stateless authentication', 'Why is JWT commonly used for stateless authentication in REST APIs?', 'JUDGE', 'HARD', 'JWT can carry signed identity and claims without server-side sessions.', 'JWT still needs expiration, key rotation, and revocation strategy.', 1, 'seed', 1, TIMESTAMP '2026-01-05 10:00:00');

INSERT INTO question_tag_relation (id, question_id, tag_id)
VALUES
  (1, 1, 1),
  (2, 2, 2),
  (3, 3, 3),
  (4, 4, 4),
  (5, 5, 5);

ALTER TABLE sys_user ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sys_role ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sys_user_role ALTER COLUMN id RESTART WITH 100;
ALTER TABLE question ALTER COLUMN id RESTART WITH 100;
ALTER TABLE question_tag ALTER COLUMN id RESTART WITH 100;
ALTER TABLE question_tag_relation ALTER COLUMN id RESTART WITH 100;
ALTER TABLE user_answer_record ALTER COLUMN id RESTART WITH 100;
ALTER TABLE user_favorite ALTER COLUMN id RESTART WITH 100;
ALTER TABLE user_wrong_question ALTER COLUMN id RESTART WITH 100;
ALTER TABLE daily_learning_record ALTER COLUMN id RESTART WITH 100;
ALTER TABLE ai_review_record ALTER COLUMN id RESTART WITH 100;
