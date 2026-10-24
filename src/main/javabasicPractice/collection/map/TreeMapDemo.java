package javabasicPractice.collection.map;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 演示 TreeMap 的排序、基本操作、邻近键查询和范围视图。
 *
 * <p>TreeMap 底层使用红黑树，按照键的自然顺序或指定的 Comparator 排序，
 * 不按照插入顺序、访问顺序或值排序。get、put、remove 和 containsKey 的时间复杂度为 O(log n)，
 * 遍历全部键值对的时间复杂度为 O(n)。</p>
 * <p>比较器或 compareTo 返回 0 时，TreeMap 将两个键视为同一个键，再次 put 会覆盖旧值。
 * 键的排序规则应与 equals 保持一致，放入后不要修改影响排序的字段。</p>
 * <p>自然排序不允许 null 键，但允许 null 值；使用比较器时，能否接受 null 键由比较器决定。
 * TreeMap 本身不保证线程安全。</p>
 */
public class TreeMapDemo {

    private static final int JAVA_COURSE_ID = 10;
    private static final int DATABASE_COURSE_ID = 20;
    private static final int CACHE_COURSE_ID = 30;
    private static final int FRAMEWORK_COURSE_ID = 40;
    private static final int NEIGHBOR_LOOKUP_COURSE_ID = 25;
    private static final String JAVA_COURSE_NAME = "Java";
    private static final String DATABASE_COURSE_NAME = "MySQL";
    private static final String CACHE_COURSE_NAME = "Redis";
    private static final String FRAMEWORK_COURSE_NAME = "Spring Boot";
    private static final String UPDATED_JAVA_COURSE_NAME = "Java 进阶";
    private static final String UPDATED_DATABASE_COURSE_NAME = "MySQL 进阶";

    /**
     * 运行各类 TreeMap 示例。
     *
     * @param args 命令行参数，本示例不使用
     */
    public static void main(String[] args) {
        demonstrateNaturalOrder();
//        demonstrateCustomOrder();
//        demonstrateNavigation();
//        demonstrateRangeView();
    }

    /** 演示整数键的自然升序，以及查询、更新、遍历和删除操作。 */
    private static void demonstrateNaturalOrder() {
        Map<Integer, String> courseMap = createCourseMap();

        System.out.println("=== TreeMap 自然排序和基本操作 ===");
        System.out.println("按课程编号升序：" + courseMap);
        System.out.println("查询 Java 课程：" + courseMap.get(JAVA_COURSE_ID));

        // 更新已有键只覆盖值，不增加键值对数量，也不会改变按键排序的规则。
        String previousCourseName = courseMap.put(JAVA_COURSE_ID, UPDATED_JAVA_COURSE_NAME);
        System.out.println("更新前的课程名称：" + previousCourseName);
        System.out.println("更新后：" + courseMap);
        System.out.println("键值对数量：" + courseMap.size());

        // 通过 entrySet 同时取得键和值，避免遍历键后再次调用 get 查询。
        for (Map.Entry<Integer, String> courseEntry : courseMap.entrySet()) {
            System.out.println(courseEntry.getKey() + " -> " + courseEntry.getValue());
        }

        courseMap.remove(CACHE_COURSE_ID);
        System.out.println("删除 Redis 课程后：" + courseMap);
        System.out.println("Redis 课程是否存在：" + courseMap.containsKey(CACHE_COURSE_ID));
        System.out.println();
    }

    /** 演示通过 Comparator 将课程编号改为降序排列。 */
    private static void demonstrateCustomOrder() {
        // 使用自然顺序的逆序，避免通过整数相减比较大小导致溢出。
        Map<Integer, String> descendingCourseMap = new TreeMap<>(Comparator.reverseOrder());
        descendingCourseMap.putAll(createCourseMap());

        System.out.println("=== TreeMap 自定义排序 ===");
        System.out.println("按课程编号降序：" + descendingCourseMap);
        System.out.println();
    }

