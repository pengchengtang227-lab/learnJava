package copy;

import lombok.Setter;

/**
 * 地址是 Employee 的可变引用成员，用它可直观看出两种复制的区别。
 */
public class Address implements Cloneable {
    private final String city;
    @Setter
    private String district;

    public Address(String city, String district) {
        this.city = city;
        this.district = district;
    }

    @Override
    protected Address clone() throws CloneNotSupportedException {
        return (Address) super.clone();
    }

    @Override
    public String toString() {
        return city + "-" + district;
    }
}
