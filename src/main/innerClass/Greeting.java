package innerClass;

public interface Greeting {
    void hello();
     default void showInfo() {
        System.out.println("飞行能力");
    }
}
