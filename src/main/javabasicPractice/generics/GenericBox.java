package javabasicPractice.generics;

/**
 * 泛型类：T 是“类型参数”，创建对象时再决定实际类型。
 *
 * @param <T> 要保存的数据类型，例如 String、Integer 或自定义类
 */
public class GenericBox<T> {
    private T value;

    public GenericBox(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
