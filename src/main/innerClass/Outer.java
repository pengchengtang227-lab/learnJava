package innerClass;

public class Outer {

    private String name = "张三";

    private static String staticName = "李四";

    // 成员内部类
    class Inner {

        public void sayHello() {
            System.out.println("你好，我是 " + name);
        }
    }

    // 静态内部类
    static class StaticInner {

        public void sayHello() {
            System.out.println(staticName);
        }
    }

    public static void createStaticInner() {
        StaticInner staticInner = new StaticInner();
    }


    public void test() {

        String a = "10";
        //局部内部类
        class Inner {

            public void sayHello() {
                System.out.println("局部内部类 Hello");
            }
        }

        Inner inner = new Inner();

        inner.sayHello();
    }

    public void sayHello() {
        // 匿名内部类
        new Greeting() {
            @Override
            public void hello() {
                System.out.println("匿名内部类");
            }
        }.hello();
    }
}