    /**
     * 演示首尾节点、邻近键查询和首节点删除操作。
     *
     * <p>lowerEntry 和 higherEntry 严格小于、大于目标键；
     * floorEntry 和 ceilingEntry 允许等于目标键。没有符合条件的节点时返回 null。</p>
     */
    private static void demonstrateNavigation() {
        NavigableMap<Integer, String> courseMap = createCourseMap();

        System.out.println("=== TreeMap 邻近键查询 ===");
        System.out.println("最小课程编号对应的节点：" + courseMap.firstEntry());
        System.out.println("最大课程编号对应的节点：" + courseMap.lastEntry());
        System.out.println("严格小于 20 的最近节点：" + courseMap.lowerEntry(DATABASE_COURSE_ID));
        System.out.println("严格大于 20 的最近节点：" + courseMap.higherEntry(DATABASE_COURSE_ID));
        System.out.println("小于或等于 20 的最近节点：" + courseMap.floorEntry(DATABASE_COURSE_ID));
        System.out.println("大于或等于 20 的最近节点：" + courseMap.ceilingEntry(DATABASE_COURSE_ID));

        // 即使目标键 25 不存在，也能找到它两侧最近的键值对。
        System.out.println("小于或等于 25 的最近节点：" + courseMap.floorEntry(NEIGHBOR_LOOKUP_COURSE_ID));
        System.out.println("大于或等于 25 的最近节点：" + courseMap.ceilingEntry(NEIGHBOR_LOOKUP_COURSE_ID));
        System.out.println("小于最小课程编号的节点：" + courseMap.lowerEntry(JAVA_COURSE_ID));

        // firstEntry 只读取首节点；pollFirstEntry 读取并删除首节点。
        System.out.println("取出并删除最小课程编号节点：" + courseMap.pollFirstEntry());
        System.out.println("删除首节点后：" + courseMap);
        System.out.println();
    }

    /**
     * 演示按键范围获取视图，以及通过复制视图得到独立的 TreeMap。
     *
     * <p>subMap、headMap 和 tailMap 返回原 Map 的视图，修改其中的映射会影响原 Map，反之亦然。
     * 通过范围视图插入范围之外的键会抛出 IllegalArgumentException。</p>
     */
    private static void demonstrateRangeView() {
        NavigableMap<Integer, String> courseMap = createCourseMap();

        // 两个布尔参数分别决定是否包含下界和上界，这里表示 [20, 40)。
        NavigableMap<Integer, String> rangeView = courseMap.subMap(
                DATABASE_COURSE_ID, true, FRAMEWORK_COURSE_ID, false);

        System.out.println("=== TreeMap 范围视图 ===");
        System.out.println("编号范围 [20, 40)：" + rangeView);
        System.out.println("编号小于 20：" + courseMap.headMap(DATABASE_COURSE_ID, false));
        System.out.println("编号大于或等于 30：" + courseMap.tailMap(CACHE_COURSE_ID, true));

        // 复制键值对到新 Map；后续替换视图中的映射不会改变副本。
        Map<Integer, String> rangeCopy = new TreeMap<>(rangeView);
        rangeView.put(DATABASE_COURSE_ID, UPDATED_DATABASE_COURSE_NAME);
        System.out.println("通过视图更新后的原 Map：" + courseMap);
        System.out.println("更新前创建的独立副本：" + rangeCopy);
        System.out.println();
    }

    /**
     * 创建课程示例数据，故意打乱插入顺序以展示 TreeMap 的排序效果。
     *
     * @return 按课程编号自然升序排列的 Map，每次调用都创建独立实例
     */
    private static NavigableMap<Integer, String> createCourseMap() {
        NavigableMap<Integer, String> courseMap = new TreeMap<>();
        courseMap.put(CACHE_COURSE_ID, CACHE_COURSE_NAME);
        courseMap.put(JAVA_COURSE_ID, JAVA_COURSE_NAME);
        courseMap.put(FRAMEWORK_COURSE_ID, FRAMEWORK_COURSE_NAME);
        courseMap.put(DATABASE_COURSE_ID, DATABASE_COURSE_NAME);
        return courseMap;
    }
}
