package SerializableProject;

import java.io.*;

public class SerializeDemo {

    public static void main(String[] args) throws Exception {

        // 创建 Java 对象
        User user = new User("张三", 20, "59599", "122");

        // 序列化
        ObjectOutputStream out =
                new ObjectOutputStream(
                        new FileOutputStream("user.txt"));

        out.writeObject(user);

        out.close();

        System.out.println("序列化完成！");
    }
}