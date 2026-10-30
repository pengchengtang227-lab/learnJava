# Spring 面试速记(Java 初级)

每题给出约 30 秒的口述版回答。先说结论,再讲 2~3 个要点,等面试官追问再展开。

## 速查表

| 题目 | 优先级 | 一句话结论 |
|---|---|---|
| IoC 和 DI | 必须 | IoC 是思想,DI 是实现方式 |
| BeanFactory 与 ApplicationContext | 必须 | 父子接口,前者懒加载,后者启动时预创建单例并带企业级功能 |
| 单例与 Bean 作用域 | 必须 | 默认 singleton,有可变成员变量就有线程安全问题 |
| 代理与 Spring AOP | 必须 | 有接口用 JDK 动态代理,没接口用 Cglib |
| 自调用导致切面失效 | 必须 | `this` 调用不走代理 |
| @Autowired 与 @Resource | 必须 | 前者默认按类型,后者默认按名称 |
| AOP 与 AspectJ | 了解 | 运行时代理 vs 编译期织入 |
| 模板方法 | 了解 | `JdbcTemplate` |
| 观察者 | 了解 | Spring 事件机制 |
| 适配器 | 了解 | `HandlerAdapter` |
| 装饰者 | 了解 | 概念为主,`InputStream` 家族是经典例子 |
| 工厂模式 | 了解 | 集中创建对象,调用方依赖接口 |
| Bean 生命周期 | 必须 | 实例化、属性填充、初始化、使用、销毁 |
| 循环依赖 | 必须 | 单例的 setter/字段注入靠三级缓存解决,构造器注入不行 |
| 事务失效场景 | 必须 | 自调用、异常被吞、非 RuntimeException、非 public 等 |
| Spring MVC 请求流程 | 必须 | DispatcherServlet 统一调度,经 HandlerMapping、HandlerAdapter 到 Controller |

## 1. IoC 和 DI

> IoC 是控制反转的思想:对象的创建和依赖装配不再由对象自己负责,而是交给容器。DI 是实现 IoC 的方式,容器把依赖对象传入使用者。这样类之间解耦,也方便替换实现和测试。

## 2. BeanFactory 与 ApplicationContext

> `BeanFactory` 是最基础的 IoC 容器,懒加载,第一次 `getBean` 时才创建 Bean。`ApplicationContext` 是它的子接口,增加了事件发布、国际化、资源加载等功能,并在启动时预创建非懒加载的单例,配置错误启动时就能发现。实际开发都用 `ApplicationContext`,Spring Boot 启动后拿到的就是它。

追问:什么时候直接用它?

> 业务代码一般靠注入。只有运行时按名称动态取 Bean、静态工具类里取 Bean 等注入做不到的场景才直接用 `ApplicationContext`。发布事件时注入 `ApplicationEventPublisher` 更精确。

## 3. 单例与作用域

> Spring Bean 默认是 singleton,实例保存在 `singletonObjects`(`ConcurrentHashMap`)。其他作用域有 prototype、request、session、application、websocket。单例 Bean 如果有可变成员变量,多线程下有安全问题;无状态的 Service、Dao 本身是线程安全的。解决办法是避免可变成员变量,必要时用 `ThreadLocal`。

## 4. 代理与 Spring AOP

> Spring AOP 基于动态代理。目标类有接口时用 JDK 动态代理,没有接口时用 Cglib 生成子类。AOP 用来抽取事务、日志、权限等通用逻辑,不侵入业务代码。

## 5. 自调用导致切面失效

> 类内部用 `this.method()` 调用,走的是真实对象而不是代理,所以 `@Transactional`、`@Async`、`@Cacheable` 都不会生效。常见解决办法是把被调用的方法拆到另一个 Bean 里,这也是项目中最常见的做法。赶进度时可以用 `@Lazy` 注入自己,或者用 `AopContext.currentProxy()`。

