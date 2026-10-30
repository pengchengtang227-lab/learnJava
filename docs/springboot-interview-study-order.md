# Spring Boot 面试题学习顺序（Java 初级）

题目来自[面试鸭 SpringBoot 面试题题库](https://www.mianshiya.com/bank/1797452903309508610?current=1&pageSize=20)，共 26 题。原题 1～20 对应第一页，21～26 对应用户提供的第二页截图；“原题”是这两页的排列序号，不是网站的题目 ID。

推荐顺序：**建立认识 → 配置 → 日常开发 → 核心原理 → 进阶 → 版本与兼容**。先掌握基础，再按题目需要补齐前置知识；尚未学习 Redis、多线程等内容时，可以先跳过相关题目。

本文是自主整理的学习要点，不是题库会员答案原文。总览和详细笔记都使用新的学习顺序编号，并保留原题序号以便查找。

## 使用方式与项目背景

| 项目背景 | 当前情况 |
|---|---|
| 版本声明 | [springboot-study/pom.xml](../springboot-study/pom.xml) 声明 Spring Boot 4.1.2、Java 21 |
| 技术栈 | Spring MVC、JDBC、H2，适合先练习请求处理和数据库访问 |
| 已有示例 | 构造器注入、过滤器、拦截器、AOP、事务等 |
| 后续练习 | 自定义配置绑定、定时任务、Runner、Redis、Actuator、异步、多数据源等，不能把建议练习当成已有实现 |

先运行 `SpringStudyApplication.main()`，确认能启动并访问现有账户接口。入门先知道入口怎么用，第 17～18 题再理解 Web 容器和完整启动过程。

| 学到哪里 | 需要的前置知识 |
|---|---|
| 建立认识、配置 | Java 类与 `main` 方法、Maven 依赖和基本注解用法 |
| 日常开发 | IoC/DI、Bean、HTTP 请求、Controller；跨域练习还需要浏览器同源策略基础 |
| 核心原理 | `@Configuration` / `@Bean`、Bean 生命周期，可结合 [Spring 笔记](spring-interview-notes.md) 复习 |
| 进阶与专项练习 | 异步先补线程池和代理；Redis 先补缓存读写；多数据源先补数据源和事务 |

## 优先级与掌握标准

| 优先级 | 学习目标 | 验证方式 |
|---|---|---|
| 重点 | 能说明作用、关键机制及与项目的关系 | 用自己的话讲清，并结合代码或流程解释；原理题先讲主线 |
| 熟悉 | 能完成基本使用，并解释主要注意点 | 做一个小练习，观察日志或实际行为 |
| 了解 | 能解释用途、适用场景及限制 | 先掌握概念，项目需要时再深入 |

优先级表示复习投入建议，不代表面试必问概率。概念题不必都画流程图，具体默认值、容器支持和配置名称需要结合所用版本确认。

## 学习总览

| 顺序 | 原题 | 题目 | 优先级 | 阶段 |
|---|---|---|---|---|
| 1 | 1 | 什么是 Spring Boot？ | 重点 | 建立认识 |
| 2 | 3 | Spring Boot 的核心特性有哪些？ | 重点 | 建立认识 |
| 3 | 13 | 如何理解 Spring Boot 中的 starter？ | 重点 | 建立认识 |
| 4 | 6 | Spring Boot 支持哪些嵌入 Web 容器？ | 熟悉 | 建立认识 |
| 5 | 10 | Spring Boot 打成的 jar 和普通的 jar 有什么区别？ | 熟悉 | 建立认识 |
| 6 | 7 | application.properties 和 application.yml 的区别是什么？ | 熟悉 | 配置 |
| 7 | 8 | 如何在 Spring Boot 中定义和读取自定义配置？ | 熟悉 | 配置 |
| 8 | 9 | Spring Boot 配置文件加载优先级你知道吗？ | 熟悉 | 配置 |
| 9 | 24 | SpringBoot（Spring）中为什么不推荐使用 @Autowired？ | 熟悉 | 日常开发 |
| 10 | 15 | 在 Spring Boot 中你是怎么使用拦截器的？ | 熟悉 | 日常开发 |
| 11 | 14 | Spring Boot 如何处理跨域请求（CORS）？ | 熟悉 | 日常开发 |
| 12 | 16 | SpringBoot 中如何实现定时任务？ | 熟悉 | 日常开发 |
| 13 | 23 | 如何在 SpringBoot 启动时执行特定代码？有哪些方式？ | 熟悉 | 日常开发 |
| 14 | 26 | 启动后，如何把数据库已有的固定内容放入 Redis 缓存？ | 了解 | 日常开发 |
| 15 | 17 | 什么是 Spring Actuator？它有什么优势？ | 了解 | 日常开发 |
| 16 | 5 | SpringBoot 是如何实现自动配置的？ | 重点 | 核心原理 |
| 17 | 4 | Spring Boot 是如何通过 main 方法启动 Web 项目的？ | 熟悉 | 核心原理 |
| 18 | 2 | 说说 Spring Boot 的启动流程？ | 重点 | 核心原理 |
| 19 | 20 | 说说你对 Spring Boot 事件机制的了解？ | 熟悉 | 核心原理 |
| 20 | 22 | Spring Boot 中如何实现异步处理？ | 熟悉 | 进阶 |
| 21 | 25 | @Async 什么时候会失效？ | 熟悉 | 进阶 |
| 22 | 12 | SpringBoot 默认同时可以处理的最大请求数是多少？ | 了解 | 进阶 |
| 23 | 21 | 在 Spring Boot 中如何实现多数据源配置？ | 了解 | 进阶 |
| 24 | 19 | Spring Boot 3.x 与 2.x 版本有哪些主要的改进和区别？ | 了解 | 版本与兼容 |
| 25 | 18 | Spring Boot 2.x 与 1.x 版本有哪些主要的改进和区别？ | 了解 | 版本与兼容 |
| 26 | 11 | Spring Boot 是否可以使用 XML 配置？ | 了解 | 版本与兼容 |

## 一、建立认识

### 1. 什么是 Spring Boot？【重点，原题 1】

Spring Boot 基于 Spring，简化应用搭建、依赖管理、配置和部署。核心思想是“约定优于配置”，也允许按业务需要调整默认行为。
- 先讲清与 Spring 的关系：IoC、AOP 等能力来自 Spring，Spring Boot 帮助把这些能力更方便地组合起来。
- **掌握标准**：能用自己的话解释“为什么项目使用 Spring Boot”，不要求背源码。

### 2. Spring Boot 的核心特性有哪些？【重点，原题 3】

| 特性 | 初学时需要理解什么 |
|---|---|
| 自动配置 | 根据类路径、配置和已有 Bean 等条件提供默认配置 |
| starter | 聚合某类功能常用的依赖，减少手动选择 |
| 嵌入式容器 | Web 应用可以自带容器运行 |
| 外部化配置 | 通过配置文件、环境变量、命令行参数调整应用 |
| Actuator | 按需加入健康检查、指标等管理能力 |

- **掌握标准**：每个特性能结合项目举一个例子，知道不必依赖 XML 配置也能搭建应用。

### 3. 如何理解 Spring Boot 中的 starter？【重点，原题 13】

starter 主要聚合一组相关依赖；对应的自动配置由相关自动配置模块提供。版本协调通常来自 Spring Boot 的 parent/BOM，不能全部归因于 starter 本身。
- 官方通常命名为 `spring-boot-starter-xxx`，第三方通常使用 `xxx-spring-boot-starter`。
- **项目对照**：[pom.xml](../springboot-study/pom.xml) 已引入 `spring-boot-starter-webmvc`、JDBC 等 starter。
- **掌握标准**：能解释引入 starter、统一管理版本、自动配置三者的关系；先不要求自定义 starter。

### 4. Spring Boot 支持哪些嵌入 Web 容器？【熟悉，原题 6】

- 先围绕本项目的 Servlet / Spring MVC 技术栈学习：常见容器包括 Tomcat、Jetty，本项目使用默认的 Tomcat。
- 容器支持范围随 Spring Boot 版本变化，不能把旧版容器列表直接套到新版本；响应式 WebFlux 的运行方式另行了解。
- **掌握标准**：能解释为什么不必先单独安装 Tomcat，就能运行这个 Web 项目。

### 5. Spring Boot 打成的 jar 和普通的 jar 有什么区别？【熟悉，原题 10】

这里比较的是经过 Spring Boot Maven/Gradle 插件重新打包的可执行 jar，与常规 jar 的组织和启动方式。
- Spring Boot 可执行 jar 通常用 `BOOT-INF/classes` 放应用类、`BOOT-INF/lib` 放依赖，由 Spring Boot Launcher 加载依赖并启动主类；Web 项目还可包含嵌入式容器依赖。
- 普通 jar 也能通过 `Main-Class` 指定入口并使用 `java -jar` 运行；是否包含依赖、如何找到依赖，取决于打包和清单配置。
- **建议练习**：打包后查看 jar 结构及 `META-INF/MANIFEST.MF`，区分 `Main-Class` 和 `Start-Class`。

## 二、配置

### 6. application.properties 和 application.yml 的区别是什么？【熟悉，原题 7】

| 对比 | properties | YAML（yml / yaml） |
|---|---|---|
| 基本形式 | 逐行键值对 | 通过缩进表达层级，也支持列表等结构 |
| 注意点 | 层级较深时会重复前缀 | 缩进使用空格，留意冒号、引号和数据类型 |

- 两者都能作为 Spring Boot 配置文件；**同一位置**同时存在两种格式时，`.properties` 优先，不能脱离加载位置直接比较。
- **项目对照**：[application.yml](../springboot-study/src/main/resources/application.yml)；先修改端口并观察启动结果，练习时保留一种主要格式。

### 7. 如何在 Spring Boot 中定义和读取自定义配置？【熟悉，原题 8】

| 方式 | 常见用途 |
|---|---|
| `@Value` | 读取少量单独的配置值，也支持表达式 |
| `@ConfigurationProperties` | 按前缀绑定一组配置，适合结构化配置和校验 |
| `Environment` | 代码中按需查询配置 |

- 使用 `@ConfigurationProperties` 时，还要通过扫描或显式启用等方式让配置类被注册和绑定。
- **建议练习**：定义一组学习用配置，分别尝试读取单个值和绑定对象；这是待新增练习，不是项目已有示例。

### 8. Spring Boot 配置文件加载优先级你知道吗？【熟悉，原题 9】

先记住常见配置来源的覆盖方向：命令行参数 > Java 系统属性 > 操作系统环境变量 > 配置文件。这是常见来源的简化比较，不是包含测试属性等来源的完整清单。

| 常规配置文件顺序 | 优先级从高到低 |
|---|---|
| 1 | jar 外部的 profile 配置 |
| 2 | jar 外部的普通配置 |
| 3 | jar 内部的 profile 配置 |
| 4 | jar 内部的普通配置 |

- 上表针对常规默认加载方式；显式指定位置、配置导入等会影响结果，遇到这些配置时查对应版本文档。
- **建议练习**：在默认加载设置下，通过 `--server.port=8081` 覆盖文件里的 `8080`，解释为什么生效。

## 三、日常开发

### 9. SpringBoot（Spring）中为什么不推荐使用 @Autowired？【熟悉，原题 24】

这道题主要讨论字段注入的不足，**不是禁止使用 `@Autowired` 注解**。一般优先使用构造器注入。

| 构造器注入的好处 | 理解方式 |
|---|---|
| 必需依赖更明确 | 创建对象时就需要提供依赖 |
| 便于普通单元测试 | 可以直接传入测试替身，无需先启动容器 |
| 可使用 `final` 字段 | 依赖引用在构造后保持稳定 |
| 容易发现设计问题 | 构造参数过多、循环依赖更容易暴露 |

- 只有一个构造器时，Spring 通常可以省略其上的 `@Autowired`。
- **项目对照**：[AccountController](../springboot-study/src/main/java/com/example/springstudy/controller/AccountController.java) 已使用构造器注入。

### 10. 在 Spring Boot 中你是怎么使用拦截器的？【熟悉，原题 15】

- 实现 `HandlerInterceptor`，通过 `WebMvcConfigurer.addInterceptors` 注册并设置生效路径。
- `preHandle` 在处理器执行前调用，返回 `false` 可终止后续处理；`postHandle` 在正常处理后、视图渲染前调用；`afterCompletion` 用于请求处理结束后的清理。
- `afterCompletion` 只会回调执行链中 `preHandle` 已成功返回 `true` 的拦截器，不能理解成任何情况下每个拦截器都必然执行。
- **项目对照**：[WebMvcConfig](../springboot-study/src/main/java/com/example/springstudy/config/WebMvcConfig.java)、[AccessLogInterceptor](../springboot-study/src/main/java/com/example/springstudy/interceptor/AccessLogInterceptor.java)、[LoginInterceptor](../springboot-study/src/main/java/com/example/springstudy/interceptor/LoginInterceptor.java)。
- **掌握标准**：结合现有代码，说明过滤器、拦截器、Controller 在普通请求中的执行顺序；异步分派等细节后学。

### 11. Spring Boot 如何处理跨域请求（CORS）？【熟悉，原题 14】

- 跨域与浏览器同源策略有关，同源要求协议、主机和端口一致；CORS 响应头告诉浏览器哪些跨域访问被允许。
- 可通过 `@CrossOrigin` 局部配置、`WebMvcConfigurer.addCorsMappings` 全局配置，或使用 `CorsFilter`。
- **只有部分跨域请求需要 `OPTIONS` 预检**。例如携带 `Authorization` 请求头的跨域请求通常需要预检；简单请求通常不需要。
- **项目对照**：`LoginInterceptor` 已放行 `OPTIONS`，但这一步本身不等于完整的 CORS 配置；当前 `WebMvcConfig` 尚未配置允许的来源。
- **建议练习**：为本地前端地址配置允许的来源，并在浏览器中分别观察简单请求与预检请求。若允许携带凭证，应明确配置允许的来源。

### 12. SpringBoot 中如何实现定时任务？【熟悉，原题 16】

- 使用 `@EnableScheduling` 开启调度，在方法上用 `@Scheduled` 声明任务。
- `fixedRate` 按固定频率调度，`fixedDelay` 按上次执行结束后再等待的时间调度，`cron` 按表达式调度；实际并发能力取决于调度器。
- 在未启用虚拟线程、使用默认自动配置且无自定义调度器的常见场景中，调度线程池默认只有一个线程；自定义调度器或虚拟线程模式下不能照搬这一结论。
- 多实例部署会让每个实例各自调度；需要唯一执行时，再学习分布式锁或分布式调度。
- **建议练习**：先记录任务开始、结束时间和线程名，比较 `fixedRate` 与 `fixedDelay`。

### 13. 如何在 SpringBoot 启动时执行特定代码？有哪些方式？【熟悉，原题 23】

| 方式 | 时机与适用范围 |
|---|---|
| `@PostConstruct` | Bean 完成依赖注入后的初始化回调，不能等同于整个应用已就绪 |
| `CommandLineRunner` / `ApplicationRunner` | 容器刷新完成后、应用就绪事件前，适合启动任务 |
| 监听 `ApplicationReadyEvent` | 启动流程末尾的就绪事件；同步监听器自身仍会影响启动耗时 |

- `ApplicationRunner` 接收封装后的 `ApplicationArguments`，`CommandLineRunner` 接收原始字符串数组；多个 Runner 可用 `@Order` 排序。
- **建议练习**：分别记录 Bean 初始化、Runner、就绪事件的日志，观察顺序；监听器的注册方式在事件机制一题继续学习。

### 14. 启动后，如何把数据库已有的固定内容放入 Redis 缓存？【了解，原题 26】

- **前置知识**：数据库查询、Redis 读写，以及上一题的启动回调。没学过 Redis 时先跳过，后续补做。
- 可在 `ApplicationRunner` 中查询并预热缓存；如使用就绪事件监听器，需要考虑同步执行对启动耗时的影响。
- 数据较多时分批查询和写入；考虑缓存键、过期策略、重复执行以及多实例同时预热。
- 失败策略由业务决定：非关键缓存可记录失败并降级；服务必须依赖的数据可选择启动失败或保持未就绪，不能一概要求忽略失败。
- **项目现状**：当前使用 H2，尚未引入 Redis。可先用 H2 练习查询，再接 Redis；题目原场景中的 MySQL 可在后续替换。

### 15. 什么是 Spring Actuator？它有什么优势？【了解，原题 17】

- 按需加入 Actuator 后，可提供 `health`、`metrics`、`info` 等端点，用于健康检查、指标采集和应用管理。
- 端点是否可用、是否通过 Web 暴露、是否需要认证，是需要分别检查的配置；可用 `management.endpoints.web.exposure.include` 控制 Web 暴露范围。
- **建议练习**：当前项目未引入 Actuator，可作为后续练习先加入并观察健康检查，只开放练习需要的端点。

## 四、核心原理

### 16. SpringBoot 是如何实现自动配置的？【重点，原题 5】

- **前置知识**：IoC、Bean 定义与生命周期、`@Configuration` / `@Bean`。先理解这些概念，再阅读启动源码。
- `@SpringBootApplication` 组合了 `@SpringBootConfiguration`、`@EnableAutoConfiguration`、`@ComponentScan` 等注解。
- 现代版本使用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 声明自动配置候选类；这一机制在 2.7 引入，3.x 起不再通过 `spring.factories` 的旧入口注册自动配置类。不能推导为 `spring.factories` 文件已完全废弃。
- 自动配置根据 `@ConditionalOnClass`、`@ConditionalOnMissingBean` 等条件决定是否生效；匹配缺失 Bean 条件的默认配置会在用户提供相应 Bean 时退让，不能概括成所有 Bean 都自动覆盖。
- **掌握标准**：能串起“依赖与配置 → 候选自动配置 → 条件判断 → 注册 Bean”；用本项目的 JDBC/H2 数据源说明自动配置的结果。

### 17. Spring Boot 是如何通过 main 方法启动 Web 项目的？【熟悉，原题 4】

入门时已经运行过 `main`，这里再理解它为什么能启动 Web 服务。以下以本项目的 Servlet / Spring MVC 应用为例。

`main` → `SpringApplication.run(...)` → 推断应用类型并准备配置 → 创建 Web 应用上下文 → 在容器刷新及生命周期处理中创建、初始化并启动内嵌 Web 容器，注册 Servlet 等组件。

- `DispatcherServlet` 负责 Spring MVC 请求分发；Tomcat 监听端口并不等于所有启动任务都已完成。
- **项目对照**：[SpringStudyApplication](../springboot-study/src/main/java/com/example/springstudy/SpringStudyApplication.java)。
- **掌握标准**：区分应用入口、Spring 容器、内嵌 Web 容器和 `DispatcherServlet`，先不要求记住每个内部方法。

### 18. 说说 Spring Boot 的启动流程？【重点，原题 2】

| 步骤 | 主线 |
|---|---|
| 1 | 创建 `SpringApplication`，推断应用类型，准备初始化器与监听器等 |
| 2 | 执行 `run`，发布早期事件并准备 `Environment` |
| 3 | 创建、准备 `ApplicationContext`，加载相关 Bean 定义 |
| 4 | 刷新上下文：处理配置与自动配置、创建 Bean，并完成 Web 容器相关生命周期处理 |
| 5 | 发布已启动事件，执行 `ApplicationRunner` / `CommandLineRunner` |
| 6 | 发布就绪事件并更新就绪状态，完成正常启动流程 |

- **掌握标准**：能讲清上面的主线，并说出配置、自动配置、Bean 生命周期和 Runner 分别在哪个阶段发挥作用。
- **源码学习顺序**：先看 `SpringApplication.run` 的整体结构，再跟踪环境准备和上下文刷新，避免一开始逐行深入所有分支。

### 19. 说说你对 Spring Boot 事件机制的了解？【熟悉，原题 20】

- 普通业务事件可用 `ApplicationEventPublisher` 发布，通过 `ApplicationListener` 或 `@EventListener` 监听；可以发布 `ApplicationEvent`，也可以发布普通对象作为事件。
- 默认事件广播机制通常同步执行，慢监听器会拖慢发布方；需要异步时再结合后面的 `@Async` 与执行器知识。
- Spring Boot 启动事件复用了 Spring 事件能力，但很早的启动事件发生在容器准备完成之前，不能只靠容器中的 `@EventListener` 接收；早期监听器需要提前注册。
- **建议练习**：先发布一个业务事件并观察监听顺序，再对照上一题认识启动、环境准备、就绪等事件。

## 五、进阶与运行参数

### 20. Spring Boot 中如何实现异步处理？【熟悉，原题 22】

- **前置知识**：线程、执行器与线程池、`CompletableFuture`、Spring AOP 代理。未掌握时先补基础。
- 使用 `@EnableAsync` 开启注解支持，在 Spring 管理的 Bean 方法上使用 `@Async`，通过代理调用将任务交给执行器；需要结果时可使用 `CompletableFuture`。
- 学习时优先显式配置并指定执行器，理解并发数、排队、拒绝策略和关闭行为；不同版本及虚拟线程设置下，默认执行器实现可能不同。
- **建议练习**：由另一个 Bean 调用异步方法，记录调用线程和执行线程，确认调用方是否等待结果。

### 21. @Async 什么时候会失效？【熟悉，原题 25】

以下讨论默认的 Spring 代理模式；AspectJ 编织模式的行为不同。

| 检查项 | 原因或现象 |
|---|---|
| 未启用 `@EnableAsync` | 没有开启相应的注解代理支持 |
| 对象由自己 `new` 创建 | 调用没有经过 Spring 创建的代理 |
| 同类内部直接调用 | `this.method()` 绕过代理 |
| 静态、私有方法或其他代理限制 | 不能按普通可代理实例方法的方式拦截；CGLIB 不能增强 `final` 方法，JDK 代理则围绕接口方法工作 |

- 不能把“非 `public` 或 `final` 方法一律失效”当作所有代理方式通用的结论。初学时优先采用可代理的 `public` 实例方法，并由另一个 Bean 经代理调用。
- `void` 异步方法中的异常不会按同步调用方式传回调用方，需要配置相应处理；这与注解是否生效是两件事。
- **项目联系**：[RequestIdFilter](../springboot-study/src/main/java/com/example/springstudy/filter/RequestIdFilter.java) 使用 MDC；切换线程后其请求 ID 通常不会自动传播，需要另外设计上下文传递与清理。

### 22. SpringBoot 默认同时可以处理的最大请求数是多少？【了解，原题 12】

**不能脱离运行方式回答一个 Spring Boot 通用固定值。** 先说明容器、版本、线程模型和配置，再区分相关概念。

| 概念 | 以常见 Tomcat 配置为例 |
|---|---|
| 请求处理线程数 | `server.tomcat.threads.max`，属于常规线程池模型下的配置 |
| 连接数 | `server.tomcat.max-connections`，不是同时执行业务的请求数 |
| 连接等待队列 | `server.tomcat.accept-count`，不是线程池中的业务任务队列 |

- 常规同步请求可以从工作线程数解释并发处理；自定义执行器、虚拟线程、异步 Servlet、HTTP/2 等会改变限制关系，不能简单套用线程数。
- **掌握标准**：能区分连接、线程、队列与吞吐量。具体默认数值通过所用版本的属性文档或运行配置确认，再结合压测判断能力。

### 23. 在 Spring Boot 中如何实现多数据源配置？【了解，原题 21】

- **前置知识**：`DataSource`、JDBC/MyBatis/JPA 中所用的数据访问方式、本地事务，以及需要时的 AOP。

| 方案 | 先理解什么 |
|---|---|
| 显式配置多个数据源 | 用名称或 `@Qualifier` 区分，并为对应的数据访问组件、事务管理建立清晰关联 |
| `AbstractRoutingDataSource` | 依据当前上下文选择目标数据源，注意路由时机与上下文清理 |

- 一个本地事务通常不能天然覆盖多个独立数据库；跨库时按一致性要求选择事务协调或最终一致性等方案。
- **掌握标准**：先能画出两个数据源分别被哪些组件使用，不要求初级阶段实现复杂动态路由。

## 六、版本与兼容知识

### 24. Spring Boot 3.x 与 2.x 版本有哪些主要的改进和区别？【了解，原题 19】

- 3.0 基于 Spring Framework 6，最低 Java 版本为 17；2.x 基于 Spring Framework 5，最低 Java 版本为 8。
- Jakarta EE 相关 API 从 `javax.*` 迁移到 `jakarta.*`，例如 Servlet、Validation；不是所有 Java 标准库的 `javax` 包都迁移。
- 3.x 完善 AOT 与 GraalVM 原生镜像支持，并通过 Micrometer Observation / Tracing 等统一可观测性能力；Micrometer 指标支持在 2.x 已存在。
- 自动配置注册在 2.7 已引入 `AutoConfiguration.imports`，3.x 移除了相关旧注册入口，不能说新文件直到 3.x 才出现。
- **项目联系**：本项目声明使用 4.1.2，因此这题是历史版本对比；实际依赖、包名与配置要按项目版本确认，不把 2.x/3.x 的示例直接套进来。

### 25. Spring Boot 2.x 与 1.x 版本有哪些主要的改进和区别？【了解，原题 18】

- 2.0 升级到 Spring Framework 5，最低 Java 版本提升至 8，并支持 WebFlux 响应式应用。
- 在满足相应条件时，默认优先选择 HikariCP 作为连接池，Actuator 的端点和管理能力也有调整。
- **掌握标准**：能说出主要变化即可，遇到旧系统维护或升级任务再深入到具体小版本差异。

### 26. Spring Boot 是否可以使用 XML 配置？【了解，原题 11】

- 可以通过 `@ImportResource` 加载 Spring XML 配置，例如兼容已有配置。
- 新项目通常以注解和 Java 配置为主；是否保留 XML 取决于项目实际情况，不必为了面试题给当前项目引入 XML。
- **掌握标准**：知道“可以使用、如何引入、何时会用到”即可。

## 建议的复习节奏

| 轮次 | 学习范围 | 完成标准 |
|---|---|---|
| 第一轮 | 第 1～8 题 | 运行项目、理解 starter、修改端口、读取配置，并能解释覆盖结果 |
| 第二轮 | 第 9～15 题 | 对照已有构造器和拦截器代码练习；新增功能每次只做一个，Redis 未学可先跳过 |
| 第三轮 | 第 16～19 题 | 补齐 IoC 和 Bean 生命周期，先讲清自动配置，再串起启动与事件主线 |
| 第四轮 | 第 20～23 题 | 补齐线程池、代理和事务基础；验证异步调用，并区分线程、连接和数据源 |
| 第五轮 | 第 24～26 题 | 了解版本与兼容知识，需要迁移或维护旧项目时再深入 |

每道题可以按“解决什么问题 → 怎么用或核心机制 → 项目例子 → 一个主要限制”的结构复述。先能讲清、做出基本效果，再补细节，避免只记注解名称和固定数字。

## 官方参考

这些入口可能默认展示当前稳定版本，查具体行为时请选择与项目一致的版本。

| 内容 | 参考 |
|---|---|
| 配置来源、覆盖顺序与格式 | [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) |
| 默认执行器、调度器与虚拟线程 | [Spring Boot Task Execution and Scheduling](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html) |
| `@Async`、`@Scheduled` 的使用 | [Spring Framework Task Execution and Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html) |
| JDK/CGLIB 代理及自调用限制 | [Spring Framework Proxying Mechanisms](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html) |
| 自动配置声明与条件 | [Creating Your Own Auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html) |
