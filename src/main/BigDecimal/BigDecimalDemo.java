package BigDecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigDecimal 学习示例。
 *
 * <p>适合金额、利率等要求精确十进制计算的场景。BigDecimal 是不可变对象，
 * 每次运算都会返回一个新的对象。</p>
 */
public class BigDecimalDemo {

    public static void main(String[] args) {
        demonstratePrecisionProblem();
        demonstrateConstruction();
        demonstrateArithmetic();
        demonstrateComparison();
        demonstrateRounding();
    }

    /**
     * double 采用二进制浮点数保存，不能精确表示大多数小数。
     */
    private static void demonstratePrecisionProblem() {
        System.out.println("=== 1. 为什么金额要使用 BigDecimal ===");
        System.out.println("double: 0.1 + 0.2 = " + (0.1 + 0.2));

        BigDecimal result = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        System.out.println("BigDecimal: 0.1 + 0.2 = " + result);
    }

    /**
     * 推荐传入 String，避免把 double 已经存在的误差带进 BigDecimal。
     */
    private static void demonstrateConstruction() {
        System.out.println("\n=== 2. 创建 BigDecimal ===");
        BigDecimal recommended = new BigDecimal("0.1");
        BigDecimal convenient = BigDecimal.valueOf(0.1);
        BigDecimal notRecommended = new BigDecimal(0.1);

        System.out.println("new BigDecimal(\"0.1\"): " + recommended);
        System.out.println("BigDecimal.valueOf(0.1): " + convenient);
        System.out.println("new BigDecimal(0.1): " + notRecommended + "（不推荐）");
    }

    /**
     * 加减乘除使用方法调用，而不是 +、-、*、/ 运算符。
     */
    private static void demonstrateArithmetic() {
        System.out.println("\n=== 3. 常用运算 ===");
        BigDecimal price = new BigDecimal("19.90");
        BigDecimal quantity = new BigDecimal("3");
        BigDecimal discount = new BigDecimal("5.00");

        BigDecimal total = price.multiply(quantity).subtract(discount);
        System.out.println("商品总价: " + total);
        System.out.println("加税后: " + total.add(new BigDecimal("1.50")));

        // 1 / 3 是无限循环小数；必须明确指定保留位数和舍入规则。
        BigDecimal average = total.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
        System.out.println("平均每件: " + average);
    }

    /**
     * equals 会同时比较数值和小数位数（scale）；比较数值请优先使用 compareTo。
     */
    private static void demonstrateComparison() {
        System.out.println("\n=== 4. 比较大小 ===");
        BigDecimal one = new BigDecimal("1.0");
        BigDecimal anotherOne = new BigDecimal("1.00");

        System.out.println("equals: " + one.equals(anotherOne) + "（scale 不同）");
        System.out.println("compareTo == 0: " + (one.compareTo(anotherOne) == 0) + "（数值相同）");
        System.out.println("是否大于 0: " + one.compareTo(BigDecimal.ZERO));
    }

    /**
     * setScale 用于统一展示或结算时的小数位数。
     */
    private static void demonstrateRounding() {
        System.out.println("\n=== 5. 保留小数与舍入 ===");
        BigDecimal amount = new BigDecimal("12.3456");

        System.out.println("四舍五入到 2 位: " + amount.setScale(2, RoundingMode.HALF_UP));
        System.out.println("直接舍去到 2 位: " + amount.setScale(2, RoundingMode.DOWN));
        System.out.println("向上进位到 2 位: " + amount.setScale(2, RoundingMode.UP));
    }
}
