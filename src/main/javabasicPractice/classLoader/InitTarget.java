package javabasicPractice.classLoader;

/**
 * 用于观察“初始化”时机的目标类。
 */
public class InitTarget {

    // 静态字段的赋值会发生在类初始化阶段。

    private static String city = createMessage1();
    private static final String MESSAGE = createMessage();

    // 静态代码块同样仅在类首次初始化时执行一次。
    static {
        System.out.println("InitTarget 的静态代码块已执行");
    }

    public InitTarget() {
        System.out.println("--------------");
    }

    private static String createMessage() {
        System.out.println("InitTarget 的静态字段开始初始化");
        return "初始化完成";
    }

    private static String createMessage1() {
        System.out.println("InitTarget 的静态字段开始初始化11111");
        return "初始化完成";
    }
}
