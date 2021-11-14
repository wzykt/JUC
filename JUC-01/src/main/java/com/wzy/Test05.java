package com.wzy;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

/**
 * interrupt()方法打断park线程
 */
@Slf4j(topic = "Test05")
public class Test05 {
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

        test();
    }

    public static void test() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            log.debug("park...");
            LockSupport.park();
            log.debug("unpark....");
            log.debug("打断状态{}", Thread.currentThread().isInterrupted());
            //打断标记为true的情况下，park方法失效，调用Thread.interrupted()方法重置打断标记，就可以生效了
            LockSupport.park();
            log.debug("unpark....");
        }, "t1");

        t1.start();
        Thread.sleep(1000);
        t1.interrupt();
    }

}
