---
theme: channing-cyan
highlight: a11y-dark
---

# 前言
面试官：ArrayList线程不安全，怎么解决？

我：CopyOnWriteList

面试官：CopyOnWriteList的原理是什么？

我：@#$%^$，F***

有感于面试官经常问，今天就深入了解一下这玩意的原理


# 实现
## 核心方法
- get：根据index获取值
- add：添加元素 

## 原理
看了CopyOnWriteList的源码，发现其实现原理很简单，核心就是读写分离。

jdk源码
```java
public class CopyOnWriteArrayList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    //...

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        synchronized (lock) {
            Object[] es = getArray();
            int len = es.length;
            es = Arrays.copyOf(es, len + 1);
            es[len] = e;
            setArray(es);
            return true;
        }
    }
    
    public E get(int index) {
        return elementAt(getArray(), index);
    }
    //...
}
    
```

读的时候直接读，写的时候复制一个新的数组，然后再写入。


## 个人代码实现

```java
public class MyCopyOnWriteList<T> {

    private ReentrantLock lock = new ReentrantLock();

    private volatile List<T> list = new ArrayList<>();

    public void add(T t) {
        lock.lock();
        try {
            ArrayList<T> temp = new ArrayList<>(list);
            temp.add(t);
            list = temp;
        } finally {
            lock.unlock();
        }
    }

    public T get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    /**
     * 测试代码
     */
    public static void main(String[] args) throws InterruptedException {
//        List<Integer> list = new ArrayList<>(); // 可以取消注释改行看看效果
        MyCopyOnWriteList<Integer> list = new MyCopyOnWriteList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                for (int i1 = 0; i1 < 100; i1++) {
                    list.add(i1);
                }
            });

        }
        TimeUnit.SECONDS.sleep(2);

        // 理想值返回1000 ， 若使用ArrayList则会返回小于1000的值
        System.out.println("end, list size = " + list.size());
    }
}
```
# 总结
虽然CopyOnWriteList的实现原理很简单，但是我们可以发现它重大的缺陷，就是add任意一个元素都会进行数组复制，可以说相当消耗内存了
，CopyOnWriteList.add的空间复杂度是O(n)，而ArrayList.add的空间复杂度是O(1)。所以在使用的时候要慎重考虑。


