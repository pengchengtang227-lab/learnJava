package javabasicPractice.multithreading;

import java.util.ArrayDeque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTest {

    public static void main(String[] args) {

        BlockingQueue<String> queue =
                new ArrayBlockingQueue<>(2);

        // 生产者
        Thread producer = new Thread(() -> {
            try {
                queue.put("任务1");
                System.out.println("生产：任务1");

                queue.put("任务2");
                System.out.println("生产：任务2");

                queue.put("任务3");
                System.out.println("生产：任务3");

                queue.put("任务4");
                System.out.println("生产：任务4");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 消费者
        Thread consumer = new Thread(() -> {
            try {
                System.out.println("消费：" + queue.take());
                System.out.println("消费：" + queue.take());
                System.out.println("消费：" + queue.take());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("1");
        stack.push("2");
        System.out.println(stack.peek());
    }
}