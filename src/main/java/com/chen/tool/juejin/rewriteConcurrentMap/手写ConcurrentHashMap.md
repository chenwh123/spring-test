
# 前言
常见的面试题了，因为我每次找工作都得重新找资源，老是忘记细节，因此自己也写一下加深记忆。

> 本文使用JDK11，源码可能和JDK8有所不同，但思路肯定是一样的。

# 核心方法
- put(K key, V value) void : 
- get(K key) => V : 


```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
        implements ConcurrentMap<K,V>, Serializable {
    /**
     * Table initialization and resizing control.  When negative, the
     * table is being initialized or resized: -1 for initialization,
     * else -(1 + the number of active resizing threads).  Otherwise,
     * when table is null, holds the initial table size to use upon
     * creation, or 0 for default. After initialization, holds the
     * next element count value upon which to resize the table.
     * 
     * 基本等同数组长度
     */
    private transient volatile int sizeCtl;

    /**
     * Creates a new, empty map with an initial table size based on
     * the given number of elements ({@code initialCapacity}), initial
     * table density ({@code loadFactor}), and number of concurrently
     * updating threads ({@code concurrencyLevel}).
     *
     * @param initialCapacity the initial capacity. The implementation
     * performs internal sizing to accommodate this many elements,
     * given the specified load factor.
     * @param loadFactor the load factor (table density) for
     * establishing the initial table size //因子，默认0.75，因子越大空位越多（没啥用，一般只用于初始化）
     * @param concurrencyLevel the estimated number of concurrently
     * updating threads. The implementation may use this value as
     * a sizing hint. //最小数组长度（没啥用，一般为1）
     *                         
     */
    public ConcurrentHashMap(int initialCapacity,
                             float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();
        if (initialCapacity < concurrencyLevel)   // Use at least as many bins
            initialCapacity = concurrencyLevel;   // as estimated threads
        long size = (long)(1.0 + (long)initialCapacity / loadFactor);
        int cap = (size >= (long)MAXIMUM_CAPACITY) ?
                MAXIMUM_CAPACITY : tableSizeFor((int)size);
        this.sizeCtl = cap;
    }
    
    // ...
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        int hash = spread(key.hashCode());
        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh; // fh 头节点的hash ， i key经过转换后的数组索引
            K fk;
            V fv;
            if (tab == null || (n = tab.length) == 0)
                tab = initTable(); // 初始化数组
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null, new Node<K, V>(hash, key, value))) // 
                    break;                   // no lock when adding to empty bin
            } else if ((fh = f.hash) == MOVED) 
                tab = helpTransfer(tab, f); // 当前数组在扩容，获取扩容后的数组
            else if (onlyIfAbsent // check first node without acquiring lock
                    && fh == hash
                    && ((fk = f.key) == key || (fk != null && key.equals(fk)))
                    && (fv = f.val) != null)
                return fv;
            else {
                V oldVal = null;
                synchronized (f) {
                    if (tabAt(tab, i) == f) { // 重新判断头节点是否是当前节点 ， 防止由于扩容或者删除导致的头结点变更
                        if (fh >= 0) {  // 判断头部节点是不是链表 ， 负数则是树
                            binCount = 1; // 链表序号
                            for (Node<K, V> e = f; ; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                        ((ek = e.key) == key ||
                                                (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value; // 替换value，跳出循环
                                    break;
                                }
                                Node<K, V> pred = e; // 遍历链表的下一个节点
                                if ((e = e.next) == null) { // 空则直接插入一个新的节点，并跳出循环
                                    pred.next = new Node<K, V>(hash, key, value);
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) { // 头部节点是红黑树根节点
                            Node<K, V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K, V>) f).putTreeVal(hash, key,
                                    value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        } else if (f instanceof ReservationNode)
                            throw new IllegalStateException("Recursive update");
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD) //若链表长度 >=8 转变为红黑树
                        treeifyBin(tab, i);
                    if (oldVal != null) // 空则是插入，非空则是替换
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount); // 尝试扩容
        return null;
    }

    /**
     * 尝试扩容到指定的大小，非2^n则自动扩大到2^n
     * 
     * Tries to presize table to accommodate the given number of elements.
     * @param size number of elements (doesn't need to be perfectly accurate)
     */
    private final void tryPresize(int size) {
        // c： 期望的数组大小，必然为2^n
        int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
                tableSizeFor(size + (size >>> 1) + 1); 
        int sc; 
        while ((sc = sizeCtl) >= 0) {
            Node<K,V>[] tab = table; int n;
            if (tab == null || (n = tab.length) == 0) { //执行数组初始化
                // 实际的数组长度，这里n可能并非是2^n ? 
                n = (sc > c) ? sc : c; // max(sc,c);
                if (U.compareAndSetInt(this, SIZECTL, sc, -1)) { //设置为resize状态
                    try {
                        if (table == tab) { // 重新判断数组是否为空
//                            @SuppressWarnings("unchecked")
//                            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
//                            table = nt;
                            table = (Node<K,V>[])new Node<?,?>[n];
                            sc = n - (n >>> 2); // 相当于 n*0.75
                        }
                    } finally { // 恢复状态
                        sizeCtl = sc;
                    }
                }
            }
            else if (c <= sc || n >= MAXIMUM_CAPACITY) // 唯一可以退出循环的行
                break;
            else if (tab == table) { 
                int rs = resizeStamp(n);
                if (U.compareAndSetInt(this, SIZECTL, sc,
                        (rs << RESIZE_STAMP_SHIFT) + 2))
                    transfer(tab, null);
            }
        }
    }
    //...
}
```