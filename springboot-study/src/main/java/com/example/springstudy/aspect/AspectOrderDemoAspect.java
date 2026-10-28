package com.example.springstudy.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 演示多个切面的执行顺序，只在 aop-order-demo 环境启用，不影响正常的转账学习项目。
 * 运行：mvnw spring-boot:run "-Dspring-boot.run.profiles=aop-order-demo"
 *
 * ApiLogAspect 没有写 @Order，默认是最低优先级（Ordered.LOWEST_PRECEDENCE）。
 * 这里 @Order(1) 数字更小，优先级更高，所以本切面在外层、ApiLogAspect 在内层：
 *   进入：本切面 → ApiLogAspect → Controller
 *   退出：Controller → ApiLogAspect → 本切面
 * 想观察相反的效果：给 ApiLogAspect 临时加 @Order(0)，它就会跑到本切面外面。
 */
@Aspect
@Component
@Order(1)
//@Profile("aop-order-demo")
public class AspectOrderDemoAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AspectOrderDemoAspect.class);

    @Around("execution(public * com.example.springstudy.controller..*(..))")
    public Object traceOrder(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        LOGGER.info("[顺序演示 @Order(1)] 进入外层切面：{}", method);
        try {
            return joinPoint.proceed();
        } finally {
            LOGGER.info("[顺序演示 @Order(1)] 退出外层切面：{}", method);
        }
    }
}
