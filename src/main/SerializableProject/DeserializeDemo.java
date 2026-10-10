package SerializableProject;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class DeserializeDemo {

    public static void main(String[] args) throws Exception {

        // 反序列化
        ObjectInputStream in =
                new ObjectInputStream(
                        new FileInputStream("user.txt"));

        User user = (User) in.readObject();

        in.close();

        System.out.println(user);
    }
}