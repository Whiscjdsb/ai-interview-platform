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

INSERT INTO sys_user (username, nickname, password_hash, status)
VALUES
  (
    'demo_user',
    'Demo User',
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

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.role_code = 'USER'
WHERE u.username = 'demo_user'
ON DUPLICATE KEY UPDATE
  deleted = 0,
  update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag (tag_name, description)
VALUES
  ('Java', 'Java language fundamentals'),
  ('Spring Boot', 'Spring Boot backend development'),
  ('MySQL', 'MySQL database knowledge'),
  ('Redis', 'Redis cache and data structure knowledge'),
  ('Security', 'Authentication and authorization knowledge'),
  ('Project Practice', 'Real backend project design and troubleshooting')
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
    SELECT 1 FROM question existing WHERE existing.title = 'String and StringBuilder differences'
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
    SELECT 1 FROM question existing WHERE existing.title = 'Spring Boot auto configuration'
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
    SELECT 1 FROM question existing WHERE existing.title = 'MySQL index usage'
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
    SELECT 1 FROM question existing WHERE existing.title = 'Redis cache penetration'
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
    SELECT 1 FROM question existing WHERE existing.title = 'JWT stateless authentication'
  );

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT
  'Backend project troubleshooting',
  'In a Spring Boot project, the frontend reports Network Error when calling login, but /api/health works in the browser. How would you troubleshoot it?',
  'SHORT_ANSWER',
  'MEDIUM',
  'Check browser Network details, CORS preflight, Spring Security CORS configuration, allowed origins, allowed headers, OPTIONS request handling, backend logs, and Axios baseURL.',
  'This is a common project practice question. Health check working only proves the backend is alive; login failing from the browser often points to CORS, preflight, security filter chain, or frontend baseURL configuration.',
  u.id,
  'seed',
  1
FROM sys_user u
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM question existing WHERE existing.title = 'Backend project troubleshooting'
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

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'Project Practice'
WHERE q.title = 'Backend project troubleshooting'
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM question q
INNER JOIN question_tag t ON t.tag_name = 'Spring Boot'
WHERE q.title = 'Backend project troubleshooting'
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

-- Extended demo question bank for Java backend interview scenarios.
INSERT INTO question_tag (tag_name, description)
VALUES
  ('Java Basics', 'Core Java syntax, object model, and language features'),
  ('Java Collection', 'Java collection framework and common implementation choices'),
  ('JVM', 'JVM memory model, GC, class loading, and runtime troubleshooting'),
  ('Concurrency', 'Java concurrency primitives, thread pools, and lock design'),
  ('Spring MVC', 'Spring MVC request lifecycle, controller binding, and exception handling'),
  ('MyBatis-Plus', 'MyBatis and MyBatis-Plus persistence layer practices'),
  ('RESTful API', 'REST API design, status codes, idempotency, and contract style'),
  ('AI Interview', 'AI interview product design, scoring, fallback, and integration scenarios')
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  deleted = 0,
  update_time = CURRENT_TIMESTAMP;

