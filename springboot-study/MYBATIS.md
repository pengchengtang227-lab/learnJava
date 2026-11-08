# MyBatis + 本地 MySQL：用户 CRUD

本示例在现有 Spring Boot 工程中添加用户管理，调用流程为：

`HTTP → UserController → UserServiceImpl → UserMapper 代理 → UserMapper.xml → MySQL`

用户表为 `study_user`，与账户转账示例分开。代码、Mapper XML 和建表 SQL 都包含中文注释。

## 一、连接并启动

| 配置项 | 默认值 | 调整方式 |
| --- | --- | --- |
| 主机 | `127.0.0.1` | `MYSQL_HOST` 环境变量 |
| 端口 | `3306` | `MYSQL_PORT` 环境变量 |
| 库名 | `springboot_study` | `MYSQL_DATABASE` 环境变量 |
| 用户名 | `root` | 本地配置或 `MYSQL_USERNAME` 环境变量 |
| 密码 | 从本地配置读取 | `application-local.properties`；也支持 `MYSQL_PASSWORD` 环境变量 |

1. 确认本机 MySQL 已启动。首次使用时创建学习库：

   ```powershell
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS springboot_study CHARACTER SET utf8mb4;"
   ```

   `-p` 会在终端提示输入密码，不要将密码直接拼在命令行中。

2. 在 `springboot-study` 目录编辑 `application-local.properties`，填写 `spring.datasource.password`；文件不存在时直接新建，内容参考下方。该文件集中保存本机连接信息，已有密码可继续使用。

   ```properties
   # 在本机文件中填写实际值，文件已加入 .gitignore。
   spring.datasource.password=填写本机密码
   # 用户名与默认值不同时再配置：
   # spring.datasource.username=root
   ```

   这是 Java Properties 文件，值无需额外加引号；密码中的反斜杠要按 Properties 规则写成 `\\`。实际配置文件留在工程目录外部资源位置，不打包进 jar。若改用 `MYSQL_PASSWORD` 环境变量，应删除本地文件的密码配置行，避免本地值覆盖环境变量占位符。

3. 从仓库根目录进入工程，直接启动：

   ```powershell
   cd .\springboot-study
   .\mvnw.cmd spring-boot:run
   ```

   IDEA 中直接运行 `SpringStudyApplication`，无需填写数据库 profile。工作目录使用仓库根目录或 `springboot-study` 均可。

