package javabasicPractice.Optional;

import java.util.Optional;

/**
 * javabasicPractice.Optional 使用示例。
 *
 * <p>javabasicPractice.Optional 用来表示一个值“可能存在，也可能不存在”，可以减少直接调用 null
 * 引起的 NullPointerException。它更适合用作方法的返回值，不建议作为实体类字段或方法参数。</p>
 */
public class OptionalDemo {

    public static void main(String[] args) {
        createOptional();
        readOptional();
        provideDefaultValue();
        transformOptional();
        handleUserAddress();
    }

    /**
     * 演示 javabasicPractice.Optional 的三种创建方式。
     */
    private static void createOptional() {
        System.out.println("=== 1. 创建 javabasicPractice.Optional ===");

        // empty() 创建一个不包含任何值的 javabasicPractice.Optional。
        Optional<String> empty = Optional.empty();

        // of() 只能传入非 null 值；如果传入 null，会立即抛出 NullPointerException。
        Optional<String> name = Optional.of("张三");

        // ofNullable() 可以接收 null：传入非 null 时包含值，传入 null 时得到 empty()。
        String possibleNull = null;
        Optional<String> safeName = Optional.ofNullable(possibleNull);

        System.out.println("empty 是否有值：" + empty.isPresent());
        System.out.println("name 的值：" + name.orElse("未知"));
        System.out.println("safeName 是否有值：" + safeName.isPresent());
    }

    /**
     * 演示判断和读取 javabasicPractice.Optional 中的值。
     */
    private static void readOptional() {
        System.out.println("\n=== 2. 判断和读取值 ===");
        Optional<String> username = Optional.of("小明");

        // isPresent() 用于判断是否有值；get() 只有在确定有值时才可以使用。
        if (username.isPresent()) {
            System.out.println("用户名：" + username.get());
        }

        // ifPresent() 可以在有值时执行操作，避免手动调用 get()。
        username.ifPresent(value -> System.out.println("ifPresent 读取到：" + value));

        // ifPresentOrElse() 可以同时处理“有值”和“无值”两种情况（Java 9+）。
        Optional<String> missingUsername = Optional.empty();
        missingUsername.ifPresentOrElse(
                value -> System.out.println("用户名：" + value),
                () -> System.out.println("没有找到用户名")
        );
    }

    /**
     * 演示 orElse、orElseGet 和 orElseThrow 的区别。
     */
    private static void provideDefaultValue() {
        System.out.println("\n=== 3. 提供默认值 ===");
        Optional<String> nickname = Optional.empty();

        // orElse() 无论 javabasicPractice.Optional 是否有值，都会先计算默认参数。
        String valueByOrElse = nickname.orElse(buildDefaultName());
        System.out.println("orElse 结果：" + valueByOrElse);

        // orElseGet() 只有在 javabasicPractice.Optional 为空时才执行 Supplier，适合默认值计算成本较高的场景。
        String valueByOrElseGet = nickname.orElseGet(OptionalDemo::buildDefaultName);
        System.out.println("orElseGet 结果：" + valueByOrElseGet);

        // orElseThrow() 在没有值时抛出指定异常，适合“没有值就是错误”的业务场景。
        String requiredValue = Optional.of("必须存在的值")
                .orElseThrow(() -> new IllegalStateException("值不存在"));
        System.out.println("orElseThrow 结果：" + requiredValue);
    }

    /**
     * 演示使用 map 和 filter 对 javabasicPractice.Optional 中的值进行转换和过滤。
     */
    private static void transformOptional() {
        System.out.println("\n=== 4. 转换和过滤值 ===");
        Optional<String> rawEmail = Optional.of("  Alice@Example.com  ");

        // map() 用于转换值；如果转换结果为 null，javabasicPractice.Optional 会自动变成 empty()。
        Optional<String> normalizedEmail = rawEmail
                .map(String::trim)
                .map(String::toLowerCase);

        // filter() 只保留满足条件的值，不满足时返回 empty()。
        Optional<String> validEmail = normalizedEmail
                .filter(email -> email.contains("@"));

        System.out.println("规范化后的邮箱：" + validEmail.orElse("邮箱格式不正确"));

        // flatMap() 用于处理返回 javabasicPractice.Optional 的方法，避免出现 javabasicPractice.Optional<javabasicPractice.Optional<T>>。
        Optional<String> domain = validEmail.flatMap(OptionalDemo::extractDomain);
        System.out.println("邮箱域名：" + domain.orElse("未知域名"));
    }

    /**
     * 演示通过 flatMap 连续获取可能为空的嵌套对象。
     */
    private static void handleUserAddress() {
        System.out.println("\n=== 5. 处理嵌套对象 ===");
        User user = new User("李四", null);

        // flatMap 可以安全地连续访问可能为空的 User 和 Address。
        String city = Optional.ofNullable(user)
                .flatMap(User::getAddress)
                .map(Address::getCity)
                .orElse("未知城市");

        System.out.println(user.getName() + "所在城市：" + city);
    }

    /**
     * 模拟一个需要额外计算的默认值方法，用来观察 orElse 和 orElseGet 的区别。
     */
    private static String buildDefaultName() {
        System.out.println("正在计算默认名称...");
        return "匿名用户";
    }

    /**
     * 从邮箱中提取域名；邮箱格式不正确时返回 javabasicPractice.Optional.empty()。
     */
    private static Optional<String> extractDomain(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(email.substring(atIndex + 1));
    }

    /**
     * 用户对象示例：地址可能不存在，所以 getAddress() 返回 javabasicPractice.Optional。
     */
    private static class User {
        private final String name;
        private final Address address;

        private User(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        private String getName() {
            return name;
        }

        private Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }
    }

    /**
     * 地址对象示例。
     */
    private static class Address {
        private final String city;

        private Address(String city) {
            this.city = city;
        }

        private String getCity() {
            return city;
        }
    }
}
