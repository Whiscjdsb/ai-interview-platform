-- Upgrade existing databases created before AI sharing and enterprise interview fields.
-- Run these statements only when the column or index is missing.

ALTER TABLE ai_review_record
  ADD COLUMN share_token VARCHAR(128) DEFAULT NULL COMMENT 'Public share token';

ALTER TABLE ai_review_record
  ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Public share flag: 0 private, 1 public';

ALTER TABLE ai_review_record
  ADD COLUMN interviewer_type VARCHAR(32) NOT NULL DEFAULT 'STARTUP' COMMENT 'Enterprise interviewer type: ALIBABA, TENCENT, BYTEDANCE, STARTUP';

CREATE UNIQUE INDEX uk_ai_review_record_share_token ON ai_review_record (share_token);

CREATE INDEX idx_ai_review_record_is_public ON ai_review_record (is_public);

CREATE INDEX idx_ai_review_record_interviewer_type ON ai_review_record (interviewer_type);

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