补充:被代理方法不能是 `private`、`static`、`final`;自己 `new` 出来的对象没有代理。

## 6. @Autowired 与 @Resource

> `@Autowired` 是 Spring 的注解,默认按类型注入,同类型有多个时再按名称,可配合 `@Qualifier`。`@Resource` 是 JDK 标准注解,默认按名称,找不到再按类型,自带 `name` 属性。`@Resource` 不能用在构造器上。日常推荐构造器注入。

## 7. Spring AOP 与 AspectJ

> Spring AOP 在运行时通过代理增强,简单易用。AspectJ 在编译期通过字节码织入,功能更强,切面多时性能更好,但配置更重。

## 8. 模板方法

> 父类定义算法骨架,部分步骤留给子类。Spring 的 `JdbcTemplate` 是典型例子,它把获取连接、处理异常、释放资源这些固定流程封装好,Spring 用回调(Callback)结合模板方法,不依赖继承,更灵活。

## 9. 观察者(事件机制)

> Spring 事件模型有三个角色:事件 `ApplicationEvent`、监听者 `ApplicationListener`(或 `@EventListener`)、发布者 `ApplicationEventPublisher`。发布事件后,容器把它广播给所有监听者。内置事件有 `ContextRefreshedEvent`、`ContextClosedEvent` 等。

## 10. 适配器

> Spring MVC 中 `DispatcherServlet` 通过 `HandlerMapping` 找到 Handler,再交给 `HandlerAdapter` 执行。有了适配器,就不用写一长串 `instanceof` 判断,新增 Controller 类型也不用改 `DispatcherServlet`,符合开闭原则。

## 11. 装饰者

> 在不修改原有代码的前提下动态扩展功能,比继承更灵活。经典例子是 JDK 的 `InputStream` 家族,如 `BufferedInputStream` 包装 `FileInputStream`。

## 12. 工厂模式

> 把创建对象的逻辑集中起来,调用方只依赖接口。分简单工厂、工厂方法、抽象工厂。Spring 的 `BeanFactory` 就是工厂思想的体现。项目中常见用法是按类型选择实现,比如支付渠道:让 Spring 注入所有实现,再放进 `Map` 按渠道取。

## 回答技巧

- 先给结论,再展开,不要从背景讲起。
- 讲两三个关键点就停,等面试官追问。
- 不确定的细节不要硬编,可以说"我的理解是……"。
- 有项目经验就挂钩项目;只是学习项目就如实说"我在练习项目里写过"。

## 13. Bean 生命周期

> Bean 的生命周期大致分为:实例化、属性填充、初始化、使用、销毁。
>
> 1. 实例化:容器通过反射创建对象。
> 2. 属性填充:完成依赖注入。
> 3. Aware 回调:如果实现了 `BeanNameAware`、`ApplicationContextAware` 等接口,容器把对应信息传给它。
> 4. 初始化:先执行 `BeanPostProcessor` 的前置方法,再依次是 `@PostConstruct`、`InitializingBean.afterPropertiesSet()`、自定义 `init-method`,最后执行 `BeanPostProcessor` 的后置方法。AOP 代理对象通常在后置方法里生成。
> 5. 使用。
> 6. 销毁:容器关闭时,依次执行 `@PreDestroy`、`DisposableBean.destroy()`、自定义 `destroy-method`。

追问:`BeanPostProcessor` 有什么用?

> 它让开发者在 Bean 初始化前后插入自己的逻辑,Spring 的很多功能(AOP 代理、`@Autowired` 注入、`@PostConstruct`)都是靠各种后处理器实现的。

记忆口诀:创建、注入、Aware、前置、初始化、后置、使用、销毁。

## 14. 循环依赖

