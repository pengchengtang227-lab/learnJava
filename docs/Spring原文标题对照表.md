# JavaGuide《Spring 常见面试题总结》按原文标题对照表

> 对应网页：https://javaguide.cn/system-design/framework/spring/spring-knowledge-and-questions-summary.html
> 图例：🔴 必须掌握　🟡 了解即可　⚪ 可以跳过　（原文自带 ⭐️ 的标题，也一并标出）
> 按网页从上到下的顺序排列，边读网页边对照。

---

## Spring 基础

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| 什么是 Spring 框架? | | 🟡 | 一两句话说清：轻量级、IoC + AOP 为核心的开发框架 |
| Spring 包含的模块有哪些？ | | 🟡 | 知道 Core Container、AOP、Data Access、Web 即可 |
| └ Core Container / AOP / Data Access / Web | | 🟡 | 不用背细节 |
| └ Messaging / Spring Test | | ⚪ | 跳过 |
| Spring, Spring MVC, Spring Boot 之间什么关系? | ⭐️ | 🔴 | 开场题，必须能答 |

## Spring IoC

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| 什么是 IoC? | ⭐️ | 🔴 | 控制反转：对象创建权交给容器 |
| IoC 解决了什么问题? | ⭐️ | 🔴 | 解耦、方便管理对象 |
| 什么是 Spring Bean？ | | 🔴 | 由容器管理的对象 |
| 将一个类声明为 Bean 的注解有哪些? | | 🔴 | `@Component` `@Service` `@Repository` `@Controller` `@Bean` |
| @Component 和 @Bean 的区别是什么？ | | 🔴 | 作用于类 vs 作用于方法；自动扫描 vs 手动注册 |
| 注入 Bean 的注解有哪些？ | | 🔴 | `@Autowired` `@Resource` `@Inject` |
| @Autowired 和 @Resource 的区别是什么？ | ⭐️ | 🔴 | 来源、默认注入方式（byType / byName） |
| 注入 Bean 的方式有哪些？ | | 🔴 | 构造器 / Setter / 字段 |
| 构造函数注入还是 Setter 注入？ | ⭐️ | 🔴 | 推荐构造器注入的理由 |
| Bean 的作用域有哪些? | ⭐️ | 🔴 | singleton 默认；prototype 等 |
| Bean 是线程安全的吗？ | ⭐️ | 🔴 | 单例 + 有状态则不安全 |
| Bean 的生命周期了解么? | ⭐️ | 🔴 | **必考**，要能按步骤讲 |

## Spring AOP

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| 谈谈自己对于 AOP 的了解 | ⭐️ | 🔴 | 含使用场景、动态代理（JDK / CGLIB） |
| Spring AOP 和 AspectJ AOP 有什么区别？ | ⭐️ | 🟡 | 记结论：运行时代理 vs 编译期/加载期织入 |
| AOP 常见的通知类型有哪些？ | ⭐️ | 🔴 | 前置、后置、返回、异常、环绕 |
| 多个切面的执行顺序如何控制？ | | 🟡 | `@Order` |

## Spring MVC

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| 说说自己对于 Spring MVC 了解? | | 🟡 | MVC 模式概念，答得简短 |
| Spring MVC 的核心组件有哪些？ | | 🟡 | 配合工作原理一起记 |
| SpringMVC 工作原理了解吗? | ⭐️ | 🔴 | **必考**，DispatcherServlet 流程 |
| 统一异常处理怎么做？ | | 🔴 | `@ControllerAdvice` + `@ExceptionHandler`，项目必用 |

## Spring 框架中用到了哪些设计模式？

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| Spring 框架中用到了哪些设计模式？ | | 🟡 | 说出 4~5 个并举例：工厂、单例、代理、模板方法、观察者 |

## Spring 的循环依赖

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| Spring 循环依赖了解吗，怎么解决？ | ⭐️ | 🔴 | 三级缓存流程，初级里偏难但高频 |
| @Lazy 能解决循环依赖吗？ | | 🟡 | 能，延迟注入代理对象；知道即可 |
| SpringBoot 允许循环依赖发生么？ | | 🟡 | 2.6+ 默认禁止 |

## Spring 事务

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| Spring 管理事务的方式有几种？ | ⭐️ | 🔴 | 编程式 vs 声明式（`@Transactional`） |
| Spring 事务中哪几种事务传播行为? | ⭐️ | 🔴 | 重点 `REQUIRED` `REQUIRES_NEW` `NESTED` |
| Spring 事务中的隔离级别有哪几种? | ⭐️ | 🔴 | 与 MySQL 四个隔离级别对应 |
| @Transactional(rollbackFor = Exception.class) 注解了解吗？ | ⭐️ | 🔴 | 默认只回滚运行时异常和 Error |
| （原文没有）事务失效场景 | | 🔴 | 自调用、非 public、异常被吞、未配 rollbackFor |

## Spring Data JPA

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| 如何使用 JPA 在数据库中非持久化一个字段？ | | ⚪ | 国内主流是 MyBatis，先跳过 |
| JPA 的审计功能是做什么的？有什么用？ | | ⚪ | 跳过 |
| 实体之间的关联关系注解有哪些？ | | ⚪ | 跳过 |

## Spring Security

| 原文标题 | 原文⭐️ | 重要性 | 提醒 |
|---|---|---|---|
| 有哪些控制请求访问权限的方法？ | | ⚪ | 跳过（做项目用到再学） |
| hasRole 和 hasAuthority 有区别吗？ | | ⚪ | 跳过 |
| 如何对密码进行加密？ | ⭐️ | 🟡 | 只记结论：用 BCrypt，不用 MD5 / 明文 |
| 如何优雅更换系统使用的加密算法？ | | ⚪ | 跳过 |

---

## 原文没有、但初级必问（需另找资料）

| 主题 | 重要性 | 要点 |
|---|---|---|
| Spring Boot 自动配置原理 | 🔴 | `@SpringBootApplication` 三合一、`AutoConfiguration.imports`、`@Conditional` |
| Starter 是什么 | 🟡 | 依赖聚合 + 自动配置 |
| MyBatis：`#{}` 与 `${}` | 🔴 | 预编译防注入 vs 字符串拼接 |
| MyBatis：一二级缓存、动态 SQL | 🟡 | 知道概念即可 |

---

## 统计：这篇网页该怎么读

- 🔴 必须掌握：约 **22 个标题**，集中在 IoC、AOP、MVC 工作原理、循环依赖、事务
- 🟡 了解即可：约 **9 个标题**
- ⚪ 可以跳过：约 **10 个标题**（JPA 整节 + Security 大部分 + 两个冷门模块）

**读网页顺序建议**：Spring 基础 → IoC → AOP → MVC → 事务 → 循环依赖 → 设计模式；JPA 和 Security 直接略过。
