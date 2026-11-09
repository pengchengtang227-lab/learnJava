# MyBatis + 本地 MySQL：用户 CRUD

本示例在现有 Spring Boot 工程中添加用户管理，调用流程为：

`HTTP → UserController → UserServiceImpl → UserMapper 代理 → UserMapper.xml → MySQL`

用户表为 `study_user`，与账户转账示例分开。代码、Mapper XML 和建表 SQL 都包含中文注释。
账户转账也使用 MyBatis：`AccountMapper.xml` 负责账户查询和余额更新，`TransferMapper.xml` 负责保存转账记录，
两者由 `AccountServiceImpl` 的同一个 Spring 事务管理。

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
| `V3__add_association_collection_demo.sql` | 创建用户资料和订单表，补充 association-demo 用户、一份资料和两个订单 |

**新建的空数据库**：直接按前述方式启动，Flyway 依次执行 V1、V2、V3。

**已有表、但没有 Flyway 历史表的数据库**：首次接入前，确认连接的是目标学习库，表结构与 V1 一致，尚未手动添加 `gender` 或同名邮箱索引。然后只在首次启动时使用：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.flyway.baseline-on-migrate=true"
```

配置中的 `baseline-version: 0` 会先为旧库登记基线 0，再执行兼容已有表的 V1 和后续 V2、V3。迁移成功后，以后正常启动即可，不再携带这次的临时参数；默认保持 `baseline-on-migrate: false`。

Flyway 将执行结果记入 `flyway_schema_history`，重启会校验已执行版本并跳过它们。后续变更新增 `V4__描述.sql`、`V5__描述.sql` 等文件，**不要修改已经执行的版本脚本，也不要再手动执行 V2 的 ALTER 语句**。生产环境通常由发布流程执行迁移。

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

## 六、关联查询：association / collection

本例用一次 LEFT JOIN 查询，把平铺的多行结果组装成一个用户对象。

| 标签 | Java 属性 | 数据关系 | 无关联数据时 |
| --- | --- | --- | --- |
| `association` | `UserProfileDO profile` | `study_user` → `study_user_profile`，一对一 | `profile: null` |
| `collection` | `List<UserOrderDO> orders` | `study_user` → `study_user_order`，一对多 | `orders: []` |

资料表的 `user_id` 有唯一索引，保证每个用户最多一份资料；订单表允许同一个用户有多条订单。

### 1. 启动后直接调用

1. 重启应用，Flyway 自动执行新增 V3，创建 `association-demo` 用户、一份资料和两个订单；保留已有用户数据。
2. 调用 `GET /api/users/dynamic-sql/search?userName=association-demo`，取得响应中 `data[0].id`。
3. 在 Swagger 的 **MyBatis 关联查询** 分组中调用 `GET /api/users/{id}/relations`，填写刚取得的 ID。

成功响应示例（所有 ID 以本机实际结果为准）：

```json
{
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {
    "id": 1,
    "userName": "association-demo",
    "profile": {
      "id": 1,
      "nickname": "关联示例用户",
      "city": "上海"
    },
    "orders": [
      {
        "id": 1,
        "orderNo": "ASSOCIATION-ORDER-001",
        "status": "PAID",
        "amountInCents": 1999
      },
      {
        "id": 2,
        "orderNo": "ASSOCIATION-ORDER-002",
        "status": "SHIPPED",
        "amountInCents": 2999
      }
    ]
  }
}
```

金额单位为分，`1999` 表示 19.99 元。已有普通用户也能调用此接口；没有资料、订单时仍返回用户，`profile` 为 null、`orders` 为空数组。用户不存在返回 404，ID 非正数或类型错误返回 400。

### 2. 对照 XML 阅读

核心代码位于 `src/main/resources/mapper/UserMapper.xml` 的 `userRelationsMap` 与 `getUserRelations`：

```xml
<association property="profile" javaType="UserProfileDO" notNullColumn="profile_id">
    <id column="profile_id" property="id"/>
    <result column="nickname" property="nickname"/>
    <result column="city" property="city"/>
</association>

<collection property="orders" ofType="UserOrderDO" notNullColumn="order_id">
    <id column="order_id" property="id"/>
    <result column="order_no" property="orderNo"/>
    <result column="status" property="status"
            typeHandler="org.apache.ibatis.type.EnumTypeHandler"/>
    <result column="amount_in_cents" property="amountInCents"/>
