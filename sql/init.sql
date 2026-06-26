CREATE DATABASE IF NOT EXISTS ai_interview_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_interview_platform;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User primary key',
  username VARCHAR(64) NOT NULL COMMENT 'Unique login username',
  nickname VARCHAR(64) DEFAULT NULL COMMENT 'Display name',
  password_hash VARCHAR(255) NOT NULL COMMENT 'Hashed password',
  email VARCHAR(128) DEFAULT NULL COMMENT 'User email address',
  phone VARCHAR(32) DEFAULT NULL COMMENT 'User phone number',
  status TINYINT NOT NULL DEFAULT 1 COMMENT 'User status: 1 enabled, 0 disabled',
  last_login_time DATETIME DEFAULT NULL COMMENT 'Last successful login time',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_email (email),
  KEY idx_sys_user_status (status),
  KEY idx_sys_user_create_time (create_time),
  KEY idx_sys_user_status_create_time (status, create_time),
  KEY idx_sys_user_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System user table';

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Role primary key',
  role_code VARCHAR(64) NOT NULL COMMENT 'Unique role code',
  role_name VARCHAR(64) NOT NULL COMMENT 'Role display name',
  description VARCHAR(255) DEFAULT NULL COMMENT 'Role description',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code),
  KEY idx_sys_role_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System role table';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User-role relation primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  role_id BIGINT NOT NULL COMMENT 'Role id',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role_user_role (user_id, role_id),
  KEY idx_sys_user_role_role_id (role_id),
  KEY idx_sys_user_role_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-role relation table';

