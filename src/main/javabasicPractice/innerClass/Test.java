package javabasicPractice.innerClass;

public class Test {
    public static void main(String[] args) {

        Outer outer = new Outer();

        Outer.Inner inner = outer.new Inner();

        inner.sayHello();

        Outer.StaticInner staticInner = new Outer.StaticInner();

        staticInner.sayHello();

        outer.test();

        outer.sayHello();
    }
}