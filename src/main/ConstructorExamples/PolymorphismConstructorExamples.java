package ConstructorExamples;

public class PolymorphismConstructorExamples {
    public static void main(String[] args) {
        System.out.println("=== 例子一：Child child = new Child() ===");
        Child child = new Child();
        System.out.println("引用类型：Child，实际对象：" + child.getClass().getSimpleName());
        child.show();
        child.childOnly();
        Child.printType();

        System.out.println("\n=== 例子二：Parent parent = new Child() ===");
        Parent parent = new Child();
        System.out.println("引用类型：Parent，实际对象：" + parent.getClass().getSimpleName());
        parent.show();
        Parent.printType();
    }

    static class Parent {
        String role = "父类字段";

        Parent() {
            System.out.println("Parent()：先执行父类构造方法");
        }

        void show() {
            System.out.println("Parent.show()：父类实例方法");
        }

        static void printType() {
            System.out.println("Parent.printType()：父类静态方法");
        }
    }

    static class Child extends Parent {
        String role = "子类字段";

        Child() {
            super();
            System.out.println("Child()：再执行子类构造方法");
        }

        @Override
        void show() {
            System.out.println("Child.show()：子类重写的实例方法");
        }

        void childOnly() {
            System.out.println("Child.childOnly()：子类特有方法");
        }

//        static void printType() {
//            System.out.println("Child.printType()：子类静态方法");
//        }
    }
}
