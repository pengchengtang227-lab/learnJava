package copy;

import lombok.Getter;
import lombok.Setter;

/**
 * 员工对象：name 为不可变 String，address 为需要重点处理的可变引用。
 */
public class Employee implements Cloneable {
    @Setter
    private String name;

    @Setter
    private int age;
    @Getter
    private Address address;

    public Employee(String name, int age, Address address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    /**
     * 浅拷贝：复制 Employee 对象本身，不复制 address 指向的实际对象。
     */
    @Override
    protected Employee clone() throws CloneNotSupportedException {
        return (Employee) super.clone();
    }

    /**
     * 深拷贝：先复制 Employee，再显式复制它的可变引用成员 address。
     */
    public Employee deepClone() throws CloneNotSupportedException {
        Employee copy = (Employee) super.clone();
        copy.address = address.clone();
        copy.age = age;
        copy.name = name;
        return copy;
    }

    @Override
    public String toString() {
        return "Employee{name='" + name + "',age='" +age+"' address=" + address + '}';
    }
}