INSERT INTO question (title, content, question_type, difficulty, correct_answer, analysis, creator_id, source, status)
SELECT v.title, v.content, v.question_type, v.difficulty, v.correct_answer, v.analysis, u.id, 'seed', 1
FROM (
  SELECT 'Java object equality and hashCode' AS title, 'Why should equals and hashCode usually be overridden together in Java?' AS content, 'SHORT_ANSWER' AS question_type, 'EASY' AS difficulty, 'equals decides logical equality while hashCode decides bucket placement in hash based collections. If two objects are equal, their hashCode values must be equal, otherwise HashMap and HashSet may fail to find existing elements.' AS correct_answer, 'This question checks whether the candidate understands object contracts and how they affect collections.' AS analysis
  UNION ALL SELECT 'Java final keyword usage', 'Which usages of final are correct? A. final class cannot be inherited B. final method cannot be overridden C. final variable cannot be reassigned D. final object cannot mutate internal fields', 'MULTIPLE_CHOICE', 'EASY', 'A,B,C', 'final restricts reassignment or inheritance behavior. It does not make the state of a referenced object immutable. This is a common basic Java question and is useful for explaining immutability boundaries.'
  UNION ALL SELECT 'Java exception handling design', 'How would you choose between checked exceptions, runtime exceptions, and global exception handling in a Spring Boot project?', 'SHORT_ANSWER', 'MEDIUM', 'Checked exceptions are suitable when callers can reasonably recover, runtime exceptions are often used for business validation and programming errors, and global exception handling converts exceptions into stable API responses. A good design keeps controller code clean and returns friendly messages.', 'The answer should connect Java exception types with backend API design rather than only reciting definitions.'
  UNION ALL SELECT 'Java String immutability benefits', 'Why is String designed as immutable in Java?', 'SHORT_ANSWER', 'MEDIUM', 'String immutability supports security, thread safety, string pool reuse, stable hashCode caching, and predictable behavior as keys in maps. It also prevents unexpected modification when strings are shared across different parts of a program.', 'A strong answer explains both language design and practical backend impact.'
  UNION ALL SELECT 'ArrayList and LinkedList choice', 'In most backend business code, why is ArrayList usually preferred over LinkedList?', 'SHORT_ANSWER', 'EASY', 'ArrayList has better cache locality, lower memory overhead, and fast random access. LinkedList only helps in limited cases where frequent insertion or deletion happens through an existing node reference, which is uncommon in normal service code.', 'This question checks whether the candidate can choose data structures based on real usage patterns.'
  UNION ALL SELECT 'HashMap collision handling', 'How does HashMap handle hash collisions in modern Java?', 'SHORT_ANSWER', 'MEDIUM', 'HashMap first maps keys to buckets by hash. When multiple keys fall into one bucket, it stores them in a linked list, and under certain thresholds it can treeify the bucket into a red black tree to improve lookup performance.', 'A good answer mentions hash, equals, bucket, list, treeification, and why collisions matter.'
  UNION ALL SELECT 'ConcurrentHashMap use cases', 'Which statement about ConcurrentHashMap is correct? A. It supports concurrent reads and updates B. It is always faster than HashMap C. It does not allow null keys or null values D. It can replace all locking logic', 'MULTIPLE_CHOICE', 'MEDIUM', 'A,C', 'ConcurrentHashMap improves concurrent access but is not always faster and cannot replace all higher level consistency requirements. This is useful for discussing thread safe collections and their limits.'
  UNION ALL SELECT 'Set duplicate removal principle', 'Why can HashSet remove duplicate custom objects only when equals and hashCode are implemented correctly?', 'SHORT_ANSWER', 'EASY', 'HashSet is backed by a hash based structure. It uses hashCode to locate a bucket and equals to confirm logical equality. If either contract is wrong, logically duplicate objects may be stored as different elements.', 'This reinforces equality contracts with a concrete collection example.'
  UNION ALL SELECT 'JVM memory areas overview', 'What are the main JVM runtime memory areas and what kind of data do they store?', 'SHORT_ANSWER', 'MEDIUM', 'The heap stores object instances, the Java stack stores method frames and local variables, the method area or metaspace stores class metadata, the program counter records the current instruction, and the native method stack supports native calls.', 'This question is basic but important for later GC and troubleshooting questions.'
  UNION ALL SELECT 'Young GC and Old GC difference', 'What is the difference between Young GC and Old GC?', 'SHORT_ANSWER', 'MEDIUM', 'Young GC mainly collects the young generation and is usually frequent and fast. Old GC or major collection targets long lived objects in the old generation and is usually more expensive. Full GC may involve a wider range of memory areas and can cause noticeable pauses.', 'A good answer should mention object lifetime and pause impact.'
  UNION ALL SELECT 'Class loading delegation model', 'Why does Java use the parent delegation model for class loading?', 'SHORT_ANSWER', 'MEDIUM', 'Parent delegation lets parent class loaders try loading classes first, which protects core Java classes, avoids duplicate loading, and keeps class identity more predictable. Custom class loaders can still extend the model for plugin or isolation scenarios.', 'This checks whether the candidate understands class loading safety and extensibility.'
  UNION ALL SELECT 'JVM memory leak troubleshooting', 'A Spring Boot service memory keeps growing and finally throws OutOfMemoryError. How would you troubleshoot it?', 'SHORT_ANSWER', 'HARD', 'Check heap usage, GC logs, dump the heap, analyze dominant objects and reference chains, inspect caches, static collections, thread locals, large result sets, and unbounded queues. Then reproduce the issue and fix the object retention source.', 'This is a practical JVM troubleshooting question for backend interviews.'
  UNION ALL SELECT 'synchronized and ReentrantLock difference', 'Compare synchronized and ReentrantLock in Java concurrency.', 'SHORT_ANSWER', 'MEDIUM', 'synchronized is built into the language and automatically releases locks. ReentrantLock provides explicit lock and unlock, tryLock, interruptible locking, fairness options, and multiple Condition objects. ReentrantLock is more flexible but requires careful finally cleanup.', 'This question checks lock usage tradeoffs rather than only syntax.'
  UNION ALL SELECT 'ThreadPoolExecutor parameters', 'Which parameters are important when configuring ThreadPoolExecutor? A. corePoolSize B. maximumPoolSize C. workQueue D. rejectionHandler', 'MULTIPLE_CHOICE', 'MEDIUM', 'A,B,C,D', 'Thread pool behavior depends on core size, max size, queue type, keep alive time, thread factory, and rejection policy. These choices should match task type and traffic pattern. This question is useful for connecting concurrency with production stability.'
  UNION ALL SELECT 'volatile visibility and atomicity', 'Can volatile guarantee both visibility and atomicity?', 'SHORT_ANSWER', 'MEDIUM', 'volatile guarantees visibility and ordering constraints for a variable, but it does not make compound operations such as count plus plus atomic. Atomic classes or locks are needed when atomic read modify write behavior is required.', 'This is a common concurrency pitfall.'
  UNION ALL SELECT 'Deadlock troubleshooting', 'How would you troubleshoot and avoid a Java deadlock?', 'SHORT_ANSWER', 'HARD', 'Use thread dumps to find blocked threads and lock ownership, inspect lock acquisition order, reduce nested locks, use timeout based locking, keep critical sections small, and define a consistent lock ordering strategy.', 'A strong answer combines diagnosis and prevention.'
  UNION ALL SELECT 'Spring Boot starter value', 'Why are Spring Boot starters useful?', 'SHORT_ANSWER', 'EASY', 'Starters group common dependencies and default integration choices for a scenario. They reduce dependency version conflicts and help developers quickly enable web, validation, security, database, or test features.', 'This question checks basic Spring Boot understanding.'
  UNION ALL SELECT 'Spring Boot configuration priority', 'How does Spring Boot load configuration and why should secrets use environment variables?', 'SHORT_ANSWER', 'MEDIUM', 'Spring Boot can read configuration from application files, profiles, command line arguments, system properties, and environment variables. Secrets should use environment variables or secret managers so they are not committed to source code.', 'This connects configuration mechanics with security practice.'
  UNION ALL SELECT 'Spring Bean lifecycle basics', 'What are common phases in a Spring Bean lifecycle?', 'SHORT_ANSWER', 'MEDIUM', 'A bean is instantiated, dependencies are injected, aware callbacks and post processors may run, initialization callbacks execute, the bean is used, and destroy callbacks run when the context closes.', 'The answer should show a lifecycle view rather than only naming annotations.'
  UNION ALL SELECT 'Spring Boot startup failure troubleshooting', 'A Spring Boot application fails to start. What would you check first?', 'SHORT_ANSWER', 'HARD', 'Check the first root cause in logs, port conflicts, missing configuration, database connectivity, bean creation errors, circular dependencies, profile selection, dependency version conflicts, and whether required environment variables are present.', 'This is a high value project practice question for interview demonstrations.'
  UNION ALL SELECT 'Spring MVC request lifecycle', 'Describe the Spring MVC request processing lifecycle.', 'SHORT_ANSWER', 'MEDIUM', 'A request enters DispatcherServlet, matches a HandlerMapping, invokes the handler through an adapter, binds and validates parameters, executes controller logic, handles exceptions if needed, and writes the response through view resolution or message converters.', 'This question checks framework flow and helps explain controller responsibilities.'
  UNION ALL SELECT 'RequestBody and RequestParam difference', 'What is the difference between @RequestBody, @RequestParam, and @PathVariable?', 'SHORT_ANSWER', 'EASY', '@RequestBody reads the HTTP body and converts JSON to an object, @RequestParam reads query or form parameters, and @PathVariable reads values from the URI path. The choice should match API contract style.', 'This is useful for REST interface design and debugging parameter binding issues.'
  UNION ALL SELECT 'Global exception handler purpose', 'Why should a backend project use a global exception handler?', 'SHORT_ANSWER', 'MEDIUM', 'A global exception handler centralizes error conversion, keeps controllers clean, avoids leaking stack traces, and ensures clients receive a stable JSON format with meaningful messages and status codes.', 'This connects Spring MVC with API consistency.'
  UNION ALL SELECT 'Validation failure handling', 'How should parameter validation failures be handled in a Spring MVC API?', 'SHORT_ANSWER', 'MEDIUM', 'Use validation annotations on DTOs, enable @Valid or @Validated, collect binding errors in global exception handling, and return a clear error message in the unified response structure.', 'This question checks whether the candidate can design friendly API validation.'
  UNION ALL SELECT 'MyBatis result mapping issue', 'MyBatis query returns data in SQL client but mapped object fields are null. What might be wrong?', 'SHORT_ANSWER', 'MEDIUM', 'Check column aliases, underscore to camel case mapping, resultMap definitions, property names, type handlers, selected columns, and whether the mapper XML or annotation matches the entity fields.', 'This is a practical persistence layer troubleshooting question.'
  UNION ALL SELECT 'MyBatis dynamic SQL benefit', 'Why is MyBatis dynamic SQL useful for search pages?', 'SHORT_ANSWER', 'MEDIUM', 'Dynamic SQL lets the query include conditions only when parameters are present, which is suitable for keyword search, filters, pagination, and optional date ranges without building unsafe string concatenation in Java code.', 'The answer should mention safety and maintainability.'
  UNION ALL SELECT 'MyBatis-Plus logical delete', 'What problem does MyBatis-Plus logical delete solve?', 'SHORT_ANSWER', 'EASY', 'Logical delete marks data as deleted without physically removing it. It preserves history, reduces accidental data loss, and lets normal queries automatically exclude deleted rows when configured correctly.', 'This matches the project design because many tables use a deleted field.'
  UNION ALL SELECT 'N plus one query problem', 'What is the N plus one query problem and how can it be avoided?', 'SHORT_ANSWER', 'HARD', 'N plus one means querying a list once and then querying related data once per row. It can be avoided by batch queries, joins, in queries, aggregation SQL, or preloading relation data by IDs.', 'This checks database performance awareness in service layer design.'
  UNION ALL SELECT 'MySQL transaction isolation', 'What problems do transaction isolation levels try to solve?', 'SHORT_ANSWER', 'MEDIUM', 'Isolation levels control dirty reads, non repeatable reads, and phantom reads. Higher isolation can improve consistency but may reduce concurrency, so backend services should choose based on business requirements.', 'A good answer explains both correctness and performance tradeoffs.'
  UNION ALL SELECT 'MySQL slow query troubleshooting', 'How would you troubleshoot a slow MySQL query?', 'SHORT_ANSWER', 'HARD', 'Use slow query logs and EXPLAIN, check indexes, join order, rows scanned, selectivity, functions on indexed columns, pagination style, returned columns, and whether the query can be rewritten or cached.', 'This is important for backend performance interviews.'
  UNION ALL SELECT 'MySQL unique constraint purpose', 'Why should username or relation tables use unique constraints?', 'SHORT_ANSWER', 'EASY', 'Unique constraints protect data consistency at the database level. They prevent duplicate usernames, duplicate favorites, or duplicate question tag relations even when concurrent requests happen.', 'This connects database design with concurrency safety.'
  UNION ALL SELECT 'Database connection failure troubleshooting', 'A backend cannot connect to MySQL after deployment. How would you investigate?', 'SHORT_ANSWER', 'HARD', 'Check MySQL service status, host and port, database name, credentials, network firewall, user permissions, JDBC URL parameters, driver dependency, connection pool logs, and whether the schema has been initialized.', 'This is one of the most common deployment troubleshooting scenarios.'
  UNION ALL SELECT 'Redis cache aside pattern', 'What is the cache aside pattern?', 'SHORT_ANSWER', 'MEDIUM', 'The service first reads from cache. On miss it reads from database, writes the result back to cache, and then returns it. On update it usually updates the database first and then deletes or refreshes the cache.', 'This question checks common Redis usage in backend systems.'
  UNION ALL SELECT 'Redis distributed lock risk', 'What risks should be considered when using Redis as a distributed lock?', 'SHORT_ANSWER', 'HARD', 'The lock must have an expiration time, a unique owner value, safe unlock logic, and reasonable timeout handling. Clock, long business execution, retry storms, and single Redis node failure should also be considered.', 'This is useful for advanced Redis and concurrency discussions.'
  UNION ALL SELECT 'Redis data structure choice', 'Which Redis structures fit common backend scenarios? A. String for counters B. Hash for object fields C. ZSet for ranking D. List for simple queues', 'MULTIPLE_CHOICE', 'MEDIUM', 'A,B,C,D', 'Redis provides different structures for different access patterns. Choosing the right structure keeps code simpler and operations efficient. This tests practical Redis modeling.'
  UNION ALL SELECT 'AI statistics cache invalidation', 'When should user statistics cache in Redis be invalidated in an interview learning platform?', 'SHORT_ANSWER', 'MEDIUM', 'It should be invalidated after answer submission, favorite changes, wrong question deletion or status changes, and other actions that affect statistics. Cache invalidation should be tied to write operations.', 'This links Redis cache with this project business flow.'
  UNION ALL SELECT 'JWT token expiration handling', 'How should a frontend and backend handle expired JWT tokens?', 'SHORT_ANSWER', 'MEDIUM', 'The backend should return a unified 401 JSON response when the token is expired or invalid. The frontend should clear local token state and redirect to login instead of showing a broken page.', 'This question matches the project JWT and Axios design.'
  UNION ALL SELECT 'Spring Security permit paths', 'Why should login, register, health check, and Swagger paths be allowed anonymously?', 'SHORT_ANSWER', 'EASY', 'Login and register must be accessible before authentication. Health check and Swagger are useful for development and diagnostics. Other protected APIs should still require a valid token and role checks.', 'This checks practical security configuration.'
  UNION ALL SELECT 'Admin role authorization', 'How can admin APIs be protected in Spring Security?', 'SHORT_ANSWER', 'MEDIUM', 'Use JWT authentication to identify the user, load roles into authorities, protect admin paths in the security filter chain, and use method level annotations such as @PreAuthorize for role based checks.', 'A good answer mentions both URL and method level protection.'
  UNION ALL SELECT 'JWT login invalid troubleshooting', 'A user logs in successfully but later requests always return unauthorized. How would you troubleshoot it?', 'SHORT_ANSWER', 'HARD', 'Check whether the frontend stores the token, sends Authorization Bearer header, backend JWT secret matches, token expiration, clock skew, CORS exposes authorization header if needed, and whether the user is disabled or roles changed.', 'This is a project practice question for auth debugging.'
  UNION ALL SELECT 'REST idempotency design', 'Which REST operations are usually expected to be idempotent? A. GET B. PUT C. DELETE D. POST create order without idempotency key', 'MULTIPLE_CHOICE', 'MEDIUM', 'A,B,C', 'GET, PUT, and DELETE are generally designed to be idempotent. POST may or may not be idempotent depending on whether an idempotency key or business constraint is used. This question checks API semantics.'
  UNION ALL SELECT 'REST status code choice', 'How would you choose HTTP status codes for validation failure, unauthorized access, and forbidden access?', 'SHORT_ANSWER', 'EASY', 'Validation failure commonly maps to 400, missing or invalid authentication maps to 401, and authenticated users without permission map to 403. The response body can still follow a unified JSON structure.', 'This helps candidates explain frontend error handling.'
  UNION ALL SELECT 'API pagination design', 'What should a good paginated list API return?', 'SHORT_ANSWER', 'MEDIUM', 'It should return current page data, total count, page number, page size, and possibly total pages. Filters and sorting should be explicit so the frontend can reproduce the same query when changing pages.', 'This is useful for question lists, favorites, wrong questions, and admin pages.'
  UNION ALL SELECT 'RESTful API versioning', 'When might an API need versioning and how can it be done?', 'SHORT_ANSWER', 'MEDIUM', 'Versioning is useful when breaking contract changes are introduced. It can be done with URI prefixes, headers, or media types. For small projects, stable DTOs and backward compatible changes are often enough.', 'A practical answer avoids overengineering but knows the options.'
  UNION ALL SELECT 'Frontend backend CORS troubleshooting', 'The browser reports Axios Network Error for login while direct health check works. What is the likely cause and fix?', 'SHORT_ANSWER', 'HARD', 'Check CORS preflight, allowed origins, allowed methods, allowed headers such as Authorization and Content-Type, Spring Security cors configuration, OPTIONS permit rules, and whether Axios baseURL points to the backend.', 'This is a direct project troubleshooting question and useful for demos.'
  UNION ALL SELECT 'MyBatis query empty troubleshooting', 'A MyBatis query returns an empty list but the database has data. What would you check?', 'SHORT_ANSWER', 'MEDIUM', 'Check SQL conditions, logical delete field, tenant or status filters, parameter names, mapper binding, database environment, transaction visibility, and whether the query is using the expected schema.', 'This question is common in backend project debugging.'
  UNION ALL SELECT 'AI provider fallback design', 'If the AI API call fails during an interview demo, how should the system degrade?', 'SHORT_ANSWER', 'MEDIUM', 'The system should log the provider failure, avoid throwing raw external errors to users, fall back to Mock scoring or a friendly retry message, and keep interview history and non AI features available.', 'This tests resilience thinking in the AI interview module.'
  UNION ALL SELECT 'DeepSeek JSON instability handling', 'DeepSeek returns text that is not valid JSON. How should the backend handle it?', 'SHORT_ANSWER', 'HARD', 'The backend should keep rawResponse, try to parse structured JSON safely, fallback to a raw response wrapper when parsing fails, avoid throwing exceptions to the user, and let ScoreEngine apply final score constraints.', 'This is central to the project AI integration design.'
  UNION ALL SELECT 'Score anomaly troubleshooting', 'A low quality answer such as I do not know receives a high AI score. How would you fix and verify it?', 'SHORT_ANSWER', 'HARD', 'Add low quality answer detection, route all scores through ScoreEngine, cap invalid or very short answers, log traceId with input and final score, and add tests for empty, meaningless, vague, and normal technical answers.', 'This question highlights the ScoreEngine engineering refactor.'
  UNION ALL SELECT 'PDF export failure troubleshooting', 'PDF export fails in production but interview result page works. What would you check?', 'SHORT_ANSWER', 'MEDIUM', 'Check the PDF library dependency, font availability for Chinese text, interview ownership, result content parsing, server file or memory limits, response content type, and backend exception logs.', 'This connects reporting features with deployment reality.'
  UNION ALL SELECT 'Share link invalid troubleshooting', 'A public interview share link returns not found. What would you check?', 'SHORT_ANSWER', 'MEDIUM', 'Check whether the interview is completed, is_public is true, share_token exists and matches the URL, the token is unique, the record is not logically deleted, and Nginx forwards the public route correctly.', 'This is useful for explaining public report sharing.'
  UNION ALL SELECT 'AI review prompt design', 'What should be included in a prompt for AI answer review?', 'SHORT_ANSWER', 'MEDIUM', 'The prompt should include the question, user answer, scoring requirements, output JSON schema, strict grading rules, low quality answer constraints, and limits on strengths, weaknesses, suggestions, and reference answer fields.', 'This tests prompt engineering in a backend controlled system.'
  UNION ALL SELECT 'Mock AI service purpose', 'Why keep MockAiService after integrating DeepSeek?', 'SHORT_ANSWER', 'EASY', 'MockAiService keeps local development, tests, and demos stable when the external AI service is unavailable or no API key is configured. It also makes the AI abstraction easier to verify.', 'This question explains engineering fallback rather than model capability.'
  UNION ALL SELECT 'AI interview history design', 'Why should rawResponse and structuredResult both be saved for AI interview results?', 'SHORT_ANSWER', 'MEDIUM', 'structuredResult is convenient for UI, PDF, sharing, and growth analysis. rawResponse preserves the original model output for troubleshooting, compatibility, and future reprocessing when parsing rules improve.', 'This is a strong explanation for robust AI data handling.'
  UNION ALL SELECT 'Interview scoring consistency', 'How can the system ensure result page, PDF, share page, and growth analysis use the same score?', 'SHORT_ANSWER', 'HARD', 'All scoring paths should call ScoreEngine and persist the final capped score. Downstream modules should read the saved final score instead of recalculating independently.', 'This question checks architectural consistency across modules.'
) v
INNER JOIN sys_user u ON u.username = 'admin'
WHERE NOT EXISTS (
  SELECT 1 FROM question existing WHERE existing.title = v.title
);