</collection>
```

| 观察点 | 本例含义 |
| --- | --- |
| `property` | Java 结果对象中的属性名：`profile`、`orders` |
| `javaType` | 单个关联对象的类型：`UserProfileDO` |
| `ofType` | 集合中每个元素的类型：`UserOrderDO`，不是 `List` |
| `column` | SQL 返回列名或别名；三张表的 ID 分别命名为 `user_id`、`profile_id`、`order_id` |
| `<id>` | 标识同一个用户或子对象，让同一用户的两行订单组装为一个用户和两个订单 |
| `notNullColumn` | 关联 ID 非空时才创建子对象，避免 LEFT JOIN 无匹配时出现空对象 |
| 类型别名 | `application.yml` 已扫描 `model` 包，XML 中的类型无需写包名 |

调用链为 `UserRelationController → UserServiceImpl → UserMapper → UserMapper.xml`。普通查询和关联查询统一使用 `UserDO`；关联接口返回完整的用户基础字段，并填充 `profile`、`orders`。上面的 JSON 省略了年龄、邮箱、性别和时间字段。普通 CRUD 不加载关联数据，这两个属性返回 null；原删除用户接口会在同一事务中清理资料和订单。

### 3. 订单状态枚举与 TypeHandler

`UserOrderDO.status` 使用 `UserOrderStatusEnum`，包含 `PAID`（已支付）和 `SHIPPED`（已发货）。
数据库仍使用 `VARCHAR(20)` 保存枚举名称，无需修改表结构、历史数据或已执行的 Flyway 脚本。

| 位置 | 表示方式 |
| --- | --- |
| Java 属性 | `UserOrderStatusEnum.PAID` |
| 数据库字段 | `PAID` |
| 接口 JSON | `"status": "PAID"` |

JOIN 的订单映射和嵌套查询的 `userOrderResultMap` 都显式指定内置 `EnumTypeHandler`，
按 `name()` 写入、按枚举名称读取；它本身也是 MyBatis 默认的枚举处理器，无需自定义处理器或包扫描配置。
业务判断可写成 `order.getStatus() == UserOrderStatusEnum.PAID`。

枚举名称区分大小写；数据库出现未定义的状态会导致映射失败。新增状态时先补充枚举常量，
修改已有常量名则需要同步迁移数据库值，并评估接口兼容性。TypeHandler 只负责数据库转换，
JSON 由 Jackson 默认将枚举序列化为名称，因此现有响应格式保持一致。

## 七、嵌套查询：Nested Select

复用上一节的表、实体和 `association-demo` 数据，无需新增建表 SQL。重启后在 Swagger 的 **MyBatis 关联查询** 分组中，用同一个实际用户 ID 对比以下两个接口：

| 接口 | 实现 | 用户存在且无缓存命中时的 SQL 次数 |
| --- | --- | --- |
| `GET /api/users/{id}/relations` | 嵌套结果映射，一次 LEFT JOIN 查询 | 1 次 |
| `GET /api/users/{id}/relations/nested-select` | 嵌套查询，先查用户再查资料和订单 | 3 次 |

两个接口返回相同的 `UserDO` 结构。本例明确设置 `fetchType="eager"` 立即加载，让三次查询在返回前完成；不是延迟加载示例。

### 1. 先看主查询与映射

`UserMapper.xml` 中的 `getUserRelationsByNestedSelect` 只查询 `study_user`，通过 `userNestedSelectMap` 处理结果：

```xml
<resultMap id="userNestedSelectMap" type="UserDO" extends="userResultMap" autoMapping="false">
    <association property="profile" javaType="UserProfileDO"
                 column="{userId=id}" select="getUserProfileByUserId" fetchType="eager"/>
    <collection property="orders" ofType="UserOrderDO"
                column="{userId=id}" select="listUserOrdersByUserId" fetchType="eager"/>
</resultMap>
```

| 配置 | 含义 |
| --- | --- |
| `extends="userResultMap"` | 复用已有的用户基础字段映射，避免重复写 id、用户名、年龄等规则 |
| `column="{userId=id}"` | 右侧 `id` 是主查询返回列，左侧 `userId` 是传给子查询的参数名 |
| `select="getUserProfileByUserId"` | 调用同一个 XML 中对应的 select，查询结果放入 `profile` |
| `select="listUserOrdersByUserId"` | 调用订单查询，将列表放入 `orders` |
| `fetchType="eager"` | 立即执行关联查询，覆盖该属性的全局延迟加载设置 |

例如主查询返回 `id=1`，MyBatis 会给两个子查询传入 `userId=1`。子查询中的 `#{userId}` 使用 JDBC 参数绑定。

### 2. 从日志观察三次 SQL

| 步骤 | 映射语句 id | 查询条件 | 填充结果 |
| --- | --- | --- | --- |
| 1 | `getUserRelationsByNestedSelect` | `study_user.id = #{id}` | 用户基础字段 |
| 2 | `getUserProfileByUserId` | `study_user_profile.user_id = #{userId}` | 单个 `profile` |
| 3 | `listUserOrdersByUserId` | `study_user_order.user_id = #{userId}` | `orders` 列表 |

项目已开启 Mapper DEBUG 日志，执行新接口后观察这三条 `Preparing` 和对应 `Parameters`。Service 只调用第一个 Mapper 方法，后两个查询由 MyBatis 自动触发；这里的嵌套查询不是 SQL 中的子查询。

用户没有资料时 `profile` 为 null，没有订单时 `orders` 为 []；用户不存在只执行主查询，接口返回 404。ID 非正数或类型错误返回 400。

### 3. 为什么会产生 N+1

当前接口只查一个用户，立即加载两种关联时通常执行 3 次 SQL。若改为查询 N 个用户，并给每个用户分别查询资料和订单，在无缓存命中的情况下可能执行 `1 + 2N` 次 SQL；只查询一种关联时则是 `1 + N` 次。

嵌套查询支持延迟加载。若要练习，可将关联上的 `fetchType="eager"` 改成 `fetchType="lazy"`；若要跟随全局 `lazy-loading-enabled` 配置，应去掉显式的 `fetchType`。HTTP 接口将对象序列化为 JSON 时也可能访问关联属性并触发查询，因此延迟加载不保证消除 N+1。