> 循环依赖指 A 依赖 B,B 又依赖 A。Spring 能解决**单例 Bean 通过 setter 或字段注入**产生的循环依赖,靠的是三级缓存:
>
> | 缓存 | 名称 | 存放内容 |
> |---|---|---|
> | 一级 | `singletonObjects` | 完整的单例 Bean |
> | 二级 | `earlySingletonObjects` | 提前暴露的半成品引用 |
> | 三级 | `singletonFactories` | 能生产早期引用的 `ObjectFactory` |
>
> 创建 A 时,先实例化 A 并把它的工厂放进三级缓存,再去注入 B;创建 B 时需要 A,就从缓存里取到 A 的早期引用,B 创建完成后 A 再继续完成。
>
> 构造器注入的循环依赖解决不了,因为对象还没实例化出来,没法提前暴露。prototype 作用域也不行。

追问:为什么需要三级缓存,两级不够吗?

> 三级缓存里存的是工厂,只有真的发生循环依赖时才通过它生成早期引用。如果 A 需要被 AOP 代理,工厂返回的就是代理对象,这样不用在所有 Bean 实例化后都提前创建代理,保持了"代理在初始化后创建"的正常流程。

补充:Spring Boot 2.6 起默认禁止循环依赖(`spring.main.allow-circular-references=false`)。实际解决办法首选重构,拆分职责;不行再用 `@Lazy` 或改成 setter 注入。

## 15. 事务失效场景

> 常见失效原因有这几类:
>
> | 场景 | 原因 |
> |---|---|
> | 自调用 | 类内部 `this` 调用不走代理 |
> | 异常被捕获没有抛出 | 事务管理器感知不到异常,不会回滚 |
> | 抛出的不是 `RuntimeException` 或 `Error` | 默认只对这两类回滚,受检异常需要 `rollbackFor = Exception.class` |
> | 方法不是 `public`,或者是 `final`、`static` | 代理无法拦截(Spring 6 起在 Cglib 代理下放宽了对 protected 和包可见方法的限制,面试按 public 回答最稳妥) |
> | 类没有被 Spring 管理 | 自己 `new` 的对象没有代理 |
> | 传播行为设置不当 | 例如 `NOT_SUPPORTED`、`NEVER`、`SUPPORTS` 会以非事务方式运行 |
> | 多线程 | 新开的线程拿不到当前事务,不在同一个事务里 |
> | 数据库引擎不支持事务 | 如 MySQL 的 MyISAM |

回答技巧:先说"最常见的是自调用和异常被吞",再补充其他几项,体现有项目经验。

## 16. Spring MVC 请求流程

> 1. 请求先到前端控制器 `DispatcherServlet`。
> 2. `DispatcherServlet` 调用 `HandlerMapping`,找到处理请求的 Handler(通常是 Controller 方法),连同拦截器一起封装成执行链。
> 3. 通过 `HandlerAdapter` 调用 Handler,过程中完成参数解析、数据绑定。
> 4. Handler 执行后返回结果。如果是 `@ResponseBody` 或 `@RestController`,由 `HttpMessageConverter` 直接写成 JSON 响应;否则返回 `ModelAndView`。
> 5. `ViewResolver` 把视图名解析成具体视图,渲染后返回给客户端。

追问:Filter 和 Interceptor 的区别?

> | 对比项 | Filter | Interceptor |
> |---|---|---|
> | 所属规范 | Servlet 规范 | Spring MVC |
> | 执行位置 | 进入 `DispatcherServlet` 之前 | `DispatcherServlet` 内部,Controller 前后 |
> | 能否使用 Spring 容器里的 Bean | 一般需要额外配置 | 可以直接注入 |
> | 典型用途 | 编码、跨域、请求 ID、全局日志 | 登录校验、权限、访问日志 |
>
> Interceptor 有三个回调:`preHandle`(Controller 前)、`postHandle`(Controller 后、视图渲染前)、`afterCompletion`(请求完成后)。

你的 `springboot-study` 项目里的 `RequestIdFilter`、`LoginInterceptor`、`AccessLogInterceptor` 正好对应这两种,可以拿来举例。
