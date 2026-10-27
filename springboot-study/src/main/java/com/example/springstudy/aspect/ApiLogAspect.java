package com.example.springstudy.aspect;

import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 把“每个接口都要写的日志”提取到切面，业务代码可以专注于转账。
 * @Aspect 声明切面，@Component 将切面注册到 Spring 容器，二者各有职责。
 */
@Aspect
@Component
public class ApiLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLogAspect.class);

    /**
     * 切点匹配 controller 包及子包中的 public 方法。
     * @Around 环绕通知：可以在目标方法之前、之后执行逻辑。
     * 不记录参数和返回数据，避免日志中出现密码、令牌或完整业务数据。
     */
    @Around("execution(public * com.example.springstudy.controller..*(..))")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long startNanos = System.nanoTime();
        boolean completed = false;
        LOGGER.info("接口开始：方法={}", method);
        try {
            // 这行才真正调用 Controller；忘记 proceed() 会使业务方法根本不执行。
            Object result = joinPoint.proceed();
            completed = true;
            return result;
        } finally {
            // 成功和抛异常都会执行。异常自然向外传播，不妨碍事务回滚和全局异常处理。
            // nanoTime 用来计算耗时，不受系统时钟校准影响。
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            LOGGER.info("接口结束：方法={}，正常返回={}，耗时={}ms", method, completed, elapsedMillis);
        }
    }
}
