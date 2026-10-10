package first;

public record User(
        Long id,
        String name,
        Integer age
) {
    public void sayHello() {
        System.out.println("Hello World");
    }
}