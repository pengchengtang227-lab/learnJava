package multithreading;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程基础示例：两个线程分别计算局部结果，再异步合并。
 *
 * <p>本示例演示以下知识点：</p>
 * <ul>
 *     <li>使用 CompletableFuture 异步执行任务</li>
 *     <li>使用线程池复用工作线程</li>
 *     <li>使用局部变量避免共享数据竞争</li>
 *     <li>任务完成后自动合并结果</li>
 * </ul>
 */
public class MultiThreadDemo {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CompletableFuture<Integer> firstTask = CompletableFuture.supplyAsync(
                () -> calculate("线程一"), executor);
        CompletableFuture<Integer> secondTask = CompletableFuture.supplyAsync(
                () -> calculate("线程二"), executor);

        firstTask.thenCombine(secondTask, Integer::sum)
                .thenAccept(result -> {
                    System.out.println("最终计数结果：" + result);
                    System.out.println("预期计数结果：2000");
                })
                .whenComplete((unused, error) -> executor.shutdown());
    }

    private static int calculate(String taskName) {
        int result = 0;
        for (int i = 0; i < 1_000; i++) {
            result++;
        }
        System.out.println(taskName + " 执行完毕");
        return result;
    }
}
