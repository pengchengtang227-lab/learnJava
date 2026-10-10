package first;

import java.util.Objects;

public abstract class Person {

    // 成员变量
    String name;

    // 方法
    public void eat() {
        System.out.println(name + "正在吃饭");
    }

    public void sleep() {
        System.out.println(name + "正在睡觉");
    }

}
