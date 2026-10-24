# 构造方法与父子类引用示例

## 父类没有无参构造方法时的子类构造

打开 `ParentConstructorExamples.java`，直接运行 `main` 方法。

| 例子 | 关键写法 | 说明 |
| --- | --- | --- |
| 一：子类有参构造 | `super(name)` | 显式调用父类可访问的有参构造方法，先初始化父类部分，再初始化子类部分 |
| 二：子类无参构造 | `this("未命名学生", 0)` | 复用子类另一个构造方法，最终由它调用父类构造方法 |
| 三：父类多个有参构造 | `super(name, subject)` | 根据参数匹配父类构造方法；父类内部也可以使用 `this(...)` 复用构造方法 |

## 命令行运行

在项目根目录 `learnJava` 打开 PowerShell，执行：

```powershell
javac -encoding UTF-8 --release 17 -d target/constructor-examples src/main/javabasicPractice.ConstructorExamples/*.java
java "-Dfile.encoding=UTF-8" -cp target/constructor-examples javabasicPractice.ConstructorExamples.ParentConstructorExamples
java "-Dfile.encoding=UTF-8" -cp target/constructor-examples javabasicPractice.ConstructorExamples.PolymorphismConstructorExamples
```

例子一的输出顺序是：

```text
=== 例子一：显式调用父类有参构造方法 ===
Person(String) 被调用
Student(String, int) 被调用
学生：张三，年龄：18
```

## 错误写法一：省略父类构造调用

下面的代码只用于说明编译错误，不包含在可运行的 Java 文件中：

```java
class Parent {
    Parent(String name) {
    }
}

class Child extends Parent {
    Child() {
    }
}
```

子类构造方法没有显式调用 `super(...)` 或 `this(...)` 时，编译器会隐式调用 `super()`。父类只有 `Parent(String)`，没有 `Parent()`，所以无法编译。

修正：

```java
class Child extends Parent {
    Child() {
        super("张三");
    }
}
```

## 错误写法二：把 super 放在普通语句后面

在 Java 17 中，下面的写法无法编译：

```java
class Child extends Parent {
    Child() {
        System.out.println("初始化子类");
        super("张三");
    }
}
```

在 Java 17 中，显式的 `super(...)` 或 `this(...)` 必须是构造方法中的第一条语句，不能在同一个构造方法中同时调用二者。较新的 Java 版本对构造方法前置语句的规则有所放宽，本示例采用 Java 17 兼容写法。

父类构造方法还必须对子类可访问。例如，父类只有 `private Parent(String name)` 时，普通的独立子类无法调用它。

## Child 引用和 Parent 引用有什么区别

打开 `PolymorphismConstructorExamples.java`，直接运行 `main` 方法。

| 例子 | 关键写法 | 观察结果 |
| --- | --- | --- |
| 一：子类引用 | `Child child = new Child()` | 先调用 `Parent()`，再调用 `Child()`；可以直接调用子类特有方法 |
| 二：父类引用 | `Parent parent = new Child()` | 构造顺序与例子一相同；调用被重写的实例方法时执行 `Child` 的版本 |
| 三：向上转型 | `Parent sameObject = child` | 两个引用指向同一个对象，不会再次调用构造方法 |
| 四：向下转型 | `parent instanceof Child convertedChild` | 检查实际对象后调用子类特有方法；实际对象为 `Parent` 时不能转为 `Child` |
| 五：同名字段 | `child.role`、`sameObject.role` | 同一个对象，通过不同引用类型分别访问子类字段和父类字段 |
| 六：同名静态方法 | `child.printType()`、`sameObject.printType()` | 根据引用类型选择静态方法；推荐写成 `Child.printType()`、`Parent.printType()` |

`Parent parent = new Child()` 中，左边决定编译时可访问哪些成员，右边决定创建什么对象，以及被重写的实例方法运行时执行哪个版本。构造方法不被继承，也不能被重写；创建 `Child` 时会调用父类构造方法来初始化对象中的父类部分。

例子一和例子二分别执行一次 `new Child()`，因此创建的是两个不同的对象。例子三的赋值只复制引用，`sameObject == child` 的结果为 `true`。

### 无法编译的调用

下面的代码仅用于说明，未放入可运行文件：

```java
Parent parent = new Child();
parent.childOnly();
```

虽然实际对象是 `Child`，但 `Parent` 没有声明 `childOnly()`，所以编译器不允许直接调用它。Java 17 可以用 `instanceof` 模式匹配来检查类型并得到子类引用：

```java
if (parent instanceof Child convertedChild) {
    convertedChild.childOnly();
}
```

例子六故意通过引用变量调用静态方法来对比行为。静态方法属于类，这种写法不代表运行时多态，日常代码应使用类名调用。
