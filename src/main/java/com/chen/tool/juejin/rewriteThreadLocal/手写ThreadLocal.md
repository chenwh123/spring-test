# 手写ThreadLocal

# 前言
每次面试都忘记这个知识点, 干脆自己写一写加深一下记忆

# ThreadLocal用法简介
## 使用场景
按我的理解， ThreadLocal只是保存上下文的一个工具。
就我自己在项目用到的场景
- 保存当前用户信息； 账号，token等
- 记录调用链路；sessionId，traceId等，跨服务调用时，出了问题方便溯源；
- 打印日志；配合log4j中的MDC，用户某个操作统一加上日志前缀，方便跟踪；
- 缓存；
- 事务管理；保存事务上下文，方便回滚
- 动态数据源切换；

## 

```java
public class Test0 {

    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("hi");

        CompletableFuture.runAsync(() -> {
            threadLocal.set("hello");
            System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
        });
        System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
    }
}
```

结果如下：

```
thread: main, value: hi
thread: ForkJoinPool.commonPool-worker-19, value: hello
```

简单来说就是每个线程可以在ThreadLocal保存独立的值，不会互相影响；

## 核心方法

- set
    - 作用：在当前线程保存某个值
- get
    - 作用：获取保存的值

# 实现

## 基本接口
按照惯例先弄个接口方便迭代

```java
public interface ThreadLocalInf<T> {

  void set(T value) ;

  T get() ;

}

```

## 版本-01
在还没看源码前，我的思路就是用一个Map，key保存线程ID，value保存值；

实现源码：
```java
public class MyThreadLocal1<T> implements ThreadLocalInf<T> {
  @Override
  public void set(T value) {
    getMap().put(Thread.currentThread().getId(), value);
  }

  @Override
  public T get() {
    return getMap().get(Thread.currentThread().getId());
  }

  private final Map<Long, T> map = new ConcurrentHashMap<>();

  public Map<Long,T> getMap() {
    return map;
  }

  public static void main(String[] args) {
    ThreadLocalInf<String> threadLocal = new MyThreadLocal1<>();
    threadLocal.set("hi");

    CompletableFuture.runAsync(() -> {
      threadLocal.set("hello");
      System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
    });
    System.out.println("thread: " + Thread.currentThread().getName() + ", value: " + threadLocal.get());
  }
}
```
这样实现就是简单直观，缺点就是要用到ConcurrentHashMap来保证线程安全，高并发环境效率相对低一点。
要是产品要我实现一个ThreadLocal，我就这么写了🐶


## 版本-02
这会我读了下ThreadLocal的源码之后发现， 它并非使用ThreadLocal持有Map，而是使用Thread持有；好处就是不用担心线程安全问题。

JDK源码：
```java
public
class Thread implements Runnable {
  //...
  
  /* ThreadLocal values pertaining to this thread. This map is maintained
   * by the ThreadLocal class. */
  ThreadLocal.ThreadLocalMap threadLocals = null;
  
  //...
}

static class ThreadLocalMap {
    /**
     * Set the value associated with key.
     *
     * @param key the thread local object
     * @param value the value to be set
     */
    private void set(ThreadLocal<?> key, Object value) {
      //...
    }
}
```
所以说我们这里还要自己实现一个Thread😅；

并且通过源码我们可以发现，这个ThreadLocalMap是以ThreadLocal作为key的，所以接下来我们实现MyThreadLocal2还得实现一下hashcode和equals方法

考虑到我们没办法修改jdk中thread的代码，我们自己实现一个MyThread类，然后里面维护一个Map

