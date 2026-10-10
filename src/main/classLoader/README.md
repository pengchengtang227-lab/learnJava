# 类加载示例

入口类：`ClassLoadingDemo`。

示例展示以下概念：

- 类加载器层级：应用类加载器、平台类加载器与启动类加载器；
- `ClassLoader#loadClass` 只加载类，通常不会触发初始化；
- `Class.forName` 默认会初始化类，从而执行静态字段和静态代码块；
- 双亲委派：加载器先委托父加载器加载类，避免核心类被应用程序重复或恶意替换。

直接运行 `ClassLoadingDemo.main()`，观察控制台输出顺序即可。
