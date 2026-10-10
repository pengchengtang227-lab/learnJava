package first;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Test {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Student student = new Student();

        // 父类的成员变量
        student.name = "张三";


        // 子类自己的成员变量
        student.school = "清华大学";
        student.score = 95;

        // 父类的方法
        student.eat();
        student.sleep();

        // 子类的方法
        student.study();
        student.showScore();


//        CompletableFuture<String> future =
//                CompletableFuture.supplyAsync(() -> {
//                    return "Hello";
//                });
//
//        System.out.println(future.get());
//
//
//        CompletableFuture<Void> future1 =
//                CompletableFuture.runAsync(() -> {
//                    System.out.println("执行任务");
//                });
//
//
        String a= "a";
        String b= "a";
        System.out.println(a.equals(b));
        System.out.println(a.hashCode());
        System.out.println(b.hashCode());
        System.out.println("==============================");
        Student person = new Student("pp", "a", 10);
        Student person1 = new Student("pp", "a", 10);
        System.out.println(person.equals(person1));
        System.out.println(person.hashCode());
        System.out.println(person1.hashCode());

    }
}