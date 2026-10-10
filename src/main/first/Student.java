package first;

import lombok.Data;

import java.util.Objects;

@Data
public class Student extends Person {


    // 子类自己的成员变量
    String school;
    int score;

    public Student(String name, String school, int score) {
        this.name = name;
        this.school = school;
        this.score = score;
    }

    public Student() {
    }

    // 子类自己的方法
    public void study() {
        System.out.println(name + "正在" + school + "学习");
    }

    public void showScore() {
        System.out.println(name + "的成绩是：" + score);
    }

    @Override
    public void eat() {
        System.out.println(name + "Student吃饭");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Student student)) {
            return false;
        }

        return score == student.score && Objects.equals(school, student.school) && Objects.equals(name, student.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, school, score);
    }
}