package javabasicPractice.generics;

import java.util.List;

/** 泛型方法与通配符的示例工具类。 */
public final class GenericUtils {
    private GenericUtils() {
        // 工具类不需要创建对象。
    }

    /**
     * 泛型方法：类型参数写在返回值前面；T 由传入参数的类型自动推断。
     */
    public static <T> T getFirst(List<T> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("列表不能为空");
        }
        return values.get(0);
    }

    /**
     * 上界通配符：? extends Number 表示“某种 Number 子类”。
     * 适合读取（Producer）：可以安全地把元素当作 Number 使用，
     * 但不能向列表中添加具体数值，因为编译器不知道它到底是 Integer 还是 Double 列表。
     */
    public static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number number : numbers) {
            total += number.doubleValue();
        }
        return total;
    }





    /**
     * 下界通配符：? super Integer 表示 Integer 或其父类。
     * 适合写入（Consumer）：可以安全添加 Integer；读取时只能保证得到 Object。
     */
    public static void addScores(List<? super Integer> scores) {
        scores.add(90);
        scores.add(100);
    }
}
