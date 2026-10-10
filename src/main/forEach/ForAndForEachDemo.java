package forEach;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 普通 for、增强 for（foreach）与 forEach() 的使用示例。
 *
 * <p>Java 没有 foreach 关键字，通常所说的 foreach 指增强 for：
 * {@code for (类型 元素 : 数组或集合)}；forEach() 则是 Java 8 起提供的方法。</p>
 * <p>普通 for 适合需要下标、控制步长、倒序遍历或修改指定元素的场景；
 * 增强 for 适合依次读取数组或实现 Iterable 的集合中的元素；
 * forEach() 适合使用 Lambda 或方法引用处理每个元素。</p>
 *
 * <p>当前项目使用 Java 17，本示例无需第三方依赖。
 * 可以在 IDE 中直接运行 main 方法，也可以在项目根目录执行：</p>
 * <pre>
 * javac -encoding UTF-8 -d target/classes src/main/forEach/ForAndForEachDemo.java
 * java -Dfile.encoding=UTF-8 -cp target/classes forEach.ForAndForEachDemo
 * </pre>
 */
public class ForAndForEachDemo {

    public static void main(String[] args) {
        basicFor();
        indexedFor();
        enhancedFor();
        mapTraversal();
        forEachMethod();
        breakAndContinue();
        modifyArrayElements();
    }

    /**
     * 普通 for 的三个部分：初始化、循环条件、每轮结束后的更新操作。
     * 本例依次累加 1 到 5，最终总和为 15。
     */
    private static void basicFor() {
        System.out.println("\n=== 1. 普通 for：计算 1 到 5 的总和 ===");
        int sum = 0;
        // number = 1 只执行一次；每轮先检查 number <= 5，再执行循环体和 number++。
        for (int number = 1; number <= 5; number++) {
            sum += number;
            System.out.println("当前数字：" + number + "，累计总和：" + sum);
        }
    }

    /**
     * 通过下标控制遍历顺序：正序下标为 0、1、2，倒序下标为 2、1、0。
     */
    private static void indexedFor() {
        System.out.println("\n=== 2. 普通 for：按下标正序、倒序遍历数组 ===");
        String[] fruits = {"苹果", "香蕉", "橙子"};
        // 数组下标从 0 开始，最大为 length - 1，条件不能写成 index <= fruits.length。
        for (int index = 0; index < fruits.length; index++) {
            System.out.println("下标 " + index + "：" + fruits[index]);
        }
        // 倒序从最后一个元素开始，每轮下标减 1，直到遍历下标 0。
        for (int index = fruits.length - 1; index >= 0; index--) {
            System.out.println("倒序下标 " + index + "：" + fruits[index]);
        }
    }

    /**
     * 增强 for 可以遍历数组和实现 Iterable 的集合，不直接提供元素下标。
     * 本例数组求和结果为 60，并依次输出 List 中的所有姓名。
     */
    private static void enhancedFor() {
        System.out.println("\n=== 3. 增强 for（foreach）：遍历数组和 List ===");
        int[] numbers = {10, 20, 30};
        int sum = 0;
        // 每轮把数组中的下一个元素赋给 number，不需要手动维护下标。
        for (int number : numbers) {
            sum += number;
        }
        System.out.println("数组总和：" + sum);

        // List.of() 创建的是不可修改的列表，本例仅进行读取和遍历。
        // 对可修改集合进行遍历时，也应避免直接用集合的 add()、remove() 改变结构。
        // 需要删除元素时，可以使用 Iterator.remove() 或集合的 removeIf()。
        List<String> names = List.of("小明", "小红", "小刚");
        for (String name : names) {
            System.out.println("姓名：" + name);
        }
    }

    /**
     * Map 本身不能直接用于增强 for，遍历 entrySet() 可以同时取得键和值。
     */
    private static void mapTraversal() {
        System.out.println("\n=== 4. 增强 for：遍历 Map 的键和值 ===");
        // LinkedHashMap 按插入顺序遍历，让示例每次都先输出小明，再输出小红。
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("小明", 90);
        scores.put("小红", 95);
        // 每个 entry 对应一组键值对，getKey() 读取姓名，getValue() 读取成绩。
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println(entry.getKey() + " 的成绩：" + entry.getValue());
        }
    }

    /**
     * forEach() 是方法，可以使用 Lambda 或方法引用指定每个元素的处理逻辑。
     * Lambda 中不能用 break、continue 控制外层遍历；return 只结束当前 Lambda 调用，
     * 后续元素仍会被遍历。数组没有实例 forEach() 方法，可以使用增强 for 遍历。
     */
    private static void forEachMethod() {
        System.out.println("\n=== 5. Java 8+ forEach()：Lambda 和方法引用 ===");
        List<String> languages = List.of("Java", "Python", "JavaScript");
        // Lambda 的 language 参数表示当前元素，箭头右侧是要执行的操作。
        languages.forEach(language -> System.out.println("正在学习：" + language));
        // 方法引用等价于 language -> System.out.println(language)。
        languages.forEach(System.out::println);

        Map<String, Integer> prices = new LinkedHashMap<>();
        prices.put("苹果", 5);
        prices.put("香蕉", 3);
        // Map.forEach() 接收两个参数，分别表示当前键和当前值。
        prices.forEach((fruit, price) -> System.out.println(fruit + "：" + price + " 元"));
    }

    /**
     * 普通 for 和增强 for 都支持 break、continue。
     * 本例先输出 1、3、5，再展示找到指定姓名后提前结束遍历。
     */
    private static void breakAndContinue() {
        System.out.println("\n=== 6. continue 跳过本次循环，break 结束循环 ===");
        for (int number = 1; number <= 10; number++) {
            if (number % 2 == 0) {
                // 跳过偶数：不执行本轮剩余代码，普通 for 接着执行 number++。
                continue;
            }
            if (number > 5) {
                // 遇到 7 时结束整个循环，后续数字不会继续处理。
                break;
            }
            System.out.println("保留的奇数：" + number);
        }

        List<String> names = List.of("小明", "小红", "小刚");
        for (String name : names) {
            if ("小红".equals(name)) {
                System.out.println("增强 for 也可以使用 break：找到 " + name);
                // 找到小红后结束遍历，小刚不会再被检查。
                break;
            }
        }
    }

    /**
     * 给增强 for 的循环变量重新赋值，不会替换原数组或集合中的元素。
     * 元素是对象时，可以通过循环变量修改该对象自身的可变属性。
     * 本例数组先保持 [1, 2, 3]，再通过下标修改为 [10, 20, 30]。
     */
    private static void modifyArrayElements() {
        System.out.println("\n=== 7. 修改数组：循环变量与数组元素的区别 ===");
        int[] numbers = {1, 2, 3};
        for (int number : numbers) {
            // number 保存当前 int 元素的值，这里只改变局部变量，不修改原数组。
            number *= 10;
            System.out.println("重新赋值后的循环变量：" + number);
        }
        System.out.println("增强 for 后的数组：" + Arrays.toString(numbers));

        for (int index = 0; index < numbers.length; index++) {
            // 通过下标直接给数组元素赋值，修改结果会保留在原数组中。
            numbers[index] *= 10;
        }
        System.out.println("按下标修改后的数组：" + Arrays.toString(numbers));
    }
}
