-- Experiment 4 RBAC schema. Existing user.role values remain compatible:
-- 0 = USER, 1 = ADMIN. Passwords and security answers are BCrypt hashes.
CREATE TABLE IF NOT EXISTS role (
    id INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS permission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role(id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission(id)
);

INSERT IGNORE INTO role (id, name, description) VALUES
    (0, 'USER', '普通用户'),
    (1, 'ADMIN', '管理员');

INSERT IGNORE INTO permission (code, description) VALUES
    ('product:read', '查看商品'),
    ('product:write', '发布和编辑商品'),
    ('user:read', '查看用户列表'),
    ('admin:stats', '查看管理统计');

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT 0, id FROM permission WHERE code IN ('product:read', 'product:write');
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;
