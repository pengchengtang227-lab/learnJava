package javabasicPractice.collection;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** 演示 List 的常用操作，以及有序、可重复和按索引访问等特点。 */
public class ListDemo {

    public static void main(String[] args) {
        demonstrateListOperations();
        demonstrateLinkedListInsertionAndDeletion();

        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        copyOnWriteArrayList.add("a");
        copyOnWriteArrayList.add("b");
        copyOnWriteArrayList.add(null);
        System.out.println("=========" +copyOnWriteArrayList);
    }

    /** 演示添加、查询、替换和删除元素。 */
    private static void demonstrateListOperations() {
        List<String> fruitList = new ArrayList<>();

        // 添加元素：List 保留插入顺序，并允许重复元素。
        fruitList.add("苹果");
        fruitList.add("香蕉");
        fruitList.add("苹果");
        fruitList.add(null);

        System.out.println("=== List ===");
        System.out.println("所有元素：" + fruitList);
        System.out.println("索引 0 的元素：" + fruitList.get(0));
        System.out.println("是否包含香蕉：" + fruitList.contains("香蕉"));

        // 按索引替换元素；remove(Object) 按元素值删除。
        fruitList.set(1, "橙子");
        fruitList.remove("苹果");
        System.out.println("替换并删除后的元素：" + fruitList);

        // 使用 removeIf 批量删除满足条件的元素，避免遍历时直接调用 list.remove()。
//        fruitList.removeIf(fruit -> fruit.startsWith("苹"));
//        System.out.println("删除苹果类水果后的元素：" + fruitList);
//        System.out.println("元素数量：" + fruitList.size());
    }

    /** 演示使用迭代器在 LinkedList 中定位并插入、删除元素。 */
    private static void demonstrateLinkedListInsertionAndDeletion() {
        LinkedList<String> linkedList = new LinkedList<>(List.of("A", "B", "C", "D"));

        System.out.println("\n=== LinkedList 插入和删除 ===");
        System.out.println("初始链表：" + linkedList);

        // 迭代器从头开始逐个访问节点，找到 B 时，在它后面插入 X。
        // 找到 B 之前的遍历可能需要 O(n)；迭代器到达位置后，插入只需调整链接，为 O(1)。
        ListIterator<String> insertIterator = linkedList.listIterator();
        while (insertIterator.hasNext()) {
            String current = insertIterator.next();
            if (current.equals("B")) {
                insertIterator.add("X");
                break;
            }
        }
        System.out.println("在 B 后插入 X：" + linkedList);

        // 再次遍历找到 C，并通过迭代器删除刚刚访问的元素。
        // 定位 C 需要遍历；定位后 remove() 通过迭代器维护的节点位置调整链接，为 O(1)。
        ListIterator<String> removeIterator = linkedList.listIterator();
        while (removeIterator.hasNext()) {
            String current = removeIterator.next();
            if (current.equals("C")) {
                removeIterator.remove();
                break;
            }
        }
        System.out.println("删除 C：" + linkedList);

        // 按下标操作虽然写法更直接，但 LinkedList 仍需先遍历到对应位置，整体通常是 O(n)。
        linkedList.add(1, "Y");
        System.out.println("按索引 1 插入 Y：" + linkedList);
    }
}