```java
class MyThread extends Thread {
  Map<ThreadLocalInf<?>, Object> threadLocalMap = new HashMap<>();
  public MyThread(Runnable runnable) {
    super(runnable);
  }
}

public class MyThreadLocal2<T> implements ThreadLocalInf<T> {
  private static final AtomicInteger nextId = new AtomicInteger(0);
  private final int id = nextId.getAndIncrement();

  // hashCode没必要写得太复杂，因为每个ThreadLocal都是唯一的，给出一个自增的id就可以了
  @Override
  public int hashCode() {
    return id;
  }

  // 这里equals == 即可，因为每个ThreadLocal都是唯一的
  @Override
  public boolean equals(Object obj) {
    return this == obj ;
  }

  @Override
  public void set(T value) {
    Thread thread = Thread.currentThread();
    if(thread instanceof MyThread) {
      MyThread myThread = (MyThread) thread;
      myThread.threadLocalMap.put(this, value);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public T get() {
    Thread thread = Thread.currentThread();
    if( thread instanceof MyThread) {
      MyThread myThread = (MyThread) thread;
      return (T) myThread.threadLocalMap.get(this);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public static void main(String[] args) {
    // 创建线程池 ， 使用MyThread
    ExecutorService executorService = Executors.newCachedThreadPool(MyThread::new);

    // 创建10个ThreadLocal
    List<ThreadLocalInf<String>> localList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      localList.add(new MyThreadLocal2<>());
    }
    //这里我们上一下强度， 开100个线程测试
    for (int i = 0; i < 100; i++) {
      CompletableFuture.runAsync(() -> {
        for (int j = 0; j < localList.size(); j++) {
          String val = Thread.currentThread().getName() + "-" + j;
          ThreadLocalInf<String> local = localList.get(j);
          System.out.println("thread :" + Thread.currentThread().getName() + ",set value: " + val);
          local.set(val);
        }
        // 暂停5秒
        try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { throw new RuntimeException(e); }
        for (ThreadLocalInf<String> local : localList) {
          System.out.println("thread :" + Thread.currentThread().getName() + ",get value: " + local.get());

        }

      }, executorService);

    }

    try {
      TimeUnit.SECONDS.sleep(10);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    System.out.println("finish");
  }
}
```
到这里你应该也基本明白ThreadLocal的构造， 接下来我们再看看细节。

## 版本-03
ThreadLocal可能就是这么简单，但一旦到了面试的场景，防止面试官使劲扣点东西来问，我们还是得再深入研究一下。

接下来说说老生常谈的内存泄漏问题。

### 为什么会出现内存泄漏

内存泄漏简单来说就是不再使用的内存无法被回收，导致内存占用越来越大，最终导致OOM（Out of Memory 内存溢出）。

我们经常使用的springboot每个请求都会使用一个线程，请求结束后线程并不会销毁，而是放到线程池中，等待下一次请求；如果在这个请求结束前，你保存了大量的数据到ThreadLocal中，但没有主动remove，而且这个Thread由于使用的是线程池，是会一直存在的，它所保存的threadLocals的对象也会一直存在。那么这部分数据就会一直存在内存中，从而很容易导致内存泄漏

为了应对这种情况ThreadLocalMap中的Entry继承了WeakReference，这样一旦发生GC，并且ThreadLocal没有其他强引用，ThreadLocalMap中的Entry的key就会被回收（等同于调用weakReference.clear()）变为null。
> 这里说一下题外话，我们一般使用ThreadLocal 会这么定义：public static final ThreadLocal<String> local = new ThreadLocal<>(); 这样做相当于加了个不可更改的强引用，因此，ThreadLocalMap中的Entry的key是不会被回收；所以我认为开发的过程中不必太在意这个Entry中的WeakReference。

这里再贴一下WeakReference生效的例子:
例子代码：
```java
/**
 * JDK11
 */
public class WeakReferenceExample {

    public static void main(String[] args) {
        String str = new String("Hello, World!"); //强制创建对象在堆中，而不是常量池中；常量池中的对象不会被回收，哪怕只有弱引用
//      String str = "Hello, World!" //对象会在常量池
        WeakReference<String> weakReference = new WeakReference<>(str);
        str = null; //尝试取消注释该行代码，看看效果
      
        System.gc(); //相当于调。weakReference.clear()

        System.out.println(weakReference.get()); //返回null
    }
}
```

如果返回null，那么就是弱引用里面的对象被回收了。



### ThreadLocal中弱引用的作用，以及ThreadLocal对弱引用的后续处理
通过上面的解释，相信你也知道这个WeakReference的确没什么用了😎（可能有但是我的使用场景用不上）。 但有时面试官可能会不依不饶，虽然我很想回答没啥用。但如果你想吹多一点接着往下看。
我们先看看它的源码

JDK源码：
```java
        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
```

我们先做个假设，哪怕这个WeakReference生效了 ，就是GC后回收了key，但是value是强引用（只有被super(k)框住的才是弱引用😥）。一般来说ThreadLocal本身是不太占用内存的，但是value是用户传值，占用内存可能会很大。
