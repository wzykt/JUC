

# JUC基础

## 并发概念

### 1、进程和线程

#### 进程

* 程序由指令和数据组成，但这些指令要运行，数据要读写，就必须将指令加载至CPU，数据加载至内存。在指令运行过程中还需要用到磁盘、网络等设备。进程就是用来加载指令、管理内存、管理IO的
* 当一个程序被运行，从磁盘加载这个程序的代码至内存，这时就开启了一个进程。
* 进程就可以视为程序的一个实例。大部分程序可以同时运行多个实例进程（例如记事本、画图、浏览器等)，也有的程序只能启动一个实例进程（例如网易云音乐、360安全卫士等)

#### 线程

* —个进程之内可以分为一到多个线程。
* 一个线程就是一个指令流，将指令流中的一条条指令以一定的顺序交给CPU执行
* Java中，线程作为最小调度单位，进程作为资源分配的最小单位。在windows 中进程是不活动的，只是作为线程的容器

#### 二者对比

* 进程基本上相互独立的，而线程存在于进程内，是进程的一个子集
* 进程拥有共享的资源，如内存空间等，供其内部的线程共享
* 进程间通信较为复杂
  * 同一台计算机的进程通信称为IPC (Inter-process communication)
  * 不同计算机之间的进程通信，需要通过网络，并遵守共同的协议，例如HTTP
* 线程通信相对简单，因为它们共享进程内的内存，一个例子是多个线程可以访问同一个共享变量
* 线程更轻量，线程上下文切换成本一般上要比进程上下文切换低

### 2、并行与并发

单核 cpu 下，线程实际还是 串行执行 的。操作系统中有一个组件叫做任务调度器，将 cpu 的时间片（windows 下时间片最小约为 15 毫秒）分给不同的程序使用，只是由于 cpu 在线程间（时间片很短）的切换非常快，人类感觉是同时运行的 。总结为一句话就是： 微观串行，宏观并行 ， 一般会将这种 线程轮流使用 CPU 的做法称为并发， concurrent

![image-20211112141404706](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112141404706.png)



多核 cpu下，每个 核（core） 都可以调度运行线程，这时候线程可以是并行的

![image-20211112141516651](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112141516651.png)



引用 Rob Pike 的一段描述：

并发（concurrent）是同一时间应对（dealing with）多件事情的能力 

并行（parallel）是同一时间动手做（doing）多件事情的能力 

例子 

* 家庭主妇做饭、打扫卫生、给孩子喂奶，她一个人轮流交替做这多件事，这时就是并发 
* 家庭主妇雇了个保姆，她们一起这些事，这时既有并发，也有并行（这时会产生竞争，例如锅只有一口，一 个人用锅时，另一个人就得等待） 
* 雇了3个保姆，一个专做饭、一个专打扫卫生、一个专喂奶，互不干扰，这时是并行



### 3、应用

以调用方角度来讲，

方法调用者如果需要等待结果返回，才能继续运行就是同步 

方法调用者不需要等待结果返回，就能继续运行就是异步，简单理解就是再开辟一个新的线程来解决问题

**设计** 

多线程可以让方法执行变为异步的（即不要巴巴干等着）比如说读取磁盘文件时，假设读取操作花费了 5 秒钟，如果没有线程调度机制，这 5 秒 cpu 什么都做不了，其它代码都得暂停

**结论** 

比如在项目中，视频文件需要转换格式等操作比较费时，这时开一个新线程处理视频转换，避免阻塞主线程 tomcat 的异步 servlet 也是类似的目的，让用户线程处理耗时较长的操作，避免阻塞 tomcat 的工作线程 ui 程序中，开线程进行其他操作，避免阻塞 ui 线程





## Java线程

### 1、创建Java线程

#### **方法一**

直接使用 Thread

```java
  		// 创建线程对象
        Thread t = new Thread() {
            public void run() {
                // 要执行的任务
            }
        };
```

现在只是创建出一个线程，但是还没有操作系统的线程关联起来

建议创建线程时自定义线程名

#### **方法二**

使用 Runnable 配合 Thread

把【线程】和【任务】（要执行的代码）分开 ，Thread 代表线程 Runnable **可运行的任务**（线程要执行的代码）

```java
Runnable runnable = new Runnable() {
 public void run(){
 // 要执行的任务
 }
};
// 创建线程对象
Thread t = new Thread( runnable );
// 启动线程
```

Java 8 以后可以使用 lambda 精简代码

