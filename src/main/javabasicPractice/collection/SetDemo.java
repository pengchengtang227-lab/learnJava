package javabasicPractice.collection;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/** 演示 Set 的常用操作、去重规则以及常见实现的顺序差异。 */
public class SetDemo {

    public static void main(String[] args) {
        demonstrateHashSet();
        demonstrateLinkedHashSet();
        demonstrateTreeSet();
    }

    /** 演示 HashSet 的去重、查询和删除操作。 */
    private static void demonstrateHashSet() {
        Set<String> hashSet = new HashSet<>();
        hashSet.add("香蕉");
        hashSet.add("苹果");
        boolean addedAgain = hashSet.add("香蕉");

        // HashSet 去重，但不保证遍历顺序。
        System.out.println("=== HashSet：去重，不保证顺序 ===");
        System.out.println(hashSet);
        System.out.println("重复添加香蕉是否成功：" + addedAgain);
        System.out.println("是否包含苹果：" + hashSet.contains("苹果"));

        hashSet.remove("苹果");
        System.out.println("删除苹果后的集合：" + hashSet);
        System.out.println();
    }

    /** 演示 LinkedHashSet 保留插入顺序的特点。 */
    private static void demonstrateLinkedHashSet() {
        Set<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("香蕉");
        linkedHashSet.add("苹果");
        linkedHashSet.add("香蕉");

        // LinkedHashSet 去重，并保留元素插入顺序。
        System.out.println("=== LinkedHashSet：保留插入顺序 ===");
        System.out.println(linkedHashSet);
        System.out.println();
    }

    /** 演示 TreeSet 的排序以及首尾元素查询。 */
    private static void demonstrateTreeSet() {
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.add(3);
        treeSet.add(1);
        treeSet.add(2);

        // TreeSet 去重，并按自然顺序排序。
        System.out.println("=== TreeSet：按自然顺序排序 ===");
        System.out.println(treeSet);
        System.out.println("最小元素：" + treeSet.first());
    }
}