INSERT INTO question_tag_relation (question_id, tag_id)
SELECT q.id, t.id
FROM (
  SELECT 'Java object equality and hashCode' AS title, 'Java Basics' AS tag_name
  UNION ALL SELECT 'Java final keyword usage', 'Java Basics'
  UNION ALL SELECT 'Java exception handling design', 'Java Basics'
  UNION ALL SELECT 'Java String immutability benefits', 'Java Basics'
  UNION ALL SELECT 'ArrayList and LinkedList choice', 'Java Collection'
  UNION ALL SELECT 'HashMap collision handling', 'Java Collection'
  UNION ALL SELECT 'ConcurrentHashMap use cases', 'Java Collection'
  UNION ALL SELECT 'Set duplicate removal principle', 'Java Collection'
  UNION ALL SELECT 'JVM memory areas overview', 'JVM'
  UNION ALL SELECT 'Young GC and Old GC difference', 'JVM'
  UNION ALL SELECT 'Class loading delegation model', 'JVM'
  UNION ALL SELECT 'JVM memory leak troubleshooting', 'JVM'
  UNION ALL SELECT 'synchronized and ReentrantLock difference', 'Concurrency'
  UNION ALL SELECT 'ThreadPoolExecutor parameters', 'Concurrency'
  UNION ALL SELECT 'volatile visibility and atomicity', 'Concurrency'
  UNION ALL SELECT 'Deadlock troubleshooting', 'Concurrency'
  UNION ALL SELECT 'Spring Boot starter value', 'Spring Boot'
  UNION ALL SELECT 'Spring Boot configuration priority', 'Spring Boot'
  UNION ALL SELECT 'Spring Bean lifecycle basics', 'Spring Boot'
  UNION ALL SELECT 'Spring Boot startup failure troubleshooting', 'Spring Boot'
  UNION ALL SELECT 'Spring MVC request lifecycle', 'Spring MVC'
  UNION ALL SELECT 'RequestBody and RequestParam difference', 'Spring MVC'
  UNION ALL SELECT 'Global exception handler purpose', 'Spring MVC'
  UNION ALL SELECT 'Validation failure handling', 'Spring MVC'
  UNION ALL SELECT 'MyBatis result mapping issue', 'MyBatis-Plus'
  UNION ALL SELECT 'MyBatis dynamic SQL benefit', 'MyBatis-Plus'
  UNION ALL SELECT 'MyBatis-Plus logical delete', 'MyBatis-Plus'
  UNION ALL SELECT 'N plus one query problem', 'MyBatis-Plus'
  UNION ALL SELECT 'MySQL transaction isolation', 'MySQL'
  UNION ALL SELECT 'MySQL slow query troubleshooting', 'MySQL'
  UNION ALL SELECT 'MySQL unique constraint purpose', 'MySQL'
  UNION ALL SELECT 'Database connection failure troubleshooting', 'MySQL'
  UNION ALL SELECT 'Redis cache aside pattern', 'Redis'
  UNION ALL SELECT 'Redis distributed lock risk', 'Redis'
  UNION ALL SELECT 'Redis data structure choice', 'Redis'
  UNION ALL SELECT 'AI statistics cache invalidation', 'Redis'
  UNION ALL SELECT 'JWT token expiration handling', 'Security'
  UNION ALL SELECT 'Spring Security permit paths', 'Security'
  UNION ALL SELECT 'Admin role authorization', 'Security'
  UNION ALL SELECT 'JWT login invalid troubleshooting', 'Security'
  UNION ALL SELECT 'REST idempotency design', 'RESTful API'
  UNION ALL SELECT 'REST status code choice', 'RESTful API'
  UNION ALL SELECT 'API pagination design', 'RESTful API'
  UNION ALL SELECT 'RESTful API versioning', 'RESTful API'
  UNION ALL SELECT 'Frontend backend CORS troubleshooting', 'Project Practice'
  UNION ALL SELECT 'MyBatis query empty troubleshooting', 'Project Practice'
  UNION ALL SELECT 'AI provider fallback design', 'Project Practice'
  UNION ALL SELECT 'DeepSeek JSON instability handling', 'Project Practice'
  UNION ALL SELECT 'Score anomaly troubleshooting', 'Project Practice'
  UNION ALL SELECT 'PDF export failure troubleshooting', 'Project Practice'
  UNION ALL SELECT 'Share link invalid troubleshooting', 'Project Practice'
  UNION ALL SELECT 'AI review prompt design', 'AI Interview'
  UNION ALL SELECT 'Mock AI service purpose', 'AI Interview'
  UNION ALL SELECT 'AI interview history design', 'AI Interview'
  UNION ALL SELECT 'Interview scoring consistency', 'AI Interview'
) m
INNER JOIN question q ON q.title = m.title AND q.deleted = 0
INNER JOIN question_tag t ON t.tag_name = m.tag_name AND t.deleted = 0
ON DUPLICATE KEY UPDATE deleted = 0, update_time = CURRENT_TIMESTAMP;
