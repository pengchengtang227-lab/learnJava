package javabasicPractice.classLoader;

/**
 * 类加载学习入口。
 *
 * 运行本类可以观察：类加载器层级、加载与初始化的区别，以及双亲委派模型。
 *
 * 
 */
public class ClassLoadingDemo {

    public static void main(String[] args) throws ClassNotFoundException {
        // 1. 查看当前类由哪个加载器加载。应用程序中的普通类通常由 AppClassLoader 加载。
        ClassLoader appClassLoader = ClassLoadingDemo.class.getClassLoader();
        System.out.println("当前类加载器: " + appClassLoader);

        // 2. 类加载器具有父子层级：AppClassLoader -> PlatformClassLoader -> BootstrapClassLoader。
        // BootstrapClassLoader 由 JVM 用原生代码实现，因此 Java 中会显示为 null。
        System.out.println("父加载器: " + appClassLoader.getParent());
        System.out.println("祖父加载器: " + appClassLoader.getParent().getParent());

        // 3. JDK 的核心类（例如 String）由 BootstrapClassLoader 加载，所以结果为 null。
        System.out.println("String 的加载器: " + String.class.getClassLoader());

        System.out.println("\n--- 只加载，不初始化 ---");
        // loadClass 只会完成加载；此时 InitTarget 的静态代码块不会执行。
        Class<?> targetClass = appClassLoader.loadClass("javabasicPractice.classLoader.InitTarget");
        System.out.println("已得到 Class 对象: " + targetClass.getName());

        System.out.println("\n--- 初始化类 ---");
        // Class.forName 默认会初始化类，因而会执行静态代码块和静态字段初始化。
        Class.forName("javabasicPractice.classLoader.InitTarget");

        System.out.println("\n--- 双亲委派验证 ---");
        // 请求加载 java.lang.String 时，AppClassLoader 会先委托父加载器，最终由 BootstrapClassLoader 加载。
        Class<?> stringClass = appClassLoader.loadClass("java.lang.String");
        System.out.println("实际加载 String 的加载器: " + stringClass.getClassLoader());
    }
}
