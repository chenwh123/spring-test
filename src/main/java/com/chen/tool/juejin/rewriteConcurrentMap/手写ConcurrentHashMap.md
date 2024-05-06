
# 前言
常见的面试题了，因为我每次找工作都得重新找资源，老是忘记细节，因此自己也写一下加深记忆。

> 本文使用JDK11，源码可能和JDK8有所不同，但思路肯定是一样的。

# 核心方法
- put(K key, V value) void : 
- get(K key) => V : 


```java
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
        implements ConcurrentMap<K,V>, Serializable {
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
    //...
}
```