package ConstructorExamples;

public class ParentConstructorExamples {
    public static void main(String[] args) {
        System.out.println("=== 例子一：显式调用父类有参构造方法 ===");
        Student student = new Student("张三", 18);
        student.introduce();

        System.out.println("\n=== 例子二：使用 this(...) 复用子类构造方法 ===");
        Student defaultStudent = new Student();
        defaultStudent.introduce();

        System.out.println("\n=== 例子三：父类有多个有参构造方法 ===");
        Teacher teacher = new Teacher("李老师", "Java");
        teacher.introduce();
    }

    static class Person {
        private final String name;

        Person(String name) {
            this.name = name;
            System.out.println("Person(String) 被调用");
        }

        String getName() {
            return name;
        }
    }

    static class Student extends Person {
        private final int age;

        Student(String name, int age) {
            super(name);
            this.age = age;
            System.out.println("Student(String, int) 被调用");
        }

        Student() {
            this("未命名学生", 0);
        }

        void introduce() {
            System.out.println("学生：" + getName() + "，年龄：" + age);
        }
    }

    static class Employee {
        private final String name;

        Employee(String name) {
            this.name = name;
        }

        Employee(String name, String department) {
            this(name);
            System.out.println("部门：" + department);
        }

        String getName() {
            return name;
        }
    }

    static class Teacher extends Employee {
        Teacher(String name, String subject) {
            super(name, subject);
        }

        void introduce() {
            System.out.println("教师：" + getName());
        }
    }

}
