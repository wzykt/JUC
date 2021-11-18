# 共享模型之管程

## 一、共享带来的问题

#### 1、上下文切换

问题表现

```java
public class Test01 {
    static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter++;
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                counter--;
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}", counter);
    }
}
```

**问题分析** 

以上的结果可能是正数、负数、零。为什么呢？因为 Java 中对静态变量的自增，自减并不是原子操作，要彻底理 解，必须从字节码来进行分析 

例如对于 i++ 而言（i 为静态变量），实际会产生如下的 JVM 字节码指令：

```
getstatic i // 获取静态变量i的值
iconst_1 // 准备常量1
iadd // 自增
putstatic i // 将修改后的值存入静态变量i
```

而对应 i-- 也是类似：

```
getstatic i // 获取静态变量i的值
iconst_1 // 准备常量1
isub // 自减
putstatic i // 将修改后的值存入静态变量i
```

 Java 的内存模型如下，完成静态变量的自增，自减需要在主存和工作内存中进行数据交换：

![image-20211114212953850](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114212953850.png)



**单线程执行**

![image-20211114212947942](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114212947942.png)

指令顺序执行，不会有任何问题



**多线程执行**

多线程下这 8 行代码可能交错运行

出现负数的情况：

![image-20211114212927553](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114212927553.png)

出现正数的情况

![image-20211114212938563](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114212938563.png)



#### 2、临界区

* 一个程序运行多个线程本身是没有问题的
* 问题出在多个线程访问**共享资源**
  * 多个线程读**共享资源**其实也没有问题
  * 在多个线程对**共享资源**读写操作时发生指令交错，就会出现问题
* 一段代码块内如果存在对**共享资源**的多线程读写操作，称这段代码块为**临界区**



#### 3、竞态条件 Race Condition

多个线程在临界区内执行，由于代码的执行序列不同而导致结果无法预测，称之为发生了竞态条件



## 二、解决方法-Sychronized

#### 1、synchronized 解决方案

为了避免临界区的竞态条件发生，有多种手段可以达到目的。

* 阻塞式的解决方案：synchronized，Lock
* 非阻塞式的解决方案：原子变量



synchronized，来解决上述问题，即俗称的【对象锁】，它采用互斥的方式让同一时刻至多只有一个线程能持有【对象锁】，其它线程再

想获取这个【对象锁】时就会阻塞住。这样就能保证拥有锁的线程可以安全的执行临界区内的代码，不用担心线程上下文切换

> **注意**
> 虽然 java 中互斥和同步都可以采用 synchronized 关键字来完成，但它们还是有区别的：
>
> 互斥是保证临界区的竞态条件发生，同一时刻只能有一个线程执行临界区代码
>
> 同步是由于线程执行的先后、顺序不同、需要一个线程等待其它线程运行到某个点



##### 示例代码

解决上面操作共享变量带来的安全问题

```java
public class Test01 {

    //解决方法Synchronized
    static int counter = 0;
    static final Object lock = new Object();
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (lock) {
                    counter++;
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (lock) {
                    counter--;
                }
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}",counter);
    }
}
```

时序图展示执行流程

![image-20211114214259319](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114214259319.png)

synchronized 实际是用对象锁保证了临界区内代码的原子性，临界区内的代码对外是不可分割的，不会被线程切 换所打断。



>### 思考
>
>Q1：如果把 synchronized(obj) 放在 for 循环的外面，如何理解？
>
>![image-20211114214650961](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114214650961.png)
>
>![image-20211114214712873](D:\Typoramd\JUC\JUC-02\JUC共享资源.assets\image-20211114214712873.png)
>
>观察可以看出，临界区不同，Synchronized放在外面可以保证for循环的原子性，而Synchronized放在里面只保证了counter++的原子性
>
>---
>
>Q2：如果 t1 synchronized(obj1) 而 t2 synchronized(obj2) 会怎样运作？
>
>这样两个临界区锁住的是不同的对象，无法保证共享资源的安全性，也证明了Java程序中的锁是对象锁
>
>---
>
>Q3：如果 t1 synchronized(obj) 而 t2 没有加会怎么样？如何理解？
>
>t2不会和t1竞争，做自己的操作，无法保证共享资源的安全性



##### 面向对象改装

使用面向对象的方式，将共享资源的操作封装成线程安全的。

```java
class Room {
    int value = 0;

    public void increment() {
        synchronized (this) {
            value++;
        }
    }

    public void decrement() {
        synchronized (this) {
            value--;
        }
    }

    public int get() {
        synchronized (this) {
            return value;
        }
    }
}

@Slf4j
public class Test1 {

    public static void main(String[] args) throws InterruptedException {
        Room room = new Room();
        Thread t1 = new Thread(() -> {
            for (int j = 0; j < 5000; j++) {
                room.increment();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int j = 0; j < 5000; j++) {
                room.decrement();
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("count: {}", room.get());
    }
}
```



#### 2、Synchronized的用法

