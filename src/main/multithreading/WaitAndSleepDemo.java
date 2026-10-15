package multithreading;

import java.util.concurrent.CountDownLatch;

/** 演示 wait() 会释放锁，而 sleep() 不会释放锁。 */
public class WaitAndSleepDemo {
    private static final Object WAIT_LOCK = new Object();
    private static final Object SLEEP_LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {
        showWaitReleasesLock();
//        showSleepKeepsLock();
//        waitTest();
    }

    private static void showWaitReleasesLock() throws InterruptedException {
        System.out.println("=== wait() 会释放锁 ===");
        CountDownLatch ready = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            synchronized (WAIT_LOCK) {
                System.out.println("等待线程：拿到锁，准备 wait()");
                ready.countDown();
                try {
                    // wait() 必须持有该对象的锁；调用后会释放锁并等待通知。\
                    System.out.println("========1========");
                    WAIT_LOCK.wait();
                    System.out.println("等待线程：被唤醒并重新拿到锁");
                } catch (InterruptedException e) {
                    // 捕获中断异常后恢复中断标记，方便上层代码感知中断。
                    Thread.currentThread().interrupt();
                }
            }
        });

        waiter.start();
        ready.await(); // 确保等待线程已拿到锁，主线程随后会等到 wait() 释放锁。

        synchronized (WAIT_LOCK) {
            System.out.println("主线程：拿到锁，说明等待线程已在 wait()");
            WAIT_LOCK.notify(); // 通知等待线程；它要等当前线程释放锁后才能继续。
            System.out.println("-------22222-----");
        }

        System.out.println("======wan1==========");
        waiter.join();
        System.out.println("======wan2==========");
    }

    private static void showSleepKeepsLock() throws InterruptedException {
        System.out.println("=== sleep() 不会释放锁 ===");
        CountDownLatch sleeping = new CountDownLatch(1);

        Thread sleeper = new Thread(() -> {
            synchronized (SLEEP_LOCK) {
                System.out.println("休眠线程：拿到锁，开始 sleep()");
                sleeping.countDown();
                try {
                    // sleep() 只让当前线程休眠；休眠期间仍持有 SLEEP_LOCK。
                    Thread.sleep(3000);
                    System.out.println("-============2========");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        sleeper.start();
        sleeping.await();
        System.out.println("主线程：尝试获取休眠线程持有的锁");
        synchronized (SLEEP_LOCK) {
            // 能执行到这里，说明休眠线程已结束休眠并释放了锁。
            System.out.println("主线程：拿到锁");
        }
        sleeper.join();
        System.out.println("记住：wait() 属于 Object，sleep() 属于 Thread；两者都可能被中断。");
    }

    private static void waitTest() {
        Object lock = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lock) {
                try {
                    System.out.println("线程1：开始等待");
                    lock.wait();
                    System.out.println("线程1：被唤醒，继续执行");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("线程2：准备通知");
                lock.notify();
                System.out.println("线程2：通知完成");
            }
        });

        Thread t3 = new Thread(() -> {
            synchronized (lock) {
                System.out.println("线程3：准备通知");
                lock.notify();
                System.out.println("线程3：通知完成");
            }
        });

        t1.start();
        t2.start();
        t3.start();
    }
}