```java
// 创建任务对象
Runnable task2 = () -> log.debug("hello");
// 参数1 是任务对象; 参数2 是线程名字，推荐
Thread t2 = new Thread(task2, "t2");
t2.start();
```

组合是优于继承的，推荐此方式。

#### 方法三

FutureTask 配合 Thread FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况

```java
// 创建任务对象
FutureTask<Integer> task3 = new FutureTask<>(() -> {
 log.debug("hello");
 return 100;
});
// 参数1 是任务对象; 参数2 是线程名字，推荐
new Thread(task3, "t3").start();
// 主线程阻塞，同步等待 task 执行完毕的结果
//get方法是同步方法
Integer result = task3.get();
log.debug("结果是:{}", result);
```





### 2、原理之 Thread 与 Runnable 的关系

Thread类使用到了静态代理的模式

```java
package com.xs.pattern.staticproxy;
 
public class ThreadStaticProxy {
	public static void main(String[] args) {
		Runnable target = new MyTarget();// 目标角色
		Thread proxy = new Thread(target);// 代理角色
		proxy.start();
	}
}
 
class MyTarget implements Runnable {
 
	public void run() {
		System.out.println("run...");
	}
}
```

会交由Thread类代为执行run方法

线程体（也就是我们要执行的具体任务）实现了Runnable接口和run方法。同时Thread类也实现了Runnable接口。此时，线程体就相当于目标角色，Thread就相

当于代理角色。当程序调用了Thread的start()方法后，Thread的run()方法会在某个特定的时候被调用。thread.run()方法：

参见源码

```java
    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }

	private Runnable target;//传入的任务对象
```



#### 静态代理好处

（１）代理对象可以做很多真实对象做不了的事情
（２）真实对象专注与自己的事情



#### 参考文章

了解为什么会出现代理模式，以及静态代理的组成和特点

https://segmentfault.com/a/1190000011291179



### 3、查看线程

基本命令，没展示具体参数

#### windows 

任务管理器可以查看进程和线程数，也可以用来杀死进程 

tasklist 查看进程

taskkill 杀死进程 



经常会遇到的，未正常终止运行的程序，在不关机重启的情况下，杀死程序的进程

```shell
tasklist | findstr "java" 找到window中的java进程
netstat -ano |findstr pid 查找进程占用的端口
taskkill /f /pid pid  杀掉进程
```



#### linux

ps -fe 查看所有进程 

ps -fT -p  查看某个进程（PID）的所有线程 

kill 杀死进程 

top 按大写 H 切换是否显示线程 

top -H -p  查看某个进程（PID）的所有线程 

#### Java 

jps 命令查看所有 Java 进程 

jstack  查看某个 Java 进程（PID）的所有线程状态 

jconsole 来查看某个 Java 进程中线程的运行情况（图形界面）



#### **jconsole使用**

win+R  搜索jconsole就会出现下面的界面

<img src="D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112151621516.png" alt="image-20211112151621516" style="zoom:50%;" />

jconsole 可以进行远程监控配置 

需要以如下方式运行你的 java 类 

```shell
java -Djava.rmi.server.hostname=`ip地址` -Dcom.sun.management.jmxremote -
Dcom.sun.management.jmxremote.port=`连接端口` -Dcom.sun.management.jmxremote.ssl=是否安全连接 -
Dcom.sun.management.jmxremote.authenticate=是否认证 java类
```

修改 /etc/hosts 文件将 127.0.0.1 映射至主机名 



如果要认证访问，还需要做如下步骤 

* 复制 jmxremote.password 文件 
* 修改 jmxremote.password 和 jmxremote.access 文件的权限为 600 即文件所有者可读写 
* 连接时填入 controlRole（用户名），R&D（密码）





### 4、线程运行原理

先看一段程序

```java
    public static void main(String[] args) {
        method1(10);
    }

    private static void method1(int x) {
        int y = x + 1;
        Object m = method2();
        System.out.println(m);
    }

    private static Object method2() {
        Object n = new Object();
        return n;
    }
```

在main方法调用method1的地方打上断点，以debug模式启动，我们可以在右下角看到对应的栈帧的执行情况。

> 调用过程（最上面的就是活动的栈帧。）
>
> 1.程序启动，主方法入栈，对应main栈帧
>
> 2.当 调用其他方法，方法对应的栈帧入栈
>
> 3.方法调用完毕，方法就会出栈

![image-20211112152513115](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112152513115.png)

也可观察栈帧中有哪些内容

![image-20211112152908470](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112152908470.png)

#### **Java Virtual Machine Stacks （Java 虚拟机栈）** 

