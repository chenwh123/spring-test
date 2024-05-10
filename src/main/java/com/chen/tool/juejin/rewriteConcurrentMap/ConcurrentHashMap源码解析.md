
# 前言
常见的面试题了，因为我每次找工作都得重新找资源，老是忘记细节，因此自己也写一下加深记忆。

> 本文使用JDK11，源码可能和JDK8有所不同，但思路肯定是一样的。

# 核心方法
- put(K key, V value) void :
- get(K key) => V :
- transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) void :


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
     * The next table to use; non-null only while resizing.
     * 扩容时用到的新数组，非扩容状态一般为空值
     */
    private transient volatile Node<K,V>[] nextTable;

    /**
     * The number of bits used for generation stamp in sizeCtl.
     * Must be at least 6 for 32bit arrays.
     */
    private static final int RESIZE_STAMP_BITS = 16;
    
    /**
     * The maximum number of threads that can help resize.
     * Must fit in 32 - RESIZE_STAMP_BITS bits.
     * 最大的扩容线程数， 这里只关心二进制的1的数量，因此是16
     * 默认值： 0b 0000 0000 0000 0000 1111 1111 1111 1111
     * 
     */
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;


    /**
     * The next table index (plus one) to split while resizing.
     */
    private transient volatile int transferIndex;
    /**
     * Minimum number of rebinnings per transfer step. Ranges are
     * subdivided to allow multiple resizer threads.  This value
     * serves as a lower bound to avoid resizers encountering
     * excessive memory contention.  The value should be at least
     * DEFAULT_CAPACITY.
     */
    private static final int MIN_TRANSFER_STRIDE = 16;


    /**
     * Base counter value, used mainly when there is no contention,
     * but also as a fallback during table initialization
     * races. Updated via CAS.
     * 表示当前map元素数量，多线程环境下需要加上counterCells的值
     */
    private transient volatile long baseCount;

    /**
     * Table of counter cells. When non-null, size is a power of 2.
     */
    private transient volatile CounterCell[] counterCells;

    /**
     * Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
     * 自旋锁，只有0，1两个值，1表示上锁
     */
    private transient volatile int cellsBusy;


    /**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * The value should be at least 4 * TREEIFY_THRESHOLD to avoid
     * conflicts between resizing and treeification thresholds.
     */
    static final int MIN_TREEIFY_CAPACITY = 64;


    
    /**
     * 核心*2 = 逻辑线程数
     * 8核则返回16
     */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

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
                    if (binCount >= TREEIFY_THRESHOLD) //若链表长度 >=8 而且 数组长度 < 64 则转变为红黑树 
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
     * Adds to count, and if table is too small and not already
     * resizing, initiates transfer. If already resizing, helps
     * perform transfer if work is available.  Rechecks occupancy
     * after a transfer to see if another resize is already needed
     * because resizings are lagging additions.
     *
     * @param x the count to add
     * @param check if <0, don't check resize, if <= 1 only check if uncontended
     */
    private final void addCount(long x, int check) {
        // 单线程环境下cs并不会赋值
        CounterCell[] cs; long b, s;
        if ((cs = counterCells) != null ||
                !U.compareAndSetLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            // 多线程竞争希望设置baseCount ，则会进入该方法
            CounterCell c; long v; int m;
            boolean uncontended = true;
            if (cs == null || (m = cs.length - 1) < 0 ||
                    (c = cs[ThreadLocalRandom.getProbe() & m]) == null ||
                    !(uncontended =
                            U.compareAndSetLong(c, CELLVALUE, v = c.value, v + x))) {
                fullAddCount(x, uncontended);
                return;
            }
            if (check <= 1)
                return;
            s = sumCount();
        }
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                    (n = tab.length) < MAXIMUM_CAPACITY) {
                int rs = resizeStamp(n) << RESIZE_STAMP_SHIFT;
                if (sc < 0) {
                    if (sc == rs + MAX_RESIZERS || sc == rs + 1 ||
                            (nt = nextTable) == null || transferIndex <= 0)
                        break;
                    if (U.compareAndSetInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                else if (U.compareAndSetInt(this, SIZECTL, sc, rs + 2))
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }

    /**
     * @param x the count to add
     * @param wasUncontended 代表对counterCell的竞争状态，如果为false，则代表是多线程竞争 ， true则无竞争
     */
    private final void fullAddCount(long x, boolean wasUncontended) {
        int h;
        if ((h = ThreadLocalRandom.getProbe()) == 0) {
            // 初始化线程的probe值，其实相当于线程hash值，只不过这个hash值有必要时可以主动改变
            ThreadLocalRandom.localInit();      // force initialization
            h = ThreadLocalRandom.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;                // True if last slot nonempty
        for (;;) {
            CounterCell[] cs; CounterCell c; int n; long v;
            // 判断CounterCell 是否有初始化
            if ((cs = counterCells) != null && (n = cs.length) > 0) {
                if ((c = cs[(n - 1) & h]) == null) {
                    if (cellsBusy == 0) {            // Try to attach new Cell
                        CounterCell r = new CounterCell(x); // Optimistic create
                        if (cellsBusy == 0 &&
                                U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
//                            boolean created = false;
                            try {               // Recheck under lock
                                CounterCell[] rs; int m, j;
                                if ((rs = counterCells) != null
                                        && (m = rs.length) > 0
                                        && rs[j = (m - 1) & h] == null) {
                                    rs[j] = r;
                                    break;
//                                    created = true;
                                }
                            } finally {
                                cellsBusy = 0;
                            }
//                            if (created)
//                                break;
                            continue;           // Slot is now non-empty
                        }
                    }
                    collide = false;
                }
                else if (!wasUncontended)       // CAS already known to fail
                    wasUncontended = true;      // Continue after rehash
                else if (U.compareAndSetLong(c, CELLVALUE, v = c.value, v + x))
                    break;
                else if (counterCells != cs || n >= NCPU)
                    collide = false;            // At max size or stale
                else if (!collide)
                    collide = true;
                else if (cellsBusy == 0 &&
                        U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
                    try {
                        if (counterCells == cs) // Expand table unless stale
                            counterCells = Arrays.copyOf(cs, n << 1);
                    } finally {
                        cellsBusy = 0;
                    }
                    collide = false;
                    continue;                   // Retry with expanded table
                }
                h = ThreadLocalRandom.advanceProbe(h);
            }
            else if (cellsBusy == 0 && counterCells == cs &&
                    U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
                // 初始化counterCell长度位2 
                // 理论上只有1个线程可以进入
//                boolean init = false;
                try {                           // Initialize table
                    if (counterCells == cs) {
                        CounterCell[] rs = new CounterCell[2];
                        rs[h & 1] = new CounterCell(x);
                        counterCells = rs;
//                        init = true;
                        break;
                    }
                } finally {
                    cellsBusy = 0;
                }
//                if (init)
//                    break;
            }
            else if (U.compareAndSetLong(this, BASECOUNT, v = baseCount, v + x)) //重新尝试设置baseCount,成功则返回，否则继续循环
                break;                          // Fall back on using base
        }
    }

    /**
     * Replaces all linked nodes in bin at given index unless table is
     * too small, in which case resizes instead.
     */
    private final void treeifyBin(Node<K,V>[] tab, int index) {
        Node<K,V> b; int n;
        if (tab != null) {
            if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
                tryPresize(n << 1);
            else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        TreeNode<K,V> hd = null, tl = null;
                        for (Node<K,V> e = b; e != null; e = e.next) {
                            TreeNode<K,V> p =
                                    new TreeNode<K,V>(e.hash, e.key, e.val,
                                            null, null);
                            if ((p.prev = tl) == null)
                                hd = p;
                            else
                                tl.next = p;
                            tl = p;
                        }
                        setTabAt(tab, index, new TreeBin<K,V>(hd));
                    }
                }
            }
        }
    }

    /**
     * Returns a power of two table size for the given desired capacity.
     * Integer.numberOfLeadingZeros(n) 取值范围为 0~32
     * 返回值 0000 0000 0000 0000 1000 0000 001x xxxx
     
     * 
     */
    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) 
                | (1 << (RESIZE_STAMP_BITS - 1)) // 32位二进制，第16位放个1，外层使用时一般会 <<16 这个1会放到首位（符号位）
                ;
    }

    /**
     * Helps transfer if a resize is in progress.
     * 帮助扩容
     * 使用场景： put , compute , replace , clear等
     * 
     */
    final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
        Node<K,V>[] nextTab; int sc;
        if (tab != null && (f instanceof ForwardingNode) &&
                (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
            int rs = resizeStamp(tab.length) << RESIZE_STAMP_SHIFT;
            while (nextTab == nextTable && table == tab &&
                    (sc = sizeCtl) < 0) { //判断是否正在扩容
                if (sc == rs + MAX_RESIZERS  // 扩容线程满了
                        || sc == rs + 1  // 当前线程扩容即将结束
                        || transferIndex <= 0)  // 需要扩容的下标已经分配完毕，或者已经扩容结束
                    break;
                if (U.compareAndSetInt(this, SIZECTL, sc, sc + 1)) { //扩容位+1
                    transfer(tab, nextTab); //开始扩容
                    break;
                }
            }
            return nextTab;
        }
        return table;
    }

    /**
     * Moves and/or copies the nodes in each bin to new table. See
     * above for explanation.
     * 数组扩容
     * @param tab 当前数组
     * @param nextTab 新的数组，空则创建
     */
    private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        // 当前数组长度，即扩容前的数组长度
        int n = tab.length;
        int stride; //步长 =  min(tab.len/(8*线程数),16)
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // subdivide range
        if (nextTab == null) {            // initiating
            try {
//                @SuppressWarnings("unchecked")
//                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
//                nextTab = nt;
                nextTab = (Node<K,V>[])new Node<?,?>[n << 1];
            } catch (Throwable ex) {      // try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            transferIndex = n;
        }
        int nextn = nextTab.length;
        // 随便创建一个节点
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
        boolean advance = true;
        boolean finishing = false; // to ensure sweep before committing nextTab
        // 从右到左遍历
        for (int i = 0, left = 0;;) {
            Node<K,V> f; int fh;
            while (advance) { // 说实话，这里写得可读性有点差；这里我改造了下 bound改成了left
                i--;
                if (i >= left || finishing)
                    advance = false;
                else if (transferIndex <= 0) {
                    i = -1;
                    advance = false;
                } else { 
                    int right = transferIndex;
                    int newLeft = (right > stride ? right - stride : 0);
                    if (U.compareAndSetInt(this, TRANSFERINDEX, right, newLeft)) {
                        left = newLeft;
                        i = right - 1;
                        advance = false;
                    }
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) { //退出扩容
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1); //退出扩容状态
                    return; 
                }
                if (U.compareAndSetInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT) 
                        // 作为次要线程退出扩容
                        // 若 sc-2 == resizeStamp(n) << 16 则为发起扩容的线程
                        return; 
                    finishing = advance = true;
                    i = n; // recheck before commit
                }
            }
            else if ((f = tabAt(tab, i)) == null) // 空节点无需迁移，直接打上迁移完成标记
                advance = casTabAt(tab, i, null, fwd);
            else if ((fh = f.hash) == MOVED) // 当前节点已经迁移完成
                advance = true; // already processed
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        if (fh >= 0) {
                            // <-- 这里一段仅用于减少new Node的操作，提高性能；实际删掉这段对逻辑影响不大
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            // -->
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0) // 保留原节点索引（即i)位置
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else  //迁移到新的索引，即 i + n , 相当于 i | n （n的二进制必然为10000000....)
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd); // 该节点已经迁移完成
                            advance = true;
                        }
