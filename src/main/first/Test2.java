package first;

import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class Test2 {
    public static void main(String[] args) {
        // 传统的随机数
        Random oldRandom = new Random();
        int oldValue = oldRandom.nextInt(1000);
        System.out.println("oldValue: " + oldValue);

// 新的随机数生成器
        RandomGenerator generator = RandomGenerator.of("L32X64MixRandom");
        int newValue = generator.nextInt(1000);
        System.out.println("newValue: " + newValue);

        // 查看所有可用的随机数算法
        RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name)
                .forEach(System.out::println);

// 选择适合并行计算的算法
        RandomGenerator splittable = RandomGenerator.of("L64X128MixRandom");

// 选择可跳跃的算法，适合 Monte Carlo 模拟
        RandomGenerator jumpable = RandomGenerator.of("Xoroshiro128PlusPlus");

        User user = new User(1L, "aa", 55);
        System.out.println(user.name());
        user.sayHello();
    }
}
