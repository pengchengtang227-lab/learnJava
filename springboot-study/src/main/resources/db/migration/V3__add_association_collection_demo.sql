-- 为 MyBatis association/collection 示例准备一对一和一对多数据。
CREATE TABLE IF NOT EXISTS study_user_profile (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '用户资料主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 ID，由业务层维护关联',
    nickname VARCHAR(50) NOT NULL COMMENT '展示昵称',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    UNIQUE KEY uk_study_user_profile_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户一对一资料示例';

CREATE TABLE IF NOT EXISTS study_user_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '订单主键',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '用户 ID，由业务层维护关联',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    amount_in_cents BIGINT NOT NULL COMMENT '订单金额，单位为分',
    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后修改时间',
    UNIQUE KEY uk_study_user_order_no (order_no(50)),
    KEY idx_study_user_order_user (user_id),
    CHECK (amount_in_cents >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户一对多订单示例';

-- 只补充一个固定的学习用户及其关联数据，重复启动不会重复插入。
INSERT INTO study_user (user_name, age, email, gender)
SELECT 'association-demo', 20, 'association-demo@example.com', 0
WHERE NOT EXISTS (
    SELECT 1 FROM study_user WHERE user_name = 'association-demo'
);

INSERT INTO study_user_profile (user_id, nickname, city)
SELECT u.id, '关联示例用户', '上海'
FROM study_user AS u
WHERE u.user_name = 'association-demo'
  AND NOT EXISTS (
      SELECT 1 FROM study_user_profile AS p WHERE p.user_id = u.id
  );

INSERT INTO study_user_order (user_id, order_no, status, amount_in_cents)
SELECT u.id, 'ASSOCIATION-ORDER-001', 'PAID', 1999
FROM study_user AS u
WHERE u.user_name = 'association-demo'
  AND NOT EXISTS (
      SELECT 1 FROM study_user_order AS o WHERE o.order_no = 'ASSOCIATION-ORDER-001'
  );

INSERT INTO study_user_order (user_id, order_no, status, amount_in_cents)
SELECT u.id, 'ASSOCIATION-ORDER-002', 'SHIPPED', 2999
FROM study_user AS u
WHERE u.user_name = 'association-demo'
  AND NOT EXISTS (
      SELECT 1 FROM study_user_order AS o WHERE o.order_no = 'ASSOCIATION-ORDER-002'
  );
