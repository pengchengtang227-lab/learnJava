package javabasicPractice.DynamicProxy;

import java.lang.reflect.Proxy;

public class Test {

    public static void main(String[] args) {

        UserServiceImpl userService = new UserServiceImpl();

        UserService proxy = (UserService) Proxy.newProxyInstance(
                userService.getClass().getClassLoader(),
                userService.getClass().getInterfaces(),
                (proxy1, method, args1) -> {

                    System.out.println("执行前");

                    Object result = method.invoke(userService, args1);

                    System.out.println("执行后");

                    return result;
                }
        );

        proxy.addUser();
    }
}