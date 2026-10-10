package Reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.util.ServiceLoader;

public class ReflectionDemo {

    public static void main(String[] args) throws Exception {

        // 1. 获取 Class 对象的三种方式
        Class<?> clazz1 = Person.class;
        Class<?> clazz2 = Class.forName("Reflection.Person");
        Person person = new Person();
        Class<?> clazz3 = person.getClass();

        System.out.println("clazz1 == clazz2 == clazz3 : "
                + (clazz1 == clazz2 && clazz2 == clazz3));

        // 2. 获取所有字段（包括私有）
        System.out.println("\n=== 字段 ===");
        for (Field field : clazz1.getDeclaredFields()) {
            System.out.println(field.getName() + " : " + field.getType());
        }

        // 3. 获取所有方法（包括私有）
        System.out.println("\n=== 方法 ===");
        for (Method method : clazz1.getDeclaredMethods()) {
            System.out.println(method.getName());
        }

        // 4. 获取所有构造器
        System.out.println("\n=== 构造器 ===");
        for (Constructor<?> constructor : clazz1.getDeclaredConstructors()) {
            System.out.println(constructor);
        }

        // 5. 通过有参构造器创建对象
        Constructor<?> constructor = clazz1.getConstructor(String.class, int.class);
        Person p1 = (Person) constructor.newInstance("张三", 18);
        System.out.println("\n通过构造器创建的对象: " + p1);

        // 6. 反射调用方法
        Method sayHello = clazz1.getMethod("sayHello");
        sayHello.invoke(p1);

        // 7. 反射访问并修改私有字段
        Field nameField = clazz1.getDeclaredField("name");
        nameField.setAccessible(true); // 绕过访问权限检查
        nameField.set(p1, "李四");
        System.out.println("反射修改私有字段后: " + p1.getName());

        // 8. 反射调用私有方法
        Method secret = clazz1.getDeclaredMethod("secret");
        secret.setAccessible(true);
        secret.invoke(p1);

    }
}