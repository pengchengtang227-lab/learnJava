package javabasicPractice.multithreading;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程基础示例。
 *
 * <p>这个类演示了几个常用的多线程知识点：</p>
 * <ul>
 *     <li>使用线程池执行多个任务，避免频繁创建和销毁线程</li>
 *     <li>使用 AtomicInteger 安全地修改多个线程共享的数据</li>
 *     <li>使用 CountDownLatch 等待所有子任务执行完成</li>
 *     <li>正确处理线程中断，并在 finally 中关闭线程池</li>
 * </ul>
 */
public class ThreadBasicsDemo {

    public static void main(String[] args) {
        // 创建一个最多同时执行两个任务的线程池。
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // 计数器初始值为 0，两个任务各自累加 1,000 次。
        // AtomicInteger 的自增操作是原子的，可以避免数据竞争。
        AtomicInteger counter = new AtomicInteger(0);

        // CountDownLatch 的计数为 2，代表需要等待两个任务完成。
        CountDownLatch finishSignal = new CountDownLatch(2);

        try {
            // 提交第一个任务。
            executor.submit(createTask("任务一", counter, finishSignal));

            // 提交第二个任务。两个任务会在线程池中并发执行。
            executor.submit(createTask("任务二", counter, finishSignal));

            // 当前主线程等待，直到两个任务都调用 countDown()。
            finishSignal.await();

            System.out.println("所有任务执行完成");
            System.out.println("最终计数结果：" + counter.get());
        } catch (InterruptedException e) {
            // 收到中断信号后恢复中断状态，交给上层代码继续处理。
            Thread.currentThread().interrupt();
            System.out.println("主线程被中断");
        } finally {
            // 不再提交新任务，并等待线程池中的任务逐步结束。
            executor.shutdown();
        }
    }

    /**
     * 创建一个可以重复提交的任务。
     *
     * @param taskName 任务名称，用于输出日志
     * @param counter  多个任务共享的线程安全计数器
     * @param latch    用于通知主线程任务已经结束的同步器
     * @return Runnable 任务对象
     */
    private static Runnable createTask(
            String taskName, AtomicInteger counter, CountDownLatch latch) {
        return () -> {
            try {
                for (int i = 0; i < 1_000; i++) {
                    // incrementAndGet() 会以线程安全的方式完成加 1 操作。
                    counter.incrementAndGet();
                }
                System.out.println(taskName + "执行完毕，线程："
                        + Thread.currentThread().getName());
            } finally {
                // 无论任务正常结束还是出现异常，都要通知等待方。
                latch.countDown();
            }
        };
    }
}



