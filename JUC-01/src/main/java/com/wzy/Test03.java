package com.wzy;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

/**
 * interrupt方法
 */
@Slf4j
public class Test03 {
    //打断sleep的线程
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

}
