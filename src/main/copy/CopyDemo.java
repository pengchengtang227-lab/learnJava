package copy;

import java.util.ArrayList;
import java.util.List;

/**
 * 深拷贝与浅拷贝演示。
 *
 * <p>运行本类后，修改副本中的地址信息：浅拷贝会影响原对象，深拷贝不会。</p>
 */
public class CopyDemo {

    public static void main(String[] args) throws CloneNotSupportedException {
        demonstrateShallowCopy();
        System.out.println("----------------------------------------");
        demonstrateDeepCopy();
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
    }

    private static void demonstrateShallowCopy() throws CloneNotSupportedException {
        Address address = new Address("北京", "朝阳区");
        Employee original = new Employee("张三",10, address);

        // Object.clone() 只复制当前对象的字段值。
        // address 是引用类型，因此 original 和 shallowCopy 指向同一个 Address 对象。
        Employee shallowCopy = original.clone();
        shallowCopy.getAddress().setDistrict("海淀区");
        shallowCopy.setAge(200);
        shallowCopy.setName("武哥");

        System.out.println("浅拷贝：");
        System.out.println("原对象 = " + original);
        System.out.println("副本   = " + shallowCopy);
        System.out.println("地址是否为同一对象：" + (original.getAddress() == shallowCopy.getAddress()));
    }

    private static void demonstrateDeepCopy() throws CloneNotSupportedException {
        Address address = new Address("上海", "浦东新区");
        Employee original = new Employee("李四", 10, address);

        // deepClone() 同时复制 Employee 和它持有的 Address 对象。
        Employee deepCopy = original.deepClone();
        deepCopy.getAddress().setDistrict("徐汇区");
        deepCopy.setAge(10000);

        System.out.println("深拷贝：");
        System.out.println("原对象 = " + original);
        System.out.println("副本   = " + deepCopy);
        System.out.println("地址是否为同一对象：" + (original.getAddress() == deepCopy.getAddress()));
    }
}
