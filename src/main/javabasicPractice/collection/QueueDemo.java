package javabasicPractice.collection;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;

/** 演示 Queue 的常用操作、先进先出行为和优先队列的排序行为。 */
public class QueueDemo {

    public static void main(String[] args) {
        demonstrateFifoQueue();
        demonstratePriorityQueue();
    }

    /** ArrayDeque 作为普通队列使用时，按先进先出顺序处理元素。 */
    private static void demonstrateFifoQueue() {
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("第一个任务");
        queue.offer("第二个任务");

        System.out.println("=== FIFO 队列 ===");
        System.out.println("队首元素：" + queue.peek());
        System.out.println("取出元素：" + queue.poll());
        System.out.println("队列是否为空：" + queue.isEmpty());
        System.out.println("剩余队列：" + queue);
        System.out.println();
    }

    /** PriorityQueue 按优先级取出元素，并允许重复元素。 */
    private static void demonstratePriorityQueue() {
        Queue<Integer> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(3);
        priorityQueue.offer(1);
        priorityQueue.offer(1);

        System.out.println("=== PriorityQueue：默认先取较小元素 ===");
        System.out.println("队首元素：" + priorityQueue.peek());
        System.out.print("依次取出：");
        while (!priorityQueue.isEmpty()) {
            System.out.print(priorityQueue.poll());
            if (!priorityQueue.isEmpty()) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }
}
