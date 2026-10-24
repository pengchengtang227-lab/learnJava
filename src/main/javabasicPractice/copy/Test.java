package javabasicPractice.copy;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<User> list = new ArrayList<>();
        Address address = new Address("北京", "朝阳区");
        User original = new User("张三",10, address);


        list.add(original);
        ArrayList<User> copy = (ArrayList<User>) ((ArrayList<User>) list).clone();
        copy.get(0).setName("pppp");
        User original1 = new User("new",100, address);
        copy.add(original1);


        System.out.println("list===="+ list);
        System.out.println("javabasicPractice.copy====" + copy);


        Integer integer = Integer.valueOf("200");
        Integer integer1 = Integer.valueOf("200");
        System.out.println(integer==integer1);
    }
}
