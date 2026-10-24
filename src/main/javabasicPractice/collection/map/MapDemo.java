package javabasicPractice.collection.map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HashMap 的哈希冲突、LinkedHashMap 的有序遍历、LRU 缓存和 ConcurrentHashMap 示例。
 *
 * <p>两个不同的键可以有相同的 hashCode。HashMap 会把它们放到同一个桶中，
 * 再通过 equals() 区分具体的键。</p>
 * <p>JDK 8 中，桶内节点达到树化阈值时会尝试转成红黑树；
 * 如果数组长度小于 64，则会先扩容。</p>
 */
public class MapDemo {

    private static final int LINKED_MAP_INITIAL_CAPACITY = 16;
    private static final float LINKED_MAP_LOAD_FACTOR = 0.75F;
    private static final int LRU_MAX_ENTRIES = 2;
    private static final String LANGUAGE_KEY = "language";
    private static final String DATABASE_KEY = "database";
    private static final String CACHE_KEY = "cache";
    private static final String JAVA_LANGUAGE = "Java";
    private static final String KOTLIN_LANGUAGE = "Kotlin";
    private static final String MYSQL_DATABASE = "MySQL";
    private static final String REDIS_CACHE = "Redis";

    public static void main(String[] args) {
//        demonstrateLinkedHashMapInsertionOrder();
//        demonstrateLinkedHashMapAccessOrder();
        demonstrateLruCache();
//        demonstrateConcurrentHashMap();
//        demonstrateHashMapCollision();
    }

