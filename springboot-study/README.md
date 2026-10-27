# Spring Boot 动手学习：账户转账

这是当前 Java 练习仓库中的独立 Maven 项目，通过一个转账流程串起 HTTP 接口、依赖注入、参数校验、数据库事务、全局异常处理和 AOP 日志。代码包含中文注释，建议按下方阅读顺序学习。

## 技术版本与环境

| 项目 | 选择 | 原因 |
| --- | --- | --- |
| Java | 21 | 与当前仓库和本机 JDK 保持一致 |
| Spring Boot | 4.1.2 | 2026-10-24 从 Maven Central 核实的最新正式版 |
| Spring Framework | 7.0.10，由 Spring Boot 管理 | 避免手工组合 Spring 组件版本 |
| JDBC、Validation、日志 | 由 Spring Boot 统一管理 | 避免依赖版本冲突 |
| 数据库 | H2 2.4.240 内存数据库，由 Spring Boot 管理 | 无需安装 MySQL，仍可执行真实 SQL 和事务 |
| Maven | Wrapper 固定为 3.9.16 | 本机无需预先安装 Maven，首次运行需要联网下载 |

Maven Central 同时存在 4.2.0-RC1 等预发布版本，本示例选择正式版。Boot 4 使用 `jakarta.*`，Web 和 AOP starter 分别使用 `spring-boot-starter-webmvc`、`spring-boot-starter-aspectj`。

外层项目的 `pom.xml` 和 Java 练习代码保持原样。本项目不继承外层 POM，请单独导入当前目录的 `pom.xml`。

## 启动和测试

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

正确导入后，`src/main/java` 会被识别为源码目录，`src/main/resources` 为资源目录，`src/test/java` 为测试源码目录。优先通过 Maven 导入，让 IDEA 同时配置源码、依赖和 JDK。

```powershell
# 自动化测试
.\mvnw.cmd test

# 打包并运行
.\mvnw.cmd clean package
java -jar .\target\springboot-study-1.0.0.jar
```

macOS / Linux 可使用 `sh ./mvnw test` 或 `sh ./mvnw spring-boot:run`。

集成测试包含一个主动触发数据库溢出的用例，用来验证 500 兜底和事务回滚；运行时会出现该用例预期的 ERROR 堆栈，最终以 `Tests run: 20, Failures: 0, Errors: 0` 判断测试通过。

## 先完成一次实验

打开 [requests.http](requests.http)，在 IDEA HTTP Client 中按编号发送请求。若 IDEA 未提供 HTTP Client，也可以在应用启动后使用 PowerShell：

```powershell
# 1. 查询初始余额
(Invoke-RestMethod http://127.0.0.1:8080/api/accounts).data | Format-Table

# 2. 正常转账 1000 分，即 10 元
$body = @{
    fromAccountId = 1
    toAccountId = 2
    amountInCents = 1000
    simulateFailure = $false
} | ConvertTo-Json
Invoke-RestMethod http://127.0.0.1:8080/api/transfers `
    -Method Post -ContentType 'application/json' -Body $body

# 3. 再次查询：小明 99000 分，小红 51000 分
(Invoke-RestMethod http://127.0.0.1:8080/api/accounts).data | Format-Table

# 4. 打开模拟失败开关。PowerShell 会因 HTTP 409 报错，这是本次实验的预期结果。
$body = @{
    fromAccountId = 1
    toAccountId = 2
    amountInCents = 1000
    simulateFailure = $true
} | ConvertTo-Json
try {
    Invoke-RestMethod http://127.0.0.1:8080/api/transfers `
        -Method Post -ContentType 'application/json' -Body $body
} catch {
    $_.ErrorDetails.Message
}

# 5. 验证回滚：余额仍然为 99000 / 51000 分
(Invoke-RestMethod http://127.0.0.1:8080/api/accounts).data | Format-Table
```

初始数据：小明（ID 1）100000 分，小红（ID 2）50000 分。请求执行次数会影响余额；重启应用可以恢复初始数据。H2 数据只存在于当前进程内，不会保存在磁盘上。

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

## 推荐阅读顺序

Java 源码统一位于 `src/main/java/com/example/springstudy/`。

| 顺序 | 文件 | 重点 |
| --- | --- | --- |
| 1 | `SpringStudyApplication.java` | 启动、自动配置、组件扫描范围 |
| 2 | `controller/AccountController.java` | URL 映射、JSON、构造器注入、`@Valid` |
| 3 | `dto/TransferRequest.java` | 请求对象和 Jakarta Validation 字段约束 |
| 4 | `service/AccountService.java` | 接口与实现分离，Controller 依赖业务接口 |
| 5 | `service/impl/AccountServiceImpl.java` | 三次数据库写入、事务边界和回滚 |
| 6 | `repository/AccountRepository.java`、`TransferRepository.java` | 参数化 SQL、更新行数、数据库主键 |
| 7 | `exception/GlobalExceptionHandler.java` | 业务异常、校验异常、框架异常、500 兜底 |
| 8 | `aspect/ApiLogAspect.java` | 切点、环绕通知、`proceed()`、耗时统计 |
| 9 | `src/main/resources/` | 数据源配置、建表和初始数据 |
| 10 | `src/test/java/.../AccountApiIntegrationTest.java` | 如何验证 HTTP 结果、真实提交和真实回滚 |

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

参数绑定和校验发生在调用 Controller 之前，因此这类错误可能没有 Controller 的 AOP 日志。这一切面统计的是方法执行情况，`正常返回=false` 表示方法抛出了异常，不能直接当作 HTTP 状态码。

## 关于事务，重点观察这些地方

| 问题 | 本示例中的答案 |
| --- | --- |
| 为什么放在 Service？ | 一次业务涉及扣款、入账和写记录，必须一起提交或撤销 |
| 为什么两个 Repository 能共享事务？ | 它们使用同一个数据源；Spring 将事务连接与当前调用线程关联 |
| 默认哪些异常会回滚？ | `RuntimeException` 和 `Error`；本例通过 `rollbackFor = Exception.class` 包含受检异常 |
| 全局处理器会不会阻止回滚？ | 异常先穿过事务代理完成回滚，再交给 MVC 异常处理器 |
| 为什么不能随便 catch？ | 吞掉异常后，事务代理可能看到正常返回而提交 |
| 为什么同类内部调用有坑？ | `this.xxx()` 不经过 Spring 代理，方法上的事务通知可能不会执行 |
| 回滚后记录 ID 为什么可能跳号？ | 自增序列不保证回滚后重用；应检查记录是否存在，不能用 ID 连续性判断事务 |

可以在 `transfer()` 的三次写入处、模拟异常处和全局处理器处打断点。再临时注释掉 `@Transactional`，执行模拟失败，观察余额和记录的变化；实验后恢复注解，测试应再次通过。

本示例只演示单数据源事务。`simulateFailure` 是学习开关；真实资金系统还需要身份验证、权限控制、幂等、防重复提交和完整审计等设计。应用默认只监听本机地址。

参考：[Spring Boot 依赖版本元数据](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml)、[Spring Boot 官方文档](https://docs.spring.io/spring-boot/)、[JavaGuide Spring 总结](https://javaguide.cn/system-design/framework/spring/spring-knowledge-and-questions-summary.html)。