4. 打开 [Swagger UI](http://127.0.0.1:8080/swagger-ui.html)，展开“用户管理”中的接口，填写参数并点击 **Execute**。也可以直接访问 <http://127.0.0.1:8080/api/users> 查看用户列表。

应用启动时由 Flyway 执行尚未执行的版本脚本，初始化表、演示账户并升级结构。**重启不会清空用户表，也不会重置现有账户余额。** 应用使用本机 MySQL 的 `springboot_study` 学习库。已有数据库首次接入 Flyway 的步骤见下方“数据库版本迁移”。

本地连接配置统一放在 `src/main/resources/application.yml`，密码单独放在 `application-local.properties`。Mapper 日志开启 DEBUG，可观察 `Preparing`、`Parameters` 和 `Total`。

## 二、CRUD 接口

| 操作 | HTTP 方法与路径 | 结果 |
| --- | --- | --- |
| 新增 | `POST /api/users` | 201，返回用户、自增 ID 和数据库时间 |
| 查询列表 | `GET /api/users` | 200，按 ID 排序；本例返回全部用户 |
| 查询详情 | `GET /api/users/{id}` | 200；用户不存在则 404 |
| 修改 | `PUT /api/users/{id}` | 200，返回更新后的完整用户数据；需提交完整的可编辑字段；用户不存在则 404 |
| 删除 | `DELETE /api/users/{id}` | 200，`data` 为 null；重复删除为 404 |

新增或修改的 JSON：

```json
{
  "userName": "alice",
  "age": 20,
  "email": "alice@example.com",
  "gender": 1
}
```

新增成功后，请使用实际返回的 `data.id` 进行详情、修改和删除操作；MySQL 自增 ID 不保证从 1 开始，也不保证连续。

`gender` 使用数字：`0` 未知、`1` 男、`2` 女。兼容旧请求，省略或传 `null` 时保存为 `0`；PUT 是完整字段更新，省略性别会将其设为未知。迁移前已有用户的性别为 `0`。邮箱使用普通索引，允许不同用户使用相同邮箱。

| 异常场景 | HTTP 状态与业务码 |
| --- | --- |
| 用户名为空或超长、年龄不在 0～150、邮箱不合法、性别不在 0～2、ID 非正数 | 400 / `INVALID_REQUEST` |
| 用户不存在 | 404 / `USER_NOT_FOUND` |
| 用户名违反唯一索引 | 409 / `DATA_CONFLICT` |

数据库唯一约束负责并发情况下的最终防重复；`#{}` 参数绑定让输入作为数据传递。PUT 是完整字段更新，第五节提供带 HTTP 接口的动态 SQL 示例，其中 PATCH 用于部分更新。

更新先执行一次 `UPDATE`，通过返回的匹配行数判断用户是否存在，无需提前查询；成功后再执行一次 `SELECT`，返回包含数据库时间字段的完整用户数据。MySQL 连接显式设置 `useAffectedRows=false`，确保相同内容重复更新仍返回匹配行数。

## 三、数据库版本迁移

数据库结构与初始化数据统一由 Flyway 管理，已停用 `spring.sql.init`，原 `db/mysql/schema.sql`、`data.sql` 的内容迁入 V1。脚本默认位置是 `src/main/resources/db/migration`。

| 脚本 | 作用 |
| --- | --- |
| `V1__init_schema_and_demo_accounts.sql` | 创建原有三张表，只补齐缺失的演示账户，保留已有用户与余额 |
| `V2__add_user_gender_and_email_index.sql` | 增加 `gender` 字段、0～2 取值约束和 `idx_study_user_email` 普通索引 |

**新建的空数据库**：直接按前述方式启动，Flyway 依次执行 V1、V2。

**已有表、但没有 Flyway 历史表的数据库**：首次接入前，确认连接的是目标学习库，表结构与 V1 一致，尚未手动添加 `gender` 或同名邮箱索引。然后只在首次启动时使用：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.flyway.baseline-on-migrate=true"
```

配置中的 `baseline-version: 0` 会先为旧库登记基线 0，再执行兼容已有表的 V1 和本次 V2。迁移成功后，以后正常启动即可，不再携带这次的临时参数；默认保持 `baseline-on-migrate: false`。

Flyway 将执行结果记入 `flyway_schema_history`，重启会校验已执行版本并跳过它们。后续变更新增 `V3__描述.sql`、`V4__描述.sql` 等文件，**不要修改已经执行的版本脚本，也不要再手动执行 V2 的 ALTER 语句**。生产环境通常由发布流程执行迁移。

可在数据库控制台检查本次结果：

```sql
SHOW COLUMNS FROM study_user LIKE 'gender';
SHOW INDEX FROM study_user;
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

`idx_study_user_email` 索引覆盖完整邮箱长度，支持按邮箱等值查询；是否被优化器选用取决于实际 SQL 和数据量。此次不新增按性别的索引。

## 四、建议源码阅读顺序

下面路径相对于 `springboot-study`：

| 顺序 | 文件 | 重点 |
| --- | --- | --- |
| 1 | `pom.xml`、`src/main/resources/application.yml` | MyBatis、Flyway、MySQL 驱动与连接配置 |
| 2 | `src/main/resources/db/migration/V1__init_schema_and_demo_accounts.sql`、`V2__add_user_gender_and_email_index.sql` | 初始结构、演示数据、字段与索引的版本变更 |
| 3 | `src/main/java/com/example/springstudy/model/UserDO.java` | JavaBean、字段映射、为何需要 setter 回填 ID |
| 4 | `src/main/java/com/example/springstudy/dto/SaveUserRequest.java` | 请求字段与校验约束 |
| 5 | `src/main/java/com/example/springstudy/controller/UserController.java` | HTTP 方法、参数校验、统一响应 |
| 6 | `src/main/java/com/example/springstudy/service/impl/UserServiceImpl.java` | 业务检查、事务、新增和更新后查询、更新匹配行数 |
| 7 | `src/main/java/com/example/springstudy/mapper/UserMapper.java` | Mapper 代理、@Param、影响行数 |
| 8 | `src/main/resources/mapper/UserMapper.xml` | namespace/id、resultMap、#{}、主键回填、按 ID 更新和删除 |

在 `insertUser` 前后设置断点，观察 `user.id` 从空值变为数据库生成的主键；再对照日志中的 SQL 与参数，理解 Mapper 接口如何调用 XML。

## 五、动态 SQL：三个接口覆盖常用标签

启动项目后，在 Swagger UI 中展开 **MyBatis 动态 SQL** 分组，填写参数并执行。控制台的 `Preparing` 展示带 `?` 的 SQL，`Parameters` 展示绑定的参数值。

| 接口 | 使用的标签 | 示例 |
| --- | --- | --- |
| `GET /api/users/dynamic-sql/search` | `if`、`where`、`choose`、`when`、`otherwise`、`bind` | `?namePrefix=ali&age=20` |
| `GET /api/users/dynamic-sql/by-ids` | `trim`、`foreach`，配合 `choose` 处理空集合 | `?ids=1,2,3` |
| `PATCH /api/users/dynamic-sql/{id}` | `if`、`set` | 只提交 `{"email":"alice.new@example.com"}` |

调用链为 `UserDynamicSqlController → UserServiceImpl → UserMapper → UserMapper.xml`。XML 中的三个例子包含标签说明；建议先看 HTTP 参数，再看 Mapper 的 `@Param`，最后对照 SQL。

### 1. 条件查询：if / where / choose / bind

以下路径都使用 GET，可直接放进浏览器或在 Swagger 中填写对应参数：

```text
/api/users/dynamic-sql/search?age=20
/api/users/dynamic-sql/search?namePrefix=ali
/api/users/dynamic-sql/search?namePrefix=ali&age=20
/api/users/dynamic-sql/search?userName=alice&namePrefix=bob
```

- `if`：年龄非 null 时才加入年龄条件；数值 0 也有效。
- `choose/when/otherwise`：有 `userName` 时精确查询；否则才考虑 `namePrefix`。两者都填时只使用精确姓名，不会在没查到记录后回退。
- `bind`：把 `ali` 生成 `ali%`，再通过 `#{namePattern}` 绑定；SQL 仍是 `LIKE ?`。输入中的 `%`、`_` 保留 LIKE 通配符含义。
- `where`：有条件才添加 WHERE，并删除开头的 AND/OR。所有参数都省略时查询全部用户。

例如只传 `namePrefix=ali&age=20`，查询条件为 `WHERE user_name LIKE ? AND age = ?`，参数依次是 `ali%`、`20`。

### 2. 多个 ID：trim / foreach

```text
/api/users/dynamic-sql/by-ids?ids=1,2,3
```

ID 请替换成已有用户的实际 ID，也支持 `?ids=1&ids=2`。每个 ID 必须为正数，一次最多 1000 项。省略或空列表返回空结果，不会查询全部用户。

- `trim`：用 `prefix="WHERE"` 添加前缀，`prefixOverrides="AND |OR "` 去掉开头多余的 AND/OR，可以与第一个例子的 `where` 对照。
- `foreach`：`collection="ids"` 对应 `@Param("ids")`；`item="id"` 是当前元素名，括号和分隔符由 `open/close/separator` 生成。

三个 ID 生成 `WHERE id IN (?, ?, ?)`。直接调用 Mapper 时，null/空集合也会走 `WHERE 1 = 0` 分支。

### 3. 部分更新：if / set

在 Swagger 中选择 PATCH，填写实际用户 ID，请求体只保留要修改的字段，例如：

```json
{
  "email": "alice.new@example.com"
}
```

`if` 只加入非 null 字段；`set` 添加 SET 并删除末尾多余逗号。本次 SQL 结构为：

```sql
UPDATE study_user
SET email = ?, gmt_modified = CURRENT_TIMESTAMP
WHERE id = ?
```

用户名、年龄、性别和创建时间保留原值；返回值通过再次查询获得完整数据。省略字段或显式传 null 都表示“不修改”，不能通过此接口把字段清空为数据库 NULL。年龄和性别传 0 会正常更新。

空请求 `{}`、非法字段返回 400；目标用户不存在返回 404；用户名重复返回 409。与原有 PUT 相比，PATCH 适合只改部分字段，PUT 仍要求提交完整可编辑字段。
