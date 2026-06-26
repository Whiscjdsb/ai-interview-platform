USE ai_interview_platform;

INSERT INTO sys_user (username, nickname, password_hash, status)
VALUES
  (
    'admin',
    'Administrator',
    '$2a$10$BCbYbDRn7VrxyjFelpJq8OckoO1z2fB7PjWLGaKjrqKOtpWV1SB5y',
    1
  )
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  password_hash = VALUES(password_hash),
  status = VALUES(status),
  update_time = CURRENT_TIMESTAMP;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE
  deleted = 0,
  update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag (tag_name, description)
VALUES
  ('Java', 'Java language fundamentals'),
  ('Spring Boot', 'Spring Boot backend development'),
  ('MySQL', 'MySQL database knowledge'),
  ('Redis', 'Redis cache and data structure knowledge'),
  ('Security', 'Authentication and authorization knowledge')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  deleted = 0,
  update_time = CURRENT_TIMESTAMP;

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT
  'String and StringBuilder differences',
  'Which option describes StringBuilder correctly? A. Mutable B. Immutable C. Database tool D. Cache server',
  'SINGLE_CHOICE',
  'EASY',
  'A',
  'StringBuilder is mutable and suitable for repeated concatenation in a single thread.',
  u.id,
  'seed',
  1
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM question existing WHERE existing.title = 'String and StringBuilder differences' AND existing.deleted = 0
  );

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT
  'Spring Boot auto configuration',
  'What is Spring Boot auto configuration and how can it be customized?',
  'SHORT_ANSWER',
  'MEDIUM',
  'Auto configuration configures beans based on classpath, properties, and existing beans. It can be customized through properties, custom beans, and exclusions.',
  'Auto configuration reduces boilerplate but still allows explicit user configuration to take precedence.',
  u.id,
  'seed',
  1
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM question existing WHERE existing.title = 'Spring Boot auto configuration' AND existing.deleted = 0
  );

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT
  'MySQL index usage',
  'Which situations may prevent MySQL from using an index effectively? A. Function on indexed column B. Leading wildcard LIKE C. Implicit type conversion D. Exact primary key lookup',
  'MULTIPLE_CHOICE',
  'MEDIUM',
  'A,B,C',
  'The optimizer can only use indexes efficiently when predicates preserve index ordering and selectivity is useful.',
  u.id,
  'seed',
  1
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM question existing WHERE existing.title = 'MySQL index usage' AND existing.deleted = 0
  );

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT
  'Redis cache penetration',
  'How can a backend service reduce Redis cache penetration?',
  'SHORT_ANSWER',
  'MEDIUM',
  'Common approaches include caching empty values, using Bloom filters, validating parameters, and adding rate limits.',
  'Cache penetration means requests miss both cache and database repeatedly, often for nonexistent keys.',
  u.id,
  'seed',
  1
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM question existing WHERE existing.title = 'Redis cache penetration' AND existing.deleted = 0
  );

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT
  'JWT stateless authentication',
  'Why is JWT commonly used for stateless authentication in REST APIs?',
  'JUDGE',
  'HARD',
  'JWT can carry signed user identity and claims so the server can validate requests without storing session state.',
  'JWT is useful for stateless services, but expiration, key rotation, and revocation strategies still matter.',
  u.id,
  'seed',
  1
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM question existing WHERE existing.title = 'JWT stateless authentication' AND existing.deleted = 0
  );

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'Java'
WHERE q.title = 'String and StringBuilder differences'
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'Spring Boot'
WHERE q.title = 'Spring Boot auto configuration'
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'MySQL'
WHERE q.title = 'MySQL index usage'
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'Redis'
WHERE q.title = 'Redis cache penetration'
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'Security'
WHERE q.title = 'JWT stateless authentication'
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;

UPDATE question
SET
  content = 'Which option describes StringBuilder correctly? A. Mutable B. Immutable C. Database tool D. Cache server',
  question_type = 'SINGLE_CHOICE',
  difficulty = 'EASY',
  correct_answer = 'A',
  analysis = 'StringBuilder is mutable and suitable for repeated concatenation in a single thread.',
  update_time = CURRENT_TIMESTAMP
WHERE title = 'String and StringBuilder differences'
  AND source = 'seed';

UPDATE question
SET
  content = 'Which situations may prevent MySQL from using an index effectively? A. Function on indexed column B. Leading wildcard LIKE C. Implicit type conversion D. Exact primary key lookup',
  question_type = 'MULTIPLE_CHOICE',
  difficulty = 'MEDIUM',
  correct_answer = 'A,B,C',
  analysis = 'Functions, leading wildcard LIKE, and implicit type conversion may reduce index effectiveness.',
  update_time = CURRENT_TIMESTAMP
WHERE title = 'MySQL index usage'
  AND source = 'seed';