//                        else if (f instanceof TreeBin) { //红黑树的处理，忽略
//                            TreeBin<K,V> t = (TreeBin<K,V>)f;
//                            TreeNode<K,V> lo = null, loTail = null;
//                            TreeNode<K,V> hi = null, hiTail = null;
//                            int lc = 0, hc = 0;
//                            for (Node<K,V> e = t.first; e != null; e = e.next) {
//                                int h = e.hash;
//                                TreeNode<K,V> p = new TreeNode<K,V>
//                                        (h, e.key, e.val, null, null);
//                                if ((h & n) == 0) {
//                                    if ((p.prev = loTail) == null)
//                                        lo = p;
//                                    else
//                                        loTail.next = p;
//                                    loTail = p;
//                                    ++lc;
//                                }
//                                else {
//                                    if ((p.prev = hiTail) == null)
//                                        hi = p;
//                                    else
//                                        hiTail.next = p;
//                                    hiTail = p;
//                                    ++hc;
//                                }
//                            }
//                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
//                                    (hc != 0) ? new TreeBin<K,V>(lo) : t;
//                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
//                                    (lc != 0) ? new TreeBin<K,V>(hi) : t;
//                            setTabAt(nextTab, i, ln);
//                            setTabAt(nextTab, i + n, hn);
//                            setTabAt(tab, i, fwd);
//                            advance = true;
//                        }
                    }
                }
            }
        }
    }

    /**
     * 尝试扩容到指定的大小，非2^n则自动扩大到2^n
     * 实际测试，入参size=10 , 最终table.size会是32而不是16
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
                // (rs << RESIZE_STAMP_SHIFT) + 2) 必然为负数
                // sc 当前值必然大于0  , 因此不会有重复的线程执行 transfer(tab, null);
                if (U.compareAndSetInt(this, SIZECTL, sc,
                        (rs << RESIZE_STAMP_SHIFT) + 2))
                    transfer(tab, null);
            }
        }
    }
    //...
}
```