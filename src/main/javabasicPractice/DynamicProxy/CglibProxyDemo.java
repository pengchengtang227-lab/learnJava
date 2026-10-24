package javabasicPractice.DynamicProxy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibProxyDemo {

    public static void main(String[] args) {

        Enhancer enhancer = new Enhancer();

        // 指定父类
        enhancer.setSuperclass(UserServiceImpl.class);

        // 设置方法拦截器
        enhancer.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(
                    Object obj,
                    Method method,
                    Object[] args,
                    MethodProxy methodProxy) throws Throwable {

                System.out.println("前置操作");

                // 调用父类原始方法
                Object result = methodProxy.invokeSuper(obj, args);

                System.out.println("后置操作");

                return result;
            }
        });

        // 创建代理对象
        UserServiceImpl userService =
                (UserServiceImpl) enhancer.create();

        userService.addUser();
    }
}
