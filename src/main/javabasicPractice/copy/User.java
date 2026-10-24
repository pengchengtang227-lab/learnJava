package javabasicPractice.copy;

import lombok.Getter;
import lombok.Setter;

public class User {
    @Setter
    private String name;

    @Setter
    private int age;
    @Getter
    private Address address;

    public User(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", address=" + address +
                '}';
    }
}
