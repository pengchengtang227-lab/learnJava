-- 单位为分：小明 1000 元，小红 500 元。重启应用后会恢复这些初始余额。
INSERT INTO account (id, owner, balance_in_cents) VALUES (1, '小明', 100000);
INSERT INTO account (id, owner, balance_in_cents) VALUES (2, '小红', 50000);
