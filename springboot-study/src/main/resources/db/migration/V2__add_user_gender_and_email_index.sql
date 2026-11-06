-- 一次 ALTER 完成字段、取值约束和普通索引的变更；已有用户性别默认为未知。
-- email 索引覆盖 VARCHAR(100) 的完整长度，允许不同用户使用相同邮箱。
ALTER TABLE study_user
    ADD COLUMN gender TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '性别：0 未知，1 男，2 女',
    ADD CONSTRAINT chk_study_user_gender CHECK (gender IN (0, 1, 2)),
    ADD INDEX idx_study_user_email (email(100));
