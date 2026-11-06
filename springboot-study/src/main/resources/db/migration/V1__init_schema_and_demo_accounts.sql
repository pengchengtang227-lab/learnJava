-- 接管原 schema.sql 和 data.sql；兼容已有学习库，保留用户与账户余额。
CREATE TABLE IF NOT EXISTS account (
    id BIGINT NOT NULL PRIMARY KEY COMMENT '演示账户 ID',
    owner VARCHAR(50) NOT NULL COMMENT '账户持有人',
    balance_in_cents BIGINT NOT NULL COMMENT '余额，单位为分',
    CHECK (balance_in_cents >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户转账演示';

CREATE TABLE IF NOT EXISTS transfer_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增转账记录 ID',
    from_account_id BIGINT NOT NULL COMMENT '转出账户 ID',
    to_account_id BIGINT NOT NULL COMMENT '转入账户 ID',
    amount_in_cents BIGINT NOT NULL COMMENT '转账金额，单位为分',
    CHECK (amount_in_cents > 0),
    CHECK (from_account_id <> to_account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账记录，账户存在性由业务层校验';

CREATE TABLE IF NOT EXISTS study_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键，由数据库生成',
    user_name VARCHAR(50) NOT NULL COMMENT '用户名，不能重复',
    age INT NOT NULL COMMENT '年龄，范围 0～150',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    UNIQUE KEY uk_study_user_name (user_name(50)),
    CHECK (age >= 0 AND age <= 150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MyBatis 用户 CRUD 学习表';

-- 只初始化缺失的演示账户，不重置已有余额；study_user 不预置数据。
INSERT INTO account (id, owner, balance_in_cents)
SELECT 1, '小明', 100000
WHERE NOT EXISTS (SELECT 1 FROM account WHERE id = 1);

INSERT INTO account (id, owner, balance_in_cents)
SELECT 2, '小红', 50000
WHERE NOT EXISTS (SELECT 1 FROM account WHERE id = 2);
