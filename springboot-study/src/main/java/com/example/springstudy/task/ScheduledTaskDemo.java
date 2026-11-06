package com.example.springstudy.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 随 Spring Boot 应用一起运行的定时日志示例，无需额外配置文件或启动参数。
 *
 * <p>注解分工：@Component 把对象交给 Spring；启动类的 @EnableScheduling 开启调度；
 * @Scheduled 声明方法的触发规则。仅仅自己 new 此类，不会自动触发任务。
 * 本例任务方法均为无参、返回 void，业务逻辑直接写在方法内或交给注入的 Service。
 *
 * <p>多实例：启动两份应用，两边都会执行这些任务，日志默认包含各自的进程 ID。
 * @Scheduled 不提供集群协调或持久化补跑。
 * 真实业务首先考虑幂等，例如只对仍未支付且已过期的订单执行条件更新；
 * 如需同一时刻只由一个实例执行，再考虑分布式锁或调度平台，不能依赖本机变量协调不同进程。
 */
//@Component
public class ScheduledTaskDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskDemo.class);

    /**
     * 固定延迟：本次方法执行结束后再等待 5 秒，数值默认单位为毫秒。
     * 如果业务耗时 2 秒，相邻两次开始约间隔 7 秒；这里只打印日志，耗时很短。
     */
    @Scheduled(fixedDelay = 5000)
    public void fixedDelayTask() {
        LOGGER.info("[定时任务][fixedDelay] 执行结束后等待 5 秒，再执行下一次");
    }

    /**
     * 固定频率：按每 5 秒一次安排执行。
     * fixedRate 不会自动让同一个任务并行；耗时超过间隔时，后续执行可能推迟。
     * 常规默认调度池只有 1 个线程，耗时任务也可能影响其他任务的触发。
     */
    @Scheduled(fixedRate = 5000)
    public void fixedRateTask() {
        LOGGER.info("[定时任务][fixedRate] 按每 5 秒一次的频率执行");
    }

    /**
     * cron 的六个字段是“秒 分 时 日 月 周”。这里每分钟的第 0、10、20、30、40、50 秒触发。
     * 每天北京时间凌晨 2 点执行可写：cron = "0 0 2 * * *", zone = "Asia/Shanghai"。
     * 任务只有在应用运行时才会触发，停机期间错过的时刻默认不会自动补跑。
     */
    @Scheduled(cron = "*/10 * * * * *", zone = "Asia/Shanghai")
    public void cronTask() {
        LOGGER.info("[定时任务][cron] 按北京时间每 10 秒触发一次");
    }
}
