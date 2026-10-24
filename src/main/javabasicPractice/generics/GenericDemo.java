package javabasicPractice.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * 泛型学习入口。运行此类可以依次观察泛型类、泛型方法、边界和通配符的效果。
 */
public class GenericDemo {
    public static void main(String[] args) {
        genericClassDemo();
        genericMethodDemo();
        wildcardDemo();
    }

    private static void genericClassDemo() {
        // 钻石操作符 <> 会让编译器根据左边自动推断 String。
        GenericBox<String> nameBox = new GenericBox<>("小明");
        String name = nameBox.getValue(); // 无需强制类型转换，类型不对会在编译期报错。
        System.out.println("泛型类保存的名字：" + name);

        GenericBox<Integer> ageBox = new GenericBox<>(18);
        System.out.println("泛型类保存的年龄：" + ageBox.getValue());
    }

    private static void genericMethodDemo() {
        List<String> cities = List.of("北京", "上海", "广州");
        // 编译器从 cities 推断出 T 是 String。
        String firstCity = GenericUtils.getFirst(cities);
        System.out.println("第一个城市：" + firstCity);
    }

    private static void wildcardDemo() {
        List<Integer> integers = List.of(1, 2, 3);
        List<Double> doubles = List.of(1.5, 2.5);
        // Integer 和 Double 都继承 Number，所以都可以传给 ? extends Number。
        System.out.println("整数之和：" + GenericUtils.sum(integers));
        System.out.println("小数之和：" + GenericUtils.sum(doubles));

        List<Number> scores = new ArrayList<>();
        // Number 是 Integer 的父类，满足 ? super Integer，可以向其中写入 Integer。
        GenericUtils.addScores(scores);
        System.out.println("写入后的分数：" + scores);

        /*
         * 泛型不支持基本数据类型：应使用 Integer，而不是 int。
         * Java 的泛型通过“类型擦除”实现：运行时通常不会保留 T 的具体类型，
         * 因而不能写 new T()、new List<String>()，也不能直接创建泛型数组 new T[10]。
         *
         * 记忆 PECS：Producer Extends，Consumer Super。
         * 要从容器“取”数据，用 extends；要向容器“放”数据，用 super。
         */
    }
}
