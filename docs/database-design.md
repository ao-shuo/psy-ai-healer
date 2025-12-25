# 数据库设计说明

## 核心实体
- `users`：主表，保存用户名、BCrypt 密码、姓名和邮箱。`created_at` 使用 `CURRENT_TIMESTAMP`，保证后端 `User` 实体与 JPA 自动填充字段对齐。
- `user_roles`：通过 `@ElementCollection` 维护 `Role`（`USER` / `ADMIN` / `THERAPIST`）的多对一关系；`user_id` 是外键，`ON DELETE CASCADE` 保证用户删除时角色记录一并移除。
- `phq9_assessments`、`therapy_sessions`：均通过 `user_id` 关联 `users`。每次心理评估或会话都必须绑定到存在的用户。
- `therapy_messages`：通过 `session_id` 关联 `therapy_sessions`，含 `sender` 枚举与 `content` 等字段。
- `knowledge_articles`：目前独立表，可根据业务加 `author_id` 做关联；`published` 标记控制前端展示。

## 外键关系图
1. `user_roles(user_id)` → `users(id)`
2. `phq9_assessments(user_id)` → `users(id)`
3. `therapy_sessions(user_id)` → `users(id)`
4. `therapy_messages(session_id)` → `therapy_sessions(id)`

当前的数据库结构保证用户删除时同时清理所有相关角色、评估与会话。

## 角色注册策略
- 新增 `THERAPIST` 角色，后端在注册阶段会根据 `role` 字段决定用户身份。
- 管理员/心理咨询师角色必须提供配置中的“注册码”（`app.registration.admin-code`、`app.registration.therapist-code`），否则注册请求会被拒绝。这些代码可以在部署环境中通过 `application.yml` 或环境变量配置，以防止普通访客直接成为高权限账户。

## 示例批量插入提示
```sql
USE psy_ai_healer;
ALTER TABLE user_roles MODIFY COLUMN role ENUM('USER','ADMIN','THERAPIST') NOT NULL;
INSERT INTO users (username, password, full_name, email) VALUES
  ('admin', '$2a$10$...hash...', '管理员', 'admin@example.com');
INSERT INTO user_roles (user_id, role) VALUES
  (1, 'ADMIN');
```

前端通过 `/auth/register` 调用时，会根据 `RegisterRequest.role` 和 `registrationCode` 控制想要成为的身份，因此数据库里只会出现符合验证规则的角色。