CREATE TABLE IF NOT EXISTS question (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Question primary key',
  title VARCHAR(255) NOT NULL COMMENT 'Question title',
  content TEXT NOT NULL COMMENT 'Question content',
  answer_reference TEXT DEFAULT NULL COMMENT 'Legacy reference answer field',
  correct_answer TEXT DEFAULT NULL COMMENT 'Correct answer or reference answer',
  analysis TEXT DEFAULT NULL COMMENT 'Question analysis and explanation',
  difficulty VARCHAR(32) NOT NULL DEFAULT 'EASY' COMMENT 'Difficulty: EASY, MEDIUM, HARD',
  question_type VARCHAR(64) NOT NULL COMMENT 'Question type: SINGLE_CHOICE, MULTIPLE_CHOICE, JUDGE, SHORT_ANSWER, CODING',
  creator_id BIGINT DEFAULT NULL COMMENT 'Creator user id',
  source VARCHAR(128) DEFAULT NULL COMMENT 'Question source',
  status TINYINT NOT NULL DEFAULT 1 COMMENT 'Question status: 1 published, 0 draft',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  KEY idx_question_difficulty (difficulty),
  KEY idx_question_type (question_type),
  KEY idx_question_creator_id (creator_id),
  KEY idx_question_status (status),
  KEY idx_question_create_time (create_time),
  KEY idx_question_deleted (deleted),
  FULLTEXT KEY ft_question_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Interview question table';

CREATE TABLE IF NOT EXISTS question_tag (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Question tag primary key',
  tag_name VARCHAR(64) NOT NULL COMMENT 'Unique tag name',
  description VARCHAR(255) DEFAULT NULL COMMENT 'Tag description',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_question_tag_name (tag_name),
  KEY idx_question_tag_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Question tag table';

CREATE TABLE IF NOT EXISTS question_tag_relation (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Question-tag relation primary key',
  question_id BIGINT NOT NULL COMMENT 'Question id',
  tag_id BIGINT NOT NULL COMMENT 'Tag id',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_question_tag_relation (question_id, tag_id),
  KEY idx_question_tag_relation_question_id (question_id),
  KEY idx_question_tag_relation_tag_id (tag_id),
  KEY idx_question_tag_relation_tag_question (tag_id, question_id),
  KEY idx_question_tag_relation_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Question-tag relation table';

CREATE TABLE IF NOT EXISTS user_answer_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Answer record primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  question_id BIGINT NOT NULL COMMENT 'Question id',
  user_answer TEXT NOT NULL COMMENT 'User submitted answer',
  answer_content TEXT DEFAULT NULL COMMENT 'Legacy user submitted answer field',
  is_correct TINYINT DEFAULT NULL COMMENT 'Auto judge result: 1 correct, 0 wrong, null pending',
  score DECIMAL(5,2) DEFAULT NULL COMMENT 'Auto judge or review score',
  answer_duration INT NOT NULL DEFAULT 0 COMMENT 'Answer duration in seconds',
  review_summary VARCHAR(512) DEFAULT NULL COMMENT 'Short review summary',
  answer_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Answer submission time',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  KEY idx_user_answer_record_user_time (user_id, create_time),
  KEY idx_user_answer_record_user_question (user_id, question_id),
  KEY idx_user_answer_record_question_id (question_id),
  KEY idx_user_answer_record_question_time (question_id, create_time),
  KEY idx_user_answer_record_user_create_time (user_id, create_time),
  KEY idx_user_answer_record_is_correct (is_correct),
  KEY idx_user_answer_record_create_time_is_correct (create_time, is_correct),
  KEY idx_user_answer_record_answer_time (answer_time),
  KEY idx_user_answer_record_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User answer record table';

CREATE TABLE IF NOT EXISTS user_favorite (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Favorite primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  question_id BIGINT NOT NULL COMMENT 'Question id',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_favorite_user_question (user_id, question_id),
  KEY idx_user_favorite_user_time (user_id, create_time),
  KEY idx_user_favorite_question_id (question_id),
  KEY idx_user_favorite_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User favorite question table';

CREATE TABLE IF NOT EXISTS user_wrong_question (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Wrong question primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  question_id BIGINT NOT NULL COMMENT 'Question id',
  wrong_count INT NOT NULL DEFAULT 1 COMMENT 'Number of wrong attempts',
  last_wrong_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Latest wrong answer time',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Wrong question status: ACTIVE, RESOLVED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_wrong_question_user_question (user_id, question_id),
  KEY idx_user_wrong_question_user_status (user_id, status),
  KEY idx_user_wrong_question_user_last_wrong_time (user_id, last_wrong_time),
  KEY idx_user_wrong_question_question_id (question_id),
  KEY idx_user_wrong_question_last_wrong_time (last_wrong_time),
  KEY idx_user_wrong_question_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User wrong question table';

CREATE TABLE IF NOT EXISTS ai_review_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI review primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  question_id BIGINT DEFAULT NULL COMMENT 'Question id for answer review records',
  answer_record_id BIGINT DEFAULT NULL COMMENT 'Related answer record id',
  record_type VARCHAR(64) NOT NULL DEFAULT 'ANSWER_REVIEW' COMMENT 'AI record type: ANSWER_REVIEW, MOCK_INTERVIEW, WEAKNESS_SUMMARY',
  input_content TEXT DEFAULT NULL COMMENT 'User input or generation parameters as JSON',
  result_content TEXT DEFAULT NULL COMMENT 'AI result as JSON',
  model_name VARCHAR(128) DEFAULT NULL COMMENT 'AI model name',
  interviewer_type VARCHAR(32) NOT NULL DEFAULT 'STARTUP' COMMENT 'Enterprise interviewer type: ALIBABA, TENCENT, BYTEDANCE, STARTUP',
  prompt TEXT DEFAULT NULL COMMENT 'Prompt sent to AI provider',
  review_result TEXT DEFAULT NULL COMMENT 'Legacy AI review result',
  score DECIMAL(5,2) DEFAULT NULL COMMENT 'AI generated score',
  share_token VARCHAR(128) DEFAULT NULL COMMENT 'Public share token',
  is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Public share flag: 0 private, 1 public',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  KEY idx_ai_review_record_user_id (user_id),
  KEY idx_ai_review_record_user_type_time (user_id, record_type, create_time),
  KEY idx_ai_review_record_question_id (question_id),
  KEY idx_ai_review_record_answer_record_id (answer_record_id),
  KEY idx_ai_review_record_create_time (create_time),
  KEY idx_ai_review_record_interviewer_type (interviewer_type),
  UNIQUE KEY uk_ai_review_record_share_token (share_token),
  KEY idx_ai_review_record_is_public (is_public),
  KEY idx_ai_review_record_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI review record table';

CREATE TABLE IF NOT EXISTS interview_template (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Interview template primary key',
  user_id BIGINT NOT NULL COMMENT 'Template owner user id',
  position_model VARCHAR(128) NOT NULL COMMENT 'Target position model',
  company_type VARCHAR(32) NOT NULL DEFAULT 'STARTUP' COMMENT 'Company interviewer style',
  difficulty VARCHAR(32) NOT NULL COMMENT 'Interview difficulty',
  question_count INT NOT NULL DEFAULT 5 COMMENT 'Question count',
  focus_areas TEXT DEFAULT NULL COMMENT 'Focus areas as JSON array',
  scoring_weights TEXT DEFAULT NULL COMMENT 'Scoring weights as JSON object',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  KEY idx_interview_template_user_time (user_id, create_time),
  KEY idx_interview_template_company_difficulty (company_type, difficulty),
  KEY idx_interview_template_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Enterprise interview template table';

CREATE TABLE IF NOT EXISTS ai_interview_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI interview session primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  review_record_id BIGINT DEFAULT NULL COMMENT 'Related ai_review_record id',
  interviewer_type VARCHAR(32) NOT NULL DEFAULT 'STARTUP' COMMENT 'Enterprise interviewer type',
  status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'Session status',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  KEY idx_ai_interview_session_user_status (user_id, status),
  KEY idx_ai_interview_session_record (review_record_id),
  KEY idx_ai_interview_session_interviewer_type (interviewer_type),
  KEY idx_ai_interview_session_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI interview session extension table';

CREATE TABLE IF NOT EXISTS daily_learning_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Daily learning record primary key',
  user_id BIGINT NOT NULL COMMENT 'User id',
  record_date DATE NOT NULL COMMENT 'Learning record date',
  answer_count INT NOT NULL DEFAULT 0 COMMENT 'Answered question count',
  correct_count INT NOT NULL DEFAULT 0 COMMENT 'Correct answer count',
  study_duration INT NOT NULL DEFAULT 0 COMMENT 'Learning duration in seconds',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0 active, 1 deleted',
  PRIMARY KEY (id),
  UNIQUE KEY uk_daily_learning_record_user_date (user_id, record_date),
  KEY idx_daily_learning_record_user_record_date (user_id, record_date),
  KEY idx_daily_learning_record_record_date (record_date),
  KEY idx_daily_learning_record_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Daily learning statistics table';

INSERT INTO sys_role (role_code, role_name, description)
VALUES
  ('USER', 'User', 'Regular platform user'),
  ('ADMIN', 'Admin', 'Platform administrator')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  update_time = CURRENT_TIMESTAMP;
