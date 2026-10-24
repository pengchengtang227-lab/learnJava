package javabasicPractice.first;

public class Test3 {
    static String s = "";
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                s = s + "a"; // 每次拼接都会生成新对象
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                s = s + "b";
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println(s.length()); // 结果可能不是 2000
    }
}