我们都知道 JVM 中由堆、栈、方法区所组成，其中栈内存是给谁用的呢？其实就是线程，每个线程启动后，虚拟 机就会为其分配一块栈内存。

* 每个栈由多个栈帧（Frame）组成，对应着每次方法调用时所占用的内存 

* 每个线程只能有一个活动栈帧，对应着当前正在执行的那个方法





**图解栈帧执行过程**

先行了解一下jvm内存结构

![image-20211114211015431](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211114211015431.png)

[Java虚拟机—栈帧、操作数栈和局部变量表](https://zhuanlan.zhihu.com/p/45354152)

参考下面的视频：https://www.bilibili.com/video/BV16J411h7Rd?p=21&spm_id_from=pageDriver

![image-20211112154054676](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112154054676.png)

>  视频中的一些概念

**栈帧的内容**

* **局部变量表**：局部变量和参数。在创建栈帧的时候，空间就已经分配好了

* **操作数栈：**

* **方法出口**：记录栈帧执行完毕后，继续执行哪一个栈帧，记录的是一个地址值。

**程序计数器**

记录当前代码执行到哪里。cpu根据pc里记录的地址来执行程序



#### 线程上下文切换

因为以下一些原因导致 cpu 不再执行当前的线程，转而执行另一个线程的代码 

1. 线程的 cpu 时间片用完
2. 垃圾回收 
3. 有更高优先级的线程需要运行 
4. 线程自己调用了 sleep、yield、wait、join、park、synchronized、lock 等方法 



当 Context Switch 发生时，需要由操作系统保存当前线程的状态，并恢复另一个线程的状态，Java 中对应的概念就是程序计数器（Program Counter Register），它的作用记住当前线程执行的状态，是线程私有的。

* 执行状态包括程序计数器（是记住下一条 jvm 指令的执行地址）、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等

* Context Switch 频繁发生会影响性能



---

### 5、常见方法

常见方法

![image-20211112155357559](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112155357559.png)

![image-20211112155607227](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112155607227.png)



#### run方法

直接调用 run

```java
public static void main(String[] args) {
 	Thread t1 = new Thread("t1") {
 		@Override
		public void run() {
 			log.debug(Thread.currentThread().getName());
 			FileReader.read(Constants.MP4_FULL_PATH);
 		}
 	};
 	t1.run();
 	log.debug("do other things ...");
}
```

![image-20211112155832052](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112155832052.png)

程序仍在 main 线程运行， FileReader.read() 方法调用还是同步的。

所以想要使用多线程，需要调用start方法来启动线程。

**小结** 

* 直接调用 run 是在主线程中执行了 run，没有启动新的线程 

* 使用 start 是启动新的线程，通过新的线程间接执行 run 中的代码



#### sleep 与 yield

**sleep**

1. 调用 sleep 会让当前线程从 Running 进入 Timed Waiting 状态（阻塞） ，放在那个线程执行旧让那个线程睡眠（CurrentThread）
2. 其它线程可以使用 interrupt 方法打断正在睡眠的线程，这时 sleep 方法会抛出 InterruptedException 
3. 睡眠结束后的线程未必会立刻得到执行 
4. 建议用 TimeUnit 的 sleep 代替 Thread 的 sleep 来获得更好的可读性 



> sleep可以用于限制对CPU的使用
>
> 在没有利用 cpu 来计算时，不要让 while(true) 空转浪费 cpu，这时可以使用 yield 或 sleep 来让出 cpu 的使用权 给其他程序
>
> ```java
> while(true) {
>  	try {
>  		Thread.sleep(50);
>  	} catch (InterruptedException e) {
>  		e.printStackTrace();
>  	}
> }
> ```
>
> 可以用 wait 或 条件变量达到类似的效果 
>
> 不同的是，后两种都需要加锁，并且需要相应的唤醒操作，一般适用于要进行同步的场景 
>
> sleep 适用于无需锁同步的场景



**yield**

1. 调用 yield 会让当前线程从 Running 进入 Runnable 就绪状态，然后调度执行其它线程 
2. 具体的实现依赖于操作系统的任务调度器



**线程优先级** 

* 线程优先级会提示（hint）调度器优先调度该线程，但它仅仅是一个提示，调度器可以忽略它 
* 如果 cpu 比较忙，那么优先级高的线程会获得更多的时间片，但 cpu 闲时，优先级几乎没作用



#### join

为什么需要 join 下面的代码执行，打印 r 是什么？

```java
	static int r = 0;

    public static void main(String[] args) throws InterruptedException {
        test1();
    }

    private static void test1() {
        log.debug("开始");
        Thread t1 = new Thread(() -> {
            log.debug("开始");
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("结束");
            r = 10;
        });
        t1.start();
        log.debug("结果为:{}", r);
        log.debug("结束");
    }
```

分析 

因为主线程和线程 t1 是并行执行的，t1 线程需要 1 秒之后才能算出 r=10 

而主线程一开始就要打印 r 的结果，所以只能打印出 r=0 

解决方法 

Q:用 sleep 行不行？为什么？ 

A:用 join，加在 t1.start() 之后即可,sleep有一定的作用，但是我们无法确定要等待的线程要执行多久。



**等待多个结果**

```java
static int r1 = 0;
    static int r2 = 0;

    public static void main(String[] args) throws InterruptedException {
        test2();
    }

    private static void test2() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            r1 = 10;
        });
        Thread t2 = new Thread(() -> {
            try {
                sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            r2 = 20;
        });
        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        long end = System.currentTimeMillis();
        log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
    }
```

分析如下 

第一个 join：等待 t1 时, t2 并没有停止, 而在运行 

第二个 join：1s 后, 执行到此, t2 也运行了 1s, 因此也只需再等待 1s

**小结：等待时间最长的那个线程。并不是等待时间综合**



执行流程

![image-20211112191423526](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112191423526.png)



**有时效的等待**

![image-20211112190836709](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112190836709.png)

超时，等待线程继续执行。



#### interrupt（重点）

##### 打断sleep，wait，join的线程

这几个方法都会让线程进入阻塞状态 打断 sleep 的线程, 会清空打断状态

以 sleep 为例

```java
private static void test1() throws InterruptedException {
    Thread t1 = new Thread(()->{
        try {
            sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }, "t1");
    t1.start();
    sleep((long) 0.5);
    t1.interrupt();
    log.debug(" 打断状态: {}", t1.isInterrupted());
}
```

```
java.lang.InterruptedException: sleep interrupted
 at java.lang.Thread.sleep(Native Method)
 at java.lang.Thread.sleep(Thread.java:340)
 at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:386)
 at cn.itcast.n2.util.Sleeper.sleep(Sleeper.java:8)
 at cn.itcast.n4.TestInterrupt.lambda$test1$3(TestInterrupt.java:59)
 at java.lang.Thread.run(Thread.java:745)
21:18:10.374 [main] c.TestInterrupt - 打断状态: false
```

sleep,join,wait的线程被打断，会抛出InterruptedException异常，同时会清掉打断标记位，为false



##### 打断正常线程

对于正常运行的线程，被其他线程打断，并不会打断正在运行的线程，而是将正在运行的线程的打断标记置为true，标识被打断，可以通过判断这个标志位来决定是否终止，由线程自己来决定。

```java
    //打断正常运行的线程
    private static void test2() throws InterruptedException {
        Thread t2 = new Thread(()->{
            while(true) {
                Thread current = Thread.currentThread();
                boolean interrupted = current.isInterrupted();
                if(interrupted) {
                    log.debug(" 打断状态: {}", interrupted);
                    break;
                }
            }
        }, "t2");
        t2.start();
        sleep((long) 0.5);
        t2.interrupt();
    }
```





##### 扩展-两阶段终止模式

> ### 终止模式之两阶段终止模式
>
> Two Phase Termination 
>
> 在一个线程 T1 中如何“优雅”终止线程 T2？这里的【优雅】指的是给 T2 一个料理后事的机会。 
>
> #### 1. 错误思路 
>
> * 使用线程对象的 stop() 方法停止线程 
>   * stop 方法会真正杀死线程，如果这时线程锁住了共享资源，那么当它被杀死后就再也没有机会释放锁， 其它线程将永远无法获取锁 使
> * 用 System.exit(int) 方法停止线程 
>   * 目的仅是停止一个线程，但这种做法会让整个程序都停止
>
> #### 2.两阶段终止模式
>
> ![image-20211112192717118](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211112192717118.png)
>
> while(true)里面不停的监控是否被打断
>
> 
>
> Q：为什么睡眠的时候出现异常要重新设置打断标记
>
> A：因为sleep的线程被打断标志位会被清掉，清为false
>
> 
>
> eg
>
> ```java
> @Slf4j(topic = "Test04")
> public class Test04 {
> 
>     public static void main(String[] args) throws InterruptedException {
>         TwoPhaseTermination tpt = new TwoPhaseTermination();
>         tpt.start();
> 
>         Thread.sleep(3500);
>         tpt.stop();
>     }
> 
> }
> 
> @Slf4j(topic = "TwoPhaseTermination")
> class TwoPhaseTermination {
>     private Thread monitor;
> 
>     public void start() {
>         monitor = new Thread(new Runnable() {
>             @Override
>             public void run() {
>                 while (true) {
>                     Thread current = Thread.currentThread();
>                     if (current.isInterrupted()) {
>                         //处理后事
>                         log.info("处理后事");
>                         break;
>                     } else {
>                         try {
>                             Thread.sleep(1000);
>                             log.info("监控");
>                         } catch (InterruptedException e) {
>                             e.printStackTrace();
>                             //防止sleep的线程被打断后，无法正常停止。因为调用下面的stop方法可能会打断sleep的线程
>                             //interrupted标志位会被置为false，代码无法走到处理后事的那一步
>                             //current.interrupt();
>                         }
>                     }
>                 }
>             }
>         });
> 
>         monitor.start();
>     }
> 
>     public void stop() {
>         monitor.interrupt();
>     }
> }
> ```



##### 打断park线程

打断park线程，不会清空打断标记位

```java
public class Test05 {
    public static void main(String[] args) throws InterruptedException {
        test();
    }

    public static void test() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            log.debug("park...");
            LockSupport.park();
            log.debug("unpark....");
            log.debug("打断状态{}",Thread.currentThread().isInterrupted());
            //打断标记为true的情况下，park方法失效，调用Thread.interrupted()方法重置打断标记，就可以生效了
            LockSupport.park();
            log.debug("unpark....");
        }, "t1");

        t1.start();
        Thread.sleep(1000);
        t1.interrupt();
    }

}
```

输出

![image-20211114202901698](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211114202901698.png)





##### 不推荐的方法

还有一些不推荐使用的方法，这些方法已过时，容易破坏同步代码块，造成线程死锁

stop     停止线程运行
suspend       挂起（暂停）线程运行
resume       恢复线程运行





### 6、主线程和守护线程

默认情况下，Java进程需要等待所有线程都运行结束，才会结束。有一种特殊的线程叫做守护线程，只要其它非守护线程运行结束了，即

使守护线程的代码没有执行完，也会强制结束。

```java
    public static void main(String[] args) throws InterruptedException {
        log.debug("开始运行...");
        Thread t1 = new Thread(() -> {
            log.debug("开始运行...");
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("运行结束...");
        }, "daemon");
        // 设置该线程为守护线程
        t1.setDaemon(true);
        t1.start();
        Thread.sleep(1);
        log.debug("运行结束...");
    }
```

```
08:26:38.123 [main] c.TestDaemon - 开始运行...
08:26:38.213 [daemon] c.TestDaemon - 开始运行...
08:26:39.215 [main] c.TestDaemon - 运行结束... 
```



### 7、线程状态

#### 五种状态

这是从 操作系统 层面来描述的

![image-20211114205927672](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211114205927672.png)

* 【初始状态】仅是在语言层面创建了线程对象，还未与操作系统线程关联 
* 【可运行状态】（就绪状态）指该线程已经被创建（与操作系统线程关联），可以由 CPU 调度执行 
* 【运行状态】指获取了 CPU 时间片运行中的状态
  * 当 CPU 时间片用完，会从【运行状态】转换至【可运行状态】，会导致线程的上下文切换
* 【阻塞状态】 
  * 如果调用了阻塞 API，如 BIO 读写文件，这时该线程实际不会用到 CPU，会导致线程上下文切换，进入 【阻塞状态】 
  * 等 BIO 操作完毕，会由操作系统唤醒阻塞的线程，转换至【可运行状态】 
  * 与【可运行状态】的区别是，对【阻塞状态】的线程来说只要它们一直不唤醒，调度器就一直不会考虑 调度它们 
* 【终止状态】表示线程已经执行完毕，生命周期已经结束，不会再转换为其它状态



#### 六种状态

![image-20211114210730967](D:\Typoramd\JUC\JUC-01\JUC基础.assets\image-20211114210730967.png)

* NEW 线程刚被创建，但是还没有调用 start() 方法 
* RUNNABLE 当调用了 start() 方法之后，注意，Java API 层面的 RUNNABLE 状态涵盖了 操作系统 层面的 【可运行状态】、【运行状态】和【阻塞状态】（由于 BIO 导致的线程阻塞，在 Java 里无法区分，仍然认为 是可运行） 
* BLOCKED ， WAITING ， TIMED_WAITING 都是 Java API 层面对【阻塞状态】的细分，后面会在状态转换一节 详述
* TERMINATED 当线程代码运行结束
