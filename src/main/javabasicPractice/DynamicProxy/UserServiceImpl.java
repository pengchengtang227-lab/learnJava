package javabasicPractice.DynamicProxy;

public class UserServiceImpl implements UserService {

    @Override
    public void addUser() {
        System.out.println("添加用户");
    }

    @Override
    public void deleteUser() {
        System.out.println("删除用户");
    }

    public void test() {
        System.out.println("test");
    }
}