    /**
     * 演示 LinkedHashMap 默认的插入顺序，以及添加、查询、修改、遍历和删除操作。
     *
     * <p>LinkedHashMap 在哈希表之外，通过贯穿所有键值对的双向链表维护遍历顺序，
     * 这条链表与单个桶中处理哈希冲突的链表不同。</p>
     * <p>默认模式下，查询或更新已有键不会改变顺序；删除后重新插入会排到末尾。
     * LinkedHashMap 本身不保证线程安全。</p>
     */
    private static void demonstrateLinkedHashMapInsertionOrder() {
        Map<String, String> insertionOrderMap = new LinkedHashMap<>();

        // 默认按照键首次插入的先后顺序遍历，不会按照键名或值自动排序。
        insertionOrderMap.put(LANGUAGE_KEY, JAVA_LANGUAGE);
        insertionOrderMap.put(DATABASE_KEY, MYSQL_DATABASE);
        insertionOrderMap.put(CACHE_KEY, REDIS_CACHE);

        System.out.println("=== LinkedHashMap 插入顺序 ===");
        System.out.println("插入后：" + insertionOrderMap);
        System.out.println("查询 language：" + insertionOrderMap.get(LANGUAGE_KEY));

        // 同一个键再次 put 会覆盖旧值，不会新增键值对，也不会改变该键的位置。
        insertionOrderMap.put(LANGUAGE_KEY, KOTLIN_LANGUAGE);
        System.out.println("更新 language 后：" + insertionOrderMap);

        // 需要同时使用键和值时，通过 entrySet 遍历，避免再调用 get 查询。
        for (Map.Entry<String, String> entry : insertionOrderMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        insertionOrderMap.remove(DATABASE_KEY);
        System.out.println("删除 database 后：" + insertionOrderMap);
        insertionOrderMap.put(DATABASE_KEY, MYSQL_DATABASE);
        System.out.println("重新插入 database 后：" + insertionOrderMap);
        System.out.println();
    }

    /**
     * 演示 LinkedHashMap 的访问顺序模式。
     *
     * <p>构造方法的第三个参数 accessOrder 为 true 时，按从最久未访问到最近访问的顺序遍历。
     * get 命中或 put 更新已有键时，会把对应节点移动到链表末尾。</p>
     * <p>该模式可用于构建 LRU 缓存，但不会自动限制大小或淘汰元素；
     * 还需要额外实现淘汰策略。遍历期间不要调用会调整顺序的操作。</p>
     */
    private static void demonstrateLinkedHashMapAccessOrder() {
        // 三个参数依次为初始容量、负载因子和是否按访问顺序维护节点。
        Map<String, String> accessOrderMap = new LinkedHashMap<>(
                LINKED_MAP_INITIAL_CAPACITY, LINKED_MAP_LOAD_FACTOR, true);
        accessOrderMap.put(LANGUAGE_KEY, JAVA_LANGUAGE);
        accessOrderMap.put(DATABASE_KEY, MYSQL_DATABASE);
        accessOrderMap.put(CACHE_KEY, REDIS_CACHE);

        System.out.println("=== LinkedHashMap 访问顺序 ===");
        System.out.println("初始顺序：" + accessOrderMap);
        System.out.println("查询 language：" + accessOrderMap.get(LANGUAGE_KEY));
        System.out.println("查询后，language 移到末尾：" + accessOrderMap);

        accessOrderMap.put(DATABASE_KEY, MYSQL_DATABASE);
        System.out.println("更新后，database 移到末尾：" + accessOrderMap);
        System.out.println();
    }

    /**
     * 演示容量上限为两个键值对的 LRU（最近最少使用）缓存。
     *
     * <p>缓存头部是最久未访问的元素，尾部是最近访问的元素。
     * 读取命中和更新已有键都会刷新访问顺序；读取未命中的键不会改变顺序。</p>
     */
    private static void demonstrateLruCache() {
        Map<String, String> lruCache = new LruCache<>(LRU_MAX_ENTRIES);
        lruCache.put(LANGUAGE_KEY, JAVA_LANGUAGE);
        lruCache.put(DATABASE_KEY, MYSQL_DATABASE);

        System.out.println("=== LinkedHashMap 完整 LRU 缓存 ===");
        System.out.println("容量上限：" + LRU_MAX_ENTRIES);
        System.out.println("填满缓存：" + lruCache);

        // language 被访问后移到尾部，database 成为最久未访问的键。
        System.out.println("读取 language：" + lruCache.get(LANGUAGE_KEY));
        System.out.println("读取后的顺序：" + lruCache);

        // 新增键导致数量超过上限，自动淘汰头部的 database。
        lruCache.put(CACHE_KEY, REDIS_CACHE);
        System.out.println("新增 cache，淘汰 database：" + lruCache);
        System.out.println("database 是否仍存在：" + lruCache.containsKey(DATABASE_KEY));

        // 更新已有键会刷新访问顺序，但不会增加数量或淘汰其他键。
        lruCache.put(LANGUAGE_KEY, KOTLIN_LANGUAGE);
        System.out.println("更新 language 后：" + lruCache);
        System.out.println("当前键值对数量：" + lruCache.size());

        System.out.println("读取已淘汰的 database：" + lruCache.get(DATABASE_KEY));
        System.out.println("读取未命中后的顺序：" + lruCache);

        // database 重新加入后，当前头部的 cache 被淘汰。
        lruCache.put(DATABASE_KEY, MYSQL_DATABASE);
        System.out.println("重新插入 database，淘汰 cache：" + lruCache);

        lruCache.remove(LANGUAGE_KEY);
        System.out.println("手动删除 language：" + lruCache);
        lruCache.clear();
        System.out.println("清空后是否为空：" + lruCache.isEmpty());
        System.out.println();
    }

    /** 演示不同键发生哈希冲突时，HashMap 如何通过 equals() 区分键。 */
    private static void demonstrateHashMapCollision() {
        Map<CollisionKey, String> map = new HashMap<>();

        CollisionKey firstKey = new CollisionKey("第一个键");
        CollisionKey secondKey = new CollisionKey("第二个键");

        map.put(firstKey, "第一个值");
        map.put(secondKey, "第二个值");

        System.out.println("=== HashMap 哈希冲突 ===");
        System.out.println("两个键的 hashCode 相同："
                + (firstKey.hashCode() == secondKey.hashCode()));
        System.out.println("两个键通过 equals 判断相同：" + firstKey.equals(secondKey));
        System.out.println("键值对数量：" + map.size());
        System.out.println("第一个键对应的值：" + map.get(firstKey));
        System.out.println("第二个键对应的值：" + map.get(secondKey));
        System.out.println();
    }

    /**
     * 演示 ConcurrentHashMap 的并发安全写入方法。
     *
     * <p>单次操作（如 putIfAbsent、computeIfAbsent）是线程安全的；
     * 不要把先检查再写入拆成两个独立操作，否则整体不具备原子性。</p>
     * <p>ConcurrentHashMap 不允许 null 键或 null 值。</p>
     */
    private static void demonstrateConcurrentHashMap() {
        Map<String, String> concurrentMap = new ConcurrentHashMap<>();

        // 仅当键不存在时才写入；并发调用时，该判断和写入作为一个原子操作完成。
        concurrentMap.putIfAbsent("language", "Java");
        concurrentMap.putIfAbsent("language", "Kotlin");

        // 仅当键不存在时才计算并写入，避免分开的 containsKey 和 put 操作。
        concurrentMap.computeIfAbsent("collection", key -> "ConcurrentHashMap");

        System.out.println("=== ConcurrentHashMap 原子写入 ===");
        System.out.println("键值对数量：" + concurrentMap.size());
        System.out.println("language 对应的值：" + concurrentMap.get("language"));
        System.out.println("javabasicPractice.collection 对应的值：" + concurrentMap.get("collection"));
        System.out.println();
    }

    /**
     * 基于 LinkedHashMap 实现按键值对数量限制容量的 LRU 缓存。
     *
     * <p>LRU 是 Least Recently Used 的缩写，通常译为“最近最少使用”，是一种缓存淘汰策略。
     * 当缓存已满且需要加入新键时，优先淘汰最久没有被访问的元素，保留最近使用的数据。</p>
     * <p>LRU 比较的是最后一次访问的先后顺序，不是累计访问次数；
     * 按使用频次淘汰属于 LFU（Least Frequently Used，最不经常使用）策略。</p>
     * <p>本实现通过 accessOrder=true 维护访问顺序：头部最久未访问，尾部最近访问。
     * 新键插入尾部；get 命中或 put 更新已有键时，对应节点移到尾部；
     * get 未命中和 containsKey 检查不会改变顺序。</p>
     * <p>例如上限为 2，初始顺序为 A → B（从头到尾）。访问 A 后变为 B → A；
     * 再插入 C 时，数量暂时超过上限，removeEldestEntry 返回 true，淘汰头部 B，最终为 A → C。</p>
     *
     * <p>继承 Map 的查询、写入、删除和清空操作；容量上限必须大于零。
     * 本实现不保证线程安全，不提供过期时间或按访问频次淘汰的功能。</p>
     *
     * @param <Key> 键类型
     * @param <Value> 值类型
     */
    private static final class LruCache<Key, Value> extends LinkedHashMap<Key, Value> {
        private static final long serialVersionUID = 1L;

        /** 缓存最多保留的键值对数量，与 LinkedHashMap 底层桶数组的容量不同。 */
        private final int maxEntries;

        /**
         * 创建按访问顺序维护节点的缓存。
         *
         * @param maxEntries 允许保存的最大键值对数量，与底层数组容量不同
         * @throws IllegalArgumentException 当容量上限小于或等于零时抛出
         */
        private LruCache(int maxEntries) {
            // accessOrder=true：读取命中或更新已有键时，将节点移动到尾部。
            super(LINKED_MAP_INITIAL_CAPACITY, LINKED_MAP_LOAD_FACTOR, true);
            if (maxEntries <= 0) {
                throw new IllegalArgumentException("LRU 缓存的容量上限必须大于零");
            }
            this.maxEntries = maxEntries;
        }

        /** 新节点插入后由 LinkedHashMap 调用，返回 true 时删除头部的最久未访问节点。 */
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Value> eldest) {
            // 此时新节点已经加入，所以用 >；使用 >= 会在刚好填满时就提前淘汰。
            return size() > maxEntries;
        }
    }

    /** 两个不同名称的键故意返回相同的哈希值，以演示哈希冲突。 */
    private static final class CollisionKey {
        private final String name;

        private CollisionKey(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof CollisionKey other)) {
                return false;
            }
            return name.equals(other.name);
        }
    }
}
