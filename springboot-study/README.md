# Spring Boot 动手学习：账户转账与 MyBatis 用户管理

这是当前 Java 练习仓库中的独立 Maven 项目，通过一个转账流程串起 HTTP 接口、依赖注入、参数校验、数据库事务、全局异常处理和 AOP 日志。代码包含中文注释，建议按下方阅读顺序学习。

账户、转账记录和用户管理统一使用 MyBatis：Service 注入 Mapper 接口，SQL 放在 `src/main/resources/mapper/`。

批量新增用户可调用 `POST /api/users/batch`，一次最多 10000 条，每组 500 条通过 `foreach` 生成多行 INSERT，
全部成功后统一提交；调用示例和代码说明见 [分组批量插入](MYBATIS.md#八分组--foreach-多行插入)。

新增的 **MyBatis 用户 CRUD + 本地 MySQL** 示例见 [MYBATIS.md](MYBATIS.md)，包含连接配置、启动步骤、接口示例和源码阅读顺序。应用默认连接本机 MySQL，数据持久保存，直接运行启动类即可。

学习 `association` 和 `collection` 可看 [关联查询示例](MYBATIS.md#六关联查询association--collection)：一个用户关联一份资料和多个订单，重启后在 Swagger 的“MyBatis 关联查询”分组调用 `GET /api/users/{id}/relations`。

同一分组还提供 `GET /api/users/{id}/relations/nested-select`，通过嵌套查询分别加载用户、资料和订单，可对照 JOIN 接口的结果及 SQL 次数；详见 [嵌套查询说明](MYBATIS.md#七嵌套查询nested-select)。

## 技术版本与环境

| 项目 | 选择 | 原因 |
| --- | --- | --- |
| Java | 21 | 与当前仓库和本机 JDK 保持一致 |
| Spring Boot | 4.1.2 | 2026-10-24 从 Maven Central 核实的最新正式版 |
| Spring Framework | 7.0.10，由 Spring Boot 管理 | 避免手工组合 Spring 组件版本 |
| JDBC、Validation、日志 | 由 Spring Boot 统一管理 | 避免依赖版本冲突 |
| 数据库 | 本机 MySQL，驱动由 Spring Boot 管理 | 默认连接 springboot_study，重启保留数据 |
| MyBatis | Starter 4.1.0，适配 Spring Boot 4.1 | 通过 Mapper 接口和 XML 完成账户查询、转账和用户 CRUD |
| 数据库迁移 | Flyway，版本由 Spring Boot 管理 | 通过版本 SQL 管理初始化、字段和索引变更 |
| 接口操作页面 | springdoc-openapi 3.2.0 + Swagger UI | 浏览器中直接查看和调用接口 |
| Maven | Wrapper 固定为 3.9.16 | 本机无需预先安装 Maven，首次运行需要联网下载 |

Maven Central 同时存在 4.2.0-RC1 等预发布版本，本示例选择正式版。Boot 4 使用 `jakarta.*`，Web 和 AOP starter 分别使用 `spring-boot-starter-webmvc`、`spring-boot-starter-aspectj`。

外层项目的 `pom.xml` 和 Java 练习代码保持原样。本项目不继承外层 POM，请单独导入当前目录的 `pom.xml`。

## 启动与打包

先启动本机 MySQL、创建 `springboot_study` 库，并在 `application-local.properties` 中配置密码，步骤见 [MYBATIS.md](MYBATIS.md)。数据库连接统一配置在 `src/main/resources/application.yml`，无需设置数据库 profile。Flyway 负责版本迁移；已有库首次接入需按该文档的“数据库版本迁移”步骤登记基线，以后正常启动即可。

在仓库根目录的 PowerShell 终端执行：

```powershell
cd .\springboot-study
.\mvnw.cmd spring-boot:run
```

看到 `Started SpringStudyApplication` 后，访问 <http://127.0.0.1:8080/api/accounts>。端口被占用时可以用：

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

也可以在 IDEA 中右键 `springboot-study/pom.xml` → 添加为 Maven 项目，等待依赖下载，然后运行 `SpringStudyApplication.main()`。JDK 选择 21；若启动了其他 Java 主类，先切换运行配置。

### IDEA 提示“Java 文件位于模块源根之外，因此不会被编译”

这通常表示 IDEA 只导入了仓库根目录的 Maven 项目，还没有导入本目录的独立项目。

1. 在项目树中找到 **`springboot-study/pom.xml`**，右键选择 **添加为 Maven 项目 / Add as Maven Project**。
2. 如果右键菜单中没有该选项，打开右侧 **Maven** 工具窗口，点击 **关联 Maven 项目 / Link Maven Projects**（通常是加号），选择这个 POM。
3. 等待 Maven 同步和索引完成，确认 Maven 窗口里同时出现 `learnJava` 和 `springboot-study`，再打开启动类。

正确导入后，`src/main/java` 会被识别为源码目录，`src/main/resources` 为资源目录。优先通过 Maven 导入，让 IDEA 同时配置源码、依赖和 JDK。

```powershell
# 打包并运行
.\mvnw.cmd clean package
java -jar .\target\springboot-study-1.0.0.jar
```

macOS / Linux 可使用 `sh ./mvnw spring-boot:run` 启动应用。

## Swagger UI：在浏览器中调用接口

启动或重启应用后，打开 [Swagger UI](http://127.0.0.1:8080/swagger-ui.html)。页面按“用户管理”“账户与转账”“登录演示”分组，接口和字段提供中文说明。

| 操作 | 页面中的步骤 |
| --- | --- |
| 查看用户 | 展开“用户管理”中的 `GET /api/users`，点击 **Execute** |
| 新增用户 | 展开 `POST /api/users`，修改示例 JSON，点击 **Execute**；成功返回 201 |
| 查询、修改、删除 | 将新增响应的 `data.id` 填入对应接口的 `id` 输入框；修改时提交完整字段 |
| 调用登录演示 | 点击 **Authorize**，输入 `demo-token`，再执行 `GET /api/secure/profile` |

**Execute 会实际调用接口，新增、修改、删除会写入本机 MySQL。** 响应区会展示 HTTP 状态码和返回 JSON。用户名需要唯一，重复新增时修改示例中的 `userName`。

页面由依赖提供，配置位于 `application.yml` 的 `springdoc` 段；`@Tag` 定义接口分组，`@Operation` 描述操作，`@Schema` 描述请求字段和示例。机器可读的接口文档为 [OpenAPI JSON](http://127.0.0.1:8080/v3/api-docs)。

## Actuator 监控与管理

项目通过 `spring-boot-starter-actuator` 引入监控能力，无需自己编写 Controller。`application.yml` 开放了以下 HTTP 端点，启动后即可在浏览器中访问：

| 地址 | 用途 | 关注的内容 |
| --- | --- | --- |
| <http://127.0.0.1:8080/actuator/health> | 健康检查 | 总体 `status`，以及 `components.db` 中的 MySQL 数据库状态 |
| <http://127.0.0.1:8080/actuator/info> | 应用信息 | `info.app` 配置的名称和描述 |
| <http://127.0.0.1:8080/actuator/metrics> | 指标目录 | `names` 中列出的 JVM、连接池和 HTTP 等指标 |
| <http://127.0.0.1:8080/actuator/metrics/jvm.memory.used> | JVM 已用内存 | `measurements` 中的内存用量，单位为字节 |
| <http://127.0.0.1:8080/actuator/metrics/http.server.requests> | HTTP 请求统计 | 请求次数、总耗时和最大耗时；先发送一次请求再查看 |

健康检查正常时，总体状态为 `"status": "UP"`。配置中的 `show-details: always` 便于在本机查看数据库检查详情；应用当前只监听 `127.0.0.1`，对外部署时应限制监控访问，并调整健康详情的展示策略。

`management.info.env.enabled: true` 用于将显式配置的 `info.*` 信息加入 `/actuator/info`，与读取环境变量的 `/actuator/env` 是不同的功能。本项目按需开放 `health,info,metrics`，Actuator 响应使用框架自己的 JSON 结构。

若修改了应用端口，上述地址也要使用相同端口。引入依赖后，在 IDEA 中重新加载 Maven 项目并重启应用。

首次初始化时：小明（ID 1）100000 分，小红（ID 2）50000 分。请求执行次数会影响余额；MySQL 持久保存数据，重启应用不会恢复初始余额。

## 接口及异常结果

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| GET | `/api/accounts` | 查询两个演示账户 |
| GET | `/api/accounts/{id}` | 查询单个账户，ID 必须为正整数 |
| POST | `/api/transfers` | 转账；可选 `simulateFailure` 验证回滚 |

成功响应示例：

```json
{
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {
    "transferId": 1,
    "fromAccountId": 1,
    "toAccountId": 2,
    "amountInCents": 1000
  }
}
```

| 场景 | HTTP 状态 | 业务码 |
| --- | --- | --- |
| 正常查询或转账 | 200 | `SUCCESS` |
| 缺少金额、金额非正数、类型错误、JSON 格式错误 | 400 | `INVALID_REQUEST` |
| 转入和转出账户相同 | 400 | `SAME_ACCOUNT` |
| 账户不存在 | 404 | `ACCOUNT_NOT_FOUND` |
| 余额不足 | 409 | `INSUFFICIENT_BALANCE` |
| 主动模拟失败并回滚 | 409 | `DEMO_ROLLBACK` |
| 路径不存在、方法或媒体类型不支持 | 404 / 405 / 415 | `INVALID_REQUEST` |
| 未预期的服务端异常 | 500 | `INTERNAL_ERROR` |

失败响应的 `data` 为 `null`。完整异常堆栈只写入服务端日志，不发送给客户端。

## 定时任务学习示例

定时任务随当前 Spring Boot 应用一起启动，定期打印日志。正常运行 `SpringStudyApplication.main()` 或执行 `.\mvnw.cmd spring-boot:run` 即可。

| 文件 | 学习内容 |
| --- | --- |
| `SpringStudyApplication.java` | `@EnableScheduling` 开启调度 |
| `task/ScheduledTaskDemo.java` | `@Component` 管理任务对象，`@Scheduled` 声明触发规则，代码附有中文注释 |

| 任务 | 规则 | 应观察到什么 |
| --- | --- | --- |
| `fixedDelay` | 执行结束后等待 5 秒 | 本例只打印日志，约每 5 秒出现一次；若业务耗时 2 秒，相邻开始约间隔 7 秒 |
| `fixedRate` | 按每 5 秒一次安排执行 | 约每 5 秒打印一次 |
| `cron` | `*/10 * * * * *`，时区 `Asia/Shanghai` | 每分钟的第 0、10、20、30、40、50 秒附近触发 |

Spring cron 的六个字段是“秒 分 时 日 月 周”；每天北京时间凌晨 2 点执行可写 `@Scheduled(cron = "0 0 2 * * *", zone = "Asia/Shanghai")`。任务类只有交给 Spring 管理，且启用调度，注解才会被处理。本例方法均为无参、返回 `void`。

固定频率不代表同一个任务自动并行执行；任务耗时超过间隔时，后续执行可能推迟。常规默认调度池只有 1 个线程，耗时任务也可能影响其他任务。应用停机期间错过的执行，普通 `@Scheduled` 默认不会持久化并补跑。

如果用不同端口启动两份应用，两边都会打印日志，可以通过日志中的进程 ID 区分。每个进程都有自己的调度器，`@Scheduled` 不会选出一台机器独占执行。

| 业务目标 | 常见做法 |
| --- | --- |
| 重复触发也不产生重复业务效果 | 幂等，例如只对仍未支付且已过期的订单进行数据库条件更新 |
| 同一时刻只允许一个实例执行 | 使用正确实现的分布式锁，或调度平台的实例分派策略 |

## Spring 事件与启动任务学习示例

正常启动 `SpringStudyApplication.main()` 或执行 `.\mvnw.cmd spring-boot:run`，会自动触发一次用户注册事件示例。
在控制台搜索 `[事件示例]`，即可观察启动事件、Runner 和自定义事件的关系，无需发送 HTTP 请求。
注册、发送欢迎邮件均使用日志模拟，不实际写入用户数据或发送邮件。

| 文件 | 职责与关注点 |
| --- | --- |
| `event/UserRegisteredEvent.java` | 事件对象，只携带用户 ID；普通 Java 对象即可，不需要注册成 Bean |
| `service/UserRegistrationService.java`、`service/impl/UserRegistrationServiceImpl.java` | 模拟注册，通过构造器注入的 `ApplicationEventPublisher` 发布事件 |
| `event/UserRegisteredEventListener.java` | `@Component` 注册监听器，`@EventListener` 根据参数类型监听用户注册事件 |
| `task/EventDemoRunner.java` | 实现 `CommandLineRunner`，在容器刷新完成后调用 Service，触发一次示例 |
| `event/ApplicationLifecycleListener.java` | 监听 `ApplicationStartedEvent` 和 `ApplicationReadyEvent`，观察 Runner 前后的启动阶段 |

过滤出 `[事件示例]` 日志后，应看到以下顺序：

```text
ApplicationStartedEvent：容器刷新完成，尚未执行 Runner
Runner：启动任务开始
注册服务：模拟注册成功，准备发布事件，userId=1001
用户监听器：收到注册事件，模拟发送欢迎邮件，userId=1001
注册服务：发布方法已返回，同步监听器已经执行完毕
Runner：启动任务执行结束
ApplicationReadyEvent：Runner 已执行完，应用就绪
```

| 观察点 | 说明 |
| --- | --- |
| 发布订阅 | 注册服务不直接调用邮件监听器，而是发布“注册成功”事件，Spring 将事件交给匹配的监听器 |
| 默认同步 | 本例未配置异步广播器，也未使用 `@Async`，监听器与发布者在同一调用线程中执行；因此监听器日志出现在发布方法返回之前 |
| 两种事件来源 | `UserRegisteredEvent` 由业务代码发布；`ApplicationStartedEvent`、`ApplicationReadyEvent` 由 Spring Boot 发布 |
| Runner 与事件监听 | Runner 主动执行启动任务；监听器在对应事件发布时执行。本例由 Runner 触发业务，业务再发布自定义事件 |
| 早期启动事件 | `ApplicationStartingEvent` 等发生时，普通监听器 Bean 尚未准备好，不能直接套用本例的组件监听方式，需要提前注册监听器 |

建议依次在 Runner、注册 Service 的 `publishEvent(...)`、用户监听器和 `onReady(...)` 处打断点。
观察 Service 到监听器的调用栈，再返回 Service，能够直接看出默认同步执行的特点。

## 推荐阅读顺序

Java 源码统一位于 `src/main/java/com/example/springstudy/`。

| 顺序 | 文件 | 重点 |
| --- | --- | --- |
| 1 | `SpringStudyApplication.java` | 启动、自动配置、组件扫描范围 |
| 2 | `controller/AccountController.java` | URL 映射、JSON、构造器注入、`@Valid` |
| 3 | `dto/TransferRequest.java` | 请求对象和 Jakarta Validation 字段约束 |
| 4 | `service/AccountService.java` | 接口与实现分离，Controller 依赖业务接口 |
| 5 | `service/impl/AccountServiceImpl.java` | 三次数据库写入、事务边界和回滚 |
| 6 | `mapper/AccountMapper.java`、`TransferMapper.java` 及对应 XML | record 构造器映射、条件扣款、更新行数和自增主键回填 |
| 7 | `exception/GlobalExceptionHandler.java` | 业务异常、校验异常、框架异常、500 兜底 |
| 8 | `aspect/ApiLogAspect.java` | 切点、环绕通知、`proceed()`、耗时统计 |
| 9 | `src/main/resources/` | 数据源配置、建表和初始数据 |

## 一次请求是怎样执行的

```text
HTTP 请求 → Spring MVC 解析参数并校验
         → AOP 日志：开始
         → AccountController
         → AccountService 的事务代理：开启事务
         → AccountServiceImpl.transfer()
             ① 转出账户扣款
             ② 转入账户入账
             ③ 保存转账记录
         → 事务代理：正常返回则提交；异常则按规则回滚
         → AOP 日志：结束、方法是否正常返回、耗时
         → 正常结果由 MVC 输出；异常由 GlobalExceptionHandler 转成 JSON
```

三次写入依次调用 `AccountMapper.deductBalance`、`AccountMapper.addBalance`、`TransferMapper.insertTransfer`。
`AccountDO` 保持不可变 record，通过 XML 的 `<constructor>` 映射查询结果；
`TransferRecordDO` 使用可变 JavaBean，数据库生成的主键通过 `useGeneratedKeys` 回填到 `id`，再组成原有 `TransferVO` 响应。

参数绑定和校验发生在调用 Controller 之前，因此这类错误可能没有 Controller 的 AOP 日志。这一切面统计的是方法执行情况，`正常返回=false` 表示方法抛出了异常，不能直接当作 HTTP 状态码。

## 关于事务，重点观察这些地方

| 问题 | 本示例中的答案 |
| --- | --- |
| 为什么放在 Service？ | 一次业务涉及扣款、入账和写记录，必须一起提交或撤销 |
| 为什么两个 Mapper 能共享事务？ | MyBatis-Spring 使用同一数据源，加入 Service 的 Spring 事务，并复用事务中的数据库连接 |
| 默认哪些异常会回滚？ | `RuntimeException` 和 `Error`；本例通过 `rollbackFor = Exception.class` 包含受检异常 |
| 全局处理器会不会阻止回滚？ | 异常先穿过事务代理完成回滚，再交给 MVC 异常处理器 |
| 为什么不能随便 catch？ | 吞掉异常后，事务代理可能看到正常返回而提交 |
| 为什么同类内部调用有坑？ | `this.xxx()` 不经过 Spring 代理，方法上的事务通知可能不会执行 |
| 回滚后记录 ID 为什么可能跳号？ | 自增序列不保证回滚后重用；应检查记录是否存在，不能用 ID 连续性判断事务 |

可以在 `transfer()` 的三次写入处、模拟异常处和全局处理器处打断点，观察异常如何穿过事务代理到达全局异常处理器。

本示例只演示单数据源事务。`simulateFailure` 是学习开关；真实资金系统还需要身份验证、权限控制、幂等、防重复提交和完整审计等设计。应用默认只监听本机地址。

## 账户转账回归验证

执行 `.\mvnw.cmd test`，运行 `src/test/java/com/example/springstudy/AccountTransferMyBatisTest.java`。
测试使用独立 H2 内存库、真实 Mapper XML 和 Spring 事务代理，验证账户查询、转账主键回填、条件扣款、
余额不足、账户不存在、同账户拦截，以及模拟失败和插入异常时的余额与记录回滚，不连接本机 MySQL。

参考：[Spring Boot 依赖版本元数据](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml)、[Spring Boot 官方文档](https://docs.spring.io/spring-boot/)、[JavaGuide Spring 总结](https://javaguide.cn/system-design/framework/spring/spring-knowledge-and-questions-summary.html)。